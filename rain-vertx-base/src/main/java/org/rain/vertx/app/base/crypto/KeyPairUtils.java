package org.rain.vertx.app.base.crypto;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfoBuilder;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.io.pem.PemHeader;
import org.bouncycastle.util.io.pem.PemObject;

import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.util.Base64;
import java.util.List;

/**
 * Public/Private key pair utility class.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 1.1.0
 */
public class KeyPairUtils {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        SecureRandom secureRandom = new SecureRandom();
        Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        PROVIDER = provider;
        SECURE_RNG = secureRandom;

        KEY_CONVERTER = new JcaPEMKeyConverter().setProvider(provider);
        PEM_DECRYPTOR_PROVIDER_BUILDER = new JcePEMDecryptorProviderBuilder().setProvider(provider);
        PKCS8_DECRYPTOR_PROVIDER_BUILDER = new JceOpenSSLPKCS8DecryptorProviderBuilder().setProvider(provider);
    }

    private final static Provider PROVIDER;
    private final static SecureRandom SECURE_RNG;
    private final static ASN1ObjectIdentifier ENCRYPTION = PKCS8Generator.DES3_CBC;

    public final static String PEM_TYPE_X509_PUBLIC_KEY = "PUBLIC KEY";
    public final static String PEM_TYPE_RSA_PKCS1_PUBLIC_KEY = "RSA PUBLIC KEY";

    public final static String PEM_TYPE_PKCS8_PRIVATE_KEY = "PRIVATE KEY";
    public final static String PEM_TYPE_PKCS8_ENCRYPTED_PRIVATE_KEY = "ENCRYPTED PRIVATE KEY";
    public final static String PEM_TYPE_RSA_PKCS1_PRRIVATE_KEY = "RSA PRIVATE KEY";

    private final static JcaPEMKeyConverter KEY_CONVERTER;
    private final static JcePEMDecryptorProviderBuilder PEM_DECRYPTOR_PROVIDER_BUILDER;
    private final static JceOpenSSLPKCS8DecryptorProviderBuilder PKCS8_DECRYPTOR_PROVIDER_BUILDER;

    /**
     * Calculate the smallest power of 2 number that is greater than or equals to the supplied value
     * (e.g. {@code nextPowerOf2(4)=4}, {@code nextPowerOf2(5)=8}).
     *
     * @param value
     * @return
     */
    private static int nextPowerOf2(int value) {
        int valuePow2 = 1;
        while (valuePow2 < value) {
            valuePow2 <<= 1;
        }
        return valuePow2;
    }

    /**
     * Generate a random keypair.
     *
     * @param keySizeInBits key's length in bits, should be power of 2
     * @param algorithm
     * @param provider      if {@code null}, default provider is used
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static KeyPair generateKeyPair(int keySizeInBits, String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        int numBitsPow2 = nextPowerOf2(keySizeInBits);
        KeyPairGenerator kpg = provider == null ?
                KeyPairGenerator.getInstance(algorithm) :
                KeyPairGenerator.getInstance(algorithm, provider);
        kpg.initialize(numBitsPow2, SECURE_RNG);
        KeyPair keyPair = kpg.generateKeyPair();
        return keyPair;
    }

    /**
     * Export a key to PEM (base64) format.
     *
     * @param keyData
     * @param withType if {@code true}, the exported key is wrapped inside "-----BEGIN _TYPE_-----" / "-----END _TYPE_-----" block
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String exportPem(PemObject keyData, boolean withType) {
        String base64 = Base64.getEncoder().encodeToString(keyData.getContent());
        if (withType) {
            StringBuffer sb = new StringBuffer("-----BEGIN ").append(keyData.getType()).append("-----").append("\n");
            List<PemHeader> headers = keyData.getHeaders();
            if (headers != null && headers.size() > 0) {
                for (PemHeader header : headers) {
                    sb.append(header.getName()).append(": ").append(header.getValue()).append("\n");
                }
                sb.append("\n");
            }
            sb.append(StringUtils.join(base64.split("(?<=\\G.{64})"), "\n")).append("\n");
            sb.append("-----END ").append(keyData.getType()).append("-----");
            return sb.toString();
        }
        return base64;
    }

    /**
     * Export a public key to PEM (base64) X.509 format.
     *
     * @param pubKey
     * @param withType if {@code true}, the exported key is wrapped inside "-----BEGIN PUBLIC KEY-----" / "-----END PUBLIC KEY-----" block.
     * @return
     */
    public static String exportPemX509(PublicKey pubKey, boolean withType) {
        String format = pubKey.getFormat();
        if (!StringUtils.endsWithIgnoreCase("X.509", format) && !StringUtils.endsWithIgnoreCase("X509", format)) {
            throw new IllegalArgumentException("Expecting a X509 key, but input is " + format);
        }
        return exportPem(new PemObject(PEM_TYPE_X509_PUBLIC_KEY, pubKey.getEncoded()), withType);
    }

    /**
     * Export a private key to PEM (base64) PKCS#8 format.
     *
     * @param privKey  private key to export
     * @param password if not empty, the exported key is encrypted with specified password; otherwise the export key is unencrypted
     * @param withType if {@code true}, the exported key is wrapped inside "-----BEGIN PRIVATE KEY-----" / "-----END PRIVATE KEY-----" block.
     * @return
     * @throws IOException
     * @throws OperatorCreationException
     */
    public static String exportPemPKCS8(PrivateKey privKey, String password, boolean withType)
            throws IOException, OperatorCreationException {
        String format = privKey.getFormat();
        if (!StringUtils.endsWithIgnoreCase("PKCS#8", format) && !StringUtils.endsWithIgnoreCase("PKCS8", format)) {
            throw new IllegalArgumentException("Expecting a PKCS#8 key, but input is " + format);
        }
        if (StringUtils.isEmpty(password)) {
            return exportPem(new PemObject(PEM_TYPE_PKCS8_PRIVATE_KEY, privKey.getEncoded()), withType);
        }

        OutputEncryptor outputEncryptor = new JceOpenSSLPKCS8EncryptorBuilder(ENCRYPTION).setProvider(PROVIDER)
                .setRandom(SECURE_RNG).setPasssword(password.trim().toCharArray()).build();
        PKCS8EncryptedPrivateKeyInfo epki = new PKCS8EncryptedPrivateKeyInfoBuilder(privKey.getEncoded())
                .build(outputEncryptor);
        return exportPem(new PemObject(PEM_TYPE_PKCS8_ENCRYPTED_PRIVATE_KEY, epki.getEncoded()), withType);
    }

    /**
     * Load a {@code X.509} or {@code PKCS#1} public key from PEM data.
     *
     * @param pemData
     * @return
     * @throws IOException
     */
    public static PublicKey loadPublicKeyFromPem(String pemData) throws IOException {
        try (PEMParser pemParser = new PEMParser(new StringReader(pemData))) {
            Object obj = pemParser.readObject();
            if (obj instanceof SubjectPublicKeyInfo) {
                SubjectPublicKeyInfo publicKeyInfo = (SubjectPublicKeyInfo) obj;
                return KEY_CONVERTER.getPublicKey(publicKeyInfo);
            }
        }
        return null;
    }

    /**
     * Load a @code PKCS#8} or {@code PKCS#1} private key from PEM data.
     *
     * @param pemData
     * @param password
     * @return
     * @throws IOException
     * @throws PKCSException
     * @throws OperatorCreationException
     */
    public static PrivateKey loadPrivateKeyFromPem(String pemData, String password)
            throws IOException, PKCSException, OperatorCreationException {
        try (PEMParser pemParser = new PEMParser(new StringReader(pemData))) {
            Object obj = pemParser.readObject();
            if (obj instanceof PrivateKeyInfo) {
                // PKCS#8
                PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) obj;
                return KEY_CONVERTER.getPrivateKey(privateKeyInfo);
            }
            if (obj instanceof PEMKeyPair) {
                // PKCS#1
                PEMKeyPair keyPair = (PEMKeyPair) obj;
                return KEY_CONVERTER.getKeyPair(keyPair).getPrivate();
            }
            if (obj instanceof PKCS8EncryptedPrivateKeyInfo) {
                // PKCS#8, encrypted
                PKCS8EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = (PKCS8EncryptedPrivateKeyInfo) obj;
                InputDecryptorProvider provider = PKCS8_DECRYPTOR_PROVIDER_BUILDER.build(password.trim().toCharArray());
                PrivateKeyInfo privateKeyInfo = encryptedPrivateKeyInfo.decryptPrivateKeyInfo(provider);
                return KEY_CONVERTER.getPrivateKey(privateKeyInfo);
            }
            if (obj instanceof PEMEncryptedKeyPair) {
                // PKCS#1, encrypted
                PEMEncryptedKeyPair encryptedKeyPair = (PEMEncryptedKeyPair) obj;
                PEMKeyPair keyPair = encryptedKeyPair
                        .decryptKeyPair(PEM_DECRYPTOR_PROVIDER_BUILDER.build(password.trim().toCharArray()));
                return KEY_CONVERTER.getKeyPair(keyPair).getPrivate();
            }
        }
        return null;
    }
}
