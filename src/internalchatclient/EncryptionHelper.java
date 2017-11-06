package internalchatclient;

import org.jetbrains.annotations.NotNull;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Created by David Lee on 2017-08-29.
 */
public class EncryptionHelper {

    /*
    public static void main(String[] args){
        try{
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.genKeyPair();
            PublicKey publicKey = kp.getPublic();
            PrivateKey privateKey = kp.getPrivate();

            System.out.println("test");
            String es = encryptWithPublicKey("nice to meet you", publicKey);
            System.out.println("ENCRYPTED: " + new String(es));
            System.out.println();

            String ds = decryptWithPrivateKey(es, privateKey);
            System.out.println("DECRYPTED: " + new String(ds));

        } catch(Exception e){

        }
    }
    */


    public static String encryptWithPublicKey(byte[] message, PublicKey publicKey)
            throws Exception {

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encVal = cipher.doFinal(message);
        return Base64.getEncoder().encodeToString(encVal);
    }
    public static String encryptWithPublicKey(String message, PublicKey publicKey)
            throws Exception {

        return encryptWithPublicKey(message.getBytes(), publicKey);
    }

    public static String decryptWithPrivateKey(String message, PrivateKey privateKey)
            throws Exception {

        byte[] decVal = Base64.getDecoder().decode(message);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(decVal));
    }

    public static String decryptWithPrivateKey(byte[] message, PrivateKey privateKey)
        throws Exception {
        byte[] decVal = Base64.getDecoder().decode(message);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(decVal));
    }

    public static byte[] publicKeyToByteArray(PublicKey key){
        return key.getEncoded();
    }

    public static PublicKey byteArrayToPublicKey(byte[] encoded)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        return KeyFactory.getInstance("RSA").generatePublic(
                new X509EncodedKeySpec(encoded)
        );
    }

    public static String byteArrayToString(byte[] bytes){
        return Base64.getEncoder().encodeToString(bytes);
    }
    public static byte[] stringToByteArray(String str){
        return Base64.getDecoder().decode(str);
    }

}
