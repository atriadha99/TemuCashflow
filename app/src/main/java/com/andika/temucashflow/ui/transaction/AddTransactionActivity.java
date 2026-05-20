package com.andika.temucashflow.ui.transaction;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.andika.temucashflow.R;
import com.andika.temucashflow.data.DatabaseHelper;
import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.databinding.ActivityAddTransactionBinding;
import com.andika.temucashflow.model.Transaction;
import com.andika.temucashflow.utils.DateUtils;
import com.andika.temucashflow.utils.NotificationHelper;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.Calendar;

public class AddTransactionActivity extends AppCompatActivity {

    private ActivityAddTransactionBinding binding;
    private DatabaseHelper db;
    private String currentType = "expense";
    private long selectedDate;
    private long editTransactionId = -1; // -1 berarti mode Tambah

    private final String[] incomeCategories = {"Gaji", "Investasi", "Freelance", "Hadiah", "Bonus", "Lainnya"};
    private final String[] expenseCategories = {"Makanan", "Transport", "Belanja", "Hiburan", "Kesehatan", "Pendidikan", "Tagihan", "Lainnya"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // MATIKAN PRIVASI LAYAR (Izinkan Share Screen / Recording)
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        binding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = DatabaseHelper.getInstance(this);
        
        // Ambil ID jika dikirim dari List (Mode Edit)
        editTransactionId = getIntent().getLongExtra("transaction_id", -1);
        
        setupListeners();

        if (editTransactionId != -1) {
            loadTransactionData();
            binding.toolbar.setTitle("Edit Transaksi");
            binding.btnSave.setText("Perbarui Transaksi");
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
        if ("income".equals(type)) {
            binding.btnIncome.setStrokeColorResource(R.color.green_income);
            binding.btnIncome.setTextColor(getResources().getColor(R.color.green_income));
            binding.btnIncome.setBackgroundColor(getResources().getColor(R.color.green_light));

            binding.btnExpense.setStrokeColorResource(R.color.text_muted);
            binding.btnExpense.setTextColor(getResources().getColor(R.color.text_muted));
            binding.btnExpense.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        } else {
            binding.btnExpense.setStrokeColorResource(R.color.red_expense);
            binding.btnExpense.setTextColor(getResources().getColor(R.color.red_expense));
            binding.btnExpense.setBackgroundColor(getResources().getColor(R.color.red_light));

            binding.btnIncome.setStrokeColorResource(R.color.text_muted);
            binding.btnIncome.setTextColor(getResources().getColor(R.color.text_muted));
            binding.btnIncome.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
        setupCategorySpinner();
    }

    private void setupListeners() {
        binding.btnIncome.setOnClickListener(v -> setType("income"));
        binding.btnExpense.setOnClickListener(v -> setType("expense"));
        binding.btnDate.setOnClickListener(v -> showDatePicker());
        binding.btnSave.setOnClickListener(v -> saveTransaction());
        binding.toolbar.setNavigationOnClickListener(v -> finish());
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
                checkBudgetWarning(amount, userId, pref);
                Toast.makeText(this, "Berhasil diperbarui", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            if (db.insertTransaction(t) != -1) {
                checkBudgetWarning(amount, userId, pref);
                Toast.makeText(this, "Berhasil disimpan", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void checkBudgetWarning(double addedAmount, long userId, SharedPrefManager pref) {
        if ("expense".equals(currentType)) {
            float budgetLimit = pref.getMonthlyBudget();
            if (budgetLimit > 0) {
                // Ambil awal bulan ini
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
        binding = null;
    }
}
