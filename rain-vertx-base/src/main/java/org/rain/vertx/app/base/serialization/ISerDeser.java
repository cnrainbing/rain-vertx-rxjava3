package org.rain.vertx.app.base.serialization;

/**
 * Serializer/Deserializer interface.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.5.0
 */
public interface ISerDeser {
    /**
     * Serialize an object.
     *
     * @return
     * @throws SerializationException
     */
    public byte[] toBytes(Object obj) throws SerializationException;

    /**
     * Serialize an object.
     *
     * @param obj
     * @param classLoader
     * @return
     * @throws SerializationException
     */
    public byte[] toBytes(Object obj, ClassLoader classLoader) throws SerializationException;

    /**
     * Deserialize an object.
     *
     * @param data
     * @param clazz
     * @return
     * @throws DeserializationException
     */
    public <T> T fromBytes(byte[] data, Class<T> clazz) throws DeserializationException;

    /**
     * Deserialize an object.
     *
     * @param data
     * @param clazz
     * @param classLoader
     * @return
     * @throws DeserializationException
     */
    public <T> T fromBytes(byte[] data, Class<T> clazz, ClassLoader classLoader)
            throws DeserializationException;
}