package org.rain.vertx.app.base.id;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class to generate IDs using Twitter Snowflake algorithm.
 *
 * <p>
 * Unless specified otherwise, generated IDs are:
 * <ul>
 * <li>Distributed: IDs generated from different nodes are NOT duplicated as long as node-ids are
 * different.</li>
 * <li>In ascending order, but not serial (e.g. {@code next-id} is NOT equal to
 * {@code prev-id + 1})</li>
 * </ul>
 * </p>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class IdGenerator {
    private final static ConcurrentMap<Long, IdGenerator> cache = new ConcurrentHashMap<Long, IdGenerator>();
    private static long macAddr = 0;

    /**
     * Returns host's MAC address.
     *
     * @return
     */
    public static long getMacAddr() {
        if (macAddr == 0) {
            try {
                InetAddress ip = InetAddress.getLocalHost();
                NetworkInterface network = NetworkInterface.getByInetAddress(ip);
                byte[] mac = network.getHardwareAddress();
                for (byte temp : mac) {
                    macAddr = (macAddr << 8) | ((int) temp & 0xFF);
                }
            } catch (Exception e) {
                macAddr = 0;
            }
        }
        return macAddr;
    }

    /**
     * Gets the default {@link IdGenerator} instance.
     *
     * @return
     * @since 0.9.1.1
     */
    public static IdGenerator getInstance() {
        return getInstance(getMacAddr());
    }

    /**
     * Gets an {@link IdGenerator} instance for a node.
     *
     * @param nodeId
     * @return
     */
    public static IdGenerator getInstance(long nodeId) {
        IdGenerator idGen = cache.get(nodeId);
        if (idGen == null) {
            idGen = new IdGenerator(nodeId);
            idGen.init();
            IdGenerator existing = cache.putIfAbsent(nodeId, idGen);
            idGen = existing != null ? existing : idGen;
        }
        return idGen;
    }

    /**
     * Disposes an unused {@link IdGenerator}.
     *
     * @param idGen
     */
    public static void disposeInstance(IdGenerator idGen) {
        if (idGen != null) {
            long nodeId = idGen.nodeId;
            IdGenerator temp = cache.remove(nodeId);
            if (temp != null) {
                temp.destroy();
            }
        }
    }

    private final static long MASK_SEQUENCE_TINY = 0xFFFFL; // 16 bits
    private final static long MAX_SEQUENCE_TINY = 0xFFFFL; // 16 bits
    private final static int SHIFT_TIMESTAMP_TINY = 16;

    private final static long MASK_TIMESTAMP_MINI = 0x1FFFFFFFFFFL; // 41 bits
    private final static long MASK_NODE_ID_MINI = 0x0L; // 0 bits
    private final static long MASK_SEQUENCE_MINI = 0x7FL; // 7 bits
    private final static long MAX_SEQUENCE_MINI = 0x7FL; // 7 bits
    private final static int SHIFT_TIMESTAMP_MINI = 7;
    private final static int SHIFT_NODE_ID_MINI = 7;

    private final static long MASK_TIMESTAMP_48 = 0xFFFFFFFFL; // 32 bits
    private final static long MASK_NODE_ID_48 = 0x7L; // 3 bits
    private final static long MASK_SEQUENCE_48 = 0x1FFFL; // 13 bits
    private final static long MAX_SEQUENCE_48 = 0x1FFFL; // 13 bits
    private final static int SHIFT_TIMESTAMP_48 = 16;
    private final static int SHIFT_NODE_ID_48 = 13;

    private final static long MASK_TIMESTAMP_64 = 0x1FFFFFFFFFFL; // 41 bits
    private final static long MASK_NODE_ID_64 = 0x3FFL; // 10 bits
    private final static long MASK_SEQUENCE_64 = 0x1FFFL; // 13 bits
    private final static long MAX_SEQUENCE_64 = 0x1FFFL; // 13 bits
    private final static int SHIFT_TIMESTAMP_64 = 23;
    private final static int SHIFT_NODE_ID_64 = 13;
    // private final static long TIMESTAMP_EPOCH = 1330534800000L; // 1-Mar-2012
    public final static long TIMESTAMP_EPOCH = 1362070800000L; // 1-Mar-2013

    private final static long MASK_NODE_ID_128 = 0xFFFFFFFFFFFFL; // 48 bits
    private final static long MASK_SEQUENCE_128 = 0xFFFF; // 16 bits
    private final static long MAX_SEQUENCE_128 = 0xFFFF; // 16 bits
    private final static int SHIFT_TIMESTAMP_128 = 64;
    private final static int SHIFT_NODE_ID_128 = 16;

    private long nodeId;
    private long template48, template64, templateMini;
    private BigInteger template128;
    private AtomicLong sequenceMillisec = new AtomicLong();
    private AtomicLong lastTimestampMillisec = new AtomicLong();
    private boolean isInited = false;

    /**
     * Constructs a new {@link IdGenerator} instance with specified node id.
     *
     * @param nodeId
     */
    protected IdGenerator(long nodeId) {
        this.nodeId = nodeId;
        init();
    }

    protected void init() {
        if (!isInited) {
            templateMini = (nodeId & MASK_NODE_ID_MINI) << SHIFT_NODE_ID_MINI;
            template64 = (nodeId & MASK_NODE_ID_64) << SHIFT_NODE_ID_64;
            template48 = (nodeId & MASK_NODE_ID_48) << SHIFT_NODE_ID_48;
            template128 = BigInteger.valueOf(nodeId & MASK_NODE_ID_128)
                    .shiftLeft(SHIFT_NODE_ID_128);
            isInited = true;
        }
    }

    protected void destroy() {
        // EMPTY
    }

    /**
     * Waits till clock moves to the next millisecond.
     *
     * @param currentMillisec
     * @return the "next" millisecond
     */
    public static long waitTillNextMillisec(long currentMillisec) {
        long nextMillisec = System.currentTimeMillis();
        for (; nextMillisec <= currentMillisec; nextMillisec = System.currentTimeMillis()) {
            Thread.yield();
        }
        return nextMillisec;
    }

    /**
     * Waits till clock moves to the next second.
     *
     * @param currentSecond
     * @return the "next" second
     */
    public static long waitTillNextSecond(long currentSecond) {
        return waitTillNextTick(currentSecond, 1000L);
    }

    /**
     * Waits till clock moves to the next tick.
     *
     * @param currentTick
     * @param tickSize    tick size in milliseconds
     * @return the "next" tick
     */
    public static long waitTillNextTick(long currentTick, long tickSize) {
        long nextBlock = System.currentTimeMillis() / tickSize;
        for (; nextBlock <= currentTick; nextBlock = System.currentTimeMillis() / tickSize) {
            Thread.yield();
        }
        return nextBlock;
    }

    /* tiny id */

    /**
     * Extracts the (UNIX) timestamp from a tiny id.
     *
     * @param idTiny
     * @return the UNIX timestamp (milliseconds)
     */
    public static long extractTimestampTiny(long idTiny) {
        final long blockSize = 10000L;
        final long threshold = 0x800000000L;
        long timestamp = idTiny > threshold ? idTiny >>> SHIFT_TIMESTAMP_TINY : idTiny;
        return timestamp * blockSize + TIMESTAMP_EPOCH;
    }

    /**
     * Extracts the (UNIX) timestamp from a tiny hex id.
     *
     * @param idTinyHex
     * @return the UNIX timestamp (milliseconds)
     * @throws NumberFormatException
     */
    public static long extractTimestampTiny(String idTinyHex) throws NumberFormatException {
        return extractTimestampTiny(Long.parseLong(idTinyHex, 16));
    }

    /**
     * Extracts the (UNIX) timestamp from a tiny ascii id (radix
     * {@link Character#MAX_RADIX}).
     *
     * @param idTinyAscii
     * @return the UNIX timestamp (milliseconds)
     * @throws NumberFormatException
     */
    public static long extractTimestampTinyAscii(String idTinyAscii) throws NumberFormatException {
        return extractTimestampTiny(Long.parseLong(idTinyAscii, Character.MAX_RADIX));
    }

    private AtomicLong sequenceTiny = new AtomicLong();

    /**
     * Generates a tiny id (various bit long, does not include node info).
     *
     * <p>
     * Format: {@code <41-bit:timestamp><0 or 16bit:sequence number>}. Where {@code timestamp} is in
     * blocks of 10-second, minus the epoch.
     * </p>
     *
     * <p>
     * Note:
     * <ul>
     * <li>Tiny IDs are not distributed! IDs generated from different nodes can be duplicated since
     * ID does not include node info.</li>
     * <li>If IDs are generated at low rate (~1 ID per 10 seconds), then ID is "tiny" (41-bit
     * long). Otherwise it is suffixed by a 16-bit sequence number.</li>
     * <li>Hence, generated IDs may NOT be in ascending order!</li>
     * </ul>
     * </p>
     *
     * @return
     */
    synchronized public long generateIdTiny() {
        final long blockSize = 10000L; // block 10000 ms
        long timestamp = System.currentTimeMillis() / blockSize;
        long sequence = 0;
        boolean done = false;
        while (!done) {
            done = true;
            while (timestamp < lastTimestampMillisec.get() / blockSize) {
                timestamp = waitTillNextTick(timestamp, blockSize);
            }
            if (timestamp == lastTimestampMillisec.get() / blockSize) {
                sequence = sequenceTiny.incrementAndGet();
                if (sequence > MAX_SEQUENCE_TINY) {
                    // reset sequence
                    sequenceTiny.set(sequence = 0);
                    timestamp = waitTillNextTick(timestamp, blockSize);
                    done = false;
                }
            }
        }
        sequenceTiny.set(sequence);
        lastTimestampMillisec.set(timestamp * blockSize);
        timestamp -= TIMESTAMP_EPOCH / blockSize;
        return sequence == 0 ? timestamp
                : (timestamp << SHIFT_TIMESTAMP_TINY) | (sequence & MASK_SEQUENCE_TINY);
    }

    /**
     * Generate a tiny id as hex string.
     *
     * @return
     */
    public String generateIdTinyHex() {
        return Long.toHexString(generateIdTiny()).toUpperCase();
    }

    /**
     * Generate a tiny id as ASCII string (radix {@link Character#MAX_RADIX}).
     *
     * @return
     */
    public String generateIdTinyAscii() {
        return Long.toString(generateIdTiny(), Character.MAX_RADIX).toUpperCase();
    }

    /* tiny id */

    /* 48-bit id */

    /**
     * Extracts the (UNIX) timestamp from a 48-bit id.
     *
     * @param id48
     * @return the UNIX timestamp (milliseconds)
     */
    public static long extractTimestamp48(long id48) {
        final long blockSize = 1000L;
        long timestamp = (id48 >>> SHIFT_TIMESTAMP_48) * blockSize + TIMESTAMP_EPOCH;
        return timestamp;
    }

    /**
     * Extracts the (UNIX) timestamp from a 48-bit hex id.
     *
     * @param id48hex
     * @return the UNIX timestamp (milliseconds)
     * @throws NumberFormatException
     */
    public static long extractTimestamp48(String id48hex) throws NumberFormatException {
        return extractTimestamp48(Long.parseLong(id48hex, 16));
    }

    /**
     * Extracts the (UNIX) timestamp from a 48-bit ASCII id (radix
     * {@link Character#MAX_RADIX}).
     *
     * @param id48ascii
     * @return the UNIX timestamp (milliseconds)
     * @throws NumberFormatException
     */
    public static long extractTimestamp48Ascii(String id48ascii) throws NumberFormatException {
        return extractTimestamp48(Long.parseLong(id48ascii, Character.MAX_RADIX));
    }

    /**
     * Generates a 48-bit id.
     *
     * <p>
     * Format: {@code <32-bit:timestamp><3-bit:node id><13 bit:sequence number>}. Where
     * {@code timestamp} is in seconds, minus the epoch.
     * </p>
     *
     * @return
     */
    synchronized public long generateId48() {
        final long blockSize = 1000L; // block 1000 ms
        long timestamp = System.currentTimeMillis() / blockSize;
        long sequence = 0;
        boolean done = false;
        while (!done) {
            done = true;
            while (timestamp < lastTimestampMillisec.get() / blockSize) {
                timestamp = waitTillNextSecond(timestamp);
            }
            if (timestamp == lastTimestampMillisec.get() / blockSize) {
                // increase sequence
                sequence = sequenceMillisec.incrementAndGet();
                if (sequence > MAX_SEQUENCE_48) {
                    // reset sequence
                    sequenceMillisec.set(sequence = 0);
                    timestamp = waitTillNextSecond(timestamp);
                    done = false;
                }
            }
        }
        sequenceMillisec.set(sequence);
        lastTimestampMillisec.set(timestamp * blockSize);
        timestamp = ((timestamp * blockSize - TIMESTAMP_EPOCH) / blockSize) & MASK_TIMESTAMP_48;
        return timestamp << SHIFT_TIMESTAMP_48 | template48 | (sequence & MASK_SEQUENCE_48);
    }

    /**
     * Generate a 48-bit id as hex string.
     *
     * @return
     */
    public String generateId48Hex() {
        return Long.toHexString(generateId48()).toUpperCase();
    }

    /**
     * Generate a 48-bit id as ASCII string (radix {@link Character#MAX_RADIX}).
     *
     * @return
     */
    public String generateId48Ascii() {
        return Long.toString(generateId48(), Character.MAX_RADIX).toUpperCase();
    }

    /* 48-bit id */

    /* mini id */

    /**
     * Extracts the (UNIX) timestamp from a mini id.
     *
     * @param idMini
     * @return the UNIX timestamp (milliseconds)
     */
    public static long extractTimestampMini(long idMini) {
        long timestamp = (idMini >>> SHIFT_TIMESTAMP_MINI) + TIMESTAMP_EPOCH;
        return timestamp;
    }

    /**
     * Extracts the (UNIX) timestamp from a mini hex id.
     *
     * @param idMinihex
     * @return the UNIX timestamp (milliseconds)
     * @throws NumberFormatException
     */
    public static long extractTimestampMini(String idMinihex) throws NumberFormatException {
        return extractTimestampMini(Long.parseLong(idMinihex, 16));
    }

    /**
     * Extracts the (UNIX) timestamp from a mini ASCII id (radix
     * {@link Character#MAX_RADIX}).
     *
     * @param idMiniAscii
     * @return the UNIX timestamp (milliseconds)
     * @throws NumberFormatException
     */
    public static long extractTimestampMiniAscii(String idMiniAscii) throws NumberFormatException {
        return extractTimestampMini(Long.parseLong(idMiniAscii, Character.MAX_RADIX));
    }

    /**
     * Generates a mini id (48-bit long, does not include node info).
     *
     * <p>
     * Format: {@code <41-bit:timestamp><7-bit:sequence number>}. Where {@code timestamp} is in
     * milliseconds, minus the epoch.
     * </p>
     *
     * <p>
     * Note:
     * <ul>
     * <li>Mini IDs are not distributed! IDs generated from different nodes can be duplicated since
     * ID does not include node info.</li>
     * </ul>
     * </p>
     *
     * @return
     */
    synchronized public long generateIdMini() {
        long timestamp = System.currentTimeMillis();
        long sequence = 0;
        boolean done = false;
        while (!done) {
            done = true;
            while (timestamp < lastTimestampMillisec.get()) {
                timestamp = waitTillNextMillisec(timestamp);
            }
            if (timestamp == lastTimestampMillisec.get()) {
                // increase sequence
                sequence = sequenceMillisec.incrementAndGet();
                if (sequence > MAX_SEQUENCE_MINI) {
                    // reset sequence
                    sequenceMillisec.set(sequence = 0);
                    timestamp = waitTillNextMillisec(timestamp);
                    done = false;
                }
            }
        }
        sequenceMillisec.set(sequence);
        lastTimestampMillisec.set(timestamp);
        timestamp = (timestamp - TIMESTAMP_EPOCH) & MASK_TIMESTAMP_MINI;
        return timestamp << SHIFT_TIMESTAMP_MINI | templateMini | (sequence & MASK_SEQUENCE_MINI);
    }

    /**
     * Generate a mini id as hex string.
     *
     * @return
     */
    public String generateIdMiniHex() {
        return Long.toHexString(generateIdMini()).toUpperCase();
    }

    /**
     * Generate a mini id as ASCII string (radix {@link Character#MAX_RADIX}).
     *
     * @return
     */
    public String generateIdMiniAscii() {
        return Long.toString(generateIdMini(), Character.MAX_RADIX).toUpperCase();
    }

    /* mini id */

    /* 64-bit id */

    /**
     * Extracts the (UNIX) timestamp from a 64-bit id.
     *
     * @param id64
     * @return the UNIX timestamp (milliseconds)
     */
    public static long extractTimestamp64(long id64) {
        // use >>> for unsigned number
        long timestamp = (id64 >>> SHIFT_TIMESTAMP_64) + TIMESTAMP_EPOCH;
        return timestamp;
    }

    /**
     * Extracts the (UNIX) timestamp from a 64-bit hex id.
     *
     * @param id64hex
     * @return the UNIX timestamp (milliseconds)
     * @throws NumberFormatException
     */
    public static long extractTimestamp64(String id64hex) throws NumberFormatException {
        return extractTimestamp64(Long.parseLong(id64hex, 16));
    }

    /**
     * Extracts the (UNIX) timestamp from a 64-bit ASCII id (radix
     * {@link Character#MAX_RADIX}).
     *
     * @param id64ascii
     * @return the UNIX timestamp (milliseconds)
     * @throws NumberFormatException
     */
    public static long extractTimestamp64Ascii(String id64ascii) throws NumberFormatException {
        return extractTimestamp64(Long.parseLong(id64ascii, Character.MAX_RADIX));
    }

    /**
     * Generates a 64-bit id.
     *
     * <p>
     * Format: {@code <41-bit:timestamp><10-bit:node-id><13-bit:sequence-number>}. Where
     * {@code timestamp} is in milliseconds, minus the epoch.
     * </p>
     *
     * @return
     */
    synchronized public long generateId64() {
        long timestamp = System.currentTimeMillis();
        long sequence = 0;
        boolean done = false;
        while (!done) {
            done = true;
            while (timestamp < lastTimestampMillisec.get()) {
                timestamp = waitTillNextMillisec(timestamp);
            }
            if (timestamp == lastTimestampMillisec.get()) {
                // increase sequence
                sequence = sequenceMillisec.incrementAndGet();
                if (sequence > MAX_SEQUENCE_64) {
                    // reset sequence
                    sequenceMillisec.set(sequence = 0);
                    timestamp = waitTillNextMillisec(timestamp);
                    done = false;
                }
            }
        }
        sequenceMillisec.set(sequence);
        lastTimestampMillisec.set(timestamp);
        timestamp = (timestamp - TIMESTAMP_EPOCH) & MASK_TIMESTAMP_64;
        return timestamp << SHIFT_TIMESTAMP_64 | template64 | (sequence & MASK_SEQUENCE_64);
    }

    /**
     * Generate a 64-bit id as hex string.
     *
     * @return
     */
    public String generateId64Hex() {
        return Long.toHexString(generateId64()).toUpperCase();
    }

    /**
     * Generate a 64-bit id as ASCII string (radix {@link Character#MAX_RADIX}).
     *
     * @return
     */
    public String generateId64Ascii() {
        return Long.toString(generateId64(), Character.MAX_RADIX).toUpperCase();
    }

    /* 64-bit id */

    /* 128-bit id */

    /**
     * Extracts the (UNIX) timestamp from a 128-bit id.
     *
     * @param id128
     * @return the UNIX timestamp (milliseconds)
     */
    public static long extractTimestamp128(BigInteger id128) {
        BigInteger result = id128.shiftRight((int) SHIFT_TIMESTAMP_128);
        return result.longValue();
    }

    /**
     * Extracts the (UNIX) timestamp from a 128-bit hex id.
     *
     * @param id128hex
     * @return the UNIX timestamp (milliseconds)
     * @throws NumberFormatException
     */
    public static long extractTimestamp128(String id128hex) throws NumberFormatException {
        return extractTimestamp128(new BigInteger(id128hex, 16));
    }

    /**
     * Extracts the (UNIX) timestamp from a 128-bit ASCII id (radix
     * {@link Character#MAX_RADIX}).
     *
     * @param id128ascii
     * @return the UNIX timestamp (milliseconds)
     * @throws NumberFormatException
     */
    public static long extractTimestamp128Ascii(String id128ascii) throws NumberFormatException {
        return extractTimestamp128(new BigInteger(id128ascii, Character.MAX_RADIX));
    }

    /**
     * Generates a 128-bit id.
     *
     * <p>
     * Format: {@code <64-bit:timestamp><48-bit:node-id><16-bit:sequence-number>}. Where
     * {@code timestamp} is in milliseconds.
     * </p>
     * .
     *
     * @return
     */
    synchronized public BigInteger generateId128() {
        long timestamp = System.currentTimeMillis();
        long sequence = 0;
        boolean done = false;
        while (!done) {
            done = true;
            while (timestamp < lastTimestampMillisec.get()) {
                timestamp = waitTillNextMillisec(timestamp);
            }
            if (timestamp == lastTimestampMillisec.get()) {
                // increase sequence
                sequence = sequenceMillisec.incrementAndGet();
                if (sequence > MAX_SEQUENCE_128) {
                    // reset sequence
                    sequenceMillisec.set(sequence = 0);
                    timestamp = waitTillNextMillisec(timestamp);
                    done = false;
                }
            }
        }
        sequenceMillisec.set(sequence);
        lastTimestampMillisec.set(timestamp);
        BigInteger biSequence = BigInteger.valueOf(sequence & MASK_SEQUENCE_128);
        BigInteger biResult = BigInteger.valueOf(timestamp);
        biResult = biResult.shiftLeft((int) SHIFT_TIMESTAMP_128);
        biResult = biResult.or(template128).or(biSequence);
        return biResult;
    }

    /**
     * Generate a 128-bit id as hex string.
     *
     * @return
     */
    public String generateId128Hex() {
        return generateId128().toString(16).toUpperCase();
    }

    /**
     * Generate a 128-bit id as ASCII string (radix {@link Character#MAX_RADIX}
     * ).
     *
     * @return
     */
    public String generateId128Ascii() {
        return generateId128().toString(Character.MAX_RADIX).toUpperCase();
    }
}
