package com.andika.temucashflow.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.fragment.app.Fragment;

import com.andika.temucashflow.R;
import com.andika.temucashflow.data.DatabaseHelper;
import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.databinding.FragmentProfileBinding;
import com.andika.temucashflow.model.Transaction;
import com.andika.temucashflow.ui.login.LoginActivity;
import com.andika.temucashflow.utils.CurrencyFormatter;
import com.andika.temucashflow.utils.DateUtils;
import com.andika.temucashflow.utils.FinancialHealthCalculator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
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

    private final ActivityResultLauncher<String> exportCsvLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("text/csv"),
                    uri -> {
                        if (uri != null) {
                            performExportCsv(uri);
                        }
                    });

    private final ActivityResultLauncher<String> exportPdfLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"),
                    uri -> {
                        if (uri != null) {
                            performExportPdf(uri);
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());
        pref = SharedPrefManager.getInstance(requireContext());
        userId = pref.getUserId();

        initViews();
        setupListeners();
        loadStats();
    }

    private void initViews() {
        binding.tvUserName.setText(pref.getUserName());
        binding.tvUserEmail.setText(pref.getUserEmail());
        
        int mode = pref.getThemeMode();
        binding.switchDarkMode.setChecked(mode == 2);

        binding.switchBiometric.setChecked(pref.isBiometricEnabled());
        binding.switchShake.setChecked(pref.isShakeEnabled());
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
                BiometricManager biometricManager = BiometricManager.from(requireContext());
                int canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
                if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
                    pref.setBiometricEnabled(true);
                    Toast.makeText(requireContext(), "Kunci biometrik diaktifkan", Toast.LENGTH_SHORT).show();
                } else {
                    binding.switchBiometric.setChecked(false);
                    Toast.makeText(requireContext(), "Hardware biometrik tidak tersedia", Toast.LENGTH_SHORT).show();
                }
            } else {
                pref.setBiometricEnabled(false);
                Toast.makeText(requireContext(), "Kunci biometrik dimatikan", Toast.LENGTH_SHORT).show();
            }
        });

        binding.switchShake.setOnCheckedChangeListener((buttonView, isChecked) -> pref.setShakeEnabled(isChecked));

        binding.layoutProfile.setOnClickListener(v -> showEditNameDialog());
        
        binding.btnAccessibility.setOnClickListener(v -> startActivity(new Intent(requireContext(), AccessibilitySettingsActivity.class)));
        
        binding.btnExportExcel.setOnClickListener(v -> {
            String date = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
            exportExcelLauncher.launch("Laporan_TemuCashflow_" + date + ".xlsx");
        });

        binding.btnExportCsv.setOnClickListener(v -> {
            String date = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
            exportCsvLauncher.launch("Laporan_TemuCashflow_" + date + ".csv");
        });

        binding.btnExportPdf.setOnClickListener(v -> {
            String date = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
            exportPdfLauncher.launch("Laporan_TemuCashflow_" + date + ".pdf");
        });

        binding.btnExportJson.setOnClickListener(v -> {
            String date = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
            exportJsonLauncher.launch("TemuCashflow_Backup_" + date + ".json");
        });

        binding.btnResetData.setOnClickListener(v -> showResetDataDialog());
        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadStats() {
        List<Transaction> transactions = db.getAllTransactions(userId);
        binding.tvTotalTransactions.setText(String.valueOf(transactions.size()));
        
        double income = db.getTotalIncome(userId);
        double expense = db.getTotalExpense(userId);
        double savings = income - expense;
        
        FinancialHealthCalculator.HealthResult health = FinancialHealthCalculator.calculate(income, expense, savings > 0 ? savings : 0);
        binding.tvHealthScore.setText(String.valueOf(health.score));
        binding.tvHealthScore.setTextColor(health.color);

        binding.tvTotalIncome.setText(CurrencyFormatter.format(income));
        binding.tvTotalExpense.setText(CurrencyFormatter.format(expense));
        binding.tvCurrentSavings.setText(CurrencyFormatter.format(savings));

        long creationDate = db.getUserCreationDate(userId);
        if (creationDate > 0) {
            String memberSinceDate = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date(creationDate));
            binding.tvMemberSince.setText(getString(R.string.member_since, memberSinceDate));
        }
        
        // Mock streak and badge for now
        binding.tvStreak.setText(getString(R.string.streak_format, 7));
        binding.tvBadge.setText(R.string.badge_pro);
    }

    private void showEditNameDialog() {
        EditText etName = new EditText(requireContext());
        etName.setText(pref.getUserName());
        etName.setHint("Nama Baru");

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(60, 20, 60, 0);
        etName.setLayoutParams(lp);
        container.addView(etName);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ubah Nama Panggilan")
                .setView(container)
                .setPositiveButton(R.string.save_label, (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        if (db.updateUserName(userId, newName)) {
                            pref.saveUser(userId, newName, pref.getUserEmail());
                            binding.tvUserName.setText(newName);
                            Toast.makeText(requireContext(), "Nama berhasil diubah", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel_label, null)
                .show();
    }

    private void performExportCsv(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
                StringBuilder csv = new StringBuilder("ID,Tipe,Jumlah,Kategori,Keterangan,Tanggal\n");
                List<Transaction> transactions = db.getAllTransactions(userId);
                for (Transaction t : transactions) {
                    csv.append(t.getId()).append(",")
                       .append(t.getType()).append(",")
                       .append(t.getAmount()).append(",")
                       .append("\"").append(t.getCategory()).append("\",")
                       .append("\"").append(t.getDescription()).append("\",")
                       .append(DateUtils.formatDate(t.getDate())).append("\n");
                }
                if (os != null) {
                    os.write(csv.toString().getBytes());
                }
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "CSV berhasil disimpan", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error exporting CSV", e);
            }
        });
    }

    private void performExportPdf(Uri uri) {
        // Implementation for PDF export using PdfDocument (simplified)
        Executors.newSingleThreadExecutor().execute(() -> {
            try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
                android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
                android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create();
                android.graphics.pdf.PdfDocument.Page page = document.startPage(pageInfo);
                
                android.graphics.Canvas canvas = page.getCanvas();
                android.graphics.Paint paint = new android.graphics.Paint();
                paint.setTextSize(12);
                
                int y = 50;
                canvas.drawText("Laporan Transaksi TemuCashflow", 50, y, paint);
                y += 30;
                
                List<Transaction> transactions = db.getAllTransactions(userId);
                for (Transaction t : transactions) {
                    String line = String.format("%s | %s | %s | %s", 
                        DateUtils.formatDate(t.getDate()), t.getType(), t.getCategory(), CurrencyFormatter.format(t.getAmount()));
                    canvas.drawText(line, 50, y, paint);
                    y += 20;
                    if (y > 800) break; // Simplified: one page only
                }
                
                document.finishPage(page);
                document.writeTo(os);
                document.close();
                
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "PDF berhasil disimpan", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error exporting PDF", e);
            }
        });
    }

    private void performExportExcel(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (Workbook workbook = new XSSFWorkbook();
                 OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
                
                Sheet sheet = workbook.createSheet("Laporan Transaksi");
                Row headerRow = sheet.createRow(0);
                String[] columns = {"ID", "Tipe", "Jumlah", "Kategori", "Keterangan", "Tanggal"};
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                }
                
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
                
                workbook.write(os);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Laporan Excel berhasil disimpan!", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error exporting Excel", e);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Gagal export Excel: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void performExportJson(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
                String json = db.exportToJson(userId);
                if (os != null) {
                    os.write(json.getBytes());
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Backup JSON berhasil disimpan", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error exporting JSON", e);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Gagal export JSON: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void showResetDataDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Hapus Semua Data")
                .setMessage("Tindakan ini akan menghapus seluruh catatan transaksi Anda secara permanen. Lanjutkan?")
                .setPositiveButton("Hapus Semua", (dialog, which) -> {
                    db.deleteAllTransactions(userId);
                    loadStats();
                    Toast.makeText(requireContext(), "Semua data transaksi berhasil dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel_label, null)
                .show();
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout)
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    pref.clear();
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton(R.string.cancel_label, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
