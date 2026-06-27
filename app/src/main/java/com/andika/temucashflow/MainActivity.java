package com.andika.temucashflow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.ui.BaseActivity;
import com.andika.temucashflow.ui.dashboard.DashboardActivity;
import com.andika.temucashflow.ui.login.LoginActivity;
import com.andika.temucashflow.utils.NotificationHelper;

import java.util.concurrent.Executor;

public class MainActivity extends BaseActivity {

    private SharedPrefManager pref;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    NotificationHelper.scheduleDailyReminder(this);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = SharedPrefManager.getInstance(this);

        super.onCreate(savedInstanceState);
        
        // MATIKAN PRIVASI LAYAR (Izinkan Share Screen / Recording)
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. Kelola Notifikasi & Izin
        checkNotificationPermission();

        // 3. Splash screen delay 2 detik sebelum masuk ke logika navigasi
        new Handler(Looper.getMainLooper()).postDelayed(this::checkNavigation, 2000);
    }

    private void checkNotificationPermission() {
        if (pref.isReminderEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED) {
                    NotificationHelper.scheduleDailyReminder(this);
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            } else {
                NotificationHelper.scheduleDailyReminder(this);
            }
        }
    }

    private void checkNavigation() {
        if (pref.isLoggedIn()) {
            if (pref.isBiometricEnabled()) {
                authenticateBiometric();
            } else {
                navigateToDashboard();
            }
        } else {
            navigateToLogin();
        }
    }

    private void authenticateBiometric() {
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(MainActivity.this, "Autentikasi Error: " + errString, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    navigateToDashboard();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(MainActivity.this, "Sidik jari tidak dikenali", Toast.LENGTH_SHORT).show();
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Kunci Keamanan")
                    .setSubtitle("Masuk menggunakan sensor biometrik")
                    .setNegativeButtonText("Batal")
                    .build();

            biometricPrompt.authenticate(promptInfo);
        } else {
            navigateToDashboard();
        }
    }

    private void navigateToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
