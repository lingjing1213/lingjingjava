package com.lingjing.utils;

import static com.lingjing.constants.LingJingConstants.ANDROID_KEYSTORE;
import static com.lingjing.constants.LingJingConstants.KEYSTORE_ALIAS;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import com.lingjing.enums.ErrorTypes;
import com.lingjing.exceptions.LingJingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * @Author：灵静
 * @Package：com.lingjing.utils
 * @Project：lingjingjava
 * @name：RsaUtils
 * @Date：2024/10/27 上午1:50
 * @Filename：RsaUtils
 * @Version：1.0.0
 */
public class RSAUtils {

    public static void generateKeyPair() throws LingJingException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE);
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT |
                            KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setAlgorithmParameterSpec(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4))
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .setUserAuthenticationRequired(false)  // 如果你需要用户认证,可以设置为 true
                    .build();

            keyPairGenerator.initialize(keyGenParameterSpec);
            keyPairGenerator.generateKeyPair();
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException |
                 NoSuchProviderException e) {
            throw new LingJingException(ErrorTypes.GENERATE_KEY_PAIR_FAIL, e);
        }

    }

    // 从 KeyStore 中获取公钥
    public static PublicKey getPublicKey() throws LingJingException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            return keyStore.getCertificate(KEYSTORE_ALIAS).getPublicKey();
        } catch (CertificateException | KeyStoreException | IOException |
                 NoSuchAlgorithmException e) {
            throw new LingJingException(ErrorTypes.GET_PUBLIC_KEY_FAIL, e);
        }
    }

    // 从 KeyStore 中获取私钥
    public static PrivateKey getPrivateKey() throws LingJingException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            return (PrivateKey) keyStore.getKey(KEYSTORE_ALIAS, null);
        } catch (UnrecoverableKeyException | CertificateException | KeyStoreException |
                 IOException | NoSuchAlgorithmException e) {
            throw new LingJingException(ErrorTypes.GET_PRIVATE_KEY_FAIL, e);
        }

    }

    // 使用私钥签名
    public String sign(String message) throws LingJingException {
        try {
            PrivateKey privateKey = getPrivateKey();
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(message.getBytes());
            byte[] signBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signBytes);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new LingJingException(ErrorTypes.PRIVATE_KEY_SIGN_FAIL, e);
        }

    }

    // 使用公钥验证签名
    public boolean verify(String message, String signatureStr) throws LingJingException {
        try {
            PublicKey publicKey = getPublicKey();
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(message.getBytes());
            byte[] signBytes = Base64.getDecoder().decode(signatureStr);
            return signature.verify(signBytes);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new LingJingException(ErrorTypes.PUBLIC_KEY_VERIFY_FAIL, e);
        }

    }

    // 加密
    public static String encrypt(String message) throws LingJingException {
        try {
            PublicKey publicKey = getPublicKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 BadPaddingException | InvalidKeyException e) {
            throw new LingJingException(ErrorTypes.ENCRYPT_FAIL, e);
        }

    }

    // 解密
    public static String decrypt(String encryptedMessage) throws LingJingException {
        try {
            PrivateKey privateKey = getPrivateKey();
            if (privateKey == null) {
                return null;
            }
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
            return new String(decryptedBytes);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 BadPaddingException | InvalidKeyException e) {
            throw new LingJingException(ErrorTypes.DECRYPT_FAIL, e);
        }

    }

    public static void deleteKeyPair() throws LingJingException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            keyStore.deleteEntry(KEYSTORE_ALIAS);
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            throw new LingJingException(ErrorTypes.DELETE_KEY_PAIR_FAIL,e);
        }

    }
}
