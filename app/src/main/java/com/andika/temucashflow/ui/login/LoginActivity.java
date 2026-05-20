package com.andika.temucashflow.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.andika.temucashflow.data.DatabaseHelper;
import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.databinding.ActivityLoginBinding;
import com.andika.temucashflow.ui.dashboard.DashboardActivity;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // MATIKAN PRIVASI LAYAR (Izinkan Share Screen / Recording)
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        // Check if already logged in
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = DatabaseHelper.getInstance(this);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            binding.etEmail.setError("Email harus diisi");
            binding.etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("Password harus diisi");
            binding.etPassword.requestFocus();
            return;
        }

        if (db.checkUser(email, password)) {
            long userId = db.getUserId(email);
            String name = db.getUserName(userId);
            SharedPrefManager.getInstance(this).saveUser(userId, name, email);

            Toast.makeText(this, "Selamat datang, " + name + "!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Email atau password salah!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
