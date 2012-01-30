package sellstome.search.solr.common;

/**
 * Created by IntelliJ IDEA.
 * User: Asus
 * Date: 25.01.12
 * Time: 8:35
 * To change this template use File | Settings | File Templates.
 */
public class NotImplementedException extends RuntimeException {

    public NotImplementedException() {
        this("This feature is not implemented yet...");
    }

    public NotImplementedException(String message) {
        super(message);
    }

    public NotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotImplementedException(Throwable cause) {
        super(cause);
    }

    public NotImplementedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
