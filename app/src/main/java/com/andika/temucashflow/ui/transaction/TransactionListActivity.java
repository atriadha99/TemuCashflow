package com.andika.temucashflow.ui.transaction;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.andika.temucashflow.R;
import com.andika.temucashflow.adapter.TransactionAdapter;
import com.andika.temucashflow.data.DatabaseHelper;
import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.databinding.ActivityTransactionListBinding;
import com.andika.temucashflow.model.Transaction;
import com.andika.temucashflow.ui.BaseActivity;
import com.andika.temucashflow.ui.analytics.AnalyticsActivity;
import com.andika.temucashflow.ui.dashboard.DashboardActivity;
import com.andika.temucashflow.ui.settings.SettingsActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class TransactionListActivity extends BaseActivity {

    private ActivityTransactionListBinding binding;
    private DatabaseHelper db;
    private TransactionAdapter adapter;
    private long userId;
    private String currentFilter = "all";
    private String currentSort = "date";
    private String currentOrder = "desc";
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Edge to edge setup
        setupEdgeToEdge();

        // MATIKAN PRIVASI LAYAR (Izinkan Share Screen / Recording)
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        binding = ActivityTransactionListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle WindowInsets for padding
        setupWindowInsets();

        db = DatabaseHelper.getInstance(this);
        userId = SharedPrefManager.getInstance(this).getUserId();

        setupFilterSpinner();
        setupSortSpinner();
        setupSearch();
        setupRecyclerView();
        setupBottomNav();
        
        // Cek filter awal dari intent
        String filter = getIntent().getStringExtra("filter");
        if (filter != null) {
            currentFilter = filter;
            if ("income".equals(filter)) binding.spinnerFilter.setSelection(1);
            else if ("expense".equals(filter)) binding.spinnerFilter.setSelection(2);
        }
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

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_transactions);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_analytics) {
                startActivity(new Intent(this, AnalyticsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return id == R.id.nav_transactions;
        });
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                loadTransactions();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                loadTransactions();
                return true;
            }
        });
    }

    private void setupFilterSpinner() {
        String[] filters = {"Semua Transaksi", "Pemasukan saja", "Pengeluaran saja"};
        ArrayAdapter<String> adapterFilter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filters);
        adapterFilter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilter.setAdapter(adapterFilter);

        binding.spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: currentFilter = "all"; break;
                    case 1: currentFilter = "income"; break;
                    case 2: currentFilter = "expense"; break;
                }
                loadTransactions();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSortSpinner() {
        String[] sorts = {"Tanggal (Terbaru)", "Tanggal (Terlama)", "Jumlah (Terbesar)", "Jumlah (Terkecil)"};
        ArrayAdapter<String> adapterSort = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, sorts);
        adapterSort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSort.setAdapter(adapterSort);

        binding.spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: currentSort = "date"; currentOrder = "desc"; break;
                    case 1: currentSort = "date"; currentOrder = "asc"; break;
                    case 2: currentSort = "amount"; currentOrder = "desc"; break;
                    case 3: currentSort = "amount"; currentOrder = "asc"; break;
                }
                loadTransactions();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this);
        adapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                Intent intent = new Intent(TransactionListActivity.this, AddTransactionActivity.class);
                intent.putExtra("transaction_id", transaction.getId());
                startActivity(intent);
            }

            @Override
            public void onTransactionLongClick(Transaction transaction) {
                showDeleteDialog(transaction);
            }
        });
        binding.rvTransactions.setAdapter(adapter);
    }

    private void loadTransactions() {
        // Memanggil DatabaseHelper dengan parameter query pencarian
        List<Transaction> transactions = db.getTransactionsFiltered(userId, currentFilter, currentSort, currentOrder, currentQuery);
        adapter.setTransactions(transactions);
        binding.tvCount.setText(getString(R.string.msg_transactions_found_format, transactions.size()));
    }

    private void showDeleteDialog(Transaction transaction) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Transaksi")
            .setMessage("Yakin ingin menghapus \"" + transaction.getDescription() + "\"?")
            .setPositiveButton("Hapus", (dialog, which) -> {
                db.deleteTransaction(transaction.getId());
                loadTransactions();
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions();
        binding.bottomNav.setSelectedItemId(R.id.nav_transactions);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
