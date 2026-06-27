package com.andika.temucashflow.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.andika.temucashflow.R;
import com.andika.temucashflow.data.DatabaseHelper;
import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.databinding.ActivitySettingsBinding;
import com.andika.temucashflow.model.Transaction;
import com.andika.temucashflow.ui.BaseActivity;
import com.andika.temucashflow.ui.analytics.AnalyticsActivity;
import com.andika.temucashflow.ui.dashboard.DashboardActivity;
import com.andika.temucashflow.ui.login.LoginActivity;
import com.andika.temucashflow.ui.transaction.TransactionListActivity;
import com.andika.temucashflow.utils.CurrencyFormatter;
import com.andika.temucashflow.utils.DateUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";
    private ActivitySettingsBinding binding;
    private DatabaseHelper db;
    private SharedPrefManager pref;
    private long userId;

    private final ActivityResultLauncher<String> exportJsonLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"),
                    uri -> {
                        if (uri != null) {
                            performExportJson(uri);
                        }
                    });

    private final ActivityResultLauncher<String> exportExcelLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                    uri -> {
                        if (uri != null) {
                            performExportExcel(uri);
                        }
                    });

    private final ActivityResultLauncher<String[]> importJsonLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri != null) {
                            performImportJson(uri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Edge to edge setup
        setupEdgeToEdge();

        // MATIKAN PRIVASI LAYAR (Izinkan Share Screen / Recording)
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle WindowInsets for padding
        setupWindowInsets();

        db = DatabaseHelper.getInstance(this);
        pref = SharedPrefManager.getInstance(this);
        userId = pref.getUserId();

        initViews();
        setupListeners();
        setupBottomNav();
    }

    private void setupEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | 
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.coordinator, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout());
            
            int paddingTop = Math.max(systemBars.top, displayCutout.top);
            
            // Header padding
            binding.mainContainer.setPadding(
                binding.mainContainer.getPaddingLeft(),
                paddingTop,
                binding.mainContainer.getPaddingRight(),
                binding.mainContainer.getPaddingBottom()
            );
            
            // Bottom nav padding
            binding.bottomNav.setPadding(
                systemBars.left,
                0,
                systemBars.right,
                systemBars.bottom
            );
            
            return insets;
        });
    }

    private void initViews() {
        binding.tvUserName.setText(pref.getUserName());
        binding.tvUserEmail.setText(pref.getUserEmail());
        
        int mode = pref.getThemeMode();
        binding.switchDarkMode.setChecked(mode == 2);

        binding.switchBiometric.setChecked(pref.isBiometricEnabled());
        updateBudgetStatus();
    }

    private void updateBudgetStatus() {
        float budget = pref.getMonthlyBudget();
        if (budget > 0) {
            binding.tvBudgetStatus.setText(getString(R.string.limit_format, CurrencyFormatter.format(budget)));
        } else {
            binding.tvBudgetStatus.setText(R.string.label_not_set);
        }
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_settings);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_transactions) {
                startActivity(new Intent(this, TransactionListActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_analytics) {
                startActivity(new Intent(this, AnalyticsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return id == R.id.nav_settings;
        });
    }

    private void setupListeners() {
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int mode = isChecked ? 2 : 1;
            pref.setThemeMode(mode);
            AppCompatDelegate.setDefaultNightMode(isChecked ? 
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        binding.switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                BiometricManager biometricManager = BiometricManager.from(this);
                int canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
                if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
                    pref.setBiometricEnabled(true);
                    Toast.makeText(this, "Kunci biometrik diaktifkan", Toast.LENGTH_SHORT).show();
                } else {
                    binding.switchBiometric.setChecked(false);
                    Toast.makeText(this, "Hardware biometrik tidak tersedia", Toast.LENGTH_SHORT).show();
                }
            } else {
                pref.setBiometricEnabled(false);
                Toast.makeText(this, "Kunci biometrik dimatikan", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnEditName.setOnClickListener(v -> showEditNameDialog());
        binding.layoutProfile.setOnClickListener(v -> showEditNameDialog());
        binding.btnSetBudget.setOnClickListener(v -> showSetBudgetDialog());
        
        binding.btnExportExcel.setOnClickListener(v -> {
            String date = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
            exportExcelLauncher.launch("Laporan_TemuCashflow_" + date + ".xlsx");
        });

        binding.btnExportJson.setOnClickListener(v -> {
            String date = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
            exportJsonLauncher.launch("TemuCashflow_Backup_" + date + ".json");
        });

        binding.btnImportJson.setOnClickListener(v -> importJsonLauncher.launch(new String[]{"application/json"}));

        binding.btnResetData.setOnClickListener(v -> showResetDataDialog());
        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showSetBudgetDialog() {
        EditText etBudget = new EditText(this);
        etBudget.setInputType(InputType.TYPE_CLASS_NUMBER);
        float currentBudget = pref.getMonthlyBudget();
        if (currentBudget > 0) {
            etBudget.setText(String.valueOf((int)currentBudget));
        }
        etBudget.setHint("Masukkan limit bulanan");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(60, 20, 60, 0);
        etBudget.setLayoutParams(lp);
        container.addView(etBudget);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Atur Anggaran Bulanan")
                .setMessage("Dapatkan notifikasi jika pengeluaran belanja melebihi batas ini.")
                .setView(container)
                .setPositiveButton(R.string.save_label, (dialog, which) -> {
                    String budgetStr = etBudget.getText().toString().trim();
                    if (!budgetStr.isEmpty()) {
                        float budget = Float.parseFloat(budgetStr);
                        pref.setMonthlyBudget(budget);
                        updateBudgetStatus();
                        Toast.makeText(this, "Anggaran berhasil disimpan", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Hapus Limit", (dialog, which) -> {
                    pref.setMonthlyBudget(0);
                    updateBudgetStatus();
                    Toast.makeText(this, "Limit anggaran dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel_label, null)
                .show();
    }

    private void showEditNameDialog() {
        EditText etName = new EditText(this);
        etName.setText(pref.getUserName());
        etName.setHint("Nama Baru");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(60, 20, 60, 0);
        etName.setLayoutParams(lp);
        container.addView(etName);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Ubah Nama Panggilan")
                .setView(container)
                .setPositiveButton(R.string.save_label, (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        if (db.updateUserName(userId, newName)) {
                            pref.saveUser(userId, newName, pref.getUserEmail());
                            binding.tvUserName.setText(newName);
                            Toast.makeText(this, "Nama berhasil diubah", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel_label, null)
                .show();
    }

    private void performExportExcel(Uri uri) {
        Toast.makeText(this, "Mengekspor data ke Excel...", Toast.LENGTH_SHORT).show();
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try (Workbook workbook = new XSSFWorkbook();
                 OutputStream os = getContentResolver().openOutputStream(uri)) {
                
                Sheet sheet = workbook.createSheet("Laporan Transaksi");
                
                // Style Header sederhana
                Row headerRow = sheet.createRow(0);
                String[] columns = {"ID", "Tipe", "Jumlah", "Kategori", "Keterangan", "Tanggal"};
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                }
                
                // Data
                List<Transaction> transactions = db.getAllTransactions(userId);
                int rowNum = 1;
                for (Transaction t : transactions) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(t.getId());
                    row.createCell(1).setCellValue(t.getType());
                    row.createCell(2).setCellValue(t.getAmount());
                    row.createCell(3).setCellValue(t.getCategory());
                    row.createCell(4).setCellValue(t.getDescription());
                    row.createCell(5).setCellValue(DateUtils.formatDate(t.getDate()));
                }
                
                // Auto-size columns
                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }
                
                workbook.write(os);
                runOnUiThread(() -> Toast.makeText(this, "Laporan Excel berhasil disimpan!", Toast.LENGTH_LONG).show());
                
            } catch (Exception e) {
                Log.e(TAG, "Error exporting Excel", e);
                runOnUiThread(() -> Toast.makeText(this, "Gagal export Excel: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void performExportJson(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                String json = db.exportToJson(userId);
                if (os != null) {
                    os.write(json.getBytes());
                    runOnUiThread(() -> Toast.makeText(this, "Backup JSON berhasil disimpan", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error exporting JSON", e);
                runOnUiThread(() -> Toast.makeText(this, "Gagal export JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void performImportJson(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                int count = db.importFromJson(userId, sb.toString());
                runOnUiThread(() -> Toast.makeText(this, count + " transaksi berhasil di-import", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e(TAG, "Error importing JSON", e);
                runOnUiThread(() -> Toast.makeText(this, "Gagal import: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showResetDataDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Hapus Semua Data")
                .setMessage("Tindakan ini akan menghapus seluruh catatan transaksi Anda secara permanen. Lanjutkan?")
                .setPositiveButton("Hapus Semua", (dialog, which) -> {
                    db.deleteAllTransactions(userId);
                    Toast.makeText(this, "Semua data transaksi berhasil dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel_label, null)
                .show();
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout)
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    pref.clear();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.cancel_label, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNav.setSelectedItemId(R.id.nav_settings);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}