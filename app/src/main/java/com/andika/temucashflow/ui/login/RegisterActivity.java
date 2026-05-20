package com.andika.temucashflow.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.andika.temucashflow.data.DatabaseHelper;
import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.databinding.ActivityRegisterBinding;
import com.andika.temucashflow.ui.dashboard.DashboardActivity;
import com.google.android.material.snackbar.Snackbar;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // MATIKAN PRIVASI LAYAR (Izinkan Share Screen / Recording)
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = DatabaseHelper.getInstance(this);

        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etName.setError("Nama harus diisi");
            binding.etName.requestFocus();
            return;
        }

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

        if (password.length() < 6) {
            binding.etPassword.setError("Password minimal 6 karakter");
            binding.etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Password tidak cocok");
            binding.etConfirmPassword.requestFocus();
            return;
        }

        long userId = db.registerUser(name, email, password);
        
        if (userId != -1) {
            SharedPrefManager.getInstance(this).saveUser(userId, name, email);
            Toast.makeText(this, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else {
            Snackbar.make(binding.getRoot(), "Email sudah terdaftar!", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
