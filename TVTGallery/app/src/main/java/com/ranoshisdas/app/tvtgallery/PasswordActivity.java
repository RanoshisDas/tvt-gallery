package com.ranoshisdas.app.tvtgallery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;

public class PasswordActivity extends AppCompatActivity {

    private EditText passwordInput;
    private Button loginButton;
    private int failedAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS = 3;

    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_BIOMETRICS_ENABLED = "biometrics_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        // Check if biometrics are enabled in SharedPreferences
        if (isBiometricEnabled() && isBiometricAvailable()) {
            showBiometricPrompt();
        } else {
            setupPasswordLogin(); // Show password login if biometrics are disabled/unavailable
        }
    }

    private boolean isBiometricEnabled() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_BIOMETRICS_ENABLED, true); // Default is false
    }

    private boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(this);
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Authentication successful!", Toast.LENGTH_SHORT).show();
                goToMainActivity();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                failedAttempts++;
                if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                    Toast.makeText(getApplicationContext(), "Too many failed attempts. Use password.", Toast.LENGTH_SHORT).show();
                    setupPasswordLogin();
                } else {
                    Toast.makeText(getApplicationContext(), "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
                    showBiometricPrompt(); // Retry biometrics
                }
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    setupPasswordLogin(); // If user cancels, force password authentication
                } else {
                    Toast.makeText(getApplicationContext(), "Error: " + errString, Toast.LENGTH_SHORT).show();
                }
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Use fingerprint or face recognition to authenticate")
                .setNegativeButtonText("Use Password") // Clicking this forces password input
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void setupPasswordLogin() {
        loginButton.setOnClickListener(v -> {
            String enteredPassword = passwordInput.getText().toString();
            if (checkPassword(enteredPassword)) {
                goToMainActivity();
            } else {
                Toast.makeText(getApplicationContext(), "Incorrect password. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkPassword(String enteredPassword) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String storedHashedPassword = sharedPreferences.getString(KEY_PASSWORD, null);

        // Hash the entered password before comparison
        String enteredHashedPassword = hashPassword(enteredPassword);

        return storedHashedPassword != null && storedHashedPassword.equals(enteredHashedPassword);
    }

    private static String hashPassword(String password) {
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

    private void goToMainActivity() {
        Intent intent = new Intent(PasswordActivity.this, SecreteMain.class);
        startActivity(intent);
        finish();
    }
}
