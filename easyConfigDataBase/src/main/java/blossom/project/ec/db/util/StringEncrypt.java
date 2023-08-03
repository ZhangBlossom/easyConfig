//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package blossom.project.ec.db.util;

import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringEncrypt {
    private static final Logger logger = LoggerFactory.getLogger(StringEncrypt.class);

    public StringEncrypt() {
    }

    public static String encrypt(String sSrc, String sKey) throws Exception {
        sKey = formatKey(sKey);
        if (sKey == null) {
            logger.error("Key为空null");
            return null;
        } else if (sKey.length() != 16) {
            logger.error("Key长度不是16位");
            return null;
        } else {
            byte[] raw = sKey.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(1, skeySpec);
            byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
            return Base32.Encode(encrypted);
        }
    }

    public static String decrypt(String sSrc, String sKey) throws Exception {
        sKey = formatKey(sKey);
        if (sKey == null) {
            logger.error("Key为空null");
            return null;
        } else if (sKey.length() != 16) {
            logger.error("Key长度不是16位");
            return null;
        } else {
            byte[] raw = sKey.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(2, skeySpec);
            byte[] encrypted1 = Base32.Decode(sSrc);
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original, "utf-8");
            return originalString;
        }
    }

    private static String formatKey(String key) {
        if (key == null) {
            key = "0000000000000000";
        } else if (key.trim().length() < 16) {
            key = key + "0000000000000000";
        }

        return key.substring(0, 16);
    }

    public static String md5String(String src) {
        char[] hexChar = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        byte[] b = src.getBytes();

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(b);
            byte[] b2 = md.digest();
            char[] str = new char[b2.length << 1];
            int len = 0;

            for(int i = 0; i < b2.length; ++i) {
                byte val = b2[i];
                str[len++] = hexChar[val >>> 4 & 15];
                str[len++] = hexChar[val & 15];
            }

            return new String(str);
        } catch (Exception var9) {
            logger.error("", var9);
            return "";
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println(decrypt("ZJVKZ7NF3QK6CST5SZNRYA7ERKEZMLINV5HUPF7ZV6T3DPYO2GBA", "tcbase.mmw.motor.realtime"));
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }
}
