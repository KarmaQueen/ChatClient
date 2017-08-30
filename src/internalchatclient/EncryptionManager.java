package internalchatclient;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

/**
 * Created by 44067301 on 8/30/2017.
 */
public class EncryptionManager extends EncryptionHelper{

    private ArrayList<PublicKey> keychain = null;
    private PublicKey publicKey = null;
    private PrivateKey privateKey = null;

    private EncryptionManager(){
        keychain = new ArrayList<>();
        generateKeyPair();
    }

    public static EncryptionManager getInstance(){
        return new EncryptionManager();
    }

    public void generateKeyPair(){
        try{
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.genKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();
        } catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            System.out.println("No but seriously this is never supposed to show up");
        }
    }

    /**
     * Attempts to add a PublicKey from a Base64-encoded string.
     * @param encodedKey
     * @return true if successfully added, false if something goes wrong.
     */
    public boolean addToChain(String encodedKey) {
        try {
            PublicKey key = EncryptionHelper.byteArrayToPublicKey(stringToByteArray(encodedKey));
            keychain.add(key);
            return true;
        } catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            System.out.println("This is never supposed to show up");
        } catch(InvalidKeySpecException e){
            e.printStackTrace();
        } finally {
            return false;
        }
    }

    public ArrayList<PublicKey> getKeyChain(){
        return keychain;
    }

    public String getEncodedPublicKey(){
        return byteArrayToString(publicKeyToByteArray(publicKey));
    }

    public String getDistributeKeyProtocol(){
        return "dst " + getEncodedPublicKey();
    }

    public String getAddKeyProtocol(){
        return "add " + getEncodedPublicKey();
    }

    public String encrypt(String str, PublicKey publicKey){
        try {
            return EncryptionHelper.encryptWithPublicKey(str, publicKey);
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String decrypt(String str){
        try{
            return EncryptionHelper.decryptWithPrivateKey(str, privateKey);
        } catch(Exception e){
            //e.printStackTrace();
            return null;
        }
    }

}