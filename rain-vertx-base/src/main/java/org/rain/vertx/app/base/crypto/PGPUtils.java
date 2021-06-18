package org.rain.vertx.app.base.crypto;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.bcpg.*;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.jcajce.JcaPGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.jcajce.JcaPGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.PGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.jcajce.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PGP encryption/signature utility class.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 1.1.0
 */
public class PGPUtils {
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private final static int DEFAULT_KEY_ENCRYPTION_ALGORITHM = SymmetricKeyAlgorithmTags.AES_256;

    /**
     * Export a key ring data.
     *
     * @param keyRing
     * @param asciiArmor if {@code true}, data is exported as ASCII-armor format; otherwise key ring data
     *                   is encoded as base64 string
     * @return
     * @throws IOException
     */
    public static String exportKeyRing(PGPKeyRing keyRing, boolean asciiArmor) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (asciiArmor) {
                try (ArmoredOutputStream aos = new ArmoredOutputStream(baos)) {
                    keyRing.encode(aos);
                }
            } else {
                keyRing.encode(baos);
            }
            baos.flush();
            byte[] data = baos.toByteArray();
            return asciiArmor ? new String(data, StandardCharsets.UTF_8) : Base64.getEncoder().encodeToString(data);
        }
    }

    /**
     * Export a secret key data.
     *
     * @param pgpKeyRingGen
     * @param asciiArmor    if {@code true}, data is exported as ASCII-armor format; otherwise key data is
     *                      encoded as base64 string
     * @return
     * @throws IOException
     */
    public static String exportSecretKey(PGPKeyRingGenerator pgpKeyRingGen, boolean asciiArmor) throws IOException {
        return exportKeyRing(pgpKeyRingGen.generateSecretKeyRing(), asciiArmor);
    }

    /**
     * Export a public key data.
     *
     * @param pgpKeyRingGen
     * @param asciiArmor    if {@code true}, data is exported as ASCII-armor format; otherwise key data is
     *                      encoded as base64 string
     * @return
     * @throws IOException
     */
    public static String exportPublicKey(PGPKeyRingGenerator pgpKeyRingGen, boolean asciiArmor) throws IOException {
        return exportKeyRing(pgpKeyRingGen.generatePublicKeyRing(), asciiArmor);
    }

    private static PGPKeyPair buildMasterKey(KeyPair keyPair) throws PGPException {
        String algoName = keyPair.getPublic().getAlgorithm();
        if ("RSA".equalsIgnoreCase(algoName)) {
            return new JcaPGPKeyPair(PGPPublicKey.RSA_SIGN, keyPair, new Date());
        }
        if ("DSA".equalsIgnoreCase(algoName)) {
            return new JcaPGPKeyPair(PGPPublicKey.DSA, keyPair, new Date());
        }
        if ("ElGamal".equalsIgnoreCase(algoName)) {
            return new JcaPGPKeyPair(PGPPublicKey.ELGAMAL_GENERAL, keyPair, new Date());
        }
        throw new PGPException("Unsupported algorithm for master key [" + algoName + "]");
    }

    private static PGPKeyPair buildEncryptionKey(KeyPair keyPair) throws PGPException {
        String algoName = keyPair.getPublic().getAlgorithm();
        if ("RSA".equalsIgnoreCase(algoName)) {
            return new JcaPGPKeyPair(PGPPublicKey.RSA_ENCRYPT, keyPair, new Date());
        }
        if ("ElGamal".equalsIgnoreCase(algoName)) {
            return new JcaPGPKeyPair(PGPPublicKey.ELGAMAL_ENCRYPT, keyPair, new Date());
        }
        throw new PGPException("Unsupported algorithm for encryption key [" + algoName + "]");
    }

    /*
     * See: https://github.com/codesandnotes/openpgp-integration/blob/master/src/main/java/be/
     * codesandnotes/OpenPGP.java#L111
     */
    private static PGPSignatureSubpacketVector buildEncryptionKeySignature() {
        PGPSignatureSubpacketGenerator encryptionKeySignatureGenerator = new PGPSignatureSubpacketGenerator();
        encryptionKeySignatureGenerator.setKeyFlags(false, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);
        return encryptionKeySignatureGenerator.generate();
    }

    /*
     * See: https://github.com/codesandnotes/openpgp-integration/blob/master/src/main/java/be/
     * codesandnotes/OpenPGP.java#L117
     */
    private static PGPSignatureSubpacketVector buildSigningKeySignature() {
        PGPSignatureSubpacketGenerator signingKeySignatureGenerator = new PGPSignatureSubpacketGenerator();
        signingKeySignatureGenerator.setKeyFlags(false, KeyFlags.SIGN_DATA | KeyFlags.CERTIFY_OTHER);
        signingKeySignatureGenerator
                .setPreferredSymmetricAlgorithms(false, new int[]{DEFAULT_KEY_ENCRYPTION_ALGORITHM});
        signingKeySignatureGenerator.setPreferredHashAlgorithms(false, new int[]{HashAlgorithmTags.SHA512});
        signingKeySignatureGenerator.setFeature(false, Features.FEATURE_MODIFICATION_DETECTION);
        return signingKeySignatureGenerator.generate();
    }

    /**
     * Generate a {@link PGPKeyRingGenerator} with an encryption sub-key, using RSA for both master
     * and encryption sub-key.
     *
     * @param keySizeInBits
     * @param identity
     * @param password
     * @return
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws PGPException
     */
    public static PGPKeyRingGenerator createPGPKeyRingGenerator(int keySizeInBits, String identity, String password)
            throws NoSuchProviderException, NoSuchAlgorithmException, PGPException {
        return createPGPKeyRingGenerator(KeyPairUtils.generateKeyPair(keySizeInBits, "RSA", null),
                KeyPairUtils.generateKeyPair(keySizeInBits, "RSA", null), identity, password);
    }

    /**
     * Generate a {@link PGPKeyRingGenerator} with an encryption sub-key.
     *
     * @param masterKeyPair
     * @param encryptionSubKeyPair
     * @param identity
     * @param password
     * @return
     * @throws PGPException
     */
    public static PGPKeyRingGenerator createPGPKeyRingGenerator(KeyPair masterKeyPair, KeyPair encryptionSubKeyPair,
                                                                String identity, String password) throws PGPException {
        PGPKeyPair masterKey = buildMasterKey(masterKeyPair);
        PGPDigestCalculator checksumCalculator = new JcaPGPDigestCalculatorProviderBuilder().build()
                .get(HashAlgorithmTags.SHA1);
        PGPContentSignerBuilder keySignerBuilder = new JcaPGPContentSignerBuilder(
                masterKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256);
        PBESecretKeyEncryptor keyEncryptor = new JcePBESecretKeyEncryptorBuilder(DEFAULT_KEY_ENCRYPTION_ALGORITHM)
                .build(password.toCharArray());

        PGPKeyRingGenerator keyRingGenerator = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, masterKey,
                identity, checksumCalculator, buildSigningKeySignature(), null, keySignerBuilder, keyEncryptor);

        PGPKeyPair encryptionSubKey = buildEncryptionKey(encryptionSubKeyPair);
        keyRingGenerator.addSubKey(encryptionSubKey, buildEncryptionKeySignature(), null);

        return keyRingGenerator;
    }

    /*----------------------------------------------------------------------*/
    private static PGPPublicKey extractFirstPublicKey(Collection<PGPPublicKey> pubKeyList, boolean encryptionOnly,
                                                      boolean acceptMasterKey) {
        if (pubKeyList != null) {
            for (PGPPublicKey pubKey : pubKeyList) {
                if ((acceptMasterKey || !pubKey.isMasterKey()) && (!encryptionOnly || pubKey.isEncryptionKey())) {
                    return pubKey;
                }
            }
        }
        return null;
    }

    /*----------------------------------------------------------------------*/

    /**
     * Extract all public keys from a key ring.
     *
     * @param keyRing
     * @return
     */
    public static List<PGPPublicKey> extractPublicKeys(PGPSecretKeyRing keyRing) {
        List<PGPPublicKey> result = new ArrayList<>();
        if (keyRing != null) {
            keyRing.getPublicKeys().forEachRemaining(pubKey -> result.add(pubKey));
        }
        return result;
    }

    /**
     * Extract public keys matching filter from a key ring.
     *
     * @param keyRing
     * @param encryptionOnly {@code true} to extract only encryption keys; otherwise, all keys
     * @return
     */
    public static List<PGPPublicKey> extractPublicKeys(PGPSecretKeyRing keyRing, boolean encryptionOnly) {
        return extractPublicKeys(keyRing).stream().filter(pubKey -> !encryptionOnly || pubKey.isEncryptionKey())
                .collect(Collectors.toList());
    }

    /**
     * Extract the first public key matching filter from a key ring.
     *
     * @param keyRing
     * @param encryptionOnly  {@code true} to extract only encryption key; otherwise, any key
     * @param acceptMasterKey {@code true} if master key can be returned; otherwise, the returned key is sub-key
     * @return
     */
    public static PGPPublicKey extractFirstPublicKey(PGPSecretKeyRing keyRing, boolean encryptionOnly,
                                                     boolean acceptMasterKey) {
        return extractFirstPublicKey(extractPublicKeys(keyRing), encryptionOnly, acceptMasterKey);
    }

    /**
     * Extract all public keys from a key ring collection.
     *
     * @param keyRingCollection
     * @return
     */
    public static List<PGPPublicKey> extractPublicKeys(PGPSecretKeyRingCollection keyRingCollection) {
        List<PGPPublicKey> result = new ArrayList<>();
        if (keyRingCollection != null) {
            keyRingCollection.getKeyRings().forEachRemaining(keyRing -> result.addAll(extractPublicKeys(keyRing)));
        }
        return result;
    }

    /**
     * Extract public keys matching filter from a key ring collection.
     *
     * @param keyRingCollection
     * @param encryptionOnly    {@code true} to extract only encryption keys; otherwise, all keys
     * @return
     */
    public static List<PGPPublicKey> extractPublicKeys(PGPSecretKeyRingCollection keyRingCollection,
                                                       boolean encryptionOnly) {
        return extractPublicKeys(keyRingCollection).stream()
                .filter(pubKey -> !encryptionOnly || pubKey.isEncryptionKey()).collect(Collectors.toList());
    }

    /**
     * Extract the first public key matching filter from a key ring collection.
     *
     * @param keyRingCollection
     * @param encryptionOnly    {@code true} to extract only encryption key; otherwise, any key
     * @param acceptMasterKey   {@code true} if master key can be returned; otherwise, the returned key is sub-key
     * @return
     */
    public static PGPPublicKey extractFirstPublicKey(PGPSecretKeyRingCollection keyRingCollection,
                                                     boolean encryptionOnly, boolean acceptMasterKey) {
        return extractFirstPublicKey(extractPublicKeys(keyRingCollection), encryptionOnly, acceptMasterKey);
    }

    /*----------------------------------------------------------------------*/

    /**
     * Extract all public keys from a key ring.
     *
     * @param keyRing
     * @return
     */
    public static List<PGPPublicKey> extractPublicKeys(PGPPublicKeyRing keyRing) {
        List<PGPPublicKey> result = new ArrayList<>();
        if (keyRing != null) {
            keyRing.getPublicKeys().forEachRemaining(pubKey -> result.add(pubKey));
        }
        return result;
    }

    /**
     * Extract public keys matching filter from a key ring.
     *
     * @param keyRing
     * @param encryptionOnly {@code true} to extract only encryption keys; otherwise, all keys
     * @return
     */
    public static List<PGPPublicKey> extractPublicKeys(PGPPublicKeyRing keyRing, boolean encryptionOnly) {
        return extractPublicKeys(keyRing).stream().filter(pubKey -> !encryptionOnly || pubKey.isEncryptionKey())
                .collect(Collectors.toList());
    }

    /**
     * Extract the first public key matching filter from a key ring.
     *
     * @param keyRing
     * @param encryptionOnly  {@code true} to extract only encryption key; otherwise, any key
     * @param acceptMasterKey {@code true} if master key can be returned; otherwise, the returned key is sub-key
     * @return
     */
    public static PGPPublicKey extractFirstPublicKey(PGPPublicKeyRing keyRing, boolean encryptionOnly,
                                                     boolean acceptMasterKey) {
        return extractFirstPublicKey(extractPublicKeys(keyRing), encryptionOnly, acceptMasterKey);
    }

    /**
     * Extract all public keys from a key ring collection.
     *
     * @param keyRingCollection
     * @return
     */
    public static List<PGPPublicKey> extractPublicKeys(PGPPublicKeyRingCollection keyRingCollection) {
        List<PGPPublicKey> result = new ArrayList<>();
        if (keyRingCollection != null) {
            keyRingCollection.getKeyRings().forEachRemaining(keyRing -> result.addAll(extractPublicKeys(keyRing)));
        }
        return result;
    }

    /**
     * Extract public keys matching filter from a key ring collection.
     *
     * @param keyRingCollection
     * @param encryptionOnly    {@code true} to extract only encryption keys; otherwise, all keys
     * @return
     */
    public static List<PGPPublicKey> extractPublicKeys(PGPPublicKeyRingCollection keyRingCollection,
                                                       boolean encryptionOnly) {
        return extractPublicKeys(keyRingCollection).stream()
                .filter(pubKey -> !encryptionOnly || pubKey.isEncryptionKey()).collect(Collectors.toList());
    }

    /**
     * Extract the first public key matching filter from a key ring collection.
     *
     * @param keyRingCollection
     * @param encryptionOnly    {@code true} to extract only encryption key; otherwise, any key
     * @param acceptMasterKey   {@code true} if master key can be returned; otherwise, the returned key is sub-key
     * @return
     */
    public static PGPPublicKey extractFirstPublicKey(PGPPublicKeyRingCollection keyRingCollection,
                                                     boolean encryptionOnly, boolean acceptMasterKey) {
        return extractFirstPublicKey(extractPublicKeys(keyRingCollection), encryptionOnly, acceptMasterKey);
    }

    /*----------------------------------------------------------------------*/

    /**
     * Load a {@link PGPPublicKeyRingCollection} from an external data source.
     *
     * @param keyRingData key ring data in base64 or ASCII-armor format
     * @param asciiArmor  {@code true} if data is ASCII-armor format; otherwise, base64
     * @return
     * @throws IOException
     * @throws PGPException
     */
    public static PGPPublicKeyRingCollection loadPublicKeyRingCollection(String keyRingData, boolean asciiArmor)
            throws IOException, PGPException {
        ByteArrayInputStream baos = new ByteArrayInputStream(
                asciiArmor ? keyRingData.getBytes(StandardCharsets.UTF_8) : Base64.getDecoder().decode(keyRingData));
        try (InputStream is = asciiArmor ? new ArmoredInputStream(baos) : baos) {
            return new JcaPGPPublicKeyRingCollection(is);
        }
    }

    /**
     * Load all public keys from a key ring.
     *
     * @param keyRingData key ring data in base64 or ASCII-armor format
     * @param asciiArmor  {@code true} if data is ASCII-armor format; otherwise, base64
     * @return
     * @throws IOException
     * @throws PGPException
     */
    public static List<PGPPublicKey> loadPublicKeys(String keyRingData, boolean asciiArmor)
            throws IOException, PGPException {
        return extractPublicKeys(loadPublicKeyRingCollection(keyRingData, asciiArmor));
    }

    /**
     * Load public keys matching filter from a key ring.
     *
     * @param keyRingData    key ring data in base64 or ASCII-armor format
     * @param asciiArmor     {@code true} if data is ASCII-armor format; otherwise, base64
     * @param encryptionOnly {@code true} to load only encryption keys; otherwise, all keys
     * @return
     * @throws IOException
     * @throws PGPException
     */
    public static List<PGPPublicKey> loadPublicKeys(String keyRingData, boolean asciiArmor, boolean encryptionOnly)
            throws IOException, PGPException {
        return loadPublicKeys(keyRingData, asciiArmor).stream()
                .filter(pubKey -> !encryptionOnly || pubKey.isEncryptionKey()).collect(Collectors.toList());
    }

    /**
     * Load the first public key matching filter from a key ring.
     *
     * @param keyRingData     key ring data in base64 or ASCII-armor format
     * @param asciiArmor      {@code true} if data is ASCII-armor format; otherwise, base64
     * @param encryptionOnly  {@code true} to load only encryption key; otherwise, any key
     * @param acceptMasterKey {@code true} if master key can be returned; otherwise, the returned key is sub-key
     * @return
     * @throws IOException
     * @throws PGPException
     */
    public static PGPPublicKey loadFirstPublicKey(String keyRingData, boolean asciiArmor, boolean encryptionOnly,
                                                  boolean acceptMasterKey) throws IOException, PGPException {
        return extractFirstPublicKey(loadPublicKeys(keyRingData, asciiArmor), encryptionOnly, acceptMasterKey);
    }

    /*----------------------------------------------------------------------*/
    private static PGPSecretKey extractFirstSecretKey(Collection<PGPSecretKey> secKeyList, boolean signingOnly,
                                                      boolean acceptMasterKey) {
        if (secKeyList != null) {
            for (PGPSecretKey secKey : secKeyList) {
                if ((acceptMasterKey || !secKey.isMasterKey()) && (!signingOnly || secKey.isSigningKey())) {
                    return secKey;
                }
            }
        }
        return null;
    }

    /**
     * Extract all secret keys from a key ring.
     *
     * @param keyRing
     * @return
     */
    public static List<PGPSecretKey> extractSecretKeys(PGPSecretKeyRing keyRing) {
        List<PGPSecretKey> result = new ArrayList<>();
        if (keyRing != null) {
            keyRing.getSecretKeys().forEachRemaining(secKey -> result.add(secKey));
        }
        return result;
    }

    /**
     * Extract secret keys matching filter from a key ring.
     *
     * @param keyRing
     * @param signingOnly {@code true} to extract only signing keys; otherwise, all keys
     * @return
     */
    public static List<PGPSecretKey> extractSecretKeys(PGPSecretKeyRing keyRing, boolean signingOnly) {
        return extractSecretKeys(keyRing).stream().filter(pubKey -> !signingOnly || pubKey.isSigningKey())
                .collect(Collectors.toList());
    }

    /**
     * Extract the first secret key matching filter from a key ring.
     *
     * @param keyRing
     * @param signingOnly     {@code true} to extract only signing key; otherwise, any key
     * @param acceptMasterKey {@code true} if master key can be returned; otherwise, the returned key is sub-key
     * @return
     */
    public static PGPSecretKey extractFirstSecretKey(PGPSecretKeyRing keyRing, boolean signingOnly,
                                                     boolean acceptMasterKey) {
        return extractFirstSecretKey(extractSecretKeys(keyRing), signingOnly, acceptMasterKey);
    }

    /**
     * Extract all secret keys from a key ring collection.
     *
     * @param keyRingCollection
     * @return
     */
    public static List<PGPSecretKey> extractSecretKeys(PGPSecretKeyRingCollection keyRingCollection) {
        List<PGPSecretKey> result = new ArrayList<>();
        if (keyRingCollection != null) {
            keyRingCollection.getKeyRings().forEachRemaining(keyRing -> result.addAll(extractSecretKeys(keyRing)));
        }
        return result;
    }

    /**
     * Extract secret keys matching filter from a key ring collection.
     *
     * @param keyRingCollection
     * @param signingOnly       {@code true} to extract only signing keys; otherwise, all keys
     * @return
     */
    public static List<PGPSecretKey> extractSecretKeys(PGPSecretKeyRingCollection keyRingCollection,
                                                       boolean signingOnly) {
        return extractSecretKeys(keyRingCollection).stream().filter(secKey -> !signingOnly || secKey.isSigningKey())
                .collect(Collectors.toList());
    }

    /**
     * Extract the first secret key matching filter from a key ring collection.
     *
     * @param keyRingCollection
     * @param signingOnly       {@code true} to extract only signing key; otherwise, any key
     * @param acceptMasterKey   {@code true} if master key can be returned; otherwise, the returned key is sub-key
     * @return
     */
    public static PGPSecretKey extractFirstSecretKey(PGPSecretKeyRingCollection keyRingCollection, boolean signingOnly,
                                                     boolean acceptMasterKey) {
        return extractFirstSecretKey(extractSecretKeys(keyRingCollection), signingOnly, acceptMasterKey);
    }

    /**
     * Load a {@link PGPSecretKeyRingCollection} from an external data source.
     *
     * @param keyRingData key ring data in base64 or ASCII-armor format
     * @param asciiArmor  {@code true} if data is ASCII-armor format; otherwise, base64
     * @return
     * @throws IOException
     * @throws PGPException
     */
    public static PGPSecretKeyRingCollection loadSecretKeyRingCollection(String keyRingData, boolean asciiArmor)
            throws IOException, PGPException {
        ByteArrayInputStream baos = new ByteArrayInputStream(
                asciiArmor ? keyRingData.getBytes(StandardCharsets.UTF_8) : Base64.getDecoder().decode(keyRingData));
        try (InputStream is = asciiArmor ? new ArmoredInputStream(baos) : baos) {
            return new JcaPGPSecretKeyRingCollection(is);
        }
    }

    /**
     * Load all secret keys from a key ring.
     *
     * @param keyRingData key ring data in base64 or ASCII-armor format
     * @param asciiArmor  {@code true} if data is ASCII-armor format; otherwise, base64
     * @return
     * @throws IOException
     * @throws PGPException
     */
    public static List<PGPSecretKey> loadSecretKeys(String keyRingData, boolean asciiArmor)
            throws IOException, PGPException {
        return extractSecretKeys(loadSecretKeyRingCollection(keyRingData, asciiArmor));
    }

    /**
     * Load all secret keys from a key ring.
     *
     * @param keyRingData key ring data in base64 or ASCII-armor format
     * @param asciiArmor  {@code true} if data is ASCII-armor format; otherwise, base64
     * @param signingOnly {@code true} to load only signing keys; otherwise, all keys
     * @return
     * @throws IOException
     * @throws PGPException
     */
    public static List<PGPSecretKey> loadSecretKeys(String keyRingData, boolean asciiArmor, boolean signingOnly)
            throws IOException, PGPException {
        return loadSecretKeys(keyRingData, asciiArmor).stream().filter(secKey -> !signingOnly || secKey.isSigningKey())
                .collect(Collectors.toList());
    }

    /**
     * Load the first secret key that matches filtering from a key ring.
     *
     * @param keyRingData     key ring data in base64 or ASCII-armor format
     * @param asciiArmor      {@code true} if data is ASCII-armor format; otherwise, base64
     * @param signingOnly     {@code true} to load only signing key; otherwise, any key
     * @param acceptMasterKey {@code true} if master key can be returned; otherwise, the returned key is sub-key
     * @return
     * @throws IOException
     * @throws PGPException
     */
    public static PGPSecretKey loadFirstSecretKey(String keyRingData, boolean asciiArmor, boolean signingOnly,
                                                  boolean acceptMasterKey) throws IOException, PGPException {
        return extractFirstSecretKey(loadSecretKeys(keyRingData, asciiArmor), signingOnly, acceptMasterKey);
    }

    /*----------------------------------------------------------------------*/

    private final static JcePBESecretKeyDecryptorBuilder keyDecryptorBuilder = new JcePBESecretKeyDecryptorBuilder();

    /**
     * Extract private key from a secret key.
     *
     * @param secretKey
     * @param password
     * @return
     * @throws PGPException
     */
    public static PGPPrivateKey extractPrivateKey(PGPSecretKey secretKey, String password) throws PGPException {
        if (secretKey == null || secretKey.isPrivateKeyEmpty()) {
            return null;
        }
        return secretKey.extractPrivateKey(keyDecryptorBuilder.build(password.toCharArray()));
    }

    /*----------------------------------------------------------------------*/

    public enum CompressionAlgorithm {
        NOCOMPRESSED(0), // No compression.
        ZIP(1), // ZIP (RFC 1951) compression. Unwrapped DEFLATE.
        ZLIB(2), // ZLIB (RFC 1950) compression. DEFLATE with a wrapper for better error detection.
        BZIP2(3); // BZIP2 compression. Better compression than ZIP but much slower to compress and
        // decompress.

        private final int value;

        CompressionAlgorithm(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        /**
         * Convert value to one of constants defined in {@link CompressionAlgorithmTags}
         *
         * @return
         */
        public int toCompressionAlgorithmTags() {
            switch (value) {
                case 1:
                    return CompressionAlgorithmTags.ZIP;
                case 2:
                    return CompressionAlgorithmTags.ZLIB;
                case 3:
                    return CompressionAlgorithmTags.BZIP2;
                default:
                    return CompressionAlgorithmTags.UNCOMPRESSED;
            }
        }
    }

    public final static CompressionAlgorithm DEFAULT_COMPRESSION_ALGORITHM = CompressionAlgorithm.ZLIB;

    private static byte[] compress(byte[] data, String name, CompressionAlgorithm algorithm,
                                   boolean wrappedInLiteralData) throws IOException {
        name = StringUtils.isBlank(name) ? PGPLiteralData.CONSOLE : name.trim();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PGPCompressedDataGenerator compressedDataGenerator =
                    algorithm != null ? new PGPCompressedDataGenerator(algorithm.toCompressionAlgorithmTags()) : null;
            try (OutputStream osCompressed = compressedDataGenerator != null ?
                    compressedDataGenerator.open(output) :
                    output) {
                PGPLiteralDataGenerator literalDataGenerator = wrappedInLiteralData ?
                        new PGPLiteralDataGenerator() :
                        null;
                try (OutputStream pOut = literalDataGenerator != null ?
                        literalDataGenerator
                                .open(osCompressed, PGPLiteralData.BINARY, name, new Date(), new byte[1024]) :
                        osCompressed) {
                    pOut.write(data);
                }
            }
            return output.toByteArray();
        }
    }

    /**
     * Compress an array of bytes using {@link #DEFAULT_COMPRESSION_ALGORITHM}.
     *
     * @param data
     * @return
     * @throws IOException
     */
    public static byte[] compress(byte[] data) throws IOException {
        return compress(data, null, DEFAULT_COMPRESSION_ALGORITHM, true);
    }

    /**
     * Compress an array of bytes.
     *
     * @param data      data to compress
     * @param name      name of the PGP data block, can be empty
     * @param algorithm compression algorithm, one of {@link CompressionAlgorithm}
     * @return a {@link PGPLiteralData} wrapped in a {@link PGPCompressedData}, presented as an
     * array of bytes.
     * @throws IOException
     */
    public static byte[] compress(byte[] data, String name, CompressionAlgorithm algorithm) throws IOException {
        return compress(data, name, algorithm, true);
    }

    /**
     * Decompress a compressed data (produced via
     * {@link #compress(byte[], String, CompressionAlgorithm)}.
     *
     * @param compressedData data to decompress (a {@link PGPLiteralData} wrapped in a
     *                       {@link PGPCompressedData}, presented as an array of bytes)
     * @return decompressed data
     * @throws IOException
     * @throws PGPException
     */
    public static byte[] decompress(byte[] compressedData) throws IOException, PGPException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedData)) {
            PGPCompressedData cData = (PGPCompressedData) new JcaPGPObjectFactory(bais).nextObject();
            try (InputStream isCompress = cData.getDataStream()) {
                PGPLiteralData lData = (PGPLiteralData) new JcaPGPObjectFactory(isCompress).nextObject();
                try (InputStream is = lData.getInputStream()) {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        IOUtils.copy(is, baos);
                        return baos.toByteArray();
                    }
                }
            }
        }
    }

    /*----------------------------------------------------------------------*/
    public enum SymmetricEncryptionAlgorithm {
        NULL(0), // Plaintext/unencrypted data
        IDEA(1), // IDEA [IDEA]
        TRIPLE_DES(2), // Triple-DES (DES-EDE, as per spec - 168 bit key derived from 192)
        CAST5(3), // CAST5 (128 bit key, as per RFC 2144)
        BLOWFISH(4), // Blowfish (128 bit key, 16 rounds) [BLOWFISH]
        SAFER(5), // SAFER-SK128 (13 rounds) [SAFER]
        DES(6), // DES/SK
        AES_128(7), // AES with 128-bit key
        AES_192(8), // AES with 192-bit key
        AES_256(9), // AES with 256-bit key
        TWOFISH(10), // Twofish
        CAMELLIA_128(11), // Camellia with 128-bit key
        CAMELLIA_192(12), // Camellia with 192-bit key
        CAMELLIA_256(13); // Camellia with 256-bit key

        private final int value;

        SymmetricEncryptionAlgorithm(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        /**
         * Convert value to one of constants defined in {@link SymmetricKeyAlgorithmTags}
         *
         * @return
         */
        public int toSymmetricKeyAlgorithmTags() {
            switch (value) {
                case 1:
                    return SymmetricKeyAlgorithmTags.IDEA;
                case 2:
                    return SymmetricKeyAlgorithmTags.TRIPLE_DES;
                case 3:
                    return SymmetricKeyAlgorithmTags.CAST5;
                case 4:
                    return SymmetricKeyAlgorithmTags.BLOWFISH;
                case 5:
                    return SymmetricKeyAlgorithmTags.SAFER;
                case 6:
                    return SymmetricKeyAlgorithmTags.DES;
                case 7:
                    return SymmetricKeyAlgorithmTags.AES_128;
                case 8:
                    return SymmetricKeyAlgorithmTags.AES_192;
                case 9:
                    return SymmetricKeyAlgorithmTags.AES_256;
                case 10:
                    return SymmetricKeyAlgorithmTags.TWOFISH;
                case 11:
                    return SymmetricKeyAlgorithmTags.CAMELLIA_128;
                case 12:
                    return SymmetricKeyAlgorithmTags.CAMELLIA_192;
                case 13:
                    return SymmetricKeyAlgorithmTags.CAMELLIA_256;
                default:
                    return SymmetricKeyAlgorithmTags.NULL;
            }
        }
    }

    public final static SymmetricEncryptionAlgorithm DEFAULT_SYMMETRIC_ENCRYPTION_ALGORITHM = SymmetricEncryptionAlgorithm.AES_256;

    private static byte[] encrypt(PGPPublicKey pubKey, byte[] data, boolean asciiArmor,
                                  CompressionAlgorithm compressionAlgorithm, boolean wrappedInLiteralData,
                                  SymmetricEncryptionAlgorithm encryptionAlgorithm) throws IOException, PGPException {
        if (!pubKey.isEncryptionKey()) {
            throw new PGPException("Supplied public key is not encryption-key.");
        }
        String name = PGPLiteralData.CONSOLE;
        encryptionAlgorithm =
                encryptionAlgorithm != null ? encryptionAlgorithm : DEFAULT_SYMMETRIC_ENCRYPTION_ALGORITHM;

        PGPDataEncryptorBuilder dataEncryptorBuilder = new JcePGPDataEncryptorBuilder(
                encryptionAlgorithm.toSymmetricKeyAlgorithmTags()).setWithIntegrityPacket(true)
                .setSecureRandom(new SecureRandom());
        PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(dataEncryptorBuilder);
        encryptedDataGenerator
                .addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(pubKey).setSecureRandom(new SecureRandom()));

        try (ByteArrayOutputStream osStorage = new ByteArrayOutputStream()) {
            try (OutputStream osEncrypted = asciiArmor ? new ArmoredOutputStream(osStorage) : osStorage) {
                try (OutputStream os = encryptedDataGenerator.open(osEncrypted, new byte[1024])) {
                    /* call compress(...) is mandatory because we need the data as PGPObject(s) */
                    os.write(compress(data, name, compressionAlgorithm, wrappedInLiteralData));
                }
            }
            return osStorage.toByteArray();
        }
    }

    /**
     * Encrypt data using public key.
     *
     * @param pubKey               public key used for encryption
     * @param data                 data to be encrypted
     * @param asciiArmor           {@code true} if output should be in ASCII-armor format; otherwise output is binary
     * @param compressionAlgorithm data compression algorithm to be used, one of {@link CompressionAlgorithm}
     * @param encryptionAlgorithm  data encryption algorithm to be used, one of {@link SymmetricEncryptionAlgorithm}
     * @return encrypted data (encoded {@code PGPPublicKeyEncryptedData(PGPCompressedData)}), either
     * in ASCII-armor or binary format
     * @throws IOException
     * @throws PGPException
     */
    public static byte[] encrypt(PGPPublicKey pubKey, byte[] data, boolean asciiArmor,
                                 CompressionAlgorithm compressionAlgorithm, SymmetricEncryptionAlgorithm encryptionAlgorithm)
            throws IOException, PGPException {
        return encrypt(pubKey, data, asciiArmor, compressionAlgorithm, true, encryptionAlgorithm);
    }

    private static PGPObjMessage decrypt(PGPPrivateKey privKey, PGPPublicKeyEncryptedData pbe)
            throws PGPException, IOException {
        try (InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(privKey))) {
            PGPObjectFactory clearObjectFactory = new JcaPGPObjectFactory(clear);
            return extractPGPObjects(clearObjectFactory);
        }
    }

    private static byte[] decrypt(Collection<PGPSecretKey> secretKeys, String passphrase, byte[] encryptedData,
                                  Collection<PGPPublicKey> publicKeysForVerification) throws IOException, PGPException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (InputStream isEncrypted = PGPUtil.getDecoderStream(new ByteArrayInputStream(encryptedData))) {
                PGPEncryptedDataList enc = PGPUtils.extractEncryptedDataList(new JcaPGPObjectFactory(isEncrypted));
                if (enc == null) {
                    throw new PGPException("No encrypted data found in input data.");
                }
                PGPPublicKeyEncryptedData pbe = PGPUtils.extractPublicKeyEncryptedData(enc);
                if (pbe == null) {
                    throw new PGPException("No public-key-encrypted data found in input data.");
                }
                PGPSecretKey secKey = secretKeys.stream().filter(key -> key.getKeyID() == pbe.getKeyID()).findFirst()
                        .orElse(null);
                if (secKey == null) {
                    throw new PGPException(
                            "No secret key [" + pbe.getKeyID() + "] found in the key ring for decryption.");
                }

                PGPPrivateKey privKey = extractPrivateKey(secKey, passphrase);
                PGPObjMessage result = decrypt(privKey, pbe);
                if (pbe.isIntegrityProtected()) {
                    if (!pbe.verify()) {
                        throw new PGPException("Message failed integrity check");
                    }
                }

                // verify message if needed
                List<PGPOnePassSignature> onePassSignatures = result.getOnePassSignatures();
                List<PGPSignature> signatures = result.getSignatures();
                if (onePassSignatures != null && onePassSignatures.size() > 0 && signatures != null
                        && signatures.size() > 0) {
                    for (PGPOnePassSignature onePassSignature : onePassSignatures) {
                        PGPPublicKey pubKey = publicKeysForVerification.stream()
                                .filter(key -> key.getKeyID() == onePassSignature.getKeyID()).findFirst().orElse(null);
                        if (pubKey == null) {
                            throw new PGPException("No public key [" + onePassSignature.getKeyID()
                                    + "] found in the key ring for verifying.");
                        }
                        onePassSignature.init(new JcaPGPContentVerifierBuilderProvider(), pubKey);
                        onePassSignature.update(result.getMessage());
                        for (PGPSignature sign : signatures) {
                            if (onePassSignature.getKeyID() == sign.getKeyID() && !onePassSignature.verify(sign)) {
                                throw new PGPException(
                                        "Message verification failed using public key " + pubKey.getKeyID()
                                                + " against signature " + sign.getKeyID());
                            }
                        }
                    }
                }

                out.write(result.getMessage());
            }
            return out.toByteArray();
        }
    }

    /**
     * Decrypt data (encrypted by
     * {@link #encrypt(PGPPublicKey, byte[], boolean, CompressionAlgorithm, SymmetricEncryptionAlgorithm)}
     * using private key.
     *
     * @param secretKeys                secret key ring used for decryption
     * @param passphrase                passphrase used to extract private key from secret key ring
     * @param encryptedData             data, encrypted by
     *                                  {@link #encrypt(PGPPublicKey, byte[], boolean, CompressionAlgorithm, SymmetricEncryptionAlgorithm)},
     *                                  to be decrypted. Input data can be either binary or ASCII-armor format.
     * @param publicKeysForVerification public key ring used for message verification (if it is signed)
     * @return decrypted data
     * @throws IOException
     * @throws PGPException
     */
    public static byte[] decrypt(PGPSecretKeyRing secretKeys, String passphrase, byte[] encryptedData,
                                 PGPPublicKeyRing publicKeysForVerification) throws IOException, PGPException {
        return decrypt(extractSecretKeys(secretKeys), passphrase, encryptedData,
                extractPublicKeys(publicKeysForVerification));
    }

    /**
     * Decrypt data (encrypted by
     * {@link #encrypt(PGPPublicKey, byte[], boolean, CompressionAlgorithm, SymmetricEncryptionAlgorithm)}
     * using private key.
     *
     * @param secretKeys                secret key ring used for decryption
     * @param passphrase                passphrase used to extract private key from secret key ring
     * @param encryptedData             data, encrypted by
     *                                  {@link #encrypt(PGPPublicKey, byte[], boolean, CompressionAlgorithm, SymmetricEncryptionAlgorithm)},
     *                                  to be decrypted. Input data can be either binary or ASCII-armor format.
     * @param publicKeysForVerification public key ring used for message verification (if it is signed)
     * @return decrypted data
     * @throws IOException
     * @throws PGPException
     */
    public static byte[] decrypt(PGPSecretKeyRingCollection secretKeys, String passphrase, byte[] encryptedData,
                                 PGPPublicKeyRingCollection publicKeysForVerification) throws IOException, PGPException {
        return decrypt(extractSecretKeys(secretKeys), passphrase, encryptedData,
                extractPublicKeys(publicKeysForVerification));
    }

    /*----------------------------------------------------------------------*/

    /**
     * Sign a message using private key.
     *
     * @param secKey               secret key used for signing.
     * @param passphrase           passphrase used to extract private key from secret key
     * @param message              message to sign
     * @param compressionAlgorithm if not {@code null}, signed message is compressed using the specified algorithm.
     *                             Accepted values: one of {@link CompressionAlgorithm}
     * @param asciiArmor           {@code true} if output should be in ASCII-armor format; otherwise output is binary
     * @return signed message (encoded
     * {@code PGPCompressedData(PGPOnePassSignature,PGPLiteralData,PGPSignature)}), either
     * in ASCII-armor or binary format
     * @throws IOException
     * @throws PGPException
     */
    public static byte[] sign(PGPSecretKey secKey, String passphrase, byte[] message,
                              CompressionAlgorithm compressionAlgorithm, boolean asciiArmor) throws IOException, PGPException {
        if (!secKey.isSigningKey()) {
            throw new PGPException("Supplied secret key is not signing-key.");
        }

        PGPPrivateKey privKey = PGPUtils.extractPrivateKey(secKey, passphrase);
        PGPPublicKey pubKey = secKey.getPublicKey();
        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder(pubKey.getAlgorithm(), HashAlgorithmTags.SHA1));
        signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privKey);

        Iterator<?> it = pubKey.getUserIDs();
        if (it.hasNext()) {
            PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
            spGen.setSignerUserID(false, it.next().toString());
            signatureGenerator.setHashedSubpackets(spGen.generate());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (OutputStream outputStream = asciiArmor ? new ArmoredOutputStream(out) : out) {
            PGPCompressedDataGenerator compressedDataGenerator = compressionAlgorithm != null ?
                    new PGPCompressedDataGenerator(compressionAlgorithm.toCompressionAlgorithmTags()) :
                    null;
            try (BCPGOutputStream bcOutputStream = new BCPGOutputStream(
                    compressedDataGenerator != null ? compressedDataGenerator.open(outputStream) : outputStream)) {
                signatureGenerator.generateOnePassVersion(false).encode(bcOutputStream);
                PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
                try (OutputStream literalDataGenOutputStream = literalDataGenerator
                        .open(bcOutputStream, PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, new Date(),
                                new byte[1024])) {
                    literalDataGenOutputStream.write(message);
                    signatureGenerator.update(message);
                }
                signatureGenerator.generate().encode(bcOutputStream);
            }
        }
        return out.toByteArray();
    }

    private static boolean verify(Collection<PGPPublicKey> pubKeys, byte[] messageWithSignature)
            throws IOException, PGPException {
        try (InputStream is = PGPUtil.getDecoderStream(new ByteArrayInputStream(messageWithSignature))) {
            PGPObjectFactory pgpObjFactory = new JcaPGPObjectFactory(is);
            PGPObjMessage result = PGPUtils.extractPGPObjects(pgpObjFactory);
            return result.verify(pubKeys);
        }
    }

    /**
     * Verify a message (signed by
     * {@link #sign(PGPSecretKey, String, byte[], CompressionAlgorithm, boolean)}) using public key.
     *
     * @param pubKeys              public key ring used for verifying
     * @param messageWithSignature message, sign by
     *                             {@link #sign(PGPSecretKey, String, byte[], CompressionAlgorithm, boolean)}, to be
     *                             verified. Input data can be either binary or ASCII-armor format.
     * @return
     * @throws IOException
     * @throws PGPException
     */
    public static boolean verify(PGPPublicKeyRing pubKeys, byte[] messageWithSignature)
            throws IOException, PGPException {
        return verify(extractPublicKeys(pubKeys), messageWithSignature);
    }

    /**
     * Verify a message (signed by
     * {@link #sign(PGPSecretKey, String, byte[], CompressionAlgorithm, boolean)}) using public key.
     *
     * @param pubKeys              public key ring used for verifying
     * @param messageWithSignature message, sign by
     *                             {@link #sign(PGPSecretKey, String, byte[], CompressionAlgorithm, boolean)}, to be
     *                             verified. Input data can be either binary or ASCII-armor format.
     * @return
     * @throws IOException
     * @throws PGPException
     */
    public static boolean verify(PGPPublicKeyRingCollection pubKeys, byte[] messageWithSignature)
            throws IOException, PGPException {
        return verify(extractPublicKeys(pubKeys), messageWithSignature);
    }

    /*----------------------------------------------------------------------*/
    public static byte[] signAndEncrypt(PGPPublicKey pubKeyToEncrypt, PGPSecretKey secKeyToSign, String passphrase,
                                        byte[] data, boolean asciiArmor, CompressionAlgorithm compressionAlgorithm,
                                        SymmetricEncryptionAlgorithm encryptionAlgorithm) throws IOException, PGPException {
        if (!pubKeyToEncrypt.isEncryptionKey()) {
            throw new PGPException("Supplied public key is not encryption-key.");
        }
        if (!secKeyToSign.isSigningKey()) {
            throw new PGPException("Supplied secret key is not signing-key.");
        }

        /*
         * encoded: PGPPublicKeyEncryptedData(PGPCompressedData(PGPOnePassSignature,PGPLiteralData,
         * PGPSignature))
         */

        byte[] signedData = sign(secKeyToSign, passphrase, data, null, false);
        return encrypt(pubKeyToEncrypt, signedData, asciiArmor, compressionAlgorithm, false, encryptionAlgorithm);
    }

    /*----------------------------------------------------------------------*/
    private static class PGPObjMessage {
        private byte[] message;
        private List<PGPOnePassSignature> onePassSignatures = new ArrayList<>();
        private List<PGPSignature> signatures = new ArrayList<>();

        //        public boolean verify(PGPPublicKeyRing pubKeys) throws PGPException {
        //            return verify(PGPUtils.extractPublicKeys(pubKeys));
        //        }
        //
        //        public boolean verify(PGPPublicKeyRingCollection pubKeys) throws PGPException {
        //            return verify(PGPUtils.extractPublicKeys(pubKeys));
        //        }

        public boolean verify(Collection<PGPPublicKey> pubKeys) throws PGPException {
            if (onePassSignatures != null && onePassSignatures.size() > 0 && signatures != null
                    && signatures.size() > 0) {
                for (PGPOnePassSignature onePassSignature : onePassSignatures) {
                    PGPPublicKey pubKey = pubKeys.stream().filter(key -> key.getKeyID() == onePassSignature.getKeyID())
                            .findFirst().orElse(null);
                    if (pubKey == null) {
                        return false;
                    }
                    onePassSignature.init(new JcaPGPContentVerifierBuilderProvider(), pubKey);
                    onePassSignature.update(getMessage());
                    for (PGPSignature sign : signatures) {
                        if (onePassSignature.getKeyID() == sign.getKeyID()) {
                            return onePassSignature.verify(sign);
                        }
                    }
                }
                return false;
            }
            return true;
        }

        public byte[] getMessage() {
            return this.message;
        }

        public PGPObjMessage setMessage(byte[] message) {
            this.message = message;
            return this;
        }

        public PGPObjMessage setMessage(PGPLiteralData literalData) throws IOException {
            try (InputStream is = literalData.getInputStream()) {
                return setMessage(is.readAllBytes());
            }
        }

        //        public PGPObjMessage setMessageIfNotPresent(byte[] message) {
        //            if (this.message == null) {
        //                setMessage(message);
        //            }
        //            return this;
        //        }

        public PGPObjMessage setMessageIfNotPresent(PGPLiteralData literalData) throws IOException {
            if (this.message == null) {
                setMessage(literalData);
            }
            return this;
        }

        public List<PGPOnePassSignature> getOnePassSignatures() {
            return onePassSignatures;
        }

        public PGPObjMessage addOnePassSignature(PGPOnePassSignature onePassSignature) {
            onePassSignatures.add(onePassSignature);
            return this;
        }

        public PGPObjMessage addOnePassSignatures(PGPOnePassSignatureList onePassSignatureList) {
            for (int i = 0, n = onePassSignatureList.size(); i < n; i++) {
                addOnePassSignature(onePassSignatureList.get(i));
            }
            return this;
        }

        public List<PGPSignature> getSignatures() {
            return signatures;
        }

        public PGPObjMessage addSignature(PGPSignature signature) {
            signatures.add(signature);
            return this;
        }

        public PGPObjMessage addSignatures(PGPSignatureList signatureList) {
            for (int i = 0, n = signatureList.size(); i < n; i++) {
                addSignature(signatureList.get(i));
            }
            return this;
        }
    }

    private static PGPObjMessage extractPGPObjects(PGPObjectFactory objF) throws PGPException, IOException {
        PGPObjMessage result = new PGPObjMessage();
        for (Object obj = objF.nextObject(); obj != null; obj = objF.nextObject()) {
            // System.err.println(obj.getClass());
            if (obj instanceof PGPCompressedData) {
                // uncompress if needed
                InputStream is = ((PGPCompressedData) obj).getDataStream();
                objF = new JcaPGPObjectFactory(is);
                obj = objF.nextObject();
                // System.err.println(obj.getClass());
            }
            if (obj instanceof PGPOnePassSignatureList) {
                result.addOnePassSignatures((PGPOnePassSignatureList) obj);
            } else if (obj instanceof PGPSignatureList) {
                result.addSignatures((PGPSignatureList) obj);
            } else if (obj instanceof PGPLiteralData) {
                result.setMessageIfNotPresent((PGPLiteralData) obj);
            } else {
                throw new PGPException("Message is not a simple encrypted file - type unknown: " + obj.getClass());
            }
        }
        return result;
    }

    private static PGPEncryptedDataList extractEncryptedDataList(PGPObjectFactory objFactory) throws IOException {
        for (Object obj = objFactory.nextObject(); obj != null; obj = objFactory.nextObject()) {
            if (obj instanceof PGPEncryptedDataList) {
                return (PGPEncryptedDataList) obj;
            }
        }
        return null;
    }

    private static PGPPublicKeyEncryptedData extractPublicKeyEncryptedData(PGPEncryptedDataList encryptedDataList) {
        for (Iterator<?> it = encryptedDataList.getEncryptedDataObjects(); it.hasNext(); ) {
            Object obj = it.next();
            if (obj instanceof PGPPublicKeyEncryptedData) {
                return (PGPPublicKeyEncryptedData) obj;
            }
        }
        return null;
    }
}
