package org.rain.vertx.app.base.serialization;

import org.rain.vertx.app.base.utils.SerializationUtils;

/**
 * This implementation of {@link ISerDeser} use Kryo library for
 * serializing/deserializing.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.5.0
 */
public class KryoSerDeser implements ISerDeser {
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] toBytes(Object obj) throws SerializationException {
        return toBytes(obj, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] toBytes(Object obj, ClassLoader classLoader) throws SerializationException {
        return SerializationUtils.toByteArrayKryo(obj, classLoader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T fromBytes(byte[] data, Class<T> clazz) throws DeserializationException {
        return fromBytes(data, clazz, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T fromBytes(byte[] data, Class<T> clazz, ClassLoader classLoader)
            throws DeserializationException {
        return SerializationUtils.fromByteArrayKryo(data, clazz, classLoader);
    }
}
