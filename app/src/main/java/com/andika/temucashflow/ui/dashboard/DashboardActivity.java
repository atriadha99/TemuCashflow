package com.andika.temucashflow.ui.dashboard;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.andika.temucashflow.R;
import com.andika.temucashflow.adapter.TransactionAdapter;
import com.andika.temucashflow.data.DatabaseHelper;
import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.databinding.ActivityDashboardBinding;
import com.andika.temucashflow.model.Transaction;
import com.andika.temucashflow.ui.analytics.AnalyticsActivity;
import com.andika.temucashflow.ui.settings.SettingsActivity;
import com.andika.temucashflow.ui.transaction.AddTransactionActivity;
import com.andika.temucashflow.ui.transaction.TransactionListActivity;
import com.andika.temucashflow.utils.CurrencyFormatter;
import com.andika.temucashflow.utils.DateUtils;
import com.andika.temucashflow.utils.IslandNotificationManager;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private DatabaseHelper db;
    private TransactionAdapter adapter;
    private IslandNotificationManager islandManager;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Edge to Edge setup
        setupEdgeToEdge();

        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // MATIKAN PRIVASI LAYAR (Izinkan Share Screen / Recording)
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        // Handle WindowInsets for padding
        setupWindowInsets();

        db = DatabaseHelper.getInstance(this);
        userId = SharedPrefManager.getInstance(this).getUserId();
        islandManager = new IslandNotificationManager(this);

        initViews();
        setupRecyclerView();
        setupBottomNav();
        setupListeners();
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
            
            // INCREASED PADDING: status bar height + cutout + extra padding for the clock
            int paddingTop = Math.max(systemBars.top, displayCutout.top) + (int)(32 * getResources().getDisplayMetrics().density);
            
            binding.headerContainer.setPadding(
                binding.headerContainer.getPaddingLeft(),
                paddingTop,
                binding.headerContainer.getPaddingRight(),
                binding.headerContainer.getPaddingBottom()
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
        String userName = SharedPrefManager.getInstance(this).getUserName();
        binding.tvGreeting.setText(getString(R.string.greeting_format, userName));
        binding.tvDate.setText(DateUtils.formatFullDate(System.currentTimeMillis()));
    }

    private void setupRecyclerView() {
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this);
        adapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                Intent intent = new Intent(DashboardActivity.this, AddTransactionActivity.class);
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

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_home);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_transactions) {
                startActivity(new Intent(this, TransactionListActivity.class));
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
            return false;
        });
    }

    private void setupListeners() {
        binding.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddTransactionActivity.class)));

        binding.btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(0, 0);
        });

        binding.cardIncome.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionListActivity.class);
            intent.putExtra("filter", "income");
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        binding.cardExpense.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionListActivity.class);
            intent.putExtra("filter", "expense");
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        binding.tvSeeAll.setOnClickListener(v -> {
            startActivity(new Intent(this, TransactionListActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    private void loadData() {
        double totalIncome = db.getTotalIncome(userId);
        double totalExpense = db.getTotalExpense(userId);
        double balance = totalIncome - totalExpense;

        binding.tvTotalIncome.setText(CurrencyFormatter.format(totalIncome));
        binding.tvTotalExpense.setText(CurrencyFormatter.format(totalExpense));
        binding.tvBalance.setText(CurrencyFormatter.format(balance));

        // Show Island Notification for most recent transaction
        List<Transaction> allTransactions = db.getAllTransactions(userId);
        if (!allTransactions.isEmpty()) {
            Transaction recent = allTransactions.get(0);
            islandManager.showSummary(totalIncome, totalExpense, balance, recent);
        }

        if (totalIncome > 0) {
            int expensePercent = (int) ((totalExpense / totalIncome) * 100);
            int balancePercent = 100 - expensePercent;
            binding.tvBalancePercent.setText(getString(R.string.balance_remaining_format, balancePercent));
            
            if (expensePercent > 80) {
                binding.tvBalancePercent.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red_light)));
                binding.tvBalancePercent.setTextColor(ContextCompat.getColor(this, R.color.red_expense));
            } else {
                binding.tvBalancePercent.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue_light)));
                binding.tvBalancePercent.setTextColor(ContextCompat.getColor(this, R.color.blue_balance));
            }
        } else {
            binding.tvBalancePercent.setText(getString(R.string.label_default_amount));
        }

        updateBudgetProgress();

        if (!allTransactions.isEmpty()) {
            adapter.setTransactions(allTransactions.subList(0, Math.min(allTransactions.size(), 5)));
        } else {
            adapter.setTransactions(new ArrayList<>());
        }

        setupChart(totalIncome, totalExpense);
    }

    private void updateBudgetProgress() {
        float budgetLimit = SharedPrefManager.getInstance(this).getMonthlyBudget();
        if (budgetLimit > 0) {
            binding.cardBudget.setVisibility(View.VISIBLE);
            
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            
            double monthlyExpense = db.getMonthlyExpense(userId, cal.getTimeInMillis());
            int progress = (int) ((monthlyExpense / budgetLimit) * 100);
            
            binding.progressBudget.setProgress(Math.min(progress, 100));
            String budgetText = CurrencyFormatter.format(monthlyExpense) + " / " + CurrencyFormatter.format(budgetLimit);
            binding.tvBudgetAmount.setText(budgetText);
            
            if (progress < 80) {
                binding.tvBudgetStatus.setText(R.string.budget_status_good);
                binding.tvBudgetStatus.setTextColor(ContextCompat.getColor(this, R.color.green_income));
            } else if (progress <= 100) {
                binding.tvBudgetStatus.setText(R.string.budget_status_warning);
                binding.tvBudgetStatus.setTextColor(ContextCompat.getColor(this, R.color.orange_warning));
            } else {
                binding.tvBudgetStatus.setText(R.string.budget_status_bad);
                binding.tvBudgetStatus.setTextColor(ContextCompat.getColor(this, R.color.red_expense));
            }
        } else {
            binding.cardBudget.setVisibility(View.GONE);
        }
    }

    private void setupChart(double income, double expense) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) income));
        entries.add(new BarEntry(1f, (float) expense));

        BarDataSet dataSet = new BarDataSet(entries, "Arus Kas");
        dataSet.setColors(
            ContextCompat.getColor(this, R.color.green_income),
            ContextCompat.getColor(this, R.color.red_expense)
        );
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.text_primary));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        binding.barChart.setData(barData);
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.getLegend().setEnabled(false);
        binding.barChart.getXAxis().setEnabled(false);
        binding.barChart.getAxisLeft().setEnabled(false);
        binding.barChart.getAxisRight().setEnabled(false);
        binding.barChart.animateY(1000);
        binding.barChart.invalidate();
    }

    private void showDeleteDialog(Transaction transaction) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Transaksi")
            .setMessage("Yakin ingin menghapus \"" + transaction.getDescription() + "\"?")
            .setPositiveButton("Hapus", (dialog, which) -> {
                db.deleteTransaction(transaction.getId());
                loadData();
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        binding.bottomNav.setSelectedItemId(R.id.nav_home);
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