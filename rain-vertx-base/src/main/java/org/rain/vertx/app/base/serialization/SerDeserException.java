package org.rain.vertx.app.base.serialization;

/**
 * Thrown to indicate that there was an exception during serialization/deserialization.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.9.1
 */
public class SerDeserException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SerDeserException() {
    }

    public SerDeserException(String message) {
        super(message);
    }

    public SerDeserException(Throwable cause) {
        super(cause);
    }

    public SerDeserException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerDeserException(String message, Throwable cause, boolean enableSuppression,
                             boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}