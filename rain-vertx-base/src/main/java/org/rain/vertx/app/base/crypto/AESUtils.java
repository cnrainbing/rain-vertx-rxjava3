package org.rain.vertx.app.base.crypto;

import org.rain.vertx.app.base.crypto.utils.RainCipherInputStream;
import org.rain.vertx.app.base.crypto.utils.RainCipherOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * AES encryption utility class.
 *
 * <p>
 * Encrypt/Decrypt data using AES:
 * <ul>
 * <li>Default: 128-bit encryption key</li>
 * <li>Default: {@code AES/CTR/NoPadding} transformation</li>
 * <li>Support custom transformation and IV.</li>
 * </ul>
 * </p>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @see <a href=
 * "https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html#cipherTable">Java
 * Cryptography Architecture Oracle Providers Documentation for JDK 8</a>
 * @see <a href=
 * "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Cipher">Java
 * Cryptography Architecture Standard Algorithm Name Documentation for JDK 8</a>
 * @since 0.7.0
 */
public class AESUtils {
    public final static String CIPHER_ALGORITHM = "AES";

    public final static String[] CIPHER_MODES = {"ECB", "CBC", "PCBC", "CTR", "CTS", "CFB", "CFB8", "CFB16", "CFB32",
            "CFB64", "CFB128", "OFB", "OFB8", "OFB16", "OFB32", "OFB64", "OFB128", "GCM"};
    public final static String[] CIPHER_PADDINGS = {"NoPadding", "PKCS5Padding", "ISO10126Padding"};

    public final static String DEFAULT_CIPHER_TRANSFORMATION = "AES/CTR/NoPadding";
    public final static String DEFAULT_IV = "0000000000000000";
    private final static byte[] DEFAULT_IV_BYTES = DEFAULT_IV.getBytes(StandardCharsets.UTF_8);

    private final static byte[][] PADDING = {{0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9},
            {10}, {11}, {12}, {13}, {14}, {15}, {}};

    static {
        for (int i = 0; i < 16; i++) {
            PADDING[i] = Arrays.copyOf(new byte[0], i);
        }
    }

    private final static SecureRandom SECURE_RNG = new SecureRandom();

    private final static Set<Character> RANDOM_CHAR_SET = new HashSet<>(
            Arrays.asList('[', ']', '{', '}', ',', '.', '/', '?', '~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')',
                    '+', '=', '-', '_'));
    private final static RandomStringGenerator RSG = new RandomStringGenerator.Builder().filteredBy(
            c -> ('0' <= c && c <= '9') || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || RANDOM_CHAR_SET
                    .contains((char) c)).withinRange(33, 254).build();

    static {
        try {
            Cipher.getInstance(DEFAULT_CIPHER_TRANSFORMATION);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create and initialize a {@link Cipher} instance.
     *
     * @param mode                 either {@link Cipher#ENCRYPT_MODE} or {@link Cipher#DECRYPT_MODE}
     * @param keyData
     * @param iv
     * @param cipherTransformation
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @since 0.9.2
     */
    public static Cipher createCipher(int mode, byte[] keyData, byte[] iv, String cipherTransformation)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException {
        if (StringUtils.isBlank(cipherTransformation)) {
            cipherTransformation = DEFAULT_CIPHER_TRANSFORMATION;
        }
        if (!cipherTransformation.startsWith("AES/ECB/")) {
            // non-ECB requires IV
            if (iv == null || iv.length == 0) {
                iv = DEFAULT_IV_BYTES;
            }
        } else {
            // must not use IV with ECB
            iv = null;
        }
        SecretKeySpec aesKey = new SecretKeySpec(keyData, CIPHER_ALGORITHM);
        Cipher cipher = Cipher.getInstance(cipherTransformation);
        if (iv == null) {
            cipher.init(mode, aesKey);
        } else {
            AlgorithmParameterSpec spec = cipherTransformation.startsWith("AES/GCM/") ?
                    new GCMParameterSpec(128, iv) :
                    new IvParameterSpec(iv);
            cipher.init(mode, aesKey, spec);
        }
        return cipher;
    }

    /*----------------------------------------------------------------------*/

    /**
     * Encrypt data using AES.
     *
     * @param keyData
     * @param iv                   initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *                             will be used.
     * @param data
     * @param cipherTransformation cipher-transformation to use. If empty, {@link #DEFAULT_CIPHER_TRANSFORMATION}
     *                             will be used.
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidAlgorithmParameterException
     */
    public static byte[] encrypt(byte[] keyData, byte[] iv, byte[] data, String cipherTransformation)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = createCipher(Cipher.ENCRYPT_MODE, keyData, iv, cipherTransformation);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] temp = cipher.update(data);
            if (temp != null && temp.length > 0) {
                baos.write(temp);
            }
            if (cipherTransformation.endsWith("/ECB/NoPadding") || cipherTransformation.endsWith("/CBC/NoPadding")
                    || cipherTransformation.endsWith("/PCBC/NoPadding")) {
                temp = cipher.doFinal(PADDING[16 - data.length % 16]);
            } else {
                temp = cipher.doFinal();
            }
            if (temp != null && temp.length > 0) {
                baos.write(temp);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    /**
     * Encrypt data using AES with {@link #DEFAULT_CIPHER_TRANSFORMATION}.
     *
     * @param keyData
     * @param iv      initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *                will be used.
     * @param data
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static byte[] encrypt(byte[] keyData, byte[] iv, byte[] data)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        return encrypt(keyData, iv, data, DEFAULT_CIPHER_TRANSFORMATION);
    }

    /**
     * Encrypt data using AES.
     *
     * @param key
     * @param iv                   initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *                             will be used.
     * @param data
     * @param cipherTransformation cipher-transformation to use. If empty, {@link #DEFAULT_CIPHER_TRANSFORMATION}
     *                             will be used.
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidAlgorithmParameterException
     */
    public static byte[] encrypt(String key, String iv, byte[] data, String cipherTransformation)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        return encrypt(key.getBytes(StandardCharsets.UTF_8),
                iv != null ? iv.getBytes(StandardCharsets.UTF_8) : DEFAULT_IV_BYTES, data, cipherTransformation);
    }

    /**
     * Encrypt data using AES with {@link #DEFAULT_CIPHER_TRANSFORMATION}.
     *
     * @param key
     * @param iv   initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *             will be used.
     * @param data
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static byte[] encrypt(String key, String iv, byte[] data)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        return encrypt(key, iv, data, DEFAULT_CIPHER_TRANSFORMATION);
    }

    /**
     * Decrypt data using AES.
     *
     * @param keyData
     * @param iv                   initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *                             will be used.
     * @param encryptedData
     * @param cipherTransformation cipher-transformation to use. If empty, {@link #DEFAULT_CIPHER_TRANSFORMATION}
     *                             will be used.
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static byte[] decrypt(byte[] keyData, byte[] iv, byte[] encryptedData, String cipherTransformation)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = createCipher(Cipher.DECRYPT_MODE, keyData, iv, cipherTransformation);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] temp = cipher.update(encryptedData);
            if (temp != null && temp.length > 0) {
                baos.write(temp);
            }
            baos.write(cipher.doFinal());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    /**
     * Decrypt data using AES with {@link #DEFAULT_CIPHER_TRANSFORMATION}.
     *
     * @param keyData
     * @param iv            initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *                      will be used.
     * @param encryptedData
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static byte[] decrypt(byte[] keyData, byte[] iv, byte[] encryptedData)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        return decrypt(keyData, iv, encryptedData, DEFAULT_CIPHER_TRANSFORMATION);
    }

    /**
     * Decrypt data using AES.
     *
     * @param key
     * @param iv                   initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *                             will be used.
     * @param encryptedData
     * @param cipherTransformation cipher-transformation to use. If empty, {@link #DEFAULT_CIPHER_TRANSFORMATION}
     *                             will be used.
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static byte[] decrypt(String key, String iv, byte[] encryptedData, String cipherTransformation)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        return decrypt(key.getBytes(StandardCharsets.UTF_8),
                iv != null ? iv.getBytes(StandardCharsets.UTF_8) : DEFAULT_IV_BYTES, encryptedData,
                cipherTransformation);
    }

    /**
     * Decrypt data using AES with {@link #DEFAULT_CIPHER_TRANSFORMATION}.
     *
     * @param key
     * @param iv            initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *                      will be used.
     * @param encryptedData
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static byte[] decrypt(String key, String iv, byte[] encryptedData)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        return decrypt(key, iv, encryptedData, DEFAULT_CIPHER_TRANSFORMATION);
    }

    /*----------------------------------------------------------------------*/

    /**
     * Encrypt data using AES.
     *
     * @param keyData
     * @param iv                   initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *                             will be used.
     * @param cipherTransformation cipher-transformation to use. If empty, {@link #DEFAULT_CIPHER_TRANSFORMATION}
     *                             will be used.
     * @param data                 input data will be read from this input stream. This method will not close the
     *                             input stream!
     * @param output               output data will be written to this output stream. This method will not close the
     *                             output stream!
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidAlgorithmParameterException
     * @throws IOException
     * @since 0.9.2
     */
    public static void encrypt(byte[] keyData, byte[] iv, String cipherTransformation, InputStream data,
                               OutputStream output)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, IOException {
        Cipher cipher = createCipher(Cipher.ENCRYPT_MODE, keyData, iv, cipherTransformation);
        try (RainCipherOutputStream cos = new RainCipherOutputStream(output, cipher, false)) {
            long count = IOUtils.copy(data, cos, 1024);
            if (cipherTransformation.endsWith("/ECB/NoPadding") || cipherTransformation.endsWith("/CBC/NoPadding")
                    || cipherTransformation.endsWith("/PCBC/NoPadding")) {
                cos.write(PADDING[(int) (16 - count % 16)]);
            }
        }
    }

    /**
     * Encrypt data using AES with {@link #DEFAULT_CIPHER_TRANSFORMATION}.
     *
     * @param keyData
     * @param iv      initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *                will be used.
     * @param data    input data will be read from this input stream. This method will not close the
     *                input stream!
     * @param output  output data will be written to this output stream. This method will not close the
     *                output stream!
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws IOException
     * @since 0.9.2
     */
    public static void encrypt(byte[] keyData, byte[] iv, InputStream data, OutputStream output)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, IOException {
        encrypt(keyData, iv, DEFAULT_CIPHER_TRANSFORMATION, data, output);
    }

    /**
     * Encrypt data using AES.
     *
     * @param key
     * @param iv                   initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *                             will be used.
     * @param cipherTransformation cipher-transformation to use. If empty, {@link #DEFAULT_CIPHER_TRANSFORMATION}
     *                             will be used.
     * @param data                 input data will be read from this input stream. This method will not close the
     *                             input stream!
     * @param output               output data will be written to this output stream. This method will not close the
     *                             output stream!
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidAlgorithmParameterException
     * @throws IOException
     * @since 0.9.2
     */
    public static void encrypt(String key, String iv, String cipherTransformation, InputStream data,
                               OutputStream output)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, IOException {
        encrypt(key.getBytes(StandardCharsets.UTF_8),
                iv != null ? iv.getBytes(StandardCharsets.UTF_8) : DEFAULT_IV_BYTES, cipherTransformation, data,
                output);
    }

    /**
     * Encrypt data using AES with {@link #DEFAULT_CIPHER_TRANSFORMATION}.
     *
     * @param key
     * @param iv     initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *               will be used.
     * @param data   input data will be read from this input stream. This method will not close the
     *               input stream!
     * @param output output data will be written to this output stream. This method will not close the
     *               output stream!
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws IOException
     * @since 0.9.2
     */
    public static void encrypt(String key, String iv, InputStream data, OutputStream output)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, IOException {
        encrypt(key, iv, DEFAULT_CIPHER_TRANSFORMATION, data, output);
    }

    /**
     * Decrypt data using AES.
     *
     * @param keyData
     * @param iv                   initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *                             will be used.
     * @param cipherTransformation cipher-transformation to use. If empty, {@link #DEFAULT_CIPHER_TRANSFORMATION}
     *                             will be used.
     * @param encryptedData        encrypted data will be read from this input stream. This method will not close the
     *                             input stream!
     * @param output               output data will be written to this output stream. This method will not close the
     *                             output stream!
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws IOException
     * @since 0.9.2
     */
    public static void decrypt(byte[] keyData, byte[] iv, String cipherTransformation, InputStream encryptedData,
                               OutputStream output)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, IOException {
        Cipher cipher = createCipher(Cipher.DECRYPT_MODE, keyData, iv, cipherTransformation);
        try (RainCipherInputStream cis = new RainCipherInputStream(encryptedData, cipher, false)) {
            IOUtils.copy(cis, output, 1024);
            output.flush();
        }
    }

    /**
     * Decrypt data using AES with {@link #DEFAULT_CIPHER_TRANSFORMATION}.
     *
     * @param keyData
     * @param iv            initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *                      will be used.
     * @param encryptedData encrypted data will be read from this input stream. This method will not close the
     *                      input stream!
     * @param output        output data will be written to this output stream. This method will not close the
     *                      output stream!
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws IOException
     * @since 0.9.2
     */
    public static void decrypt(byte[] keyData, byte[] iv, InputStream encryptedData, OutputStream output)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, IOException {
        decrypt(keyData, iv, DEFAULT_CIPHER_TRANSFORMATION, encryptedData, output);
    }

    /**
     * Decrypt data using AES.
     *
     * @param key
     * @param iv                   initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *                             will be used.
     * @param cipherTransformation cipher-transformation to use. If empty, {@link #DEFAULT_CIPHER_TRANSFORMATION}
     *                             will be used.
     * @param encryptedData        encrypted data will be read from this input stream. This method will not close the
     *                             input stream!
     * @param output               output data will be written to this output stream. This method will not close the
     *                             output stream!
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws IOException
     * @since 0.9.2
     */
    public static void decrypt(String key, String iv, String cipherTransformation, InputStream encryptedData,
                               OutputStream output)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, IOException {
        decrypt(key.getBytes(StandardCharsets.UTF_8),
                iv != null ? iv.getBytes(StandardCharsets.UTF_8) : DEFAULT_IV_BYTES, cipherTransformation,
                encryptedData, output);
    }

    /**
     * Decrypt data using AES with {@link #DEFAULT_CIPHER_TRANSFORMATION}.
     *
     * @param key
     * @param iv            initial vector. If {@code null} or empty, {@link #DEFAULT_IV}
     *                      will be used.
     * @param encryptedData encrypted data will be read from this input stream. This method will not close the
     *                      input stream!
     * @param output        output data will be written to this output stream. This method will not close the
     *                      output stream!
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws IOException
     * @since 0.9.2
     */
    public static void decrypt(String key, String iv, InputStream encryptedData, OutputStream output)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, IOException {
        decrypt(key, iv, DEFAULT_CIPHER_TRANSFORMATION, encryptedData, output);
    }

    /*----------------------------------------------------------------------*/

    /**
     * AES key should be 16 bytes in length.
     *
     * @param key
     * @return
     */
    public static String normalizeKey(String key) {
        if (StringUtils.isBlank(key)) {
            key = RSG.generate(16);
        } else if (key.length() > 16) {
            key = key.substring(0, 16);
        } else if (key.length() < 16) {
            key += RSG.generate(16 - key.length());
        }
        return key;
    }

    /**
     * AES key should be 16 bytes in length.
     *
     * @param key
     * @return
     */
    public static byte[] normalizeKey(byte[] keyData) {
        if (keyData == null) {
            return RSG.generate(16).getBytes(StandardCharsets.UTF_8);
        } else {
            return normalizeKey(new String(keyData, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * Generate a random AES key, 16-byte length.
     *
     * @return
     */
    public static String randomKey() {
        return RSG.generate(16);
    }

    /**
     * Generate a random AES key, 16-byte length.
     *
     * @return
     */
    public static byte[] randomKeyAsBytes() {
        return RSG.generate(16).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Generate a random AES IV, 16-byte length.
     *
     * @return
     * @since 0.9.1
     */
    public static String randomIV() {
        return RSG.generate(16);
    }

    /**
     * Generate a random AES IV, 16-byte length.
     *
     * @return
     * @since 0.9.1
     */
    public static byte[] randomIVAsBytes() {
        return RSG.generate(16).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Generate a random AES IV.
     *
     * @param length
     * @return
     * @since 0.9.2
     */
    public static String randomIV(int length) {
        return RSG.generate(length);
    }

    /**
     * Generate a random AES IV.
     *
     * @param length
     * @return
     * @since 0.9.2
     */
    public static byte[] randomIVAsBytes(int length) {
        return RSG.generate(length).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Generate a random AES key, 16-byte length, using {@link SecureRandom}.
     *
     * @return
     * @since 0.9.2
     */
    public static byte[] randomKeyAsBytesSecure() {
        byte[] data = new byte[16];
        SECURE_RNG.nextBytes(data);
        return data;
    }

    /**
     * Generate a random AES IV, 16-byte length, using {@link SecureRandom}.
     *
     * @return
     * @since 0.9.2
     */
    public static byte[] randomIVAsBytesSecure() {
        return randomIVAsBytesSecure(16);
    }

    /**
     * Generate a random AES IV, using {@link SecureRandom}.
     *
     * @param length
     * @return
     * @since 0.9.2
     */
    public static byte[] randomIVAsBytesSecure(int length) {
        byte[] data = new byte[length];
        SECURE_RNG.nextBytes(data);
        return data;
    }
}
