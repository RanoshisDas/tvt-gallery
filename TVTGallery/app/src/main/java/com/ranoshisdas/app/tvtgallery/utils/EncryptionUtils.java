package com.ranoshisdas.app.tvtgallery.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    // Generate AES key from password
    private static Key generateKey(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(password.getBytes("UTF-8"));
        return new SecretKeySpec(key, ALGORITHM);
    }

    // Encrypt and save file
    public static void encryptFile(String password, File inputFile, File outputFile) throws Exception {
        Key secretKey = generateKey(password);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] inputBytes = new byte[(int) inputFile.length()];
        FileInputStream fis = new FileInputStream(inputFile);
        fis.read(inputBytes);
        fis.close();

        byte[] outputBytes = cipher.doFinal(inputBytes);
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(outputBytes);
        fos.close();
    }

    // Decrypt and retrieve file
    public static void decryptFile(String password, File inputFile, File outputFile) throws Exception {
        Key secretKey = generateKey(password);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] inputBytes = new byte[(int) inputFile.length()];
        FileInputStream fis = new FileInputStream(inputFile);
        fis.read(inputBytes);
        fis.close();

        byte[] outputBytes = cipher.doFinal(inputBytes);
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(outputBytes);
        fos.close();
    }
}
