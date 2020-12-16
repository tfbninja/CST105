package uno;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Barber
 */
public class UNOPlayer {

    private final String ID;
    private String username;

    public UNOPlayer() {
        Random rand = new Random(System.currentTimeMillis() + 1);
        username = "Player " + rand.nextInt(9) + rand.nextInt(9) + rand.nextInt(9) + rand.nextInt(9) + rand.nextInt(9) + rand.nextInt(9);
        String temp = "";
        try { // most of this is from https://www.baeldung.com/sha-256-hashing-java
            final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            String originalString = System.currentTimeMillis() + "";
            final byte[] hashbytes = digest.digest(originalString.getBytes(StandardCharsets.UTF_8));
            temp = bytesToHex(hashbytes);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UNOPlayer.class.getName()).log(Level.SEVERE, null, ex);
            temp = "";
            char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
            for (int i = 0; i < 64; i++) {
                temp += chars[rand.nextInt(chars.length - 1)];
            }
        }
        ID = temp;
    }

    public String getID() {
        return ID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public static String bytesToHex(byte[] hash) { // from https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-security-2/src/main/java/com/baeldung/hashing/SHACommonUtils.java
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte h : hash) {
            String hex = Integer.toHexString(0xff & h);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
