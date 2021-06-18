package org.rain.vertx.app.base.serialization;

import org.rain.vertx.app.base.utils.SerializationUtils;

import java.nio.charset.StandardCharsets;

/**
 * This implementation of {@link ISerDeser} utilizes JSON for
 * serializing/deserializing.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.5.0
 */
public class JsonSerDeser implements ISerDeser {
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
        String json = SerializationUtils.toJsonString(obj, classLoader);
        return json != null ? json.getBytes(StandardCharsets.UTF_8) : null;
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
        String json = data != null ? new String(data, StandardCharsets.UTF_8) : null;
        return SerializationUtils.fromJsonString(json, clazz, classLoader);
    }
}
