package sellstome.search.solr.response;

/**
 * Created by IntelliJ IDEA.
 * User: Alex
 * Date: 12.10.11
 * Time: 0:31
 * Definition of the ad solr index schema in code.
 * Note: ideally this file should be in sync with the ad/schema.xml
 */
public enum AdSchema {
    /** doc id */
    ID("id"),
    /** title */
    TITLE("title"),
    /** User supplied ad description. */
    BODY("body"),
    /** Item price. */
    PRICE("price"),
    /** Item location. */
    LOCATION("location");

    private final String fieldName;

    AdSchema(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
