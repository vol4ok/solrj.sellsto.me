package sellstome;

/**
 * @author Aliaksandr Zhuhrou
 */
public class HelperRandomTestException extends RuntimeException {
    public HelperRandomTestException() {}

    public HelperRandomTestException(String message) {
        super(message);
    }

    public HelperRandomTestException(String message, Throwable cause) {
        super(message, cause);
    }

    public HelperRandomTestException(Throwable cause) {
        super(cause);
    }

    public HelperRandomTestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
