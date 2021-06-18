package org.rain.vertx.app.base.hash;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * Helper class to calculate hash values.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class HashUtils {
    public final static HashFunction murmur332bit = Hashing.murmur3_32(0);
    public final static HashFunction murmur3 = Hashing.murmur3_128(0);
    public final static HashFunction crc32 = Hashing.crc32();
    public final static HashFunction md5 = Hashing.md5();
    public final static HashFunction sha1 = Hashing.sha1();
    public final static HashFunction sha256 = Hashing.sha256();
    public final static HashFunction sha512 = Hashing.sha512();

    public final static HashFunction fastHashFunc = murmur3;

    /*---------- Hashing methods ----------*/

    /**
     * Calculate checksum of an object using Murmur3 hash.
     *
     * @param obj
     * @return
     * @see #checksum(Object, HashFunction)
     * @since 0.6.3
     */
    public static long checksumMurmur3(Object obj) {
        return checksum(obj, murmur3);
    }

    /**
     * Calculate checksum of an object using CRC32 hash.
     *
     * @param obj
     * @return
     * @see #checksum(Object, HashFunction)
     * @since 0.6.3
     */
    public static long checksumCrc32(Object obj) {
        return checksum(obj, crc32);
    }

    /**
     * Calculate checksum of an object using MD5 hash.
     *
     * @param obj
     * @return
     * @see #checksum(Object, HashFunction)
     * @since 0.6.3
     */
    public static long checksumMd5(Object obj) {
        return checksum(obj, md5);
    }

    /**
     * Calculate checksum of an object using SHA1 hash.
     *
     * @param obj
     * @return
     * @see #checksum(Object, HashFunction)
     * @since 0.6.3
     */
    public static long checksumSha1(Object obj) {
        return checksum(obj, sha1);
    }

    /**
     * Calculate checksum of an object using SHA256 hash.
     *
     * @param obj
     * @return
     * @see #checksum(Object, HashFunction)
     * @since 0.6.3
     */
    public static long checksumSha256(Object obj) {
        return checksum(obj, sha256);
    }

    /**
     * Calculate checksum of an object using SHA512 hash.
     *
     * @param obj
     * @return
     * @see #checksum(Object, HashFunction)
     * @since 0.6.3
     */
    public static long checksumSha512(Object obj) {
        return checksum(obj, sha512);
    }

    /**
     * Calculate checksum of an object using default hash function.
     *
     * @param obj
     * @return
     * @see #checksum(Object, HashFunction)
     * @since 0.6.3
     */
    public static long checksum(Object obj) {
        return checksum(obj, fastHashFunc);
    }

    /**
     * Calculate checksum of an object, using a supplied hash function.
     *
     * <p>
     * Note: to calculate checksum of a {@link com.fasterxml.jackson.databind.JsonNode}, use
     * {@link JacksonUtils#checksum(com.fasterxml.jackson.databind.JsonNode)}
     * </p>
     *
     * <ul>
     * <li>Checksum is calculated recursively (if the target object is not scalar type).</li>
     * <li>If {@code obj} is {@code null}: return {@code 0L}.</li>
     * <li>If {@code obj} is wrapper type ({@link Integer}, {@link Long}, {@link Boolean}, etc), or
     * {@link String}: return {@code hashFunc(obj)}.</li>
     * <li>If {@code obj} is {@link BigInteger} or {@link BigDecimal}: return
     * {@code hashFunc(obj.hashCode())}.</li>
     * <li>If {@code obj} is array of primitive types, {@link Object}s or a {@link List}: combine
     * each array/list entry's checksum in-order and return the final hash value as long.</li>
     * <li>If {@code obj} is of type {@code Map}: combine each map entry's (key & value) checksum
     * un-order and return the final hash value as long.</li>
     * <li>If {@code obj} is of type {@link Collection}: combine each entry's checksum un-order and
     * return the final hash value as long.</li>
     * <li>Otherwise, return {@code obj.hashCode()}</li>
     * </ul>
     *
     * <p>
     * Note:
     * <ul>
     * <li>{@code Checksum(BigInteger(x))} should NOT be equal to
     * {@code Checksum(BigDecimal(x))}.</li>
     * <li>{@code Checksum(Double(x))} should be equal to {@code Checksum(Float(x)).}</li>
     * <li>{@code Checksum(Byte(x))}, {@code Checksum(Short(x))}, {@code Checksum(Integer(x))} and
     * {@code Checksum(Long(x))} should be equal.</li>
     * <li>{@code Checksum(int[1,2,3,4])}, {@code Checksum(Long[1,2,3,4])} and
     * {@code Checksum(List<integer-type>[1,2,3,4])} should be equal.</li>
     * <li>{@code Checksum(Map[key1=>value1,key2=>value2])} should be equal to
     * {@code Checksum(Map[key2=>value2,key1=>value1])}.</li>
     * <li>{@code Checksum(non-list-Collection[value1,value2]} should be equal to
     * {@code Checksum(non-list-Collection[value2,value1]}.</li>
     * <li>{@code Checksum(array[1,2,3,4])} should be equal to {@code Checksum(List[1,2,3,4])}, but
     * should NOT be equal to {@code Checksum(non-list-Collection[1,2,3,4]}.</li>
     * </ul>
     * </p>
     *
     * @param obj
     * @param hashFunc
     * @return
     * @since 0.6.3
     */
    public static long checksum(Object obj, HashFunction hashFunc) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Number) {
            Number num = (Number) obj;
            if (num instanceof BigDecimal || num instanceof BigInteger) {
                // BigDecimal & BigInteger have DIFFERENT checksum values.
                return hashFunc.newHasher().putInt(obj.hashCode()).hash().padToLong();
            }
            if (num instanceof Double || num instanceof Float) {
                // Double & Float should have same checksum value.
                return hashFunc.newHasher().putDouble(num.doubleValue()).hash().padToLong();
            }
            // Integers (Byte, Short, Integer, Double) should have same checksum value.
            return hashFunc.newHasher().putLong(num.longValue()).hash().padToLong();
        }

        if (obj instanceof Boolean) {
            return hashFunc.newHasher().putBoolean(((Boolean) obj).booleanValue()).hash()
                    .padToLong();
        }
        if (obj instanceof Character) {
            return hashFunc.newHasher().putChar(((Character) obj).charValue()).hash().padToLong();
        }
        if (obj instanceof String) {
            return hashFunc.newHasher().putString(obj.toString(), StandardCharsets.UTF_8).hash()
                    .padToLong();
        }
        if (obj instanceof byte[] || obj instanceof short[] || obj instanceof int[]
                || obj instanceof long[] || obj instanceof float[] || obj instanceof double[]
                || obj instanceof boolean[] || obj instanceof char[]) {
            final Hasher hasher = hashFunc.newHasher();
            switch (obj.getClass().getCanonicalName()) {
                case "byte[]":
                    for (byte v : (byte[]) obj) {
                        hasher.putLong(checksum(v, hashFunc));
                    }
                    break;
                case "short[]":
                    for (short v : (short[]) obj) {
                        hasher.putLong(checksum(v, hashFunc));
                    }
                    break;
                case "int[]":
                    for (int v : (int[]) obj) {
                        hasher.putLong(checksum(v, hashFunc));
                    }
                    break;
                case "long[]":
                    for (long v : (long[]) obj) {
                        hasher.putLong(checksum(v, hashFunc));
                    }
                    break;
                case "float[]":
                    for (float v : (float[]) obj) {
                        hasher.putLong(checksum(v, hashFunc));
                    }
                    break;
                case "double[]":
                    for (double v : (double[]) obj) {
                        hasher.putLong(checksum(v, hashFunc));
                    }
                    break;
                case "boolean[]":
                    for (boolean v : (boolean[]) obj) {
                        hasher.putLong(checksum(v, hashFunc));
                    }
                    break;
                case "char[]":
                    for (char v : (char[]) obj) {
                        hasher.putLong(checksum(v, hashFunc));
                    }
                    break;
                default:
                    throw new IllegalStateException("This should not happen!");
            }
            return hasher.hash().padToLong();
        }
        if (obj instanceof Object[]) {
            final Hasher hasher = hashFunc.newHasher();
            for (Object item : (Object[]) obj) {
                hasher.putLong(checksum(item, hashFunc));
            }
            return hasher.hash().padToLong();
        }
        if (obj instanceof List<?>) {
            final Hasher hasher = hashFunc.newHasher();
            for (Object item : (List<?>) obj) {
                hasher.putLong(checksum(item, hashFunc));
            }
            return hasher.hash().padToLong();
        }
        if (obj instanceof Map<?, ?>) {
            final List<HashCode> hashCodes = new ArrayList<>();
            for (Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                hashCodes.add(hashFunc.newHasher().putLong(checksum(entry.getKey(), hashFunc))
                        .putLong(checksum(entry.getValue(), hashFunc)).hash());
            }
            return hashCodes.size() > 0 ? Hashing.combineUnordered(hashCodes).padToLong()
                    : hashFunc.newHasher().hash().padToLong();
        }
        if (obj instanceof Collection<?>) {
            final List<HashCode> hashCodes = new ArrayList<>();
            for (Object item : (Collection<?>) obj) {
                hashCodes.add(hashFunc.newHasher().putLong(checksum(item, hashFunc)).hash());
            }
            return hashCodes.size() > 0 ? Hashing.combineUnordered(hashCodes).padToLong()
                    : hashFunc.newHasher().hash().padToLong();
        }
        return obj.hashCode();
    }

    /*---------- Hashing methods ----------*/

    /**
     * Calculate hash value of an object using a fast
     * non-cryptographic-strength hash function.
     *
     * @param object
     * @return
     */
    public static long fastHashValue(Object object) {
        if (object == null) {
            return 0;
        }
        HashFunction hashFunction = fastHashFunc;
        if (object instanceof Boolean || object instanceof Number || object instanceof String) {
            return hashFunction.hashString(object.toString(), StandardCharsets.UTF_8).asLong();
        }
        return hashFunction.hashInt(object.hashCode()).asLong();
    }

    /**
     * Maps an object to a bucket, using linear hash method.
     * <p>
     * This method uses a fast non-cryptographic-strength hash function to
     * calculate object's hash value.
     *
     * @param object
     * @param numBuckets
     * @return the slot index (0 to numSlots-1)
     */
    public static long linearHashingMap(Object object, int numBuckets) {
        if (numBuckets < 1) {
            String msg = "Number of slots must be equal or larger than 1!";
            throw new IllegalArgumentException(msg);
        }
        if (numBuckets == 1 || object == null) {
            return 0;
        }
        return Math.abs(fastHashValue(object) % (long) numBuckets);
    }

    /**
     * Maps an object to a bucket, using consistent hash method.
     * <p>
     * This method uses a fast non-cryptographic-strength hash function to
     * calculate object's hash value.
     *
     * @param object
     * @param numBucket
     * @return the slot index (0 to numSlots-1)
     */
    public static long consistentHashingMap(Object object, int numBucket) {
        if (numBucket < 1) {
            String msg = "Number of slots must be equal or larger than 1!";
            throw new IllegalArgumentException(msg);
        }
        if (numBucket == 1 || object == null) {
            return 0;
        }
        long hashValue = fastHashValue(object);
        return Hashing.consistentHash(hashValue, numBucket);
    }

    /**
     * Calculate Murmur3-hash value (32-bit) of a string.
     *
     * @param value
     * @return
     * @since 0.9.1.1
     */
    public static String murmur332bit(String value) {
        if (value == null) {
            return null;
        }
        return murmur332bit.hashString(value, StandardCharsets.UTF_8).toString().toLowerCase();
    }

    /**
     * Calculate Murmur3-hash value (32-bit) of a byte array.
     *
     * @param value
     * @return
     * @since 0.9.1.1
     */
    public static String murmur332bit(byte[] value) {
        if (value == null) {
            return null;
        }
        return murmur332bit.hashBytes(value).toString().toLowerCase();
    }

    /**
     * Calculate Murmur3-hash value (128-bit) of a string.
     *
     * @param value
     * @return
     * @since 0.6.3
     */
    public static String murmur3(String value) {
        if (value == null) {
            return null;
        }
        return murmur3.hashString(value, StandardCharsets.UTF_8).toString().toLowerCase();
    }

    /**
     * Calculate Murmur3-hash value (128-bit) of a byte array.
     *
     * @param value
     * @return
     * @since 0.6.3
     */
    public static String murmur3(byte[] value) {
        if (value == null) {
            return null;
        }
        return murmur3.hashBytes(value).toString().toLowerCase();
    }

    /**
     * Calculate CRC32-hash value of a string.
     *
     * @param value
     * @return
     */
    public static String crc32(String value) {
        if (value == null) {
            return null;
        }
        return crc32.hashString(value, StandardCharsets.UTF_8).toString().toLowerCase();
    }

    /**
     * Calculate CRC32-hash value of a byte array.
     *
     * @param value
     * @return
     */
    public static String crc32(byte[] value) {
        if (value == null) {
            return null;
        }
        return crc32.hashBytes(value).toString().toLowerCase();
    }

    /**
     * Calculate MD5-hash value of a string.
     *
     * @param value
     * @return
     */
    public static String md5(String value) {
        if (value == null) {
            return null;
        }
        return md5.hashString(value, StandardCharsets.UTF_8).toString().toLowerCase();
    }

    /**
     * Calculate MD5-hash value of a byte array.
     *
     * @param value
     * @return
     */
    public static String md5(byte[] value) {
        if (value == null) {
            return null;
        }
        return md5.hashBytes(value).toString().toLowerCase();
    }

    /**
     * Calculate SHA1-hash value of a string.
     *
     * @param value
     * @return
     */
    public static String sha1(String value) {
        if (value == null) {
            return null;
        }
        return sha1.hashString(value, StandardCharsets.UTF_8).toString().toLowerCase();
    }

    /**
     * Calculate SHA1-hash value of a byte array.
     *
     * @param value
     * @return
     */
    public static String sha1(byte[] value) {
        if (value == null) {
            return null;
        }
        return sha1.hashBytes(value).toString().toLowerCase();
    }

    /**
     * Calculate SHA256-hash value of a string.
     *
     * @param value
     * @return
     */
    public static String sha256(String value) {
        if (value == null) {
            return null;
        }
        return sha256.hashString(value, StandardCharsets.UTF_8).toString().toLowerCase();
    }

    /**
     * Calculate SHA256-hash value of a byte array.
     *
     * @param value
     * @return
     */
    public static String sha256(byte[] value) {
        if (value == null) {
            return null;
        }
        return sha256.hashBytes(value).toString().toLowerCase();
    }

    /**
     * Calculate SHA512-hash value of a string.
     *
     * @param value
     * @return
     */
    public static String sha512(String value) {
        if (value == null) {
            return null;
        }
        return sha512.hashString(value, StandardCharsets.UTF_8).toString().toLowerCase();
    }

    /**
     * Calculate SHA512-hash value of a byte array.
     *
     * @param value
     * @return
     */
    public static String sha512(byte[] value) {
        if (value == null) {
            return null;
        }
        return sha512.hashBytes(value).toString().toLowerCase();
    }
}
