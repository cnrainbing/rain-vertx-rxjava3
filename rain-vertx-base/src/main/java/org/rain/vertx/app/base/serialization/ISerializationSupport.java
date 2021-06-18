package org.rain.vertx.app.base.serialization;

/**
 * Class implements this interface to provide its own
 * serialization/deserialization methods.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.5.0
 */
public interface ISerializationSupport {
    /**
     * Serializes this object to {@code byte[]}.
     *
     * @return
     * @throws SerializationException
     */
    public byte[] toBytes() throws SerializationException;

    /**
     * Deserializes and populates this object.
     *
     * @param data
     * @return
     * @throws DeserializationException
     */
    public ISerializationSupport fromBytes(byte[] data) throws DeserializationException;
}
