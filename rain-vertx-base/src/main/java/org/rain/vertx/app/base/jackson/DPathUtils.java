package org.rain.vertx.app.base.jackson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;

/**
 * Utility to access data from a hierarchy structure.
 *
 * <p>
 * Notation:
 * </p>
 *
 * <ul>
 * <li><code>.</code> (the dot character): path separator</li>
 * <li><code>name</code>: access a map's attribute specified by
 * <code>name</code></li>
 * <li><code>[i]</code>: access i'th element of a list/array (0-based)</li>
 * </ul>
 * Example: <code>employees.[1].first_name</code>. The dot right before {@code []} can be omitted:
 * <code>employees[1].first_name</code>.
 *
 * <p>
 * Sample usage: assuming you have the following data structure:
 * </p>
 *
 * <pre>
 * Map&lt;String, Object&gt; company = new HashMap&lt;String, Object&gt;();
 * company.put(&quot;name&quot;, &quot;Monster Corp.&quot;);
 * company.put(&quot;year&quot;, 2003);
 *
 * List&lt;Map&lt;String, Object&gt;&gt; employees = new ArrayList&lt;Map&lt;String, Object&gt;&gt;();
 * company.put(&quot;employees&quot;, employees);
 *
 * Map&lt;String, Object&gt; employee1 = new HashMap&lt;String, Object&gt;();
 * employee1.put(&quot;first_name&quot;, &quot;Mike&quot;);
 * employee1.put(&quot;last_name&quot;, &quot;Wazowski&quot;);
 * employee1.put(&quot;email&quot;, &quot;mike.wazowski@monster.com&quot;);
 * employee1.put(&quot;age&quot;, 29);
 * employees.add(employee1);
 *
 * Map&lt;String, Object&gt; employee2 = new HashMap&lt;String, Object&gt;();
 * employee2.put(&quot;first_name&quot;, &quot;Sulley&quot;);
 * employee2.put(&quot;last_name&quot;, &quot;Sullivan&quot;);
 * employee2.put(&quot;email&quot;, &quot;sulley.sullivan@monster.com&quot;);
 * employee2.put(&quot;age&quot;, 30);
 * employees.add(employee2);
 * </pre>
 *
 * <p>
 * You can access company's attributes:
 * </p>
 *
 * <pre>
 * String companyName = DPathUtils.getValue(company, &quot;name&quot;, String.class);
 * // got string &quot;Monster Corp.&quot;
 *
 * Integer companyYear = DPathUtils.getValue(company, &quot;year&quot;, Integer.class);
 * // got integer 2003
 * </pre>
 *
 * <p>
 * You can access two employees:
 * </p>
 *
 * <pre>
 * Object user1 = DPathUtils.getValue(company, &quot;employees.[0]&quot;);
 * Map&lt;String, Object&gt; user2 = DPathUtils.getValue(company, &quot;employees.[1]&quot;, Map.class);
 * </pre>
 *
 * <p>
 * Or, employee's attributes:
 * </p>
 *
 * <pre>
 * String firstName1 = DPathUtils.getValue(company, &quot;employees.[0].first_name&quot;, String.class);
 * Object email2 = DPathUtils.getValue(company, &quot;employees.[1].email&quot;);
 * Long age2 = DPathUtils.getValue(company, &quot;employees.[1].age&quot;, Long.class);
 * </pre>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class DPathUtils {

    private final static Pattern PATTERN_INDEX = Pattern.compile("^\\[(.*?)\\]$");
    private final static Pattern PATTERN_END_INDEX = Pattern.compile("^(.*)(\\[.*?\\])$");
    public final static char PATH_SEPARATOR = '.';

    /**
     * Split {@code DPath} string to tokens.
     *
     * <ul>
     * <li>{@code "a.b.c.[i].d"} or {@code "a.b.c[i].d"} will be split into
     * {@code ["a","b","c","[i]","d"]}</li>
     * <li>{@code "a.b.c.[i].[j].d"} or {@code "a.b.c[i].[j].d"} or
     * {@code "a.b.c[i][j].d"} or {@code "a.b.c.[i][j].d"} will be split into
     * {@code ["a","b","c","[i]","[j]","d"]}</li>
     * </ul>
     *
     * @param dpath
     * @return
     * @since 0.6.1
     */
    @SuppressWarnings("serial")
    public final static String[] splitDpath(String dpath) {
        String[] tokens = StringUtils.split(dpath, PATH_SEPARATOR);
        List<String> tokenList = new ArrayList<String>() {
            {
                for (String token : tokens) {
                    List<String> temp = new ArrayList<String>() {
                        {
                            String _token = token;
                            Matcher m = PATTERN_END_INDEX.matcher(_token);
                            while (m.matches()) {
                                _token = m.group(1);
                                add(0, m.group(2));
                                m = PATTERN_END_INDEX.matcher(_token);
                            }
                            if (!StringUtils.isBlank(_token)) {
                                add(0, _token);
                            }
                        }
                    };
                    addAll(temp);
                }
            }
        };
        return tokenList.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    static Object extractValue(POJONode pojoNode) {
        Object value = pojoNode;
        while (value instanceof POJONode) {
            value = ((POJONode) value).getPojo();
        }
        return value;
    }

    private static Object extractValue(Object target, String index) {
        if (target == null) {
            return null;
        }
        if (target instanceof JsonNode) {
            return extractValue((JsonNode) target, index);
        }
        Matcher m = PATTERN_INDEX.matcher(index);
        if (m.matches()) {
            try {
                int i = Integer.parseInt(m.group(1));
                if (target instanceof Object[]) {
                    return ((Object[]) target)[i];
                }
                if (target instanceof List<?>) {
                    return ((List<?>) target).get(i);
                }
                throw new IllegalArgumentException("Expect an array or list for index [" + index
                        + "] but received [" + target.getClass() + "] instead.");
            } catch (IndexOutOfBoundsException e) {
                // rethrow for now
                throw e;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid index value: " + index, e);
            }
        }
        if (target instanceof Map<?, ?>) {
            return ((Map<?, ?>) target).get(index);
        }
        throw new IllegalArgumentException(
                "Unsupported type [" + target.getClass() + "] or invalid index [" + index + "]");
    }

    private static Object extractValue(JsonNode node, String index) {
        if (node == null || node instanceof NullNode || node instanceof MissingNode) {
            return null;
        }
        if (node instanceof POJONode) {
            return extractValue(extractValue((POJONode) node), index);
        }
        Matcher m = PATTERN_INDEX.matcher(index);
        if (m.matches()) {
            try {
                int i = Integer.parseInt(m.group(1));
                if (node instanceof ArrayNode) {
                    if (i >= 0 && i < node.size()) {
                        return node.get(i);
                    } else {
                        throw new IndexOutOfBoundsException(String.valueOf(i));
                    }
                }
                throw new IllegalArgumentException("Expect an ArrayNode for index [" + index
                        + "] but received [" + node.getClass() + "] instead.");
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid index value: " + index, e);
            }
        }
        if (node instanceof ObjectNode) {
            return node.get(index);
        }
        throw new IllegalArgumentException(
                "Unsupported type [" + node.getClass() + "] or invalid index [" + index + "]");
    }

    /**
     * @param dPath
     * @param cursor
     * @param index
     * @since 0.9.2
     */
    @SuppressWarnings("unchecked")
    private static void deleteFieldValue(String dPath, Object cursor, String index) {
        if (cursor instanceof POJONode) {
            cursor = extractValue((POJONode) cursor);
        }
        if (cursor instanceof Map<?, ?>) {
            Map<Object, Object> temp = (Map<Object, Object>) cursor;
            temp.remove(index);
        } else if (cursor instanceof ObjectNode) {
            ObjectNode temp = (ObjectNode) cursor;
            temp.remove(index);
        } else {
            throw new IllegalArgumentException("Target object of type [" + cursor.getClass()
                    + "] is not writable with path [" + dPath + "]!");
        }
    }

    /**
     * @param dPath
     * @param cursor
     * @param index
     * @since 0.9.2
     */
    @SuppressWarnings("unchecked")
    private static void deleteFieldValue(String dPath, Object cursor, int index) {
        if (cursor instanceof POJONode) {
            cursor = extractValue((POJONode) cursor);
        }
        if (cursor instanceof List<?>) {
            List<Object> temp = (List<Object>) cursor;
            if (index >= 0 && index < temp.size()) {
                temp.remove(index);
            } else {
                throw new IllegalArgumentException("Error: Index out of bound. Path [" + dPath
                        + "], target [" + cursor.getClass() + "].");
            }
        } else if (cursor instanceof ArrayNode) {
            ArrayNode temp = (ArrayNode) cursor;
            if (index >= 0 && index < temp.size()) {
                temp.remove(index);
            } else {
                throw new IllegalArgumentException("Error: Index out of bound. Path [" + dPath
                        + "], target [" + cursor.getClass() + "].");
            }
        } else {
            throw new IllegalArgumentException(
                    "Target object [" + cursor.getClass() + "] is not an ArrayNode or List!");
        }
    }

    /**
     * @param dPath
     * @param node
     * @param index
     * @param value
     * @since 0.9.2
     */
    @SuppressWarnings("unchecked")
    private static void setFieldValue(String dPath, Object cursor, String index, Object value) {
        if (cursor instanceof POJONode) {
            cursor = extractValue((POJONode) cursor);
        }
        if (cursor instanceof Map<?, ?>) {
            Map<Object, Object> temp = (Map<Object, Object>) cursor;
            if (value == null) {
                temp.remove(index);
            } else {
                temp.put(index, value);
            }
        } else if (cursor instanceof ObjectNode) {
            ObjectNode temp = (ObjectNode) cursor;
            if (value == null) {
                temp.remove(index);
            } else {
                temp.set(index,
                        value instanceof JsonNode ? (JsonNode) value : JacksonUtils.toJson(value));
            }
        } else {
            throw new IllegalArgumentException("Target object of type [" + cursor.getClass()
                    + "] is not writable with path [" + dPath + "]!");
        }
    }

    /**
     * @param dPath
     * @param cursor
     * @param index
     * @param value
     * @since 0.9.2
     */
    @SuppressWarnings("unchecked")
    private static void setFieldValue(String dPath, Object cursor, int index, Object value) {
        if (cursor instanceof POJONode) {
            cursor = extractValue((POJONode) cursor);
        }
        if (cursor instanceof List<?>) {
            List<Object> temp = (List<Object>) cursor;
            if (index >= temp.size()) {
                temp.add(value);
            } else if (index >= 0) {
                temp.set(index, value);
            } else {
                throw new IllegalArgumentException("Error: Index out of bound. Path [" + dPath
                        + "], target [" + cursor.getClass() + "].");
            }
        } else if (cursor instanceof Object[]) {
            Object[] temp = (Object[]) cursor;
            if (index >= 0 && index < temp.length) {
                temp[index] = value;
            } else {
                throw new IllegalArgumentException("Error: Index out of bound. Path [" + dPath
                        + "], target [" + cursor.getClass() + "].");
            }
        } else if (cursor instanceof ArrayNode) {
            ArrayNode temp = (ArrayNode) cursor;
            if (index >= temp.size() && value != null) {
                temp.add(value instanceof JsonNode ? (JsonNode) value : JacksonUtils.toJson(value));
            } else if (index >= 0) {
                temp.set(index,
                        value instanceof JsonNode ? (JsonNode) value : JacksonUtils.toJson(value));
            } else {
                throw new IllegalArgumentException("Error: Index out of bound. Path [" + dPath
                        + "], target [" + cursor.getClass() + "].");
            }
        } else {
            throw new IllegalArgumentException(
                    "Target object [" + cursor.getClass() + "] is not a List, ArrayNode or array!");
        }
    }
    /*----------------------------------------------------------------------*/

    /**
     * Extract a date value from the target object using DPath expression. If the extracted value is
     * a string, parse it as a {@link Date} using the specified date-time format.
     *
     * @param target
     * @param dPath
     * @param dateTimeFormat see {@link SimpleDateFormat}
     * @return
     * @since 0.6.2
     */
    public static Date getDate(Object target, String dPath, String dateTimeFormat) {
        Object obj = getValue(target, dPath);
        return ValueUtils.convertDate(obj, dateTimeFormat);
    }

    /**
     * Extract a value from the target object using DPath expression (generic
     * version).
     *
     * @param target
     * @param dPath
     * @param clazz
     * @return
     */
    public static <T> T getValue(Object target, String dPath, Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("Class parameter is null!");
        }
        Object temp = getValue(target, dPath);
        return ValueUtils.convertValue(temp, clazz);
    }

    /**
     * Extract a value from the target object using DPath expression (generic
     * version).
     *
     * @param target
     * @param dPath
     * @param clazz
     * @return
     * @since 0.7.1
     */
    public static <T> Optional<T> getValueOptional(Object target, String dPath, Class<T> clazz) {
        return Optional.ofNullable(getValue(target, dPath, clazz));
    }

    /**
     * Extract a value from the target object using DPath expression.
     *
     * @param target
     * @param dPath
     */
    public static Object getValue(Object target, String dPath) {
        if (target instanceof JsonNode) {
            return getValue((JsonNode) target, dPath);
        }
        String[] paths = splitDpath(dPath);
        Object result = target;
        for (String path : paths) {
            result = extractValue(result, path);
        }
        return result instanceof POJONode ? extractValue((POJONode) result) : result;
    }

    /**
     * Extract a value from the target object using DPath expression.
     *
     * @param target
     * @param dPath
     * @return
     * @since 0.7.1
     */
    public static Optional<Object> getValueOptional(Object target, String dPath) {
        return Optional.ofNullable(getValue(target, dPath));
    }

    /*----------------------------------------------------------------------*/

    /**
     * Extract a date value from the target {@link JsonNode} using DPath expression. If the
     * extracted value is a string, parse it as a {@link Date} using the specified date-time format.
     *
     * @param node
     * @param dPath
     * @param dateTimeFormat see {@link SimpleDateFormat}
     * @return
     * @since 0.6.2
     */
    public static Date getDate(JsonNode node, String dPath, String dateTimeFormat) {
        JsonNode obj = getValue(node, dPath);
        return ValueUtils.convertDate(obj, dateTimeFormat);
    }

    /**
     * Extract a value from the target {@link JsonNode} using DPath expression (generic
     * version).
     *
     * @param node
     * @param dPath
     * @param clazz
     * @return
     * @since 0.6.2
     */
    public static <T> T getValue(JsonNode node, String dPath, Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("Class parameter is null!");
        }
        JsonNode temp = getValue(node, dPath);
        return ValueUtils.convertValue(temp, clazz);
    }

    /**
     * Extract a value from the target {@link JsonNode} using DPath expression (generic
     * version).
     *
     * @param node
     * @param dPath
     * @param clazz
     * @return
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
     * @since 0.6.2
     */
    public static JsonNode getValue(JsonNode node, String dPath) {
        String[] paths = splitDpath(dPath);
        Object result = node;
        for (String path : paths) {
            result = extractValue(result, path);
        }
        if (result instanceof POJONode) {
            result = extractValue((POJONode) result);
        }
        return result != null
                ? result instanceof JsonNode ? ((JsonNode) result) : JacksonUtils.toJson(result)
                : null;
    }

    /**
     * Extract a value from the target {@link JsonNode} using DPath expression.
     *
     * @param node
     * @param dPath
     * @return
     * @since 0.7.1
     */
    public static Optional<JsonNode> getValueOptional(JsonNode node, String dPath) {
        return Optional.ofNullable(getValue(node, dPath));
    }

    /*----------------------------------------------------------------------*/

    private static Object createIntermediate(Object target, StringBuffer pathSofar, String index,
                                             String nextIndex) {
        if (target instanceof JsonNode) {
            return createIntermediate((JsonNode) target, pathSofar, index, nextIndex);
        }
        Object value = PATTERN_INDEX.matcher(nextIndex).matches() ? new ArrayList<Object>()
                : new HashMap<String, Object>();
        return createIntermediate(target, pathSofar, index, value);
    }

    @SuppressWarnings("unchecked")
    private static Object createIntermediate(Object target, StringBuffer pathSofar, String index,
                                             Object value) {
        if (target == null) {
            return null;
        }
        if (target instanceof JsonNode) {
            return createIntermediate((JsonNode) target, pathSofar, index, value);
        }
        Matcher m = PATTERN_INDEX.matcher(index);
        if (m.matches()) {
            try {
                int i = Integer.parseInt(m.group(1));
                if (target instanceof List<?>) {
                    List<Object> temp = (List<Object>) target;
                    if (i >= 0 && i < temp.size()) {
                        // the middle item is null (in some cases)
                        temp.set(i, value);
                    } else if (i == temp.size()) {
                        // special case: add the last item
                        temp.add(value);
                    } else {
                        throw new IllegalArgumentException("Error: Index out of bound. Path ["
                                + pathSofar + "], target [" + target.getClass() + "].");
                    }
                    return value;
                } else if (target instanceof Object[]) {
                    Object[] temp = (Object[]) target;
                    if (i >= 0 && i < temp.length) {
                        // the middle item is null (in some cases)
                        temp[i] = value;
                    } else {
                        throw new IllegalArgumentException("Error: Index out of bound. Path ["
                                + pathSofar + "], target [" + target.getClass() + "].");
                    }
                    return value;
                } else {
                    throw new IllegalArgumentException("Expect an array or list for path ["
                            + pathSofar + "] but received [" + target.getClass() + "] instead.");
                }
            } catch (IndexOutOfBoundsException e) {
                // rethrow for now
                throw e;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error: Invalid index. Path [" + pathSofar
                        + "], target [" + target.getClass() + "].", e);
            }
        } else if (target instanceof Map<?, ?>) {
            Map<Object, Object> temp = (Map<Object, Object>) target;
            temp.put(index, value);
            return value;
        } else {
            throw new IllegalArgumentException("Target object of type [" + target.getClass()
                    + "] is not writable with path [" + pathSofar + "]!");
        }
    }

    /**
     * Set a value to the target object specified by DPath expression.
     *
     * <p>
     * Note: intermediated nodes will NOT be created.
     * </p>
     *
     * <p>
     * Note: if {@code value} is {@code null}:
     * <ul>
     * <li>If the specified item's parent is a list or array, the item
     * (specified by {@code dPath}) will be set to {@code null}.</li>
     * <li>If the specified item's parent is a map, the item (specified by
     * {@code dPath}) will be removed.</li>
     * </ul>
     * </p>
     *
     * @param target
     * @param dPath
     * @param value
     */
    public static void setValue(Object target, String dPath, Object value) {
        setValue(target, dPath, value, false);
    }

    /**
     * Set a value to the target object specified by DPath expression.
     *
     * <p>
     * Note: intermediated nodes will be created if
     * {@code createIntermediatePath} is true.
     * </p>
     *
     * <p>
     * Note: if {@code value} is {@code null}:
     * <ul>
     * <li>If the specified item's parent is a list or array, the item
     * (specified by {@code dPath}) will be set to {@code null}.</li>
     * <li>If the specified item's parent is a map, the item (specified by
     * {@code dPath}) will be removed.</li>
     * </ul>
     * </p>
     *
     * @param target
     * @param dPath
     * @param value
     * @param createIntermediatePath
     * @since 0.6.1
     */
    public static void setValue(Object target, String dPath, Object value,
                                boolean createIntermediatePath) {
        if (target == null) {
            throw new IllegalArgumentException("Target is null!");
        }
        if (target instanceof JsonNode) {
            setValue((JsonNode) target, dPath, value, createIntermediatePath);
            return;
        }
        String[] paths = splitDpath(dPath);
        Object cursor = target, prevCursor = target;
        StringBuffer pathSofar = new StringBuffer();
        // "seek"to the correct position
        for (int i = 0; i < paths.length - 1; i++) {
            if (i > 0) {
                pathSofar.append(PATH_SEPARATOR);
            }
            String index = paths[i], nextIndex = paths[i + 1];
            pathSofar.append(index);
            try {
                cursor = extractValue(cursor, index);
            } catch (IndexOutOfBoundsException e) {
                cursor = null;
            }
            if (cursor == null && createIntermediatePath) {
                // creating intermediate value
                cursor = createIntermediate(prevCursor, pathSofar, index, nextIndex);
            }
            prevCursor = cursor;
        }
        if (cursor == null) {
            throw new IllegalArgumentException("Path not found [" + dPath + "]!");
        }

        String index = paths[paths.length - 1];
        Matcher m = PATTERN_INDEX.matcher(index);
        if (m.matches() || "[]".equals(index)) {
            try {
                int i = "[]".equals(index) ? Integer.MAX_VALUE : Integer.parseInt(m.group(1));
                setFieldValue(dPath, cursor, i, value);
            } catch (IndexOutOfBoundsException e) {
                // rethrow for now
                throw e;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error: Invalid index. Path [" + dPath
                        + "], target [" + cursor.getClass() + "].", e);
            }
        } else {
            setFieldValue(dPath, cursor, index, value);
        }
    }

    /**
     * Delete a value from the target object specified by DPath expression.
     *
     * <ul>
     * <li>If the specified item's parent is a list or map, the item (specified
     * by {@code dPath}) will be removed (for list, next items will be shifted up one
     * position).</li>
     * <li>If the specified item's parent is an array, the item (specified by
     * {@code dPath}) will be set to {@code null}.</li>
     * </ul>
     *
     * @param target
     * @param dPath
     * @since 0.6.1
     */
    public static void deleteValue(Object target, String dPath) {
        if (target instanceof JsonNode) {
            deleteValue((JsonNode) target, dPath);
            return;
        }
        String[] paths = splitDpath(dPath);
        Object cursor = target;
        // "seek"to the correct position
        for (int i = 0; i < paths.length - 1; i++) {
            cursor = extractValue(cursor, paths[i]);
        }
        if (cursor == null) {
            return;
        }

        String index = paths[paths.length - 1];
        Matcher m = PATTERN_INDEX.matcher(index);
        if (m.matches()) {
            try {
                int i = Integer.parseInt(m.group(1));
                deleteFieldValue(dPath, cursor, i);
            } catch (IndexOutOfBoundsException e) {
                // rethrow for now
                throw e;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error: Invalid index. Path [" + dPath
                        + "], target [" + cursor.getClass() + "].", e);
            }
        } else {
            deleteFieldValue(dPath, cursor, index);
        }
    }

    /*----------------------------------------------------------------------*/

    @SuppressWarnings("rawtypes")
    private static Object createIntermediate(JsonNode node, StringBuffer pathSofar, String index,
                                             String nextIndex) {
        if (node instanceof POJONode) {
            return createIntermediate(extractValue((POJONode) node), pathSofar, index, nextIndex);
        }
        if (node instanceof ContainerNode) {
            ContainerNode temp = (ContainerNode) node;
            JsonNode value = PATTERN_INDEX.matcher(nextIndex).matches() ? temp.arrayNode()
                    : temp.objectNode();
            return createIntermediate(temp, pathSofar, index, value);
        }
        return null;
    }

    private static Object createIntermediate(JsonNode node, StringBuffer pathSofar, String index,
                                             Object value) {
        if (node == null) {
            return null;
        }
        if (node instanceof POJONode) {
            return createIntermediate(extractValue((POJONode) node), pathSofar, index, value);
        }
        Matcher m = PATTERN_INDEX.matcher(index);
        if (m.matches()) {
            try {
                int i = Integer.parseInt(m.group(1));
                if (node instanceof ArrayNode) {
                    ArrayNode temp = (ArrayNode) node;
                    if (i >= 0 && i < temp.size()) {
                        // the middle item is null (in some cases)
                        temp.set(i, value instanceof JsonNode ? (JsonNode) value
                                : JacksonUtils.toJson(value));
                    } else if (i == temp.size()) {
                        // special case: add the last item
                        if (value instanceof JsonNode) {
                            temp.add((JsonNode) value);
                        } else {
                            temp.addPOJO(value);
                        }
                    } else {
                        throw new IllegalArgumentException("Error: Index out of bound. Path ["
                                + pathSofar + "], target [" + node.getClass() + "].");
                    }
                    return value;
                } else {
                    throw new IllegalArgumentException("Expect an ArrayNode for path [" + pathSofar
                            + "] but received [" + node.getClass() + "] instead.");
                }
            } catch (IndexOutOfBoundsException e) {
                // rethrow for now
                throw e;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error: Invalid index. Path [" + pathSofar
                        + "], target [" + node.getClass() + "].", e);
            }
        } else if (node instanceof ObjectNode) {
            ObjectNode temp = (ObjectNode) node;
            if (value instanceof JsonNode) {
                temp.set(index, (JsonNode) value);
            } else {
                temp.putPOJO(index, value);
            }
            return value;
        } else {
            throw new IllegalArgumentException("Target object of type [" + node.getClass()
                    + "] is not writable with path [" + pathSofar + "]!");
        }
    }

    /**
     * Set a value to the target {@link JsonNode} specified by DPath expression.
     *
     * <p>
     * Note: intermediated nodes will NOT be created.
     * </p>
     *
     * <p>
     * Note: if {@code value} is {@code null}:
     * <ul>
     * <li>If the specified item's parent is an {@link ArrayNode}, the item
     * (specified by {@code dPath}) will be set to {@code null}.</li>
     * <li>If the specified item's parent is an {@link ObjectNode}, the item (specified by
     * {@code dPath}) will be removed.</li>
     * </ul>
     * </p>
     *
     * @param node
     * @param dPath
     * @param value
     * @since 0.6.2
     */
    public static void setValue(JsonNode node, String dPath, Object value) {
        setValue(node, dPath, value, false);
    }

    /**
     * Set a value to the target {@link JsonNode} specified by DPath expression.
     *
     * <p>
     * Note: intermediated nodes will be created if {@code createIntermediatePath} is true.
     * </p>
     *
     * <p>
     * Note: if {@code value} is {@code null}:
     * <ul>
     * <li>If the specified item's parent is an {@link ArrayNode}, the item
     * (specified by {@code dPath}) will be set to {@code null}.</li>
     * <li>If the specified item's parent is an {@link ObjectNode}, the item (specified by
     * {@code dPath}) will be removed.</li>
     * </ul>
     * </p>
     *
     * @param node
     * @param dPath
     * @param value
     * @param createIntermediatePath
     * @since 0.6.2
     */
    public static void setValue(JsonNode node, String dPath, Object value,
                                boolean createIntermediatePath) {
        if (node == null) {
            throw new IllegalArgumentException("Target is null!");
        }
        String[] paths = splitDpath(dPath);
        Object cursor = node, prevCursor = node;
        StringBuffer pathSofar = new StringBuffer();
        // "seek"to the correct position
        for (int i = 0; i < paths.length - 1; i++) {
            if (i > 0) {
                pathSofar.append(PATH_SEPARATOR);
            }
            String index = paths[i], nextIndex = paths[i + 1];
            pathSofar.append(index);
            try {
                cursor = extractValue(cursor, index);
            } catch (IndexOutOfBoundsException e) {
                cursor = null;
            }
            if ((cursor == null || cursor instanceof NullNode || cursor instanceof MissingNode)
                    && createIntermediatePath) {
                // creating intermediate value
                cursor = createIntermediate(prevCursor, pathSofar, index, nextIndex);
            }
            prevCursor = cursor;
        }
        if (cursor == null || cursor instanceof NullNode || cursor instanceof MissingNode) {
            throw new IllegalArgumentException("Path not found [" + dPath + "]!");
        }

        JsonNode valueNode = value instanceof JsonNode ? (JsonNode) value
                : JacksonUtils.toJson(value);
        String index = paths[paths.length - 1];
        Matcher m = PATTERN_INDEX.matcher(index);
        if (m.matches() || "[]".equals(index)) {
            try {
                int i = "[]".equals(index) ? Integer.MAX_VALUE : Integer.parseInt(m.group(1));
                setFieldValue(dPath, cursor, i, valueNode);
            } catch (IndexOutOfBoundsException e) {
                // rethrow for now
                throw e;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error: Invalid index. Path [" + dPath
                        + "], target [" + cursor.getClass() + "].", e);
            }
        } else {
            setFieldValue(dPath, cursor, index, valueNode);
        }
    }

    /**
     * Delete a value from the target {@link JsonNode} specified by DPath expression.
     *
     * @param node
     * @param dPath
     * @since 0.6.2
     */
    public static void deleteValue(JsonNode node, String dPath) {
        String[] paths = splitDpath(dPath);
        Object cursor = node;
        // "seek"to the correct position
        for (int i = 0; i < paths.length - 1; i++) {
            cursor = extractValue(cursor, paths[i]);
        }
        if (cursor == null || cursor instanceof NullNode || cursor instanceof MissingNode) {
            return;
        }

        String index = paths[paths.length - 1];
        Matcher m = PATTERN_INDEX.matcher(index);
        if (m.matches()) {
            try {
                int i = Integer.parseInt(m.group(1));
                deleteFieldValue(dPath, cursor, i);
            } catch (IndexOutOfBoundsException e) {
                // rethrow for now
                throw e;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error: Invalid index. Path [" + dPath
                        + "], target [" + cursor.getClass() + "].", e);
            }
        } else {
            deleteFieldValue(dPath, cursor, index);
        }
    }

    /*@SuppressWarnings("unchecked")
    public static void main(String[] args) {
        Object sample1 = new int[0];
        Object sample2 = new Integer[0];
        Object sample3 = new Object[0];
        System.out.println("int[] data     : " + sample1);
        System.out.println("int[] class    : " + sample1.getClass());

        System.out.println("Integer[] data : " + sample2);
        System.out.println("Integer[] class: " + sample2.getClass());

        System.out.println("Object[] data  : " + sample3);
        System.out.println("Object[] class : " + sample3.getClass());

        System.out.println();

        System.out.println("int[] is Object[]    : " + (sample1 instanceof Object[]));
        System.out.println("Integer[] is Object[]: " + (sample2 instanceof Object[]));
        System.out.println("Object[] is Object[] : " + (sample3 instanceof Object[]));

        System.out.println();

        // init data
        Map<String, Object> company = new HashMap<String, Object>();
        {
            company.put("name", "Monster Corp.");
            company.put("year", 2003);

            List<Map<String, Object>> employees = new ArrayList<Map<String, Object>>();
            company.put("employees", employees);

            Map<String, Object> employee1 = new HashMap<String, Object>();
            employee1.put("first_name", "Mike");
            employee1.put("last_name", "Wazowski");
            employee1.put("email", "mike.wazowski@monster.com");
            employee1.put("age", 29);
            employees.add(employee1);

            Map<String, Object> employee2 = new HashMap<String, Object>();
            employee2.put("first_name", "Sulley");
            employee2.put("last_name", "Sullivan");
            employee2.put("email", "sulley.sullivan@monster.com");
            employee2.put("age", 30);
            employees.add(employee2);
        }

        // access company's attributes:
        String companyName = DPathUtils.getValue(company, "name", String.class);
        System.out.println(companyName);
        Integer companyYear = DPathUtils.getValue(company, "year", Integer.class);
        System.out.println(companyYear);

        // access the two employee:
        Object user1 = DPathUtils.getValue(company, "employees.[0]");
        System.out.println(user1);
        Map<String, Object> user2 = DPathUtils.getValue(company, "employees.[1]", Map.class);
        System.out.println(user2);

        // access employee's attributes:
        String firstName1 = DPathUtils.getValue(company, "employees.[0].first_name", String.class);
        System.out.println(firstName1);
        Object email2 = DPathUtils.getValue(company, "employees.[1].email");
        System.out.println(email2);
        Long age2 = DPathUtils.getValue(company, "employees.[1].age", Long.class);
        System.out.println(age2);
    }*/
}
