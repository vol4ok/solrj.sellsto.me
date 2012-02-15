package sellstome.search;

import org.apache.lucene.search.FieldValueHitQueue;
import org.apache.lucene.search.SortField;

import java.io.IOException;

/**
 * @author Aliaksandr Zhuhrou
 */
public abstract class SellstomeFieldValueHitQueue<T extends FieldValueHitQueue.Entry>
       extends FieldValueHitQueue<T> {

    /**
     * An implementation of {@link FieldValueHitQueue} which is optimized in case
     * there is just one comparator.
     */
    private static final class OneComparatorFieldValueHitQueue<T extends FieldValueHitQueue.Entry> extends SellstomeFieldValueHitQueue<T> {
        private final int oneReverseMul;

        public OneComparatorFieldValueHitQueue(SortField[] fields, int size)
                throws IOException {
            super(fields, size);

            SortField field = fields[0];
            setComparator(0,field.getComparator(size, 0));
            oneReverseMul = field.getReverse() ? -1 : 1;

            reverseMul[0] = oneReverseMul;
        }

        /**
         * Returns whether <code>a</code> is less relevant than <code>b</code>.
         * @param a ScoreDoc
         * @param b ScoreDoc
         * @return <code>true</code> if document <code>a</code> should be sorted after document <code>b</code>.
         */
        @Override
        protected boolean lessThan(final Entry hitA, final Entry hitB) {

            assert hitA != hitB;
            assert hitA.slot != hitB.slot;

            final int c = oneReverseMul * firstComparator.compare(hitA.slot, hitB.slot);
            if (c != 0) {
                return c > 0;
            }

            // avoid random sort order that could lead to duplicates (bug #31241):
            return hitA.doc > hitB.doc;
        }

    }

    /**
     * An implementation of {@link FieldValueHitQueue} which is optimized in case
     * there is more than one comparator.
     */
    private static final class MultiComparatorsFieldValueHitQueue<T extends FieldValueHitQueue.Entry> extends SellstomeFieldValueHitQueue<T> {

        public MultiComparatorsFieldValueHitQueue(SortField[] fields, int size)
                throws IOException {
            super(fields, size);

            int numComparators = comparators.length;
            for (int i = 0; i < numComparators; ++i) {
                SortField field = fields[i];

                reverseMul[i] = field.getReverse() ? -1 : 1;
                setComparator(i, field.getComparator(size, i));
            }
        }

        @Override
        protected boolean lessThan(final Entry hitA, final Entry hitB) {

            assert hitA != hitB;
            assert hitA.slot != hitB.slot;

            int numComparators = comparators.length;
            for (int i = 0; i < numComparators; ++i) {
                final int c = reverseMul[i] * comparators[i].compare(hitA.slot, hitB.slot);
                if (c != 0) {
                    // Short circuit
                    return c > 0;
                }
            }

            // avoid random sort order that could lead to duplicates (bug #31241):
            return hitA.doc > hitB.doc;
        }

    }

    public SellstomeFieldValueHitQueue(SortField[] fields, int size) {
        super(fields, size);
    }

    /**
     * Creates a hit queue sorted by the given list of fields.
     *
     * <p><b>NOTE</b>: The instances returned by this method
     * pre-allocate a full array of length <code>numHits</code>.
     *
     * @param fields
     *          SortField array we are sorting by in priority order (highest
     *          priority first); cannot be <code>null</code> or empty
     * @param size
     *          The number of hits to retain. Must be greater than zero.
     * @throws IOException
     */
    public static <T extends FieldValueHitQueue.Entry> FieldValueHitQueue<T> create(SortField[] fields, int size) throws IOException {

        if (fields.length == 0) {
            throw new IllegalArgumentException("Sort must contain at least one field");
        }

        if (fields.length == 1) {
            return new OneComparatorFieldValueHitQueue<T>(fields, size);
        } else {
            return new MultiComparatorsFieldValueHitQueue<T>(fields, size);
        }
    }

}