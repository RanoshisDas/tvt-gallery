package com.ranoshisdas.app.tvtgallery.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageStorageHelper {

    private static final String DIRECTORY_NAME = "EncryptedGallery";

    private static String  encryptionPassword;


    // Fetch Cloudinary credentials from SharedPreferences
    public static void setConfig(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        encryptionPassword = sharedPreferences.getString("password", null); //hash password
    }

    // Get external storage directory for encrypted images
    public static File getStorageDirectory() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), DIRECTORY_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    // Save and encrypt an image, then upload it to Cloudinary
    public static void saveEncryptedImage(Context context, File originalFile) {
        try {
            setConfig(context);

            File encryptedFile = new File(getStorageDirectory(), originalFile.getName() + ".enc");
            EncryptionUtils.encryptFile(encryptionPassword, originalFile, encryptedFile);

            boolean delete = originalFile.delete();
        } catch (Exception e) {
            Log.e("EncryptionError", "Error encrypting and uploading file", e);
        }
    }

    // Retrieve and decrypt image
    public static File getDecryptedImage(Context context, String imageName) {
        try {
            setConfig(context);
            File encryptedFile = new File(getStorageDirectory(), imageName + ".enc");
            File decryptedFile = new File(context.getCacheDir(), imageName);
            EncryptionUtils.decryptFile(encryptionPassword, encryptedFile, decryptedFile);
            return decryptedFile;
        } catch (Exception e) {
            Log.e("DecryptionError", "Error decrypting file", e);
            return null;
        }
    }

    // Restore decrypted image to Pictures folder
    public static boolean restoreImage(Context context, String imageName) {
        setConfig(context);
        File encryptedFile = new File(getStorageDirectory(), imageName + ".enc");

        if (!encryptedFile.exists()) {
            Log.e("Restore", "Encrypted file not found!");
            return false;
        }

        try {
            File decryptedFile = new File(context.getCacheDir(), imageName);
            EncryptionUtils.decryptFile(encryptionPassword, encryptedFile, decryptedFile);
            if (saveImageToGallery(context, decryptedFile)) {
                deleteEncryptedImage(imageName);
                return true;
            }
        } catch (Exception e) {
            Log.e("RestoreError", "Error restoring image", e);
        }
        return false;
    }

    // Save decrypted image to Pictures folder
    private static boolean saveImageToGallery(Context context, File file) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            Uri imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                try (OutputStream outputStream = context.getContentResolver().openOutputStream(imageUri);
                     InputStream inputStream = context.getContentResolver().openInputStream(Uri.fromFile(file))) {

                    if (outputStream != null && inputStream != null) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            Log.e("SaveGalleryError", "Error saving image to gallery", e);
        }
        return false;
    }

    // Delete encrypted image
    public static boolean deleteEncryptedImage(String imageName) {
        File encryptedFile = new File(getStorageDirectory(), imageName + ".enc");
        return encryptedFile.exists() && encryptedFile.delete();
    }
}
