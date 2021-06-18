package org.rain.vertx.app.base.jackson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.POJONode;
import org.rain.vertx.app.base.date.DateFormatUtils;

/**
 * Utility class to convert values.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.6.1
 */
public class ValueUtils {

    /**
     * Convert a target object to a specified number type.
     *
     * <p>
     * Nullable:
     * <ul>
     * <li>If {@code clazz} is a concrete numeric class (e.g. {@link Integer} or {@code int}, this
     * method will not return {@code null}. If {@code target} can not be converted to {@code clazz},
     * {@code zero} will be return.</li>
     * <li>If {@code clazz} is {@link Number}, {@code null} will be returned if {@code Target} can
     * not be converted to {@link Number}.</li>
     * </ul>
     * </p>
     *
     * @param target
     * @param clazz  one of {@link Number}, {@link Byte}, {@link byte},
     *               {@link Short}, {@link short}, {@link Integer}, {@link int},
     *               {@link Long}, {@link long}, {@link Float}, {@link float},
     *               {@link Double}, {@link double}, {@link BigInteger} or
     *               {@link BigDecimal}
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <N> N convertNumber(Object target, Class<N> clazz) throws NumberFormatException {
        if (target instanceof JsonNode) {
            return convertNumber((JsonNode) target, clazz);
        }
        if (clazz == Number.class) {
            return target instanceof Number ? (N) target : null;
        }
        if (clazz == Byte.class || clazz == byte.class) {
            byte value = target instanceof Number ? ((Number) target).byteValue()
                    : target instanceof String ? Byte.parseByte(target.toString()) : 0;
            return (N) Byte.valueOf(value);
        }
        if (clazz == Short.class || clazz == short.class) {
            short value = target instanceof Number ? ((Number) target).shortValue()
                    : target instanceof String ? Short.parseShort(target.toString()) : 0;
            return (N) Short.valueOf(value);
        }
        if (clazz == Integer.class || clazz == int.class) {
            int value = target instanceof Number ? ((Number) target).intValue()
                    : target instanceof String ? Integer.parseInt(target.toString()) : 0;
            return (N) Integer.valueOf(value);
        }
        if (clazz == Long.class || clazz == long.class) {
            long value = target instanceof Number ? ((Number) target).longValue()
                    : target instanceof String ? Long.parseLong(target.toString()) : 0;
            return (N) Long.valueOf(value);
        }
        if (clazz == Float.class || clazz == float.class) {
            float value = target instanceof Number ? ((Number) target).floatValue()
                    : target instanceof String ? Float.parseFloat(target.toString()) : 0;
            return (N) Float.valueOf(value);
        }
        if (clazz == Double.class || clazz == double.class) {
            double value = target instanceof Number ? ((Number) target).doubleValue()
                    : target instanceof String ? Double.parseDouble(target.toString()) : 0;
            return (N) Double.valueOf(value);
        }
        if (clazz == BigInteger.class) {
            BigInteger value = target instanceof BigInteger ? (BigInteger) target
                    : target instanceof Number ? BigInteger.valueOf(((Number) target).longValue())
                    : target instanceof String
                    ? BigInteger.valueOf(Long.parseLong(target.toString()))
                    : BigInteger.ZERO;
            return (N) value;
        }
        if (clazz == BigDecimal.class) {
            BigDecimal value = target instanceof BigDecimal ? (BigDecimal) target
                    : target instanceof Number ? BigDecimal.valueOf(((Number) target).doubleValue())
                    : target instanceof String
                    ? BigDecimal.valueOf(Double.parseDouble(target.toString()))
                    : BigDecimal.ZERO;
            return (N) value;
        }
        return null;
    }

    /**
     * Convert a target object to {@link Boolean}.
     *
     * <p>
     * Nullable: this method does not return {@code null}. If {@code target} can not be converted to
     * {@Link Boolean}, {@code false} is returned.
     * </p>
     *
     * @param target
     * @return
     */
    public static Boolean convertBoolean(Object target) {
        if (target instanceof JsonNode) {
            return convertBoolean((JsonNode) target);
        }
        return target instanceof Boolean ? (Boolean) target
                : target instanceof String ? Boolean.parseBoolean(target.toString())
                : Boolean.FALSE;
    }

    /**
     * Convert a target object to {@link Character}.
     *
     * <p>
     * Nullable: this method does not return {@code null}. If {@code target} can not be converted to
     * {@Link Character}, character {@code #0} is returned.
     * </p>
     *
     * @param target
     * @return
     */
    public static Character convertChar(Object target) {
        if (target instanceof JsonNode) {
            return convertChar((JsonNode) target);
        }
        return target instanceof Character ? (Character) target
                : target instanceof Number ? (char) ((Number) target).intValue()
                : target instanceof String
                ? target.toString().length() > 0 ? target.toString().charAt(0) : 0
                : 0;
    }

    /**
     * Convert a target object to {@link Date}.
     *
     * @param target
     * @return
     */
    public static Date convertDate(Object target) {
        if (target instanceof JsonNode) {
            return convertDate((JsonNode) target);
        }
        DateFormat df = new SimpleDateFormat();
        try {
            return target instanceof Date ? (Date) target
                    : target instanceof Number ? new Date(((Number) target).longValue())
                    : target instanceof String ? df.parse(target.toString()) : null;
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Convert a target object to {@link Date}. If the target object is a string, parse it as a
     * {@link Date} using the specified date-time format.
     *
     * @param target
     * @param dateTimeFormat
     * @return
     * @since 0.6.3.1
     */
    public static Date convertDate(Object target, String dateTimeFormat) {
        if (target instanceof JsonNode) {
            return convertDate((JsonNode) target, dateTimeFormat);
        }
        return target instanceof Number ? new Date(((Number) target).longValue())
                : (target instanceof String
                ? DateFormatUtils.fromString(target.toString(), dateTimeFormat)
                : (target instanceof Date ? (Date) target : null));
    }

    /**
     * Convert a target object to {@link List}.
     *
     * @param target
     * @return
     */
    public static List<?> convertArrayOrList(Object target) {
        if (target instanceof JsonNode) {
            return convertArrayOrList((JsonNode) target);
        }
        return target instanceof Object[] ? Arrays.asList((Object[]) target)
                : target instanceof Collection ? new ArrayList<Object>((Collection<?>) target)
                : null;
    }

    /**
     * Convert a target object to a specified value type.
     *
     * @param target
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertValue(Object target, Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("Class parameter is null!");
        }
        if (target == null) {
            return null;
        }
        if (target instanceof JsonNode) {
            return convertValue((JsonNode) target, clazz);
        }
        if (Number.class.isAssignableFrom(clazz) || byte.class == clazz || short.class == clazz
                || int.class == clazz || long.class == clazz || float.class == clazz
                || double.class == clazz) {
            return convertNumber(target, clazz);
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) convertBoolean(target);
        }
        if (clazz == Character.class || clazz == char.class) {
            return (T) convertChar(target);
        }
        if (Date.class.isAssignableFrom(clazz)) {
            return (T) convertDate(target);
        }
        if (Object[].class.isAssignableFrom(clazz) || List.class.isAssignableFrom(clazz)) {
            return (T) convertArrayOrList(target);
        }
        if (clazz.isAssignableFrom(target.getClass())) {
            return (T) target;
        }
        if (clazz == String.class) {
            return (T) target.toString();
        }
        throw new IllegalArgumentException(
                "Cannot convert an object of type [" + target.getClass() + "] to [" + clazz + "]!");
    }

    /*----------------------------------------------------------------------*/

    /**
     * Convert a {@link JsonNode} to a specified number type.
     *
     * <p>
     * This method will not return {@code null}. If {@code node} can not be converted to
     * {@code clazz},
     * {@code zero} will be return.
     * </p>
     *
     * @param node
     * @param clazz one of {@link Number}, {@link Byte}, {@link byte},
     *              {@link Short}, {@link short}, {@link Integer}, {@link int},
     *              {@link Long}, {@link long}, {@link Float}, {@link float},
     *              {@link Double}, {@link double}, {@link BigInteger} or
     *              {@link BigDecimal}
     * @return
     * @since 0.6.2
     */
    @SuppressWarnings("unchecked")
    public static <N> N convertNumber(JsonNode node, Class<N> clazz) {
        if (node instanceof POJONode) {
            return convertNumber(DPathUtils.extractValue((POJONode) node), clazz);
        }
        if (clazz == Byte.class || clazz == byte.class) {
            byte value = node.isNumber() ? (byte) node.asInt()
                    : node.isTextual() ? Byte.parseByte(node.asText()) : 0;
            return (N) Byte.valueOf(value);
        }
        if (clazz == Short.class || clazz == short.class) {
            short value = node.isNumber() ? (short) node.asInt()
                    : node.isTextual() ? Short.parseShort(node.asText()) : 0;
            return (N) Short.valueOf(value);
        }
        if (clazz == Integer.class || clazz == int.class) {
            int value = node.isNumber() ? node.asInt()
                    : node.isTextual() ? Integer.parseInt(node.asText()) : 0;
            return (N) Integer.valueOf(value);
        }
        if (clazz == Long.class || clazz == long.class) {
            long value = node.isNumber() ? node.asLong()
                    : node.isTextual() ? Long.parseLong(node.asText()) : 0;
            return (N) Long.valueOf(value);
        }
        if (clazz == Float.class || clazz == float.class) {
            float value = node.isNumber() ? (float) node.asDouble()
                    : node.isTextual() ? Float.parseFloat(node.asText()) : 0;
            return (N) Float.valueOf(value);
        }
        if (clazz == Double.class || clazz == double.class) {
            double value = node.isNumber() ? node.asDouble()
                    : node.isTextual() ? Double.parseDouble(node.asText()) : 0;
            return (N) Double.valueOf(value);
        }
        if (clazz == BigInteger.class) {
            BigInteger value = node instanceof NumericNode ? ((NumericNode) node).bigIntegerValue()
                    : BigInteger.valueOf(node.isTextual() ? Long.parseLong(node.asText()) : 0);
            return (N) value;
        }
        if (clazz == BigDecimal.class) {
            BigDecimal value = node instanceof NumericNode ? ((NumericNode) node).decimalValue()
                    : BigDecimal.valueOf(node.isTextual() ? Double.parseDouble(node.asText()) : 0);
            return (N) value;
        }
        if (clazz == Number.class && node.isNumber()) {
            if (node.isIntegralNumber()) {
                return (N) Long.valueOf(node.asLong());
            }
            if (node.isFloatingPointNumber()) {
                return (N) Double.valueOf(node.asDouble());
            }
        }
        return null;
    }

    /**
     * Convert a {@link JsonNode} to {@link Boolean}.
     *
     * <p>
     * Nullable: this method does not return {@code null}. If {@code target} can not be converted to
     * {@Link Boolean}, {@code false} is returned.
     * </p>
     *
     * @param node
     * @return
     * @since 0.6.2
     */
    public static Boolean convertBoolean(JsonNode node) {
        if (node instanceof POJONode) {
            return convertBoolean(DPathUtils.extractValue((POJONode) node));
        }
        boolean value = node.isBoolean() ? node.asBoolean()
                : node.isTextual() ? Boolean.parseBoolean(node.asText()) : false;
        return Boolean.valueOf(value);
    }

    /**
     * Convert a {@link JsonNode} to {@link Character}.
     *
     * <p>
     * Nullable: this method does not return {@code null}. If {@code target} can not be converted to
     * {@Link Character}, character {@code #0} is returned.
     * </p>
     *
     * @param node
     * @return
     * @since 0.6.2
     */
    public static Character convertChar(JsonNode node) {
        if (node instanceof POJONode) {
            return convertChar(DPathUtils.extractValue((POJONode) node));
        }
        return node.isNumber() ? (char) node.asInt()
                : node.isTextual() ? (node.asText().length() > 0 ? node.asText().charAt(0) : 0) : 0;
    }

    /**
     * Convert a {@link JsonNode} to {@link Date}.
     *
     * @param node
     * @return
     */
    public static Date convertDate(JsonNode node) {
        if (node instanceof POJONode) {
            return convertDate(DPathUtils.extractValue((POJONode) node));
        }
        DateFormat df = new SimpleDateFormat();
        try {
            return node.isNumber() ? new Date(node.asLong())
                    : node.isTextual() ? df.parse(node.asText()) : null;
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Convert a {@link JsonNode} to {@link Date}. If the target node is a string, parse it as a
     * {@link Date} using the specified date-time format.
     *
     * @param node
     * @return
     * @since 0.6.3.1
     */
    public static Date convertDate(JsonNode node, String dateTimeFormat) {
        if (node instanceof POJONode) {
            return convertDate(DPathUtils.extractValue((POJONode) node), dateTimeFormat);
        }
        return node.isNumber() ? new Date(node.asLong())
                : node.isTextual() ? DateFormatUtils.fromString(node.asText(), dateTimeFormat)
                : null;
    }

    /**
     * Convert a {@link JsonNode} to {@link List&lt;JsonNode&gt;}.
     *
     * @param node
     * @return
     */
    public static List<?> convertArrayOrList(JsonNode node) {
        if (node instanceof POJONode) {
            return convertArrayOrList(DPathUtils.extractValue((POJONode) node));
        }
        if (node instanceof ArrayNode) {
            List<JsonNode> result = new ArrayList<>();
            ArrayNode arrNode = (ArrayNode) node;
            for (JsonNode jNode : arrNode) {
                result.add(jNode);
            }
            return result;
        }
        return null;
    }

    /**
     * Convert a {@link JsonNode} to a specified value type.
     *
     * @param node
     * @param clazz
     * @return
     * @since 0.6.2
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertValue(JsonNode node, Class<T> clazz) {
        if (node instanceof POJONode) {
            return convertValue(DPathUtils.extractValue((POJONode) node), clazz);
        }
        if (clazz == null) {
            throw new NullPointerException("Class parameter is null!");
        }
        if (node == null || node instanceof NullNode || node instanceof MissingNode) {
            return null;
        }
        if (node instanceof POJONode) {
            return convertValue(DPathUtils.extractValue((POJONode) node), clazz);
        }
        if (Number.class.isAssignableFrom(clazz) || byte.class == clazz || short.class == clazz
                || int.class == clazz || long.class == clazz || float.class == clazz
                || double.class == clazz) {
            return convertNumber(node, clazz);
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) convertBoolean(node);
        }
        if (clazz == Character.class || clazz == char.class) {
            return (T) convertChar(node);
        }
        if (Date.class.isAssignableFrom(clazz)) {
            return (T) convertDate(node);
        }
        if (Object[].class.isAssignableFrom(clazz) || List.class.isAssignableFrom(clazz)) {
            return (T) convertArrayOrList(node);
        }
        if (clazz.isAssignableFrom(node.getClass())) {
            return (T) node;
        }
        if (clazz == String.class) {
            return (T) (node.isTextual() ? node.asText() : node.toString());
        }
        throw new IllegalArgumentException(
                "Cannot convert an object of type [" + node.getClass() + "] to [" + clazz + "]!");
    }

}
