package org.rain.vertx.app.base.serialization;

/**
 * Thrown to indicate that there was an exception during deserialization.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.5.0
 */
public class DeserializationException extends SerDeserException {

    private static final long serialVersionUID = 1L;

    public DeserializationException() {
    }

    public DeserializationException(String message) {
        super(message);
    }

    public DeserializationException(Throwable cause) {
        super(cause);
    }

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeserializationException(String message, Throwable cause, boolean enableSuppression,
                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
