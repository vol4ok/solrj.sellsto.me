package sellstome.search;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.*;
import org.apache.lucene.util.PriorityQueue;

import java.io.IOException;

/**
 * Adds sort refinements functionality.
 * todo zhugrov a - it really sucks. In order to add a few lines of code
 * I was forced to copy about 13 classes.
 * @author Aliaksandr Zhuhrou
 */
public abstract class SellstomeTopFieldCollector extends org.apache.lucene.search.TopFieldCollector {

    /*
     * Implements a TopFieldCollector over one SortField criteria, without
     * tracking document scores and maxScore.
     */
    private static class OneComparatorNonScoringCollector extends
            SellstomeTopFieldCollector {

        FieldComparator comparator;
        final int reverseMul;
        final FieldValueHitQueue<FieldValueHitQueue.Entry> queue;

        public OneComparatorNonScoringCollector(FieldValueHitQueue<FieldValueHitQueue.Entry> queue,
                                                int numHits, boolean fillFields) throws IOException {
            super(queue, numHits, fillFields);
            this.queue = queue;
            comparator = queue.getComparators()[0];
            reverseMul = queue.getReverseMul()[0];
        }

        final void updateBottom(int doc) {
            // bottom.score is already set to Float.NaN in add().
            bottom.doc = docBase + doc;
            bottom = pq.updateTop();
        }

        @Override
        public void collect(int doc) throws IOException {
            ++totalHits;
            if (queueFull) {
                if ((reverseMul * comparator.compareBottom(doc)) <= 0) {
                    // since docs are visited in doc Id order, if compare is 0, it means
                    // this document is largest than anything else in the queue, and
                    // therefore not competitive.
                    return;
                }

                // This hit is competitive - replace bottom element in queue & adjustTop
                comparator.copy(bottom.slot, doc);
                updateBottom(doc);
                comparator.setBottom(bottom.slot);
            } else {
                // Startup transient: queue hasn't gathered numHits yet
                final int slot = totalHits - 1;
                // Copy hit into queue
                comparator.copy(slot, doc);
                add(slot, doc, Float.NaN);
                if (queueFull) {
                    comparator.setBottom(bottom.slot);
                }
            }
        }

        @Override
        public void setNextReader(AtomicReaderContext context) throws IOException {
            this.docBase = context.docBase;
            queue.setComparator(0, comparator.setNextReader(context));
            comparator = queue.getComparators()[0];
        }

        @Override
        public void setScorer(Scorer scorer) throws IOException {
            comparator.setScorer(scorer);
        }

    }

    /*
    * Implements a TopFieldCollector over one SortField criteria, without
    * tracking document scores and maxScore, and assumes out of orderness in doc
    * Ids collection.
    */
    private static class OutOfOrderOneComparatorNonScoringCollector extends
            OneComparatorNonScoringCollector {

        public OutOfOrderOneComparatorNonScoringCollector(FieldValueHitQueue<FieldValueHitQueue.Entry> queue,
                                                          int numHits, boolean fillFields) throws IOException {
            super(queue, numHits, fillFields);
        }

        @Override
        public void collect(int doc) throws IOException {
            ++totalHits;
            if (queueFull) {
                // Fastmatch: return if this hit is not competitive
                final int cmp = reverseMul * comparator.compareBottom(doc);
                if (cmp < 0 || (cmp == 0 && doc + docBase > bottom.doc)) {
                    return;
                }

                // This hit is competitive - replace bottom element in queue & adjustTop
                comparator.copy(bottom.slot, doc);
                updateBottom(doc);
                comparator.setBottom(bottom.slot);
            } else {
                // Startup transient: queue hasn't gathered numHits yet
                final int slot = totalHits - 1;
                // Copy hit into queue
                comparator.copy(slot, doc);
                add(slot, doc, Float.NaN);
                if (queueFull) {
                    comparator.setBottom(bottom.slot);
                }
            }
        }

        @Override
        public boolean acceptsDocsOutOfOrder() {
            return true;
        }

    }

    /*
    * Implements a TopFieldCollector over one SortField criteria, while tracking
    * document scores but no maxScore.
    */
    private static class OneComparatorScoringNoMaxScoreCollector extends
            OneComparatorNonScoringCollector {

        Scorer scorer;

        public OneComparatorScoringNoMaxScoreCollector(FieldValueHitQueue<FieldValueHitQueue.Entry> queue,
                                                       int numHits, boolean fillFields) throws IOException {
            super(queue, numHits, fillFields);
        }

        final void updateBottom(int doc, float score) {
            bottom.doc = docBase + doc;
            bottom.score = score;
            bottom = pq.updateTop();
        }

        @Override
        public void collect(int doc) throws IOException {
            ++totalHits;
            if (queueFull) {
                if ((reverseMul * comparator.compareBottom(doc)) <= 0) {
                    // since docs are visited in doc Id order, if compare is 0, it means
                    // this document is largest than anything else in the queue, and
                    // therefore not competitive.
                    return;
                }

                // Compute the score only if the hit is competitive.
                final float score = scorer.score();

                // This hit is competitive - replace bottom element in queue & adjustTop
                comparator.copy(bottom.slot, doc);
                updateBottom(doc, score);
                comparator.setBottom(bottom.slot);
            } else {
                // Compute the score only if the hit is competitive.
                final float score = scorer.score();

                // Startup transient: queue hasn't gathered numHits yet
                final int slot = totalHits - 1;
                // Copy hit into queue
                comparator.copy(slot, doc);
                add(slot, doc, score);
                if (queueFull) {
                    comparator.setBottom(bottom.slot);
                }
            }
        }

        @Override
        public void setScorer(Scorer scorer) throws IOException {
            this.scorer = scorer;
            comparator.setScorer(scorer);
        }

    }

    /*
    * Implements a TopFieldCollector over one SortField criteria, while tracking
    * document scores but no maxScore, and assumes out of orderness in doc Ids
    * collection.
    */
    private static class OutOfOrderOneComparatorScoringNoMaxScoreCollector extends
            OneComparatorScoringNoMaxScoreCollector {

        public OutOfOrderOneComparatorScoringNoMaxScoreCollector(
                FieldValueHitQueue<FieldValueHitQueue.Entry> queue, int numHits, boolean fillFields)
                throws IOException {
            super(queue, numHits, fillFields);
        }

        @Override
        public void collect(int doc) throws IOException {
            ++totalHits;
            if (queueFull) {
                // Fastmatch: return if this hit is not competitive
                final int cmp = reverseMul * comparator.compareBottom(doc);
                if (cmp < 0 || (cmp == 0 && doc + docBase > bottom.doc)) {
                    return;
                }

                // Compute the score only if the hit is competitive.
                final float score = scorer.score();

                // This hit is competitive - replace bottom element in queue & adjustTop
                comparator.copy(bottom.slot, doc);
                updateBottom(doc, score);
                comparator.setBottom(bottom.slot);
            } else {
                // Compute the score only if the hit is competitive.
                final float score = scorer.score();

                // Startup transient: queue hasn't gathered numHits yet
                final int slot = totalHits - 1;
                // Copy hit into queue
                comparator.copy(slot, doc);
                add(slot, doc, score);
                if (queueFull) {
                    comparator.setBottom(bottom.slot);
                }
            }
        }

        @Override
        public boolean acceptsDocsOutOfOrder() {
            return true;
        }

    }

    /*
    * Implements a TopFieldCollector over one SortField criteria, with tracking
    * document scores and maxScore.
    */
    private static class OneComparatorScoringMaxScoreCollector extends
            OneComparatorNonScoringCollector {

        Scorer scorer;

        public OneComparatorScoringMaxScoreCollector(FieldValueHitQueue<FieldValueHitQueue.Entry> queue,
                                                     int numHits, boolean fillFields) throws IOException {
            super(queue, numHits, fillFields);
            // Must set maxScore to NEG_INF, or otherwise Math.max always returns NaN.
            maxScore = Float.NEGATIVE_INFINITY;
        }

        final void updateBottom(int doc, float score) {
            bottom.doc = docBase + doc;
            bottom.score = score;
            bottom =  pq.updateTop();
        }

        @Override
        public void collect(int doc) throws IOException {
            final float score = scorer.score();
            if (score > maxScore) {
                maxScore = score;
            }
            ++totalHits;
            if (queueFull) {
                if ((reverseMul * comparator.compareBottom(doc)) <= 0) {
                    // since docs are visited in doc Id order, if compare is 0, it means
                    // this document is largest than anything else in the queue, and
                    // therefore not competitive.
                    return;
                }

                // This hit is competitive - replace bottom element in queue & adjustTop
                comparator.copy(bottom.slot, doc);
                updateBottom(doc, score);
                comparator.setBottom(bottom.slot);
            } else {
                // Startup transient: queue hasn't gathered numHits yet
                final int slot = totalHits - 1;
                // Copy hit into queue
                comparator.copy(slot, doc);
                add(slot, doc, score);
                if (queueFull) {
                    comparator.setBottom(bottom.slot);
                }
            }

        }

        @Override
        public void setScorer(Scorer scorer) throws IOException {
            this.scorer = scorer;
            super.setScorer(scorer);
        }
    }

    /*
    * Implements a TopFieldCollector over one SortField criteria, with tracking
    * document scores and maxScore, and assumes out of orderness in doc Ids
    * collection.
    */
    private static class OutOfOrderOneComparatorScoringMaxScoreCollector extends
            OneComparatorScoringMaxScoreCollector {

        public OutOfOrderOneComparatorScoringMaxScoreCollector(FieldValueHitQueue<FieldValueHitQueue.Entry> queue,
                                                               int numHits, boolean fillFields) throws IOException {
            super(queue, numHits, fillFields);
        }

        @Override
        public void collect(int doc) throws IOException {
            final float score = scorer.score();
            if (score > maxScore) {
                maxScore = score;
            }
            ++totalHits;
            if (queueFull) {
                // Fastmatch: return if this hit is not competitive
                final int cmp = reverseMul * comparator.compareBottom(doc);
                if (cmp < 0 || (cmp == 0 && doc + docBase > bottom.doc)) {
                    return;
                }

                // This hit is competitive - replace bottom element in queue & adjustTop
                comparator.copy(bottom.slot, doc);
                updateBottom(doc, score);
                comparator.setBottom(bottom.slot);
            } else {
                // Startup transient: queue hasn't gathered numHits yet
                final int slot = totalHits - 1;
                // Copy hit into queue
                comparator.copy(slot, doc);
                add(slot, doc, score);
                if (queueFull) {
                    comparator.setBottom(bottom.slot);
                }
            }
        }

        @Override
        public boolean acceptsDocsOutOfOrder() {
            return true;
        }

    }

    /*
    * Implements a TopFieldCollector over multiple SortField criteria, without
    * tracking document scores and maxScore.
    */
    private static class MultiComparatorNonScoringCollector extends SellstomeTopFieldCollector {

        final FieldComparator[] comparators;
        final int[] reverseMul;
        final FieldValueHitQueue<FieldValueHitQueue.Entry> queue;
        public MultiComparatorNonScoringCollector(FieldValueHitQueue<FieldValueHitQueue.Entry> queue,
                                                  int numHits, boolean fillFields) throws IOException {
            super(queue, numHits, fillFields);
            this.queue = queue;
            comparators = queue.getComparators();
            reverseMul = queue.getReverseMul();
        }

        final void updateBottom(int doc) {
            // bottom.score is already set to Float.NaN in add().
            bottom.doc = docBase + doc;
            bottom = pq.updateTop();
        }

        @Override
        public void collect(int doc) throws IOException {
            ++totalHits;
            if (queueFull) {
                // Fastmatch: return if this hit is not competitive
                for (int i = 0;; i++) {
                    final int c = reverseMul[i] * comparators[i].compareBottom(doc);
                    if (c < 0) {
                        // Definitely not competitive.
                        return;
                    } else if (c > 0) {
                        // Definitely competitive.
                        break;
                    } else if (i == comparators.length - 1) {
                        // Here c=0. If we're at the last comparator, this doc is not
                        // competitive, since docs are visited in doc Id order, which means
                        // this doc cannot compete with any other document in the queue.
                        return;
                    }
                }

                // This hit is competitive - replace bottom element in queue & adjustTop
                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].copy(bottom.slot, doc);
                }

                updateBottom(doc);

                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].setBottom(bottom.slot);
                }
            } else {
                // Startup transient: queue hasn't gathered numHits yet
                final int slot = totalHits - 1;
                // Copy hit into queue
                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].copy(slot, doc);
                }
                add(slot, doc, Float.NaN);
                if (queueFull) {
                    for (int i = 0; i < comparators.length; i++) {
                        comparators[i].setBottom(bottom.slot);
                    }
                }
            }
        }

        @Override
        public void setNextReader(AtomicReaderContext context) throws IOException {
            docBase = context.docBase;
            for (int i = 0; i < comparators.length; i++) {
                queue.setComparator(i, comparators[i].setNextReader(context));
            }
        }

        @Override
        public void setScorer(Scorer scorer) throws IOException {
            // set the scorer on all comparators
            for (int i = 0; i < comparators.length; i++) {
                comparators[i].setScorer(scorer);
            }
        }
    }

    /*
    * Implements a TopFieldCollector over multiple SortField criteria, without
    * tracking document scores and maxScore, and assumes out of orderness in doc
    * Ids collection.
    */
    private static class OutOfOrderMultiComparatorNonScoringCollector extends
            MultiComparatorNonScoringCollector {

        public OutOfOrderMultiComparatorNonScoringCollector(FieldValueHitQueue<FieldValueHitQueue.Entry> queue,
                                                            int numHits, boolean fillFields) throws IOException {
            super(queue, numHits, fillFields);
        }

        @Override
        public void collect(int doc) throws IOException {
            ++totalHits;
            if (queueFull) {
                // Fastmatch: return if this hit is not competitive
                for (int i = 0;; i++) {
                    final int c = reverseMul[i] * comparators[i].compareBottom(doc);
                    if (c < 0) {
                        // Definitely not competitive.
                        return;
                    } else if (c > 0) {
                        // Definitely competitive.
                        break;
                    } else if (i == comparators.length - 1) {
                        // This is the equals case.
                        if (doc + docBase > bottom.doc) {
                            // Definitely not competitive
                            return;
                        }
                        break;
                    }
                }

                // This hit is competitive - replace bottom element in queue & adjustTop
                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].copy(bottom.slot, doc);
                }

                updateBottom(doc);

                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].setBottom(bottom.slot);
                }
            } else {
                // Startup transient: queue hasn't gathered numHits yet
                final int slot = totalHits - 1;
                // Copy hit into queue
                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].copy(slot, doc);
                }
                add(slot, doc, Float.NaN);
                if (queueFull) {
                    for (int i = 0; i < comparators.length; i++) {
                        comparators[i].setBottom(bottom.slot);
                    }
                }
            }
        }

        @Override
        public boolean acceptsDocsOutOfOrder() {
            return true;
        }

    }

    /*
    * Implements a TopFieldCollector over multiple SortField criteria, with
    * tracking document scores and maxScore.
    */
    private static class MultiComparatorScoringMaxScoreCollector extends MultiComparatorNonScoringCollector {

        Scorer scorer;

        public MultiComparatorScoringMaxScoreCollector(FieldValueHitQueue<FieldValueHitQueue.Entry> queue,
                                                       int numHits, boolean fillFields) throws IOException {
            super(queue, numHits, fillFields);
            // Must set maxScore to NEG_INF, or otherwise Math.max always returns NaN.
            maxScore = Float.NEGATIVE_INFINITY;
        }

        final void updateBottom(int doc, float score) {
            bottom.doc = docBase + doc;
            bottom.score = score;
            bottom =  pq.updateTop();
        }

        @Override
        public void collect(int doc) throws IOException {
            final float score = scorer.score();
            if (score > maxScore) {
                maxScore = score;
            }
            ++totalHits;
            if (queueFull) {
                // Fastmatch: return if this hit is not competitive
                for (int i = 0;; i++) {
                    final int c = reverseMul[i] * comparators[i].compareBottom(doc);
                    if (c < 0) {
                        // Definitely not competitive.
                        return;
                    } else if (c > 0) {
                        // Definitely competitive.
                        break;
                    } else if (i == comparators.length - 1) {
                        // Here c=0. If we're at the last comparator, this doc is not
                        // competitive, since docs are visited in doc Id order, which means
                        // this doc cannot compete with any other document in the queue.
                        return;
                    }
                }

                // This hit is competitive - replace bottom element in queue & adjustTop
                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].copy(bottom.slot, doc);
                }

                updateBottom(doc, score);

                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].setBottom(bottom.slot);
                }
            } else {
                // Startup transient: queue hasn't gathered numHits yet
                final int slot = totalHits - 1;
                // Copy hit into queue
                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].copy(slot, doc);
                }
                add(slot, doc, score);
                if (queueFull) {
                    for (int i = 0; i < comparators.length; i++) {
                        comparators[i].setBottom(bottom.slot);
                    }
                }
            }
        }

        @Override
        public void setScorer(Scorer scorer) throws IOException {
            this.scorer = scorer;
            super.setScorer(scorer);
        }
    }

    /*
    * Implements a TopFieldCollector over multiple SortField criteria, with
    * tracking document scores and maxScore, and assumes out of orderness in doc
    * Ids collection.
    */
    private final static class OutOfOrderMultiComparatorScoringMaxScoreCollector
            extends MultiComparatorScoringMaxScoreCollector {

        public OutOfOrderMultiComparatorScoringMaxScoreCollector(FieldValueHitQueue<FieldValueHitQueue.Entry> queue,
                                                                 int numHits, boolean fillFields) throws IOException {
            super(queue, numHits, fillFields);
        }

        @Override
        public void collect(int doc) throws IOException {
            final float score = scorer.score();
            if (score > maxScore) {
                maxScore = score;
            }
            ++totalHits;
            if (queueFull) {
                // Fastmatch: return if this hit is not competitive
                for (int i = 0;; i++) {
                    final int c = reverseMul[i] * comparators[i].compareBottom(doc);
                    if (c < 0) {
                        // Definitely not competitive.
                        return;
                    } else if (c > 0) {
                        // Definitely competitive.
                        break;
                    } else if (i == comparators.length - 1) {
                        // This is the equals case.
                        if (doc + docBase > bottom.doc) {
                            // Definitely not competitive
                            return;
                        }
                        break;
                    }
                }

                // This hit is competitive - replace bottom element in queue & adjustTop
                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].copy(bottom.slot, doc);
                }

                updateBottom(doc, score);

                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].setBottom(bottom.slot);
                }
            } else {
                // Startup transient: queue hasn't gathered numHits yet
                final int slot = totalHits - 1;
                // Copy hit into queue
                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].copy(slot, doc);
                }
                add(slot, doc, score);
                if (queueFull) {
                    for (int i = 0; i < comparators.length; i++) {
                        comparators[i].setBottom(bottom.slot);
                    }
                }
            }
        }

        @Override
        public boolean acceptsDocsOutOfOrder() {
            return true;
        }

    }

    /*
    * Implements a TopFieldCollector over multiple SortField criteria, with
    * tracking document scores and maxScore.
    */
    private static class MultiComparatorScoringNoMaxScoreCollector extends MultiComparatorNonScoringCollector {

        Scorer scorer;

        public MultiComparatorScoringNoMaxScoreCollector(FieldValueHitQueue<FieldValueHitQueue.Entry> queue,
                                                         int numHits, boolean fillFields) throws IOException {
            super(queue, numHits, fillFields);
        }

        final void updateBottom(int doc, float score) {
            bottom.doc = docBase + doc;
            bottom.score = score;
            bottom = pq.updateTop();
        }

        @Override
        public void collect(int doc) throws IOException {
            ++totalHits;
            if (queueFull) {
                // Fastmatch: return if this hit is not competitive
                for (int i = 0;; i++) {
                    final int c = reverseMul[i] * comparators[i].compareBottom(doc);
                    if (c < 0) {
                        // Definitely not competitive.
                        return;
                    } else if (c > 0) {
                        // Definitely competitive.
                        break;
                    } else if (i == comparators.length - 1) {
                        // Here c=0. If we're at the last comparator, this doc is not
                        // competitive, since docs are visited in doc Id order, which means
                        // this doc cannot compete with any other document in the queue.
                        return;
                    }
                }

                // This hit is competitive - replace bottom element in queue & adjustTop
                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].copy(bottom.slot, doc);
                }

                // Compute score only if it is competitive.
                final float score = scorer.score();
                updateBottom(doc, score);

                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].setBottom(bottom.slot);
                }
            } else {
                // Startup transient: queue hasn't gathered numHits yet
                final int slot = totalHits - 1;
                // Copy hit into queue
                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].copy(slot, doc);
                }

                // Compute score only if it is competitive.
                final float score = scorer.score();
                add(slot, doc, score);
                if (queueFull) {
                    for (int i = 0; i < comparators.length; i++) {
                        comparators[i].setBottom(bottom.slot);
                    }
                }
            }
        }

        @Override
        public void setScorer(Scorer scorer) throws IOException {
            this.scorer = scorer;
            super.setScorer(scorer);
        }
    }

    /*
    * Implements a TopFieldCollector over multiple SortField criteria, with
    * tracking document scores and maxScore, and assumes out of orderness in doc
    * Ids collection.
    */
    private final static class OutOfOrderMultiComparatorScoringNoMaxScoreCollector
            extends MultiComparatorScoringNoMaxScoreCollector {

        public OutOfOrderMultiComparatorScoringNoMaxScoreCollector(
                FieldValueHitQueue<FieldValueHitQueue.Entry> queue, int numHits, boolean fillFields)
                throws IOException {
            super(queue, numHits, fillFields);
        }

        @Override
        public void collect(int doc) throws IOException {
            ++totalHits;
            if (queueFull) {
                // Fastmatch: return if this hit is not competitive
                for (int i = 0;; i++) {
                    final int c = reverseMul[i] * comparators[i].compareBottom(doc);
                    if (c < 0) {
                        // Definitely not competitive.
                        return;
                    } else if (c > 0) {
                        // Definitely competitive.
                        break;
                    } else if (i == comparators.length - 1) {
                        // This is the equals case.
                        if (doc + docBase > bottom.doc) {
                            // Definitely not competitive
                            return;
                        }
                        break;
                    }
                }

                // This hit is competitive - replace bottom element in queue & adjustTop
                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].copy(bottom.slot, doc);
                }

                // Compute score only if it is competitive.
                final float score = scorer.score();
                updateBottom(doc, score);

                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].setBottom(bottom.slot);
                }
            } else {
                // Startup transient: queue hasn't gathered numHits yet
                final int slot = totalHits - 1;
                // Copy hit into queue
                for (int i = 0; i < comparators.length; i++) {
                    comparators[i].copy(slot, doc);
                }

                // Compute score only if it is competitive.
                final float score = scorer.score();
                add(slot, doc, score);
                if (queueFull) {
                    for (int i = 0; i < comparators.length; i++) {
                        comparators[i].setBottom(bottom.slot);
                    }
                }
            }
        }

        @Override
        public void setScorer(Scorer scorer) throws IOException {
            this.scorer = scorer;
            super.setScorer(scorer);
        }

        @Override
        public boolean acceptsDocsOutOfOrder() {
            return true;
        }

    }


    public SellstomeTopFieldCollector(PriorityQueue<FieldValueHitQueue.Entry> pq, int numHits, boolean fillFields) {
        super(pq, numHits, fillFields);
    }

    /**
     * Creates a new {@link TopFieldCollector} from the given
     * arguments.
     *
     * <p><b>NOTE</b>: The instances returned by this method
     * pre-allocate a full array of length
     * <code>numHits</code>.
     *
     * @param sort
     *          the sort criteria (SortFields).
     * @param numHits
     *          the number of results to collect.
     * @param fillFields
     *          specifies whether the actual field values should be returned on
     *          the results (FieldDoc).
     * @param trackDocScores
     *          specifies whether document scores should be tracked and set on the
     *          results. Note that if set to false, then the results' scores will
     *          be set to Float.NaN. Setting this to true affects performance, as
     *          it incurs the score computation on each competitive result.
     *          Therefore if document scores are not required by the application,
     *          it is recommended to set it to false.
     * @param trackMaxScore
     *          specifies whether the query's maxScore should be tracked and set
     *          on the resulting {@link org.apache.lucene.search.TopDocs}. Note that if set to false,
     *          {@link org.apache.lucene.search.TopDocs#getMaxScore()} returns Float.NaN. Setting this to
     *          true affects performance as it incurs the score computation on
     *          each result. Also, setting this true automatically sets
     *          <code>trackDocScores</code> to true as well.
     * @param docsScoredInOrder
     *          specifies whether documents are scored in doc Id order or not by
     *          the given {@link Scorer} in {@link #setScorer(Scorer)}.
     * @return a {@link TopFieldCollector} instance which will sort the results by
     *         the sort criteria.
     * @throws IOException
     */
    public static TopFieldCollector create(Sort sort, int numHits,
                                           boolean fillFields, boolean trackDocScores, boolean trackMaxScore,
                                           boolean docsScoredInOrder)
            throws IOException {

        if (sort.getSort().length == 0) {
            throw new IllegalArgumentException("Sort must contain at least one field");
        }

        if (numHits <= 0) {
            throw new IllegalArgumentException("numHits must be > 0; please use TotalHitCountCollector if you just need the total hit count");
        }

        FieldValueHitQueue<FieldValueHitQueue.Entry> queue = SellstomeFieldValueHitQueue.create(sort.getSort(), numHits);
        if (queue.getComparators().length == 1) {
            if (docsScoredInOrder) {
                if (trackMaxScore) {
                    return new OneComparatorScoringMaxScoreCollector(queue, numHits, fillFields);
                } else if (trackDocScores) {
                    return new OneComparatorScoringNoMaxScoreCollector(queue, numHits, fillFields);
                } else {
                    return new OneComparatorNonScoringCollector(queue, numHits, fillFields);
                }
            } else {
                if (trackMaxScore) {
                    return new OutOfOrderOneComparatorScoringMaxScoreCollector(queue, numHits, fillFields);
                } else if (trackDocScores) {
                    return new OutOfOrderOneComparatorScoringNoMaxScoreCollector(queue, numHits, fillFields);
                } else {
                    return new OutOfOrderOneComparatorNonScoringCollector(queue, numHits, fillFields);
                }
            }
        }

        // multiple comparators.
        if (docsScoredInOrder) {
            if (trackMaxScore) {
                return new MultiComparatorScoringMaxScoreCollector(queue, numHits, fillFields);
            } else if (trackDocScores) {
                return new MultiComparatorScoringNoMaxScoreCollector(queue, numHits, fillFields);
            } else {
                return new MultiComparatorNonScoringCollector(queue, numHits, fillFields);
            }
        } else {
            if (trackMaxScore) {
                return new OutOfOrderMultiComparatorScoringMaxScoreCollector(queue, numHits, fillFields);
            } else if (trackDocScores) {
                return new OutOfOrderMultiComparatorScoringNoMaxScoreCollector(queue, numHits, fillFields);
            } else {
                return new OutOfOrderMultiComparatorNonScoringCollector(queue, numHits, fillFields);
            }
        }
    }

    /**
     * Returns the documents in the rage [start .. start+howMany) that were
     * collected by this collector. Note that if start >= pq.size(), an empty
     * TopDocs is returned, and if pq.size() - start &lt; howMany, then only the
     * available documents in [start .. pq.size()) are returned.<br>
     * This method is useful to call in case pagination of search results is
     * allowed by the search application, as well as it attempts to optimize the
     * memory used by allocating only as much as requested by howMany.<br>
     * <b>NOTE:</b> you cannot call this method more than once for each search
     * execution. If you need to call it more than once, passing each time a
     * different range, you should call {@link #topDocs()} and work with the
     * returned {@link TopDocs} object, which will contain all the results this
     * search execution collected.
     */
    @Override
    public TopDocs topDocs(int start, int howMany) {

        // In case pq was populated with sentinel values, there might be less
        // results than pq.size(). Therefore return all results until either
        // pq.size() or totalHits.
        int size = topDocsSize();

        // Don't bother to throw an exception, just return an empty TopDocs in case
        // the parameters are invalid or out of range.
        // TODO: shouldn't we throw IAE if apps give bad params here so they dont
        // have sneaky silent bugs?
        if (start < 0 || start >= size || howMany <= 0) {
            return newTopDocs(null, start);
        }

        //apply refinement. should we create a interface
        applyRefinementSorting();

        // We know that start < pqsize, so just fix howMany.
        howMany = Math.min(size - start, howMany);
        ScoreDoc[] results = new ScoreDoc[howMany];

        // pq's pop() returns the 'least' element in the queue, therefore need
        // to discard the first ones, until we reach the requested range.
        // Note that this loop will usually not be executed, since the common usage
        // should be that the caller asks for the last howMany results. However it's
        // needed here for completeness.
        for (int i = pq.size() - start - howMany; i > 0; i--) { pq.pop(); }

        // Get the requested results from pq.
        populateResults(results, howMany);

        return newTopDocs(results, start);
    }


    protected void applyRefinementSorting() {

    }



}