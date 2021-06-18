package org.rain.vertx.app.base.jackson;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.rain.vertx.app.base.hash.HashUtils;
import org.rain.vertx.app.base.utils.SerializationUtils;

/**
 * Utility class to work with Jackson's {@link JsonNode}.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.6.2
 */
public class JacksonUtils {

    /**
     * Serialize an object to {@link JsonNode}.
     *
     * @param obj
     * @return
     */
    public static JsonNode toJson(Object obj) {
        return toJson(obj, null);
    }

    /**
     * Serialize an object to {@link JsonNode}, with a custom class loader.
     *
     * @param obj
     * @param classLoader
     * @return
     */
    public static JsonNode toJson(Object obj, ClassLoader classLoader) {
        return SerializationUtils.toJson(obj, classLoader);
    }

    /**
     * Deserialize a {@link JsonNode}.
     *
     * @param json
     * @return
     */
    public static Object fromJson(JsonNode json) {
        return fromJson(json, Object.class, null);
    }

    /**
     * Deserialize a {@link JsonNode}, with custom class loader.
     *
     * @param json
     * @param classLoader
     * @return
     */
    public static Object fromJson(JsonNode json, ClassLoader classLoader) {
        return fromJson(json, Object.class, classLoader);
    }

    /**
     * Deserialize a {@link JsonNode}.
     *
     * @param json
     * @param clazz
     * @return
     */
    public static <T> T fromJson(JsonNode json, Class<T> clazz) {
        return fromJson(json, clazz, null);
    }

    /**
     * Deserialize a {@link JsonNode}, with custom class loader.
     *
     * @param json
     * @param clazz
     * @return
     */
    public static <T> T fromJson(JsonNode json, Class<T> clazz, ClassLoader classLoader) {
        return SerializationUtils.fromJson(json, clazz, classLoader);
    }

    /**
     * Read a JSON string and parse to {@link JsonNode} instance.
     *
     * @param source
     * @return
     */
    public static JsonNode readJson(String source) {
        return readJson(source, null);
    }

    /**
     * Read a JSON string and parse to {@link JsonNode} instance, with a custom class loader.
     *
     * @param source
     * @param classLoader
     * @return
     */
    public static JsonNode readJson(String source, ClassLoader classLoader) {
        return SerializationUtils.readJson(source, classLoader);
    }

    /**
     * Read a JSON string and parse to {@link JsonNode} instance.
     *
     * @param source
     * @return
     */
    public static JsonNode readJson(byte[] source) {
        return readJson(source, null);
    }

    /**
     * Read a JSON string and parse to {@link JsonNode} instance, with a custom class loader.
     *
     * @param source
     * @param classLoader
     * @return
     */
    public static JsonNode readJson(byte[] source, ClassLoader classLoader) {
        return SerializationUtils.readJson(source, classLoader);
    }

    /**
     * Read a JSON string and parse to {@link JsonNode} instance.
     *
     * @param source
     * @return
     */
    public static JsonNode readJson(InputStream source) {
        return readJson(source, null);
    }

    /**
     * Read a JSON string and parse to {@link JsonNode} instance, with a custom class loader.
     *
     * @param source
     * @param classLoader
     * @return
     */
    public static JsonNode readJson(InputStream source, ClassLoader classLoader) {
        return SerializationUtils.readJson(source, classLoader);
    }

    /**
     * Read a JSON string and parse to {@link JsonNode} instance.
     *
     * @param source
     * @return
     */
    public static JsonNode readJson(Reader source) {
        return readJson(source, null);
    }

    /**
     * Read a JSON string and parse to {@link JsonNode} instance, with a custom class loader.
     *
     * @param source
     * @param classLoader
     * @return
     */
    public static JsonNode readJson(Reader source, ClassLoader classLoader) {
        return SerializationUtils.readJson(source, classLoader);
    }

    /**
     * Extract value from a {@link JsonNode}.
     *
     * @param node
     * @param clazz
     * @return
     */
    public static <T> T asValue(JsonNode node, Class<T> clazz) {
        return node != null ? ValueUtils.convertValue(node, clazz) : null;
    }

    /**
     * Extract value from a {@link JsonNode} as a date. If the extracted value is string, parse it
     * as {@link Date} using the specified date-time format.
     *
     * @param node
     * @param dateTimeFormat
     * @return
     * @since 0.6.3.3
     */
    public static Date asDate(JsonNode node, String dateTimeFormat) {
        return node != null ? ValueUtils.convertDate(node, dateTimeFormat) : null;
    }

    /**
     * Extract a date value from the target {@link JsonNode} using DPath expression. If the
     * extracted value is
     * a string, parse it as a {@link Date} using the specified date-time format.
     *
     * @param node
     * @param dPath
     * @param dateTimeFormat see {@link SimpleDateFormat}
     * @return
     * @see DPathUtils
     */
    public static Date getDate(JsonNode node, String dPath, String dateTimeFormat) {
        return DPathUtils.getDate(node, dPath, dateTimeFormat);
    }

    /**
     * Extract a value from the target {@link JsonNode} using DPath expression (generic
     * version).
     *
     * @param node
     * @param dPath
     * @param clazz
     * @return
     * @see DPathUtils
     */
    public static <T> T getValue(JsonNode node, String dPath, Class<T> clazz) {
        return DPathUtils.getValue(node, dPath, clazz);
    }

    /**
     * Extract a value from the target {@link JsonNode} using DPath expression (generic
     * version).
     *
     * @param node
     * @param dPath
     * @param clazz
     * @return
     * @see DPathUtils
     * @since 0.7.1
     */
    public static <T> Optional<T> getValueOptional(JsonNode node, String dPath, Class<T> clazz) {
        return Optional.ofNullable(getValue(node, dPath, clazz));
    }

    /**
     * Extract a value from the target {@link JsonNode} using DPath expression.
     *
     * @param node
     * @param dPath
     * @see DPathUtils
     */
    public static JsonNode getValue(JsonNode node, String dPath) {
        return DPathUtils.getValue(node, dPath);
    }

    /**
     * Extract a value from the target {@link JsonNode} using DPath expression.
     *
     * @param node
     * @param dPath
     * @return
     * @see DPathUtils
     * @since 0.7.1
     */
    public static Optional<JsonNode> getValueOptional(JsonNode node, String dPath) {
        return Optional.ofNullable(getValue(node, dPath));
    }

    /**
     * Set a value to the target {@link JsonNode} specified by DPath expression.
     *
     * <p>
     * Note: intermediated nodes will be created if {@code createIntermediatePath} is true.
     * </p>
     *
     * <p>
     * Note: if {@code value} is {@code null}, the item (specified by {@code dPath} will be removed.
     * </p>
     *
     * @param node
     * @param dPath
     * @param value
     * @param createIntermediatePath
     * @see DPathUtils
     */
    public static void setValue(JsonNode node, String dPath, Object value,
                                boolean createIntermediatePath) {
        DPathUtils.setValue(node, dPath, value, createIntermediatePath);
    }

    /**
     * Set a value to the target {@link JsonNode} specified by DPath expression.
     *
     * <p>
     * Note: intermediated nodes will NOT be created.
     * </p>
     *
     * <p>
     * Note: if {@code value} is {@code null}, the item (specified by {@code dPath} will be removed.
     * </p>
     *
     * @param node
     * @param dPath
     * @param value
     * @see DPathUtils
     */
    public static void setValue(JsonNode node, String dPath, Object value) {
        DPathUtils.setValue(node, dPath, value, false);
    }

    /**
     * Delete a value from the target {@link JsonNode} specified by DPath expression.
     *
     * @param node
     * @param dPath
     * @see DPathUtils
     */
    public static void deleteValue(JsonNode node, String dPath) {
        DPathUtils.deleteValue(node, dPath);
    }

    /**
     * Calculate checksum of a {@link JsonNode}, using a supplied hash function.
     *
     * <ul>
     * <li>Checksum is calculated recursively (e.g. child-nodes are included).</li>
     * <li>If target node is {@code null}, {@link NullNode} or {@link MissingNode}, value {@code 0}
     * is returned.</li>
     * <li>If target node is {@link ValueNode}, hash it using the supplied {@code hashFunc} and
     * return the hash value as long.</li>
     * <li>If target node is {@link ArrayNode}: combine each child-node's checksum in-order and
     * return the final hash value as long.</li>
     * <li>If target node is {@link ObjectNode}: combine each child-node's checksum (node's named
     * included) un-order and return the final hash value as long.</li>
     * <li>Otherwise, return {@code obj.hashCode()}</li>
     * </ul>
     *
     * @param node
     * @param hashFunc
     * @return
     * @since 0.6.3
     */
    public static long checksum(JsonNode node, HashFunction hashFunc) {
        if (node == null || node instanceof NullNode || node instanceof MissingNode) {
            return 0;
        }
        if (node instanceof ValueNode) {
            return hashFunc.hashString(node.toString(), StandardCharsets.UTF_8).asLong();
        }
        if (node instanceof ArrayNode) {
            final List<HashCode> hashCodes = new ArrayList<>();
            for (JsonNode child : node) {
                hashCodes.add(hashFunc.newHasher().putLong(checksum(child, hashFunc)).hash());
            }
            return hashCodes.size() > 0 ? Hashing.combineOrdered(hashCodes).padToLong() : 0;
        }
        if (node instanceof ObjectNode) {
            final List<HashCode> hashCodes = new ArrayList<>();
            node.fields().forEachRemaining(new Consumer<Entry<String, JsonNode>>() {
                @Override
                public void accept(Entry<String, JsonNode> entry) {
                    hashCodes.add(
                            hashFunc.newHasher().putString(entry.getKey(), StandardCharsets.UTF_8)
                                    .putLong(checksum(entry.getValue(), hashFunc)).hash());
                }
            });
            return hashCodes.size() > 0 ? Hashing.combineUnordered(hashCodes).padToLong() : 0;
        }
        return node.hashCode();
    }

    /**
     * Calculate checksum of a {@link JsonNode} using default hash function.
     *
     * @param node
     * @return
     * @see #checksum(JsonNode, HashFunction)
     * @since 0.6.2
     */
    public static long checksum(JsonNode node) {
        return checksum(node, HashUtils.fastHashFunc);
    }

    /**
     * Calculate checksum of a {@link JsonNode} using Murmur3 hash.
     *
     * @param node
     * @return
     * @see #checksum(JsonNode, HashFunction)
     * @since 0.6.3
     */
    public static long checksumMurmur3(JsonNode node) {
        return checksum(node, HashUtils.murmur3);
    }

    /**
     * Calculate checksum of a {@link JsonNode} using CRC32 hash.
     *
     * @param node
     * @return
     * @see #checksum(JsonNode, HashFunction)
     * @since 0.6.3
     */
    public static long checksumCrc32(JsonNode node) {
        return checksum(node, HashUtils.crc32);
    }

    /**
     * Calculate checksum of a {@link JsonNode} using MD5 hash.
     *
     * @param node
     * @return
     * @see #checksum(JsonNode, HashFunction)
     * @since 0.6.3
     */
    public static long checksumMd5(JsonNode node) {
        return checksum(node, HashUtils.md5);
    }

    /**
     * Calculate checksum of a {@link JsonNode} using SHA1 hash.
     *
     * @param node
     * @return
     * @see #checksum(JsonNode, HashFunction)
     * @since 0.6.3
     */
    public static long checksumSha1(JsonNode node) {
        return checksum(node, HashUtils.sha1);
    }

    /**
     * Calculate checksum of a {@link JsonNode} using SHA256 hash.
     *
     * @param node
     * @return
     * @see #checksum(JsonNode, HashFunction)
     * @since 0.6.3
     */
    public static long checksumSha256(JsonNode node) {
        return checksum(node, HashUtils.sha256);
    }

    /**
     * Calculate checksum of a {@link JsonNode} using SHA512 hash.
     *
     * @param node
     * @return
     * @see #checksum(JsonNode, HashFunction)
     * @since 0.6.3
     */
    public static long checksumSha512(JsonNode node) {
        return checksum(node, HashUtils.sha512);
    }

    /*----------------------------------------------------------------------*/

    private static class MyClass {
        public int age = 0;
        public String name = "";
        public boolean gender = false;

        public int hashCode() {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(age).append(name).append(gender);
            return hcb.hashCode();
        }
    }

    /*public static void main(String[] args) throws Exception {
        Map<String, Object> mapBase = new HashMap<String, Object>();
        mapBase.put("key1", "value1");
        mapBase.put("key2", 2);
        mapBase.put("key3", 3.0);
        mapBase.put("key4", true);
        mapBase.put("key5", new MyClass());

        JsonNode json1 = toJson(mapBase);
        System.out.println(json1);
        System.out.println(checksum(json1));

        JsonNode json2 = toJson(fromJson(json1));
        deleteValue(json2, "key3");
        System.out.println(json2);
        setValue(json2, "key3", 4.0);
        System.out.println(json2);
        System.out.println(checksum(json2));
    }*/
}
