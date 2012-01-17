package sellstome.search.solr.response;

/**
 * Created by IntelliJ IDEA.
 * User: Alex
 * Date: 12.10.11
 * Time: 0:31
 * Definition of the ad solr index schema in code.
 * Note: ideally this file should be in sync with the ad/schema.xml
 * todo zhugrov a - make schema.xml the only source of schema information
 */
public enum AdSchema {
    /** doc id */
    ID("id", "_id", StringSolrFieldConverter$.MODULE$),
    /** A short ad description. */
    MESSAGE("message", StringSolrFieldConverter$.MODULE$),
    /** Item price. todo zhugrov a - add a more precise logic for converting price data. */
    PRICE("price", StringSolrFieldConverter$.MODULE$),
    /** Item location. */
    LOCATION("location", LocationSolrFieldConverter$.MODULE$);

    private final String fieldName;
    
    private final String responseFieldName;

    private final SolrField2JsonConverter converter;

    AdSchema(String fieldName, SolrField2JsonConverter converter) {
        this.fieldName = fieldName;
        this.responseFieldName = fieldName;
        this.converter = converter;
    }

    AdSchema(String fieldName, String responseFieldName, SolrField2JsonConverter converter) {
        this.fieldName = fieldName;
        this.responseFieldName = responseFieldName;
        this.converter = converter;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getResponseFieldName() {
        return responseFieldName;
    }
    
    public Object toJson(String storedField) {
        return converter.toJson(storedField);
    }
}
