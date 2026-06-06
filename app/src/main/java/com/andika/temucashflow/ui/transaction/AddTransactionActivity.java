package com.andika.temucashflow.ui.transaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.andika.temucashflow.R;
import com.andika.temucashflow.data.DatabaseHelper;
import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.databinding.ActivityAddTransactionBinding;
import com.andika.temucashflow.model.Transaction;
import com.andika.temucashflow.utils.DateUtils;
import com.andika.temucashflow.utils.IslandNotificationManager;
import com.andika.temucashflow.utils.NotificationHelper;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddTransactionActivity extends AppCompatActivity {

    private static final String TAG = "AddTransactionActivity";
    private ActivityAddTransactionBinding binding;
    private DatabaseHelper db;
    private IslandNotificationManager islandManager;
    private String currentType = "expense";
    private long selectedDate;
    private long editTransactionId = -1; // -1 berarti mode Tambah

    private final String[] incomeCategories = {"Gaji", "Investasi", "Freelance", "Hadiah", "Bonus", "Bank", "E-Wallet", "Tunai", "Lainnya"};
    private final String[] expenseCategories = {"Makanan", "Transport", "Belanja", "Hiburan", "Kesehatan", "Pendidikan", "Tagihan", "Bank", "E-Wallet", "Tunai", "Lainnya"};

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            processReceiptImage(imageBitmap);
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        processReceiptImage(bitmap);
                    } catch (IOException e) {
                        Log.e(TAG, "Error loading image from gallery", e);
                    }
                }
            }
    );

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean cameraGranted = result.getOrDefault(Manifest.permission.CAMERA, false);
                if (Boolean.TRUE.equals(cameraGranted)) {
                    showScanOptions();
                } else {
                    Toast.makeText(this, R.string.msg_camera_permission_required, Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // MATIKAN PRIVASI LAYAR (Izinkan Share Screen / Recording)
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        binding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = DatabaseHelper.getInstance(this);
        islandManager = new IslandNotificationManager(this);
        
        // Ambil ID jika dikirim dari List (Mode Edit)
        editTransactionId = getIntent().getLongExtra("transaction_id", -1);
        
        setupListeners();

        if (editTransactionId != -1) {
            loadTransactionData();
            binding.toolbar.setTitle(R.string.edit_name); 
            binding.btnSave.setText(R.string.update_label);
        } else {
            selectedDate = System.currentTimeMillis();
            initViews();
        }
    }

    private void initViews() {
        binding.tvDate.setText(DateUtils.formatDate(selectedDate));
        setType("expense");
    }

    private void loadTransactionData() {
        Transaction t = db.getTransactionById(editTransactionId);
        if (t != null) {
            selectedDate = t.getDate();
            binding.tvDate.setText(DateUtils.formatDate(selectedDate));
            binding.etAmount.setText(String.valueOf((int) t.getAmount()));
            binding.etDescription.setText(t.getDescription());
            
            setType(t.getType());
            
            // Set category spinner
            binding.spinnerCategory.setText(t.getCategory(), false);
        }
    }

    private void setupCategorySpinner() {
        String[] categories = "income".equals(currentType) ? incomeCategories : expenseCategories;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        binding.spinnerCategory.setAdapter(adapter);
        
        // Jika tidak sedang edit, default ke item pertama
        if (editTransactionId == -1) {
            binding.spinnerCategory.setText(categories[0], false);
        }
    }

    private void setType(String type) {
        currentType = type;
        int incomeColor = ContextCompat.getColor(this, R.color.green_income);
        int expenseColor = ContextCompat.getColor(this, R.color.red_expense);
        int mutedColor = ContextCompat.getColor(this, R.color.text_muted);
        int transparentColor = ContextCompat.getColor(this, android.R.color.transparent);

        if ("income".equals(type)) {
            binding.btnIncome.setStrokeColor(android.content.res.ColorStateList.valueOf(incomeColor));
            binding.btnIncome.setTextColor(incomeColor);
            binding.btnIncome.setBackgroundColor(ContextCompat.getColor(this, R.color.green_light));

            binding.btnExpense.setStrokeColor(android.content.res.ColorStateList.valueOf(mutedColor));
            binding.btnExpense.setTextColor(mutedColor);
            binding.btnExpense.setBackgroundColor(transparentColor);
        } else {
            binding.btnExpense.setStrokeColor(android.content.res.ColorStateList.valueOf(expenseColor));
            binding.btnExpense.setTextColor(expenseColor);
            binding.btnExpense.setBackgroundColor(ContextCompat.getColor(this, R.color.red_light));

            binding.btnIncome.setStrokeColor(android.content.res.ColorStateList.valueOf(mutedColor));
            binding.btnIncome.setTextColor(mutedColor);
            binding.btnIncome.setBackgroundColor(transparentColor);
        }
        setupCategorySpinner();
    }

    private void setupListeners() {
        binding.btnIncome.setOnClickListener(v -> setType("income"));
        binding.btnExpense.setOnClickListener(v -> setType("expense"));
        binding.btnDate.setOnClickListener(v -> showDatePicker());
        binding.btnSave.setOnClickListener(v -> saveTransaction());
        binding.btnScanReceipt.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                showScanOptions();
            } else {
                permissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
            }
        });
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void showScanOptions() {
        String[] options = {"Ambil Foto", "Pilih dari Galeri"};
        new AlertDialog.Builder(this)
                .setTitle("Scan Struk Transaksi")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                    } else {
                        galleryLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void processReceiptImage(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        Toast.makeText(this, R.string.msg_analyzing_receipt, Toast.LENGTH_SHORT).show();

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String fullText = visionText.getText();
                    
                    // 1. Extract Amount
                    double detectedAmount = extractAmountFromText(fullText);
                    if (detectedAmount > 0) {
                        binding.etAmount.setText(String.valueOf((int) detectedAmount));
                    }

                    // 2. Extract Merchant Name (usually first few lines)
                    String merchant = extractMerchant(fullText);
                    if (!merchant.isEmpty()) {
                        binding.etDescription.setText(merchant);
                    }

                    // 3. Extract Category based on keywords
                    String category = detectCategory(fullText);
                    binding.spinnerCategory.setText(category, false);

                    if (detectedAmount > 0 || !merchant.isEmpty()) {
                        Toast.makeText(this, "Scan berhasil!", Toast.LENGTH_SHORT).show();
                        
                        // Show Island Notification for scan result
                        Transaction mock = new Transaction(currentType, detectedAmount, category, merchant, selectedDate, 0);
                        if ("income".equals(currentType)) {
                            islandManager.showIncome(detectedAmount, mock);
                        } else {
                            islandManager.showExpense(detectedAmount, mock);
                        }
                    } else {
                        Toast.makeText(this, R.string.msg_detect_amount_failed, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(AddTransactionActivity.this, R.string.msg_process_image_failed, Toast.LENGTH_SHORT).show());
    }

    private String extractMerchant(String text) {
        String[] lines = text.split("\n");
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            // Skip if it looks like date or random numbers
            if (firstLine.matches(".*[a-zA-Z]{3,}.*")) {
                return firstLine;
            }
        }
        return "";
    }

    private String detectCategory(String text) {
        String lowerText = text.toLowerCase();
        
        if (lowerText.contains("alfamart") || lowerText.contains("indomaret") || lowerText.contains("supermarket") || 
            lowerText.contains("belanja") || lowerText.contains("mart") || lowerText.contains("grocery")) {
            return "Belanja";
        } else if (lowerText.contains("kfc") || lowerText.contains("mcd") || lowerText.contains("food") || 
                   lowerText.contains("resto") || lowerText.contains("kopi") || lowerText.contains("makanan") || 
                   lowerText.contains("bakso") || lowerText.contains("mie") || lowerText.contains("warung")) {
            return "Makanan";
        } else if (lowerText.contains("pertamina") || lowerText.contains("shell") || lowerText.contains("gojek") || 
                   lowerText.contains("grab") || lowerText.contains("transport") || lowerText.contains("parkir")) {
            return "Transport";
        } else if (lowerText.contains("bioskop") || lowerText.contains("cinema") || lowerText.contains("hiburan") || 
                   lowerText.contains("game") || lowerText.contains("tonton")) {
            return "Hiburan";
        } else if (lowerText.contains("apotek") || lowerText.contains("klinik") || lowerText.contains("kesehatan") || 
                   lowerText.contains("obat") || lowerText.contains("rs")) {
            return "Kesehatan";
        } else if (lowerText.contains("listrik") || lowerText.contains("pdam") || lowerText.contains("pulsa") || 
                   lowerText.contains("tagihan") || lowerText.contains("telkom") || lowerText.contains("wifi")) {
            return "Tagihan";
        }
        
        return "Lainnya";
    }

    private double extractAmountFromText(String text) {
        Pattern pattern = Pattern.compile("(?i)(total|jumlah|amount|rp|bayar)?\\s*[:.]?\\s*(\\d{1,3}([.,]\\d{3})*|\\d+)");
        Matcher matcher = pattern.matcher(text.replace("\n", " "));

        double maxAmount = 0;
        while (matcher.find()) {
            try {
                String group2 = matcher.group(2);
                if (group2 != null) {
                    String amountStr = group2.replaceAll("[.,]", "");
                    double amount = Double.parseDouble(amountStr);
                    // Standard logic for receipt totals: biasanya nominal terbesar di atas 1000
                    if (amount > 1000 && amount > maxAmount) {
                        maxAmount = amount;
                    }
                }
            } catch (Exception ignored) {}
        }
        return maxAmount;
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih Tanggal")
                .setSelection(selectedDate)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = selection;
            binding.tvDate.setText(DateUtils.formatDate(selectedDate));
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void saveTransaction() {
        if (binding.etAmount.getText() == null || binding.etDescription.getText() == null) return;

        String amountStr = binding.etAmount.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String category = binding.spinnerCategory.getText().toString();

        if (amountStr.isEmpty()) {
            binding.etAmount.setError("Jumlah harus diisi");
            return;
        }

        if (description.isEmpty()) {
            binding.etDescription.setError("Deskripsi harus diisi");
            return;
        }

        double amount = Double.parseDouble(amountStr);
        SharedPrefManager pref = SharedPrefManager.getInstance(this);
        long userId = pref.getUserId();

        Transaction t = new Transaction(currentType, amount, category, description, selectedDate, userId);
        
        if (editTransactionId != -1) {
            t.setId(editTransactionId);
            if (db.updateTransaction(t)) {
                checkBudgetWarning(userId, pref);
                Toast.makeText(this, "Berhasil diperbarui", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            if (db.insertTransaction(t) != -1) {
                checkBudgetWarning(userId, pref);
                Toast.makeText(this, "Berhasil disimpan", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void checkBudgetWarning(long userId, SharedPrefManager pref) {
        if ("expense".equals(currentType)) {
            float budgetLimit = pref.getMonthlyBudget();
            if (budgetLimit > 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                
                double monthlyExpense = db.getMonthlyExpense(userId, calendar.getTimeInMillis());
                
                if (monthlyExpense > budgetLimit) {
                    NotificationHelper.showBudgetWarning(this, monthlyExpense, budgetLimit);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (islandManager != null) {
            islandManager.destroy();
        }
        binding = null;
    }
}