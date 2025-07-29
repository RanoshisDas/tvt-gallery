package com.ranoshisdas.app.tvtgallery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SetupPassword extends AppCompatActivity {

    private EditText etPassword, etConfirmPassword;
    private CheckBox cbEnableBiometric;
    private Button btnSetPassword;
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_BIOMETRICS_ENABLED = "biometrics_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_password);

        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbEnableBiometric = findViewById(R.id.cbEnableBiometric);
        btnSetPassword = findViewById(R.id.btnSetPassword);

        btnSetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPassword();
            }
        });
    }

    private void setPassword() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        boolean biometricEnabled = cbEnableBiometric.isChecked(); // Check if the user enabled biometrics

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        String hashedPassword = hashPassword(password);

        if (hashedPassword != null) {
            SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_PASSWORD, hashedPassword);
            editor.putBoolean(KEY_BIOMETRICS_ENABLED, biometricEnabled); // Save biometric preference
            editor.apply();

            Toast.makeText(this, "Password Set Successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SecreteMain.class)); // Go back to MainActivity
            finish();
        } else {
            Toast.makeText(this, "Error setting password", Toast.LENGTH_SHORT).show();
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
