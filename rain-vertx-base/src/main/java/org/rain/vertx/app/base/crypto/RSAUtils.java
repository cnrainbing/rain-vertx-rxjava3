package org.rain.vertx.app.base.crypto;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.io.pem.PemObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * RSA encryption/signature utility class.
 *
 * <p>
 * Encrypt/Decrypt data, sign data and verify signature using RSA public/private key:
 * <ul>
 * <li>Default: {@code RSA/ECB/PKCS1Padding} transformation (11-byte padding size)</li>
 * <li>Support custom transformation and padding size</li>
 * </ul>
 * </p>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html#cipherTable">Java Cryptography Architecture Oracle Providers Documentation for JDK 8</a>
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Cipher">Java Cryptography Architecture Standard Algorithm Name Documentation for JDK 8</a>
 * @since 0.7.0
 */
public class RSAUtils {
    public final static String CIPHER_ALGORITHM = "RSA";

    public final static String CIPHER_MODE = "ECB";

    public final static String[] CIPHER_PADDINGS = {"NoPadding", "PKCS1Padding", "OAEPWithMD5AndMGF1Padding",
            "OAEPWithSHA1AndMGF1Padding", "OAEPWithSHA-1AndMGF1Padding", "OAEPWithSHA-224AndMGF1Padding",
            "OAEPWithSHA-256AndMGF1Padding", "OAEPWithSHA-384AndMGF1Padding", "OAEPWithSHA-512AndMGF1Padding"};
    public final static int[] CIPHER_PADDINGS_SIZE = {1, 11, 34, 42, 42, 58, 66, 98, 130};

    public final static String DEFAULT_CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    public final static int DEFAULT_PADDING_SIZE = 11;

    public final static String[] SIGNATURE_ALGORITHMS = {"MD2withRSA", "MD5withRSA", "SHA1withRSA", "SHA224withRSA",
            "SHA256withRSA", "SHA384withRSA", "SHA512withRSA"};

    public final static String DEFAULT_SIGNATURE_ALGORITHM = "SHA1withRSA";

    private final static SecureRandom SECURE_RNG;

    static {
        SecureRandom secureRandom = new SecureRandom();
        SECURE_RNG = secureRandom;
        try {
            Cipher.getInstance(DEFAULT_CIPHER_TRANSFORMATION);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        PEM_ENCRYPTOR_BUILDER = new JcePEMEncryptorBuilder("DES-EDE3-CBC").setProvider(provider)
                .setSecureRandom(secureRandom);
    }

    private final static JcePEMEncryptorBuilder PEM_ENCRYPTOR_BUILDER;

    /**
     * Construct the RSA public key from a key string data.
     *
     * @param base64KeyData RSA public key in base64 (base64 of {@link RSAPublicKey#getEncoded()})
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @deprecated since 1.1.0, use {@link #loadPublicKeyFromPem(String)}
     */
    public static RSAPublicKey buildPublicKey(String base64KeyData)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyData = Base64.getDecoder().decode(base64KeyData.replaceAll("[\r\n]+", ""));
        return buildPublicKey(keyData);
    }

    /**
     * Construct the RSA public key from a key binary data.
     *
     * @param keyData RSA public key data (value of {@link RSAPublicKey#getEncoded()})
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @deprecated since 1.1.0, use {@link #loadPublicKeyFromPem(String)}
     */
    public static RSAPublicKey buildPublicKey(byte[] keyData) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyData);
        KeyFactory keyFactory = KeyFactory.getInstance(CIPHER_ALGORITHM);
        PublicKey generatePublic = keyFactory.generatePublic(publicKeySpec);
        return (RSAPublicKey) generatePublic;
    }

    /**
     * Construct the RSA private key from a key binary data.
     *
     * @param keyData RSA private key data (value of {@link RSAPrivateKey#getEncoded()})
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @deprecated since 1.1.0, use {@link #loadPrivateKeyFromPem(String, String)}
     */
    public static RSAPrivateKey buildPrivateKey(final byte[] keyData)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyData);
        KeyFactory keyFactory = KeyFactory.getInstance(CIPHER_ALGORITHM);
        PrivateKey generatePrivate = keyFactory.generatePrivate(privateKeySpec);
        return (RSAPrivateKey) generatePrivate;
    }

    /**
     * Construct the RSA private key from a key string data.
     *
     * @param base64KeyData RSA private key in base64 (base64 of {@link RSAPrivateKey#getEncoded()})
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @deprecated since 1.1.0, use {@link #loadPrivateKeyFromPem(String, String)}
     */
    public static RSAPrivateKey buildPrivateKey(final String base64KeyData)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyData = Base64.getDecoder().decode(base64KeyData.replaceAll("[\r\n]+", ""));
        return buildPrivateKey(keyData);
    }

    /*----------------------------------------------------------------------*/

    /**
     * Sign a message with RSA private key.
     *
     * @param key
     * @param message  the message to sign
     * @param signAlgo signature algorithm to use. If empty, {@link #DEFAULT_SIGNATURE_ALGORITHM} will be
     *                 used.
     * @return the signature
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static byte[] signMessage(RSAPrivateKey key, byte[] message, String signAlgo)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (StringUtils.isBlank(signAlgo)) {
            signAlgo = DEFAULT_SIGNATURE_ALGORITHM;
        }
        Signature sign = Signature.getInstance(signAlgo);
        sign.initSign(key, SECURE_RNG);
        sign.update(message);
        return sign.sign();
    }

    /**
     * Sign a message with RSA private key, using {@link #DEFAULT_SIGNATURE_ALGORITHM}.
     *
     * @param key
     * @param message
     * @return the signature
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     */
    public static byte[] signMessage(RSAPrivateKey key, byte[] message)
            throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        return signMessage(key, message, DEFAULT_SIGNATURE_ALGORITHM);
    }

    /**
     * Sign a message with RSA private key.
     *
     * @param keyData  RSA private key data (value of {@link RSAPrivateKey#getEncoded()})
     * @param message
     * @param signAlgo signature algorithm to use. If empty, {@link #DEFAULT_SIGNATURE_ALGORITHM} will be
     *                 used.
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws SignatureException
     * @deprecated since 1.1.0, use {@link #signMessage(RSAPrivateKey, byte[], String)}
     */
    public static byte[] signMessageWithPrivateKey(byte[] keyData, byte[] message, String signAlgo)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        RSAPrivateKey key = buildPrivateKey(keyData);
        return signMessage(key, message, signAlgo);
    }

    /**
     * Sign a message with RSA private key, using {@link #DEFAULT_SIGNATURE_ALGORITHM}.
     *
     * @param keyData RSA private key data (value of {@link RSAPrivateKey#getEncoded()})
     * @param message
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws SignatureException
     * @deprecated since 1.1.0, use {@link #signMessage(RSAPrivateKey, byte[])}
     */
    public static byte[] signMessageWithPrivateKey(byte[] keyData, byte[] message)
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
        return signMessageWithPrivateKey(keyData, message, DEFAULT_SIGNATURE_ALGORITHM);
    }

    /**
     * Sign a message with RSA private key.
     *
     * @param base64PrivateKeyData RSA private key in base64 (base64 of {@link RSAPrivateKey#getEncoded()})
     * @param message
     * @param signAlgo             signature algorithm to use. If empty, {@link #DEFAULT_SIGNATURE_ALGORITHM} will be
     *                             used.
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws SignatureException
     * @deprecated since 1.1.0, use {@link #signMessage(RSAPrivateKey, byte[], String)}
     */
    public static byte[] signMessageWithPrivateKey(String base64PrivateKeyData, byte[] message, String signAlgo)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        RSAPrivateKey key = buildPrivateKey(base64PrivateKeyData);
        return signMessage(key, message, signAlgo);
    }

    /**
     * Sign a message with RSA private key, using {@link #DEFAULT_SIGNATURE_ALGORITHM}.
     *
     * @param base64PrivateKeyData RSA private key in base64 (base64 of {@link RSAPrivateKey#getEncoded()})
     * @param message
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws SignatureException
     * @deprecated since 1.1.0, use {@link #signMessage(RSAPrivateKey, byte[])}
     */
    public static byte[] signMessageWithPrivateKey(String base64PrivateKeyData, byte[] message)
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
        return signMessageWithPrivateKey(base64PrivateKeyData, message, DEFAULT_SIGNATURE_ALGORITHM);
    }

    /*----------------------------------------------------------------------*/

    /**
     * Verify a signature with RSA public key.
     *
     * @param key
     * @param message   the message to verify
     * @param signature the signature created by {@link #signMessage(RSAPrivateKey, byte[])}
     * @param signAlgo  signature algorithm to use. If empty, {@link #DEFAULT_SIGNATURE_ALGORITHM} will be
     *                  used.
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static boolean verifySignature(RSAPublicKey key, byte[] message, byte[] signature, String signAlgo)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (StringUtils.isBlank(signAlgo)) {
            signAlgo = DEFAULT_SIGNATURE_ALGORITHM;
        }
        Signature sign = Signature.getInstance(signAlgo);
        sign.initVerify(key);
        sign.update(message);
        return sign.verify(signature);
    }

    /**
     * Verify a signature with RSA public key, using {@link #DEFAULT_SIGNATURE_ALGORITHM}.
     *
     * @param key
     * @param message
     * @param signature
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     */
    public static boolean verifySignature(RSAPublicKey key, byte[] message, byte[] signature)
            throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        return verifySignature(key, message, signature, DEFAULT_SIGNATURE_ALGORITHM);
    }

    /**
     * Verify a signature with RSA public key.
     *
     * @param keyData   RSA public key data (value of {@link RSAPublicKey#getEncoded()})
     * @param message
     * @param signature
     * @param signAlgo  signature algorithm to use. If empty, {@link #DEFAULT_SIGNATURE_ALGORITHM} will be
     *                  used.
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws SignatureException
     * @deprecated since 1.1.0, use {@link #verifySignature(RSAPublicKey, byte[], byte[], String)}
     */
    public static boolean verifySignatureWithPublicKey(byte[] keyData, byte[] message, byte[] signature,
                                                       String signAlgo)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        RSAPublicKey key = buildPublicKey(keyData);
        return verifySignature(key, message, signature, signAlgo);
    }

    /**
     * Verify a signature with RSA public key, using {@link #DEFAULT_SIGNATURE_ALGORITHM}.
     *
     * @param keyData   RSA public key data (value of {@link RSAPublicKey#getEncoded()})
     * @param message
     * @param signature
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws SignatureException
     * @deprecated since 1.1.0, use {@link #verifySignature(RSAPublicKey, byte[], byte[])}
     */
    public static boolean verifySignatureWithPublicKey(byte[] keyData, byte[] message, byte[] signature)
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
        return verifySignatureWithPublicKey(keyData, message, signature, DEFAULT_SIGNATURE_ALGORITHM);
    }

    /**
     * Verify a signature with RSA public key.
     *
     * @param base64PublicKeyData RSA public key in base64 (base64 of {@link RSAPublicKey#getEncoded()})
     * @param message
     * @param signature
     * @param signAlgo            signature algorithm to use. If empty, {@link #DEFAULT_SIGNATURE_ALGORITHM} will be
     *                            used.
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws SignatureException
     * @deprecated since 1.1.0, use {@link #verifySignature(RSAPublicKey, byte[], byte[], String)}
     */
    public static boolean verifySignatureWithPublicKey(String base64PublicKeyData, byte[] message, byte[] signature,
                                                       String signAlgo)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        RSAPublicKey key = buildPublicKey(base64PublicKeyData);
        return verifySignature(key, message, signature, signAlgo);
    }

    /**
     * Verify a signature with RSA public key, using {@link #DEFAULT_SIGNATURE_ALGORITHM}.
     *
     * @param base64PublicKeyData RSA public key in base64 (base64 of {@link RSAPublicKey#getEncoded()})
     * @param message
     * @param signature
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws SignatureException
     * @deprecated since 1.1.0, use {@link #verifySignature(RSAPublicKey, byte[], byte[])}
     */
    public static boolean verifySignatureWithPublicKey(String base64PublicKeyData, byte[] message, byte[] signature)
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
        return verifySignatureWithPublicKey(base64PublicKeyData, message, signature, DEFAULT_SIGNATURE_ALGORITHM);
    }
    /*----------------------------------------------------------------------*/

    /**
     * Encrypt data with RSA public key.
     *
     * <p>
     * Note: input data is divided into chunks of
     * {@code size = key's size (in bytes) - paddingSizeInBytes}, so that long input data can be
     * encrypted.
     * </p>
     *
     * @param key
     * @param data
     * @param cipherTransformation cipher-transformation to use. If empty, {@link #DEFAULT_CIPHER_TRANSFORMATION}
     *                             will be used.
     * @param paddingSizeInBytes
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IOException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @see #CIPHER_PADDINGS
     * @see #CIPHER_PADDINGS_SIZE
     */
    public static byte[] encrypt(RSAPublicKey key, byte[] data, String cipherTransformation, int paddingSizeInBytes)
            throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        if (StringUtils.isBlank(cipherTransformation)) {
            cipherTransformation = DEFAULT_CIPHER_TRANSFORMATION;
            paddingSizeInBytes = DEFAULT_PADDING_SIZE;
        }
        Cipher cipher = Cipher.getInstance(cipherTransformation);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        try (ByteArrayInputStream bois = new ByteArrayInputStream(data)) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                int bytesRead;
                byte[] buf = new byte[key.getModulus().bitLength() / 8 - paddingSizeInBytes];
                while ((bytesRead = bois.read(buf)) != -1) {
                    baos.write(cipher.doFinal(buf, 0, bytesRead));
                }
                return baos.toByteArray();
            }
        }
    }

    /**
     * Encrypt data with RSA public key, using {@link #DEFAULT_CIPHER_TRANSFORMATION} and {@link #DEFAULT_PADDING_SIZE}.
     *
     * <p>
     * Note: input data is divided into chunks of
     * {@code size = key's size (in bytes) - paddingSizeInBytes}, so that long input data can be
     * encrypted.
     * </p>
     *
     * @param key
     * @param data
     * @return
     * @throws IOException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static byte[] encrypt(RSAPublicKey key, byte[] data)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException, IOException {
        return encrypt(key, data, DEFAULT_CIPHER_TRANSFORMATION, DEFAULT_PADDING_SIZE);
    }

    /**
     * Encrypt data with RSA public key.
     *
     * <p>
     * Note: input data is divided into chunks of
     * {@code size = key's size (in bytes) - paddingSizeInBytes}, so that long input data can be
     * encrypted.
     * </p>
     *
     * @param base64PublicKeyData  RSA public key in base64 (base64 of {@link RSAPublicKey#getEncoded()})
     * @param data
     * @param cipherTransformation cipher-transformation to use. If empty, {@link #DEFAULT_CIPHER_TRANSFORMATION}
     *                             will be used
     * @param paddingSizeInBytes
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException
     * @see #CIPHER_PADDINGS
     * @see #CIPHER_PADDINGS_SIZE
     * @deprecated since 1.1.0, use {@link #encrypt(RSAPublicKey, byte[], String, int)}
     */
    public static byte[] encryptWithPublicKey(String base64PublicKeyData, byte[] data, String cipherTransformation,
                                              int paddingSizeInBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, IOException {
        RSAPublicKey publicKey = buildPublicKey(base64PublicKeyData);
        return encrypt(publicKey, data, cipherTransformation, paddingSizeInBytes);
    }

    /**
     * Encrypt data with RSA public key, using {@link #DEFAULT_CIPHER_TRANSFORMATION}.
     *
     * <p>
     * Note: input data is divided into chunks of
     * {@code size = key's size (in bytes) - paddingSizeInBytes}, so that long input data can be
     * encrypted.
     * </p>
     *
     * @param base64PublicKeyData RSA public key in base64 (base64 of {@link RSAPublicKey#getEncoded()})
     * @param data
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException
     * @deprecated since 1.1.0, use {@link #encrypt(RSAPublicKey, byte[])}
     */
    public static byte[] encryptWithPublicKey(String base64PublicKeyData, byte[] data)
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, IOException {
        return encryptWithPublicKey(base64PublicKeyData, data, DEFAULT_CIPHER_TRANSFORMATION, DEFAULT_PADDING_SIZE);
    }

    /**
     * Encrypt data with RSA public key.
     *
     * <p>
     * Note: input data is divided into chunks of
     * {@code size = key's size (in bytes) - paddingSizeInBytes}, so that long input data can be
     * encrypted.
     * </p>
     *
     * @param publicKeyData        RSA public key data (value of {@link RSAPublicKey#getEncoded()})
     * @param data
     * @param cipherTransformation cipher-transformation to use. If empty, {@link #DEFAULT_CIPHER_TRANSFORMATION}
     *                             will be used
     * @param paddingSizeInBytes
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException
     * @see #CIPHER_PADDINGS
     * @see #CIPHER_PADDINGS_SIZE
     * @deprecated since 1.1.0, use {@link #encrypt(RSAPublicKey, byte[], String, int)}
     */
    public static byte[] encryptWithPublicKey(byte[] publicKeyData, byte[] data, String cipherTransformation,
                                              int paddingSizeInBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, IOException {
        RSAPublicKey publicKey = buildPublicKey(publicKeyData);
        return encrypt(publicKey, data, cipherTransformation, paddingSizeInBytes);
    }

    /**
     * Encrypt data with RSA public key, using {@link #DEFAULT_CIPHER_TRANSFORMATION}.
     *
     * <p>
     * Note: input data is divided into chunks of
     * {@code size = key's size (in bytes) - paddingSizeInBytes}, so that long input data can be
     * encrypted.
     * </p>
     *
     * @param publicKeyData RSA public key data (value of {@link RSAPublicKey#getEncoded()})
     * @param data
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException
     * @deprecated since 1.1.0, use {@link #encrypt(RSAPublicKey, byte[])}
     */
    public static byte[] encryptWithPublicKey(byte[] publicKeyData, byte[] data)
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, IOException {
        return encryptWithPublicKey(publicKeyData, data, DEFAULT_CIPHER_TRANSFORMATION, DEFAULT_PADDING_SIZE);
    }

    /*----------------------------------------------------------------------*/

    /**
     * Decrypt encrypted data with RSA private key.
     *
     * <p>
     * Note: if long data was encrypted using
     * {@link #encrypt(RSAPublicKey, byte[], String, int)}, it will be correctly decrypted.
     * </p>
     *
     * @param key
     * @param encryptedData
     * @param cipherTransformation cipher-transformation to use. If empty, {@link #DEFAULT_CIPHER_TRANSFORMATION}
     *                             will be used
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IOException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static byte[] decrypt(RSAPrivateKey key, byte[] encryptedData, String cipherTransformation)
            throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(
                StringUtils.isBlank(cipherTransformation) ? DEFAULT_CIPHER_TRANSFORMATION : cipherTransformation);
        cipher.init(Cipher.DECRYPT_MODE, key);
        try (ByteArrayInputStream bois = new ByteArrayInputStream(encryptedData)) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                int bytesRead;
                byte[] buf = new byte[key.getModulus().bitLength() / 8];
                while ((bytesRead = bois.read(buf)) != -1) {
                    byte[] buff = cipher.doFinal(buf, 0, bytesRead);
                    if ("RSA/ECB/NoPadding".equals(cipherTransformation)) {
                        // remove leading zeros
                        int index = 0;
                        while (index < buff.length && buff[index] == 0)
                            index++;
                        if (index >= buff.length) {
                            buff = ArrayUtils.EMPTY_BYTE_ARRAY;
                        } else {
                            buff = Arrays.copyOfRange(buff, index, buff.length);
                        }
                    }
                    baos.write(buff);
                }
                return baos.toByteArray();
            }
        }
    }

    /**
     * Decrypt encrypted data with RSA private key, using {@link #DEFAULT_CIPHER_TRANSFORMATION}.
     *
     * <p>
     * Note: if long data was encrypted using
     * {@link #encrypt(RSAPublicKey, byte[])}, it will be correctly decrypted.
     * </p>
     *
     * @param key
     * @param encryptedData
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IOException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static byte[] decrypt(RSAPrivateKey key, byte[] encryptedData)
            throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        return decrypt(key, encryptedData, DEFAULT_CIPHER_TRANSFORMATION);
    }

    /**
     * Decrypt encrypted data with RSA private key.
     *
     * <p>
     * Note: if long data was encrypted using
     * {@link #encryptWithPublicKey(byte[], byte[], String, int)}, it will be correctly decrypted.
     * </p>
     *
     * @param privateKeyData       RSA private key data (value of {@link RSAPrivateKey#getEncoded()})
     * @param encryptedData
     * @param cipherTransformation cipher-transformation to use. If empty, {@link #DEFAULT_CIPHER_TRANSFORMATION}
     *                             will be used
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException
     * @deprecated since 1.1.0, use {@link #decrypt(RSAPrivateKey, byte[], String)}
     */
    public static byte[] decryptWithPrivateKey(byte[] privateKeyData, byte[] encryptedData, String cipherTransformation)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, IOException {
        RSAPrivateKey privateKey = buildPrivateKey(privateKeyData);
        return decrypt(privateKey, encryptedData, cipherTransformation);
    }

    /**
     * Decrypt encrypted data with RSA private key, using {@link #DEFAULT_CIPHER_TRANSFORMATION}.
     *
     * <p>
     * Note: if long data was encrypted using
     * {@link #encryptWithPublicKey(byte[], byte[])}, it will be correctly decrypted.
     * </p>
     *
     * @param privateKeyData RSA private key data (value of {@link RSAPrivateKey#getEncoded()})
     * @param encryptedData
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException
     * @deprecated since 1.1.0, use {@link #decrypt(RSAPrivateKey, byte[])}
     */
    public static byte[] decryptWithPrivateKey(byte[] privateKeyData, byte[] encryptedData)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, IOException {
        return decryptWithPrivateKey(privateKeyData, encryptedData, DEFAULT_CIPHER_TRANSFORMATION);
    }

    /**
     * Decrypt encrypted data with RSA private key.
     *
     * <p>
     * Note: if long data was encrypted using
     * {@link #encryptWithPublicKey(String, byte[], String, int)}, it will be correctly decrypted.
     * </p>
     *
     * @param base64PrivateKeyData RSA private key in base64 (base64 of {@link RSAPrivateKey#getEncoded()})
     * @param encryptedData
     * @param cipherTransformation cipher-transformation to use. If empty, {@link #DEFAULT_CIPHER_TRANSFORMATION}
     *                             will be used
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException
     * @deprecated since 1.1.0, use {@link #decrypt(RSAPrivateKey, byte[], String)}
     */
    public static byte[] decryptWithPrivateKey(String base64PrivateKeyData, byte[] encryptedData,
                                               String cipherTransformation)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, IOException {
        RSAPrivateKey privateKey = buildPrivateKey(base64PrivateKeyData);
        return decrypt(privateKey, encryptedData, cipherTransformation);
    }

    /**
     * Decrypt encrypted data with RSA private key, using {@link #DEFAULT_CIPHER_TRANSFORMATION}.
     *
     * <p>
     * Note: if long data was encrypted using
     * {@link #encryptWithPublicKey(String, byte[])}, it will be correctly decrypted.
     * </p>
     *
     * @param base64PrivateKeyData RSA private key in base64 (base64 of {@link RSAPrivateKey#getEncoded()})
     * @param encryptedData
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException
     * @deprecated since 1.1.0, use {@link #decrypt(RSAPrivateKey, byte[], String)}
     */
    public static byte[] decryptWithPrivateKey(String base64PrivateKeyData, byte[] encryptedData)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, IOException {
        return decryptWithPrivateKey(base64PrivateKeyData, encryptedData, DEFAULT_CIPHER_TRANSFORMATION);
    }

    /*----------------------------------------------------------------------*/

    /**
     * Generate a random RSA key pair.
     *
     * @param keySizeInBits key's length in bits, should be power of 2
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static KeyPair generateKeys(int keySizeInBits) throws NoSuchAlgorithmException, NoSuchProviderException {
        return KeyPairUtils.generateKeyPair(keySizeInBits, CIPHER_ALGORITHM, null);
    }

    /**
     * Export a RSA public key to PEM (base64) X.509 format.
     *
     * @param pubKey
     * @param withType if {@code true}, the exported key is wrapped inside "-----BEGIN PUBLIC KEY-----" / "-----END PUBLIC KEY-----" block.
     * @return
     * @since 1.1.0
     */
    public static String exportPemX509(RSAPublicKey pubKey, boolean withType) {
        return KeyPairUtils.exportPemX509(pubKey, withType);
    }

    /**
     * Export a RSA public key to PEM (base64) PKCS#1 format.
     *
     * @param pubKey
     * @param withType if {@code true}, the exported key is wrapped inside "-----BEGIN RSA PUBLIC KEY-----" / "-----END RSA PUBLIC KEY-----" block.
     * @return
     * @throws IOException
     * @since 1.1.0
     */
    public static String exportPemPKCS1(RSAPublicKey pubKey, boolean withType) throws IOException {
        ASN1Primitive primitive = SubjectPublicKeyInfo.getInstance(pubKey.getEncoded()).parsePublicKey();
        PemObject pemObj = new PemObject(KeyPairUtils.PEM_TYPE_RSA_PKCS1_PUBLIC_KEY, primitive.getEncoded());
        return KeyPairUtils.exportPem(pemObj, withType);
    }

    /**
     * Export a RSA private key to PEM (base64) PKCS#8 format.
     *
     * @param privKey
     * @param password if not empty, the exported key is encrypted with specified password; otherwise the export key is unencrypted
     * @param withType if {@code true}, the exported key is wrapped inside "-----BEGIN PRIVATE KEY-----" / "-----END PRIVATE KEY-----" block.
     * @return
     * @throws IOException
     * @throws OperatorCreationException
     * @since 1.1.0
     */
    public static String exportPemPKCS8(RSAPrivateKey privKey, String password, boolean withType)
            throws IOException, OperatorCreationException {
        return KeyPairUtils.exportPemPKCS8(privKey, password, withType);
    }

    /**
     * Export a RSA private key to PEM (base64) PKCS#1 format.
     *
     * @param privKey
     * @param password if not empty, the exported key is encrypted with specified password; otherwise the export key is unencrypted
     * @param withType if {@code true}, the exported key is wrapped inside "-----BEGIN RSA PRIVATE KEY-----" / "-----END RSA PRIVATE KEY-----" block.
     * @return
     * @throws IOException
     * @since 1.1.0
     */
    public static String exportPemPKCS1(RSAPrivateKey privKey, String password, boolean withType) throws IOException {
        byte[] privKeyBytes = privKey.getEncoded();
        PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privKeyBytes);
        ASN1Encodable encodable = pkInfo.parsePrivateKey();
        ASN1Primitive primitive = encodable.toASN1Primitive();
        if (StringUtils.isEmpty(password)) {
            return KeyPairUtils
                    .exportPem(new PemObject(KeyPairUtils.PEM_TYPE_RSA_PKCS1_PRRIVATE_KEY, primitive.getEncoded()),
                            withType);
        }

        JcaMiscPEMGenerator pemGenerator = new JcaMiscPEMGenerator(privKey,
                PEM_ENCRYPTOR_BUILDER.build(password.trim().toCharArray()));
        return KeyPairUtils.exportPem(pemGenerator.generate(), withType);
    }

    /**
     * Convert the key data to base64 string.
     *
     * @param key
     * @return base64 of {@link Key#getEncoded()}
     * @throws IOException
     * @throws OperatorCreationException
     * @deprecated since 1.1.0, use specific method {@link #exportPemX509(RSAPublicKey, boolean)}
     * or {@link #exportPemPKCS1(RSAPublicKey, boolean)}
     * or {@link #exportPemPKCS8(RSAPrivateKey, String, boolean)}
     * or {@link #exportPemPKCS1(RSAPrivateKey, String, boolean)}
     */
    public static String toBase64(Key key) throws IOException, OperatorCreationException {
        if (key == null) {
            throw new NullPointerException();
        }
        if (key instanceof RSAPublicKey) {
            return exportPemX509((RSAPublicKey) key, false);
        }
        if (key instanceof RSAPrivateKey) {
            return exportPemPKCS8((RSAPrivateKey) key, null, false);
        }
        throw new IllegalArgumentException(key.getClass().toString());
    }

    /**
     * Extract the private key data from a keypair as base64 string.
     *
     * @param keyPair
     * @return
     * @throws IOException
     * @throws OperatorCreationException
     * @deprecated since 1.1.0, use specific method {@link #exportPemX509(RSAPublicKey, boolean)}
     * or {@link #exportPemPKCS1(RSAPublicKey, boolean)}
     */
    public static String extractPrivateKeyAsBase64(KeyPair keyPair) throws IOException, OperatorCreationException {
        return toBase64(keyPair.getPrivate());
    }

    /**
     * Extract the public key data from a keypair as base64 string.
     *
     * @param keyPair
     * @return
     * @throws IOException
     * @throws OperatorCreationException
     * @deprecated since 1.1.0, use specific method {@link #exportPemPKCS8(RSAPrivateKey, String, boolean)}
     * or {@link #exportPemPKCS1(RSAPrivateKey, String, boolean)}
     */
    public static String extractPublicKeyAsBase64(KeyPair keyPair) throws IOException, OperatorCreationException {
        return toBase64(keyPair.getPublic());
    }

    /*----------------------------------------------------------------------*/

    /**
     * Load a RSA public key from PEM data.
     *
     * <ul>
     * <li>Support {@code X.509} and {@code PKCS#1} public key.</li>
     * <li>If PEM data has no type header, this method assumes the key is {@code X.509}.</li>
     * </ul>
     *
     * @param pemData
     * @return
     * @throws IOException
     * @since 1.1.0
     */
    public static RSAPublicKey loadPublicKeyFromPem(String pemData) throws IOException {
        if (!pemData.startsWith("-----BEGIN ")) {
            //no type header: assuming X.509 format
            return loadPublicKeyFromPem("-----BEGIN PUBLIC KEY-----\n" + pemData + "\n-----END PUBLIC KEY-----");
        }
        PublicKey pubKey = KeyPairUtils.loadPublicKeyFromPem(pemData);
        return (RSAPublicKey) pubKey;
    }

    /**
     * Load a RSA private key from PEM data.
     *
     * <ul>
     * <li>Support {@code PKCS#8} and {@code PKCS#1} private key.</li>
     * <li>If PEM data has no type header, this method assumes the key is {@code PKCS#8}.</li>
     * </ul>
     *
     * @param pemData
     * @param password
     * @return
     * @throws IOException
     * @throws PKCSException
     * @throws OperatorCreationException
     * @since 1.1.0
     */
    public static RSAPrivateKey loadPrivateKeyFromPem(String pemData, String password)
            throws IOException, PKCSException, OperatorCreationException {
        if (!pemData.startsWith("-----BEGIN ")) {
            //no type header: assuming PKCS#8 format
            return loadPrivateKeyFromPem("-----BEGIN PRIVATE KEY-----\n" + pemData + "\n-----END PRIVATE KEY-----",
                    password);
        }
        PrivateKey privKey = KeyPairUtils.loadPrivateKeyFromPem(pemData, password);
        return (RSAPrivateKey) privKey;
    }
}
