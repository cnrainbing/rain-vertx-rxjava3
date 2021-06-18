package org.rain.vertx.app.base.rocksdb;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.CompressionType;
import org.rocksdb.DBOptions;
import org.rocksdb.Env;
import org.rocksdb.Options;
import org.rocksdb.Priority;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksObject;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RocksDb utility class.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @see https://github.com/facebook/rocksdb/wiki/RocksJava-Basics
 * @since 0.8.0
 */
public class RocksDbUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(RocksDbUtils.class);

    /**
     * Build {@link DBOptions} with default options.
     *
     * <p>
     * Default options are set following guidelines at
     * {@linkplain https://github.com/facebook/rocksdb/wiki/Setup-Options-and-Basic-Tuning}.
     * </p>
     * <ul>
     * <li>{@code createIfMissing=true}</li>
     * <li>{@code createMissingColumnFamilies=true}</li>
     * <li>{@code errorIfExists=false}</li>
     * <li>{@code maxBackgroundFlushes=2}</li>
     * <li>{@code maxBackgroundCompactions=4}</li>
     * <li>{@code increaseParallelism=8}</li>
     * <li>{@code backgroundThreads[HIGH=2,LOW=4,BOTTOM=8]}</li>
     * <li>{@code bytesPerSync=1Mb}</li>
     * <li>Keep all logs in one log file ({@code maxLogFileSize=0}), rotated daily
     * ({@code logFileTimeToRoll=86400}), keep last 1000 log files
     * ({@code keepLogFileNum=1000}).</li>
     * </ul>
     *
     * @return
     * @since 0.9.3
     */
    public static DBOptions defaultDbOptions() {
        DBOptions opts = new DBOptions();
        opts.setEnv(Env.getDefault());
        opts.setCreateIfMissing(true).setCreateMissingColumnFamilies(true).setErrorIfExists(false);
        opts.getEnv().setBackgroundThreads(2, Priority.HIGH).setBackgroundThreads(4, Priority.LOW)
                .setBackgroundThreads(8, Priority.BOTTOM);
        opts.setMaxBackgroundCompactions(4)
                .setMaxBackgroundFlushes(2)
                .setIncreaseParallelism(8);
        opts.setBytesPerSync(1024 * 1024);
        opts.setMaxLogFileSize(0).setLogFileTimeToRoll(24 * 3600).setKeepLogFileNum(1000);
        return opts;
    }

    /**
     * Build {@link ReadOptions} with default options.
     *
     * <p>
     * Default options are set following guidelines at
     * {@linkplain https://github.com/facebook/rocksdb/wiki/Setup-Options-and-Basic-Tuning}.
     * </p>
     * <ul>
     * <li>{@code tailing=true}</li>
     * <li>{@code backgroundPurgeOnIteratorCleanup=true}</li>
     * </ul>
     *
     * @return
     * @since 0.9.3
     */
    public static ReadOptions defaultReadOptions() {
        ReadOptions opts = new ReadOptions();
        opts.setTailing(true).setBackgroundPurgeOnIteratorCleanup(true);
        return opts;
    }

    /**
     * Build {@link WriteOptions} with default options.
     *
     * <p>
     * Default options are set following guidelines at
     * {@linkplain https://github.com/facebook/rocksdb/wiki/Setup-Options-and-Basic-Tuning}.
     * </p>
     * <ul>
     * <li>{@code disableWAL=false}</li>
     * <li>{@code sync=false}</li>
     * </ul>
     *
     * @return
     * @since 0.9.3
     */
    public static WriteOptions defaultWriteOptions() {
        WriteOptions opts = new WriteOptions();
        opts.setDisableWAL(false).setSync(false);
        return opts;
    }

    /**
     * Build {@link ColumnFamilyOptions} with default options.
     *
     * <p>
     * Default options are set following guidelines at
     * {@linkplain https://github.com/facebook/rocksdb/wiki/Setup-Options-and-Basic-Tuning}.
     * </p>
     * <ul>
     * <li>{@code levelCompactionDynamicLevelBytes=true}</li>
     * <li>{@code compressionType=LZ4_COMPRESSION}</li>
     * <li>{@code bottommostCompressionType=ZSTD_COMPRESSION}</li>
     * </ul>
     *
     * @return
     * @since 0.9.3
     */
    public static ColumnFamilyOptions defaultColumnFamilyOptions() {
        ColumnFamilyOptions opts = new ColumnFamilyOptions();
        opts.setLevelCompactionDynamicLevelBytes(true)
                .setCompressionType(CompressionType.LZ4_COMPRESSION)
                .setBottommostCompressionType(CompressionType.ZSTD_COMPRESSION);
        return opts;
    }

    /*----------------------------------------------------------------------*/

    /**
     * Silently close RocksDb objects.
     *
     * @param rocksObjList
     */
    public static void closeRocksObjects(RocksObject... rocksObjList) {
        if (rocksObjList != null) {
            for (RocksObject obj : rocksObjList) {
                try {
                    if (obj != null) {
                        obj.close();
                    }
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Get all available column family names from a RocksDb data directory.
     *
     * @param path
     * @return
     * @throws RocksDBException
     */
    public static String[] getColumnFamilyList(String path) throws RocksDBException {
        List<byte[]> cfList = RocksDB.listColumnFamilies(new Options(), path);
        if (cfList == null || cfList.size() == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        List<String> result = new ArrayList<>(cfList.size());
        for (byte[] cf : cfList) {
            result.add(new String(cf, StandardCharsets.UTF_8));
        }
        return result.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    /**
     * Build a {@link ColumnFamilyDescriptor} with default options.
     *
     * @param cfName
     * @return
     */
    public static ColumnFamilyDescriptor buildColumnFamilyDescriptor(String cfName) {
        return buildColumnFamilyDescriptor(defaultColumnFamilyOptions(), cfName);
    }

    /**
     * Build a {@link ColumnFamilyDescriptor}, specifying options.
     *
     * @param cfOptions
     * @param cfName
     * @return
     */
    public static ColumnFamilyDescriptor buildColumnFamilyDescriptor(ColumnFamilyOptions cfOptions,
                                                                     String cfName) {
        return cfOptions != null
                ? new ColumnFamilyDescriptor(cfName.getBytes(StandardCharsets.UTF_8), cfOptions)
                : new ColumnFamilyDescriptor(cfName.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Build a list of {@link ColumnFamilyDescriptor}s with default options.
     *
     * @param cfNames
     * @return
     */
    public static List<ColumnFamilyDescriptor> buildColumnFamilyDescriptors(String... cfNames) {
        return buildColumnFamilyDescriptors(defaultColumnFamilyOptions(), cfNames);
    }

    /**
     * Build a list of {@link ColumnFamilyDescriptor}s, specifying options.
     *
     * @param cfOptions
     * @param cfNames
     * @return
     */
    public static List<ColumnFamilyDescriptor> buildColumnFamilyDescriptors(
            ColumnFamilyOptions cfOptions, String... cfNames) {
        List<ColumnFamilyDescriptor> result = new ArrayList<>();
        if (cfNames != null) {
            for (String cfName : cfNames) {
                result.add(buildColumnFamilyDescriptor(cfOptions, cfName));
            }
        }
        return result;
    }
}
