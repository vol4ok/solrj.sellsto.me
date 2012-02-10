package sellstome.search.solr.common;

/**
 * The exception is thrown when a requested feature or operation is not implemented.
 * This class at some point very similar to a {@link FeatureNotImplemented}
 * @author Aliaksandr Zhuhrou
 * @since 1.0
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
