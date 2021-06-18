package org.rain.vertx.app.base.config;

import java.io.File;
import java.io.Reader;
import java.net.URL;
import java.time.Duration;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigResolveOptions;
import com.typesafe.config.ConfigValue;

/**
 * Utility class to work with type-safe application config (see
 * https://github.com/lightbend/config).
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @see https://github.com/lightbend/config
 * @see https://github.com/lightbend/config/blob/master/HOCON.md
 * @since 0.9.1
 */
public class TypesafeConfigUtils {
    /**
     * Load, Parse & Resolve configurations from a file, with default parse & resolve options.
     *
     * @param configFile
     * @param useSystemEnvironment {@code true} to resolve substitutions falling back to environment
     *                             variables
     * @return
     */
    public static Config loadConfig(String configFile, boolean useSystemEnvironment) {
        return loadConfig(new File(configFile), useSystemEnvironment);
    }

    /**
     * Load, Parse & Resolve configurations from a file, with default parse & resolve options.
     *
     * @param configFile
     * @param useSystemEnvironment {@code true} to resolve substitutions falling back to environment
     *                             variables
     * @return
     */
    public static Config loadConfig(File configFile, boolean useSystemEnvironment) {
        return loadConfig(ConfigParseOptions.defaults(),
                ConfigResolveOptions.defaults().setUseSystemEnvironment(useSystemEnvironment),
                configFile);
    }

    /**
     * Load, Parse & Resolve configurations from a reader, with default parse & resolve options.
     *
     * @param configReader
     * @param useSystemEnvironment {@code true} to resolve substitutions falling back to environment
     *                             variables
     * @return
     */
    public static Config loadConfig(Reader configReader, boolean useSystemEnvironment) {
        return loadConfig(ConfigParseOptions.defaults(),
                ConfigResolveOptions.defaults().setUseSystemEnvironment(useSystemEnvironment),
                configReader);
    }

    /**
     * Load, Parse & Resolve configurations from a resource, with default parse & resolve options.
     *
     * @param classLoader
     * @param configResource
     * @param useSystemEnvironment {@code true} to resolve substitutions falling back to environment
     *                             variables
     * @return
     */
    public static Config loadConfig(ClassLoader classLoader, String configResource,
                                    boolean useSystemEnvironment) {
        return loadConfig(ConfigParseOptions.defaults(),
                ConfigResolveOptions.defaults().setUseSystemEnvironment(useSystemEnvironment),
                classLoader, configResource);
    }

    /**
     * Load, Parse & Resolve configurations from a URL, with default parse & resolve options.
     *
     * @param configUrl
     * @param useSystemEnvironment {@code true} to resolve substitutions falling back to environment
     *                             variables
     * @return
     */
    public static Config loadConfig(URL configUrl, boolean useSystemEnvironment) {
        return loadConfig(ConfigParseOptions.defaults(),
                ConfigResolveOptions.defaults().setUseSystemEnvironment(useSystemEnvironment),
                configUrl);
    }

    /**
     * Load, Parse & Resolve configurations from a file, specifying parse & resolve options.
     *
     * @param parseOptions
     * @param resolveOptions
     * @param configFile
     * @return
     */
    public static Config loadConfig(ConfigParseOptions parseOptions,
                                    ConfigResolveOptions resolveOptions, String configFile) {
        return loadConfig(parseOptions, resolveOptions, new File(configFile));
    }

    /**
     * Load, Parse & Resolve configurations from a file, specifying parse & resolve options.
     *
     * @param parseOptions
     * @param resolveOptions
     * @param configFile
     * @return
     */
    public static Config loadConfig(ConfigParseOptions parseOptions,
                                    ConfigResolveOptions resolveOptions, File configFile) {
        return ConfigFactory.parseFile(configFile, parseOptions).resolve(resolveOptions);
    }

    /**
     * Load, Parse & Resolve configurations from a reader, with default parse & resolve options.
     *
     * @param parseOptions
     * @param resolveOptions
     * @param configReader
     * @return
     */
    public static Config loadConfig(ConfigParseOptions parseOptions,
                                    ConfigResolveOptions resolveOptions, Reader configReader) {
        return ConfigFactory.parseReader(configReader, parseOptions).resolve(resolveOptions);
    }

    /**
     * Load, Parse & Resolve configurations from a resource, with default parse & resolve options.
     *
     * @param parseOptions
     * @param resolveOptions
     * @param classLoader
     * @param configResource
     * @return
     */
    public static Config loadConfig(ConfigParseOptions parseOptions,
                                    ConfigResolveOptions resolveOptions, ClassLoader classLoader, String configResource) {
        return ConfigFactory.parseResources(classLoader, configResource, parseOptions)
                .resolve(resolveOptions);
    }

    /**
     * Load, Parse & Resolve configurations from a URL, with default parse & resolve options.
     *
     * @param parseOptions
     * @param resolveOptions
     * @param configUrl
     * @return
     */
    public static Config loadConfig(ConfigParseOptions parseOptions,
                                    ConfigResolveOptions resolveOptions, URL configUrl) {
        return ConfigFactory.parseURL(configUrl, parseOptions).resolve(resolveOptions);
    }
    /*----------------------------------------------------------------------*/

    private final static Logger LOGGER = LoggerFactory.getLogger(TypesafeConfigUtils.class);

    /**
     * Get a sub-configuration. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Config getConfig(Config config, String path) {
        try {
            return config.getConfig(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a sub-configuration. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<Config> getConfigOptional(Config config, String path) {
        return Optional.ofNullable(getConfig(config, path));
    }

    /**
     * Get list of sub-configurations. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static List<? extends Config> getConfigList(Config config, String path) {
        try {
            return config.getConfigList(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get list of sub-configurations. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<List<? extends Config>> getConfigListOptional(Config config,
                                                                         String path) {
        return Optional.ofNullable(getConfigList(config, path));
    }

    /*----------------------------------------------------------------------*/

    /**
     * Get a configuration as Boolean. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Boolean getBoolean(Config config, String path) {
        try {
            return config.getBoolean(path) ? Boolean.TRUE : Boolean.FALSE;
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as Boolean. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<Boolean> getBooleanOptional(Config config, String path) {
        return Optional.ofNullable(getBoolean(config, path));
    }

    /**
     * Get a configuration as list of Booleans. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static List<Boolean> getBooleanList(Config config, String path) {
        try {
            return config.getBooleanList(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as list of Booleans. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<List<Boolean>> getBooleanListOptional(Config config, String path) {
        return Optional.ofNullable(getBooleanList(config, path));
    }

    /**
     * Get a configuration as Integer. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Integer getInteger(Config config, String path) {
        try {
            Object obj = config.getAnyRef(path);
            return obj instanceof Number ? ((Number) obj).intValue() : null;
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as Integer. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<Integer> getIntegerOptional(Config config, String path) {
        return Optional.ofNullable(getInteger(config, path));
    }

    /**
     * Get a configuration as list of Integers. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static List<Integer> getIntegerList(Config config, String path) {
        try {
            return config.getIntList(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as list of Integers. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<List<Integer>> getIntegerListOptional(Config config, String path) {
        return Optional.ofNullable(getIntegerList(config, path));
    }

    /**
     * Get a configuration as Long. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Long getLong(Config config, String path) {
        try {
            Object obj = config.getAnyRef(path);
            return obj instanceof Number ? ((Number) obj).longValue() : null;
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as Long. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<Long> getLongOptional(Config config, String path) {
        return Optional.ofNullable(getLong(config, path));
    }

    /**
     * Get a configuration as list of Longs. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static List<Long> getLongList(Config config, String path) {
        try {
            return config.getLongList(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as list of Longs. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<List<Long>> getLongListOptional(Config config, String path) {
        return Optional.ofNullable(getLongList(config, path));
    }

    /**
     * Get a configuration as Double. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Double getDouble(Config config, String path) {
        try {
            Object obj = config.getAnyRef(path);
            return obj instanceof Number ? ((Number) obj).doubleValue() : null;
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as Double. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<Double> getDoubleOptional(Config config, String path) {
        return Optional.ofNullable(getDouble(config, path));
    }

    /**
     * Get a configuration as list of Doubles. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static List<Double> getDoubleList(Config config, String path) {
        try {
            return config.getDoubleList(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as list of Doubles. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<List<Double>> getDoubleListOptional(Config config, String path) {
        return Optional.ofNullable(getDoubleList(config, path));
    }

    /**
     * Get a configuration as String. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static String getString(Config config, String path) {
        try {
            return config.getString(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as String. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<String> getStringOptional(Config config, String path) {
        return Optional.ofNullable(getString(config, path));
    }

    /**
     * Get a configuration as list of Strings. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static List<String> getStringList(Config config, String path) {
        try {
            return config.getStringList(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as list of Strings. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<List<String>> getStringListOptional(Config config, String path) {
        return Optional.ofNullable(getStringList(config, path));
    }

    /*----------------------------------------------------------------------*/

    /**
     * Get a configuration as size in bytes (parses special strings like "1Mb"). Return {@code null}
     * if missing, wrong type or bad value.
     *
     * @param config
     * @param path
     * @return
     */
    public static Long getBytes(Config config, String path) {
        try {
            return config.getBytes(path);
        } catch (ConfigException.Missing | ConfigException.WrongType | ConfigException.BadValue e) {
            if (e instanceof ConfigException.WrongType || e instanceof ConfigException.BadValue) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as size in bytes (parses special strings like "1Mb"). Return {@code null}
     * if missing, wrong type or bad value.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<Long> getBytesOptional(Config config, String path) {
        return Optional.ofNullable(getBytes(config, path));
    }

    /**
     * Get a configuration as list of sizes in bytes (parses special strings like "1Mb"). Return
     * {@code null} if missing, wrong type or bad value.
     *
     * @param config
     * @param path
     * @return
     */
    public static List<Long> getBytesList(Config config, String path) {
        try {
            return config.getBytesList(path);
        } catch (ConfigException.Missing | ConfigException.WrongType | ConfigException.BadValue e) {
            if (e instanceof ConfigException.WrongType || e instanceof ConfigException.BadValue) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as list of sizes in bytes (parses special strings like "1Mb"). Return
     * {@code null} if missing, wrong type or bad value.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<List<Long>> getBytesListOptional(Config config, String path) {
        return Optional.ofNullable(getBytesList(config, path));
    }

    /**
     * Get a configuration as period (parses special strings like "1w"). Return {@code null}
     * if missing, wrong type or bad value.
     *
     * @param config
     * @param path
     * @return
     */
    public static Period getPeriod(Config config, String path) {
        try {
            return config.getPeriod(path);
        } catch (ConfigException.Missing | ConfigException.WrongType | ConfigException.BadValue e) {
            if (e instanceof ConfigException.WrongType || e instanceof ConfigException.BadValue) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as period (parses special strings like "1w"). Return {@code null}
     * if missing, wrong type or bad value.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<Period> getPeriodOptional(Config config, String path) {
        return Optional.ofNullable(getPeriod(config, path));
    }

    /**
     * Get a configuration as duration (parses special strings like "10s"). Return {@code null}
     * if missing, wrong type or bad value.
     *
     * @param config
     * @param path
     * @return
     */
    public static Duration getDuration(Config config, String path) {
        try {
            return config.getDuration(path);
        } catch (ConfigException.Missing | ConfigException.WrongType | ConfigException.BadValue e) {
            if (e instanceof ConfigException.WrongType || e instanceof ConfigException.BadValue) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as duration (parses special strings like "10s"). Return {@code null}
     * if missing, wrong type or bad value.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<Duration> getDurationOptional(Config config, String path) {
        return Optional.ofNullable(getDuration(config, path));
    }

    /**
     * Get a configuration as list of durations (parses special strings like "10s"). Return
     * {@code null} if missing, wrong type or bad value.
     *
     * @param config
     * @param path
     * @return
     */
    public static List<Duration> getDurationList(Config config, String path) {
        try {
            return config.getDurationList(path);
        } catch (ConfigException.Missing | ConfigException.WrongType | ConfigException.BadValue e) {
            if (e instanceof ConfigException.WrongType || e instanceof ConfigException.BadValue) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as list of durations (parses special strings like "10s"). Return
     * {@code null} if missing, wrong type or bad value.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<List<Duration>> getDurationListOptional(Config config, String path) {
        return Optional.ofNullable(getDurationList(config, path));
    }

    /**
     * Get a configuration as duration (parses special strings like "10s"). Return {@code null}
     * if missing, wrong type or bad value.
     *
     * @param config
     * @param path
     * @param timeUnit
     * @return
     */
    public static Long getDuration(Config config, String path, TimeUnit timeUnit) {
        try {
            return config.getDuration(path, timeUnit);
        } catch (ConfigException.Missing | ConfigException.WrongType | ConfigException.BadValue e) {
            if (e instanceof ConfigException.WrongType || e instanceof ConfigException.BadValue) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as duration (parses special strings like "10s"). Return {@code null}
     * if missing, wrong type or bad value.
     *
     * @param config
     * @param path
     * @param timeUnit
     * @return
     */
    public static Optional<Long> getDurationOptional(Config config, String path,
                                                     TimeUnit timeUnit) {
        return Optional.ofNullable(getDuration(config, path, timeUnit));
    }

    /**
     * Get a configuration as list of durations (parses special strings like "10s"). Return
     * {@code null} if missing, wrong type or bad value.
     *
     * @param config
     * @param path
     * @param timeUnit
     * @return
     */
    public static List<Long> getDurationList(Config config, String path, TimeUnit timeUnit) {
        try {
            return config.getDurationList(path, timeUnit);
        } catch (ConfigException.Missing | ConfigException.WrongType | ConfigException.BadValue e) {
            if (e instanceof ConfigException.WrongType || e instanceof ConfigException.BadValue) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as list of durations (parses special strings like "10s"). Return
     * {@code null} if missing, wrong type or bad value.
     *
     * @param config
     * @param path
     * @param timeUnit
     * @return
     */
    public static Optional<List<Long>> getDurationListOptional(Config config, String path,
                                                               TimeUnit timeUnit) {
        return Optional.ofNullable(getDurationList(config, path, timeUnit));
    }
    /*----------------------------------------------------------------------*/

    /**
     * Get a configuration as Java object. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Object getObject(Config config, String path) {
        try {
            return config.getAnyRef(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as Java object. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<Object> getObjectOptional(Config config, String path) {
        return Optional.ofNullable(getObject(config, path));
    }

    /**
     * Get a configuration as list of Java objects. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static List<? extends Object> getObjectList(Config config, String path) {
        try {
            return config.getAnyRefList(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as list of Java objects. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<List<? extends Object>> getObjectListOptional(Config config,
                                                                         String path) {
        return Optional.ofNullable(getObjectList(config, path));
    }

    /**
     * Get a configuration as Java object. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getObject(Config config, String path, Class<T> clazz) {
        Object obj = getObject(config, path);
        return obj != null && clazz.isAssignableFrom(obj.getClass()) ? (T) obj : null;
    }

    /**
     * Get a configuration as Java object. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static <T> Optional<T> getObjectOptional(Config config, String path, Class<T> clazz) {
        return Optional.ofNullable(getObject(config, path, clazz));
    }

    /**
     * Get a configuration as list of Java objects. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> List<? extends T> getObjectList(Config config, String path, Class<T> clazz) {
        List<? extends Object> objList = getObjectList(config, path);
        if (objList == null) {

        }
        if (objList.size() == 0) {
            return Collections.emptyList();
        }
        return clazz.isAssignableFrom(objList.get(0).getClass()) ? (List<? extends T>) objList
                : null;
    }

    /**
     * Get a configuration as list of Java objects. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @param clazz
     * @return
     */
    public static <T> Optional<List<? extends Object>> getObjectListOptional(Config config,
                                                                             String path, Class<T> clazz) {
        return Optional.ofNullable(getObjectList(config, path, clazz));
    }

    /**
     * Get a configuration as Value. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static ConfigValue getValue(Config config, String path) {
        try {
            return config.getValue(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as Value. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<ConfigValue> getValueOptional(Config config, String path) {
        return Optional.ofNullable(getValue(config, path));
    }

    /**
     * Get a configuration as list of Values. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static ConfigList getValueList(Config config, String path) {
        try {
            return config.getList(path);
        } catch (ConfigException.Missing | ConfigException.WrongType e) {
            if (e instanceof ConfigException.WrongType) {
                LOGGER.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Get a configuration as list of Values. Return {@code null} if missing or wrong type.
     *
     * @param config
     * @param path
     * @return
     */
    public static Optional<ConfigList> getValueListOptional(Config config, String path) {
        return Optional.ofNullable(getValueList(config, path));
    }
}
