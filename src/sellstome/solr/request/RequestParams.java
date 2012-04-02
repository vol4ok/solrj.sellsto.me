package sellstome.solr.request;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Contains a constants that could be found in the search request.
 * For example the ones that defines the bounded location of search results.
 */
public interface RequestParams {

    /** defines a bounded box for the search query */
    public enum LocationBound {
        BOTTOM("location.bottom"),
        TOP("location.top"),
        LEFT("location.left"),
        RIGHT("location.right");
        
        private String parameterName;

        private LocationBound(String parameterName) {
            this.parameterName = parameterName;
        }

        public String getParameterName() {
            return parameterName;
        }

        /** transforms request params to a solr request query */
        public static void populateFQQuery(@Nonnull SolrQueryRequest req) {

            //todo temporary solution do not take into account such thing as existent query filter. Need to add a merge logic in future
            SolrParams reqParams = req.getParams();
            String filterQuery = getFilterQuery(reqParams);

            if (filterQuery != null) {
                SolrParams fqParam = toSolrParams(CommonParams.FQ, filterQuery);
                req.setParams(SolrParams.wrapDefaults(fqParam, reqParams));
            }

        }

        /** returns null if the request does not contain all required parameters */
        @Nullable
        private static String getFilterQuery(@Nonnull SolrParams reqParams) {
            String bottom = reqParams.get(BOTTOM.getParameterName());
            String top    = reqParams.get(TOP.getParameterName());
            String left   = reqParams.get(LEFT.getParameterName());
            String right  = reqParams.get(RIGHT.getParameterName());
            if (bottom == null || top == null || left == null || right == null ) return null;
            return Joiner.on("").join("location:[", bottom, ",", left, " TO ", top, ",", right,"]");
        }

        @Nonnull
        private static SolrParams toSolrParams(@Nonnull String name,@Nonnull String value) {
            Map<String,String> params = Maps.newHashMap();
            params.put(name, value);
            return new MapSolrParams(params);
        }
        
    }

}