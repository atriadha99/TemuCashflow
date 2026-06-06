package com.andika.temucashflow.ui.analytics;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.andika.temucashflow.R;
import com.andika.temucashflow.adapter.GoalAdapter;
import com.andika.temucashflow.data.DatabaseHelper;
import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.databinding.ActivityAnalyticsBinding;
import com.andika.temucashflow.model.CategoryStat;
import com.andika.temucashflow.model.SavingsGoal;
import com.andika.temucashflow.ui.dashboard.DashboardActivity;
import com.andika.temucashflow.ui.settings.SettingsActivity;
import com.andika.temucashflow.ui.transaction.TransactionListActivity;
import com.andika.temucashflow.utils.CurrencyFormatter;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {

    private ActivityAnalyticsBinding binding;
    private DatabaseHelper db;
    private GoalAdapter goalAdapter;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Edge to edge setup
        setupEdgeToEdge();

        // MATIKAN PRIVASI LAYAR (Izinkan Share Screen / Recording)
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        binding = ActivityAnalyticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle WindowInsets for padding
        setupWindowInsets();

        db = DatabaseHelper.getInstance(this);
        userId = SharedPrefManager.getInstance(this).getUserId();

        setupRecyclerView();
        setupBottomNav();
        setupListeners();
        loadAnalytics();
        loadGoals();
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

    private void setupRecyclerView() {
        goalAdapter = new GoalAdapter();
        binding.rvGoals.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGoals.setAdapter(goalAdapter);

        goalAdapter.setOnGoalClickListener(new GoalAdapter.OnGoalClickListener() {
            @Override
            public void onGoalClick(SavingsGoal goal) {
                showUpdateGoalDialog(goal);
            }

            @Override
            public void onGoalLongClick(SavingsGoal goal) {
                new AlertDialog.Builder(AnalyticsActivity.this)
                        .setTitle(R.string.delete_goal_title)
                        .setMessage(getString(R.string.delete_goal_confirm, goal.getName()))
                        .setPositiveButton(R.string.delete_label, (d, w) -> {
                            db.deleteGoal(goal.getId());
                            loadGoals();
                        })
                        .setNegativeButton(R.string.cancel_label, null)
                        .show();
            }
        });
    }

    private void setupListeners() {
        binding.btnAddGoal.setOnClickListener(v -> showAddGoalDialog());
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_analytics);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_transactions) {
                startActivity(new Intent(this, TransactionListActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_analytics) {
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadAnalytics() {
        double income = db.getTotalIncome(userId);
        setupFinancialRatio(income);
        setupCategoryChart();
    }

    private void setupFinancialRatio(double totalIncome) {
        if (totalIncome <= 0) {
            binding.tvFinancialAdvice.setText(R.string.no_income_data);
            return;
        }

        Map<String, Double> breakdown = db.getCategoryBreakdown(userId, "expense");
        double needs = 0;
        double wants = 0;

        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            String cat = entry.getKey();
            double val = entry.getValue();
            // Klasifikasi berdasarkan kategori yang tersedia di AddTransactionActivity
            if (cat.equals("Makanan") || cat.equals("Transport") || cat.equals("Kesehatan") ||
                    cat.equals("Pendidikan") || cat.equals("Tagihan")) {
                needs += val;
            } else {
                wants += val;
            }
        }

        double savings = totalIncome - (needs + wants);
        if (savings < 0) savings = 0;

        int needsPercent = (int) ((needs / totalIncome) * 100);
        int wantsPercent = (int) ((wants / totalIncome) * 100);
        int savingsPercent = (int) ((savings / totalIncome) * 100);

        binding.progressNeeds.setProgress(Math.min(needsPercent, 100));
        binding.progressWants.setProgress(Math.min(wantsPercent, 100));
        binding.progressSavings.setProgress(Math.min(savingsPercent, 100));

        String advice;
        if (needsPercent > 50) {
            advice = getString(R.string.advice_needs_high, needsPercent);
        } else if (wantsPercent > 30) {
            advice = getString(R.string.advice_wants_high, wantsPercent);
        } else if (savingsPercent < 20) {
            advice = getString(R.string.advice_savings_low, savingsPercent);
        } else {
            advice = getString(R.string.advice_ideal);
        }
        binding.tvFinancialAdvice.setText(advice);
    }

    private void setupCategoryChart() {
        List<CategoryStat> stats = db.getCategoryStats(userId, "expense");

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (CategoryStat stat : stats) {
            entries.add(new PieEntry((float) stat.getTotal(), stat.getCategory()));
        }

        if (entries.isEmpty()) {
            binding.pieChart.setNoDataText("Belum ada data pengeluaran");
            binding.pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.text_white));
        dataSet.setSliceSpace(3f);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setCenterText("Pengeluaran");
        binding.pieChart.setHoleRadius(45f);
        binding.pieChart.animateY(1400);
        binding.pieChart.invalidate();
    }

    private void loadGoals() {
        List<SavingsGoal> goals = db.getAllGoals(userId);
        goalAdapter.setGoals(goals);
    }

    private void showAddGoalDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_goal, null);
        EditText etName = view.findViewById(R.id.etGoalName);
        EditText etTarget = view.findViewById(R.id.etGoalTarget);

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_goal_title)
                .setView(view)
                .setPositiveButton(R.string.save_label, (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String targetStr = etTarget.getText().toString().trim();

                    if (!name.isEmpty() && !targetStr.isEmpty()) {
                        double target = Double.parseDouble(targetStr);
                        SavingsGoal goal = new SavingsGoal(name, target, 0, userId);
                        db.insertGoal(goal);
                        loadGoals();
                        Toast.makeText(this, R.string.goal_added_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.empty_data_warning, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel_label, null)
                .show();
    }

    private void showUpdateGoalDialog(SavingsGoal goal) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_goal_update, null);
        EditText etCurrent = view.findViewById(R.id.etCurrentAmount);
        etCurrent.setText(String.valueOf((int) goal.getCurrentAmount()));

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.update_goal_title_prefix, goal.getName()))
                .setView(view)
                .setPositiveButton(R.string.update_label, (d, w) -> {
                    String currentStr = etCurrent.getText().toString().trim();
                    if (!currentStr.isEmpty()) {
                        double current = Double.parseDouble(currentStr);
                        db.updateGoalCurrentAmount(goal.getId(), current);
                        loadGoals();
                        Toast.makeText(this, R.string.progress_updated_success, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel_label, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNav.setSelectedItemId(R.id.nav_analytics);
        loadAnalytics();
        loadGoals();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
