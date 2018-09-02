
package it.nicholasbertazzon.crypto.aes;

import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class RNCryptoAesModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;

    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String DEFAULT_HASH_ALGORITHM = "SHA-256";

    public RNCryptoAesModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNCryptoAes";
    }

    @ReactMethod
    public void hashKey(ReadableMap params, Promise promise) {
        try {
            String hashAlgorithm = DEFAULT_HASH_ALGORITHM;
            String key = params.getString("key");
            if (params.hasKey("algorithm")) {
                hashAlgorithm = params.getString("algorithm");
            }

            MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
            byte[] hash = digest.digest(key.getBytes("UTF-8"));
            String base64Key = Base64.encodeToString(hash, Base64.NO_WRAP);

            promise.resolve(base64Key);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            promise.reject("-1", e.getMessage());
        }
    }

    @ReactMethod
    public void encrypt(ReadableMap params, Promise promise) {
        try {
            String cipherAlgorithm = DEFAULT_CIPHER_ALGORITHM;
            String text = params.getString("text");
            String key = params.getString("key");

            //Override default algorithm if given
            if (params.hasKey("algorithm")) {
                cipherAlgorithm = params.getString("algorithm");
            }

            //Generate the initial vector
            byte[] iv = this.getRandomIv();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.decode(key, Base64.NO_WRAP), "AES");

            //Encrypt
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(text.getBytes("UTF-8"));

            //Generate the object for the JS side
            WritableMap map = Arguments.createMap();
            map.putString("encrypted", Base64.encodeToString(encrypted, Base64.NO_WRAP));
            map.putString("iv", Base64.encodeToString(iv, Base64.NO_WRAP));

            promise.resolve(map);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException
                | InvalidAlgorithmParameterException
                | UnsupportedEncodingException e) {
            promise.reject("-1", e.getMessage());
        }
    }

    @ReactMethod
    public void decrypt(ReadableMap params, Promise promise) {
        try {
            String cipherAlgorithm = DEFAULT_CIPHER_ALGORITHM;
            String encrypted = params.getString("encrypted");
            String key = params.getString("key");
            String iv = params.getString("iv");

            //Override default algorithm if given
            if (params.hasKey("algorithm")) {
                cipherAlgorithm = params.getString("algorithm");
            }

            //Get the iv
            IvParameterSpec ivParameterSpec = new IvParameterSpec(Base64.decode(iv, Base64.NO_WRAP));
            //Get the secret key
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.decode(key, Base64.NO_WRAP), "AES");

            // Decrypt
            Cipher cipherDecrypt = Cipher.getInstance(cipherAlgorithm);
            cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decrypted = cipherDecrypt.doFinal(Base64.decode(encrypted, Base64.NO_WRAP));
            String decryptedUtf8 = new String(decrypted, "UTF-8");

            promise.resolve(decryptedUtf8);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException
                | InvalidAlgorithmParameterException
                | UnsupportedEncodingException e) {
            promise.reject("-1", e.getMessage());
        }
    }

    private byte[] getRandomIv() {
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }
}