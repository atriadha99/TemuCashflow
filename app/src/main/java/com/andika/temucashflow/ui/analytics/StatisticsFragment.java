package com.andika.temucashflow.ui.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.andika.temucashflow.R;
import com.andika.temucashflow.adapter.GoalAdapter;
import com.andika.temucashflow.data.DatabaseHelper;
import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.databinding.FragmentStatisticsBinding;
import com.andika.temucashflow.model.CategoryStat;
import com.andika.temucashflow.model.SavingsGoal;
import com.andika.temucashflow.utils.CurrencyFormatter;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private DatabaseHelper db;
    private GoalAdapter goalAdapter;
    private long userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());
        userId = SharedPrefManager.getInstance(requireContext()).getUserId();

        setupRecyclerView();
        setupListeners();
        loadData();
    }

    private void setupRecyclerView() {
        goalAdapter = new GoalAdapter();
        binding.rvGoals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGoals.setAdapter(goalAdapter);

        goalAdapter.setOnGoalClickListener(new GoalAdapter.OnGoalClickListener() {
            @Override
            public void onGoalClick(SavingsGoal goal) {
                showUpdateGoalDialog(goal);
            }

            @Override
            public void onGoalLongClick(SavingsGoal goal) {
                new AlertDialog.Builder(requireContext())
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

    private void loadData() {
        double income = db.getTotalIncome(userId);
        double expense = db.getTotalExpense(userId);
        
        calculateFinancialHealthScore(income, expense);
        setupFinancialRatio(income);
        setupCategoryChart();
        setupMonthlyBarChart();
        loadGoals();
    }

    private void calculateFinancialHealthScore(double income, double expense) {
        int score = 0;
        
        if (income > 0) {
            // 1. Savings Ratio (target > 20%)
            double savings = income - expense;
            double savingsRatio = (savings / income) * 100;
            if (savingsRatio >= 20) score += 40;
            else if (savingsRatio > 0) score += 20;

            // 2. Expense Ratio (target < 50% for needs)
            // Simplified: total expense vs income
            double expenseRatio = (expense / income) * 100;
            if (expenseRatio <= 50) score += 40;
            else if (expenseRatio <= 80) score += 20;
            
            // 3. Consistency (checking last 3 months)
            // For now, let's just give points if there's any transaction
            score += 20; 
        }

        binding.tvHealthScore.setText(String.valueOf(score));
        if (score >= 71) {
            binding.tvHealthCategory.setText(R.string.score_excellent);
            binding.tvHealthCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.success));
        } else if (score >= 41) {
            binding.tvHealthCategory.setText(R.string.score_good);
            binding.tvHealthCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange_warning));
        } else {
            binding.tvHealthCategory.setText(R.string.score_poor);
            binding.tvHealthCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.danger));
        }
    }

    private void setupFinancialRatio(double totalIncome) {
        if (totalIncome <= 0) {
            binding.tvFinancialAdvice.setText(R.string.no_income_data);
            binding.progressNeeds.setProgress(0);
            binding.progressWants.setProgress(0);
            binding.progressSavings.setProgress(0);
            return;
        }

        Map<String, Double> breakdown = db.getCategoryBreakdown(userId, "expense");
        double needs = 0;
        double wants = 0;

        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            String cat = entry.getKey();
            double val = entry.getValue();
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
        
        int[] colors = {
            ContextCompat.getColor(requireContext(), R.color.primary),
            ContextCompat.getColor(requireContext(), R.color.success),
            ContextCompat.getColor(requireContext(), R.color.orange_warning),
            ContextCompat.getColor(requireContext(), R.color.danger),
            ContextCompat.getColor(requireContext(), R.color.secondary)
        };

        for (CategoryStat stat : stats) {
            entries.add(new PieEntry((float) stat.getTotal(), stat.getCategory()));
        }

        if (entries.isEmpty()) {
            binding.pieChart.setNoDataText("Belum ada data pengeluaran");
            binding.pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
            R.color.primary, R.color.success, R.color.orange_warning, 
            R.color.danger, R.color.secondary, R.color.purple_category
        }, requireContext());
        
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        dataSet.setSliceSpace(4f);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setCenterText("Kategori");
        binding.pieChart.setCenterTextSize(16f);
        binding.pieChart.setHoleRadius(50f);
        binding.pieChart.getLegend().setEnabled(false);
        binding.pieChart.animateY(1000);
        binding.pieChart.invalidate();
    }

    private void setupMonthlyBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        
        Calendar cal = Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            Calendar mCal = (Calendar) cal.clone();
            mCal.add(Calendar.MONTH, -i);
            mCal.set(Calendar.DAY_OF_MONTH, 1);
            mCal.set(Calendar.HOUR_OF_DAY, 0);
            
            double monthlyExpense = db.getMonthlyExpense(userId, mCal.getTimeInMillis());
            entries.add(new BarEntry(5 - i, (float) monthlyExpense));
            
            String monthName = android.text.format.DateFormat.format("MMM", mCal).toString();
            labels.add(monthName);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Pengeluaran");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary));
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        binding.barChart.setData(data);
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.getLegend().setEnabled(false);
        
        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        
        binding.barChart.getAxisLeft().setDrawGridLines(false);
        binding.barChart.getAxisRight().setEnabled(false);
        binding.barChart.animateY(1000);
        binding.barChart.invalidate();
    }

    private void loadGoals() {
        List<SavingsGoal> goals = db.getAllGoals(userId);
        goalAdapter.setGoals(goals);
    }

    private void showAddGoalDialog() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_goal, null);
        EditText etName = view.findViewById(R.id.etGoalName);
        EditText etTarget = view.findViewById(R.id.etGoalTarget);

        new AlertDialog.Builder(requireContext())
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
                        Toast.makeText(requireContext(), R.string.goal_added_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), R.string.empty_data_warning, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel_label, null)
                .show();
    }

    private void showUpdateGoalDialog(SavingsGoal goal) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_goal_update, null);
        EditText etCurrent = view.findViewById(R.id.etCurrentAmount);
        etCurrent.setText(String.valueOf((int) goal.getCurrentAmount()));

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.update_goal_title_prefix, goal.getName()))
                .setView(view)
                .setPositiveButton(R.string.update_label, (d, w) -> {
                    String currentStr = etCurrent.getText().toString().trim();
                    if (!currentStr.isEmpty()) {
                        double current = Double.parseDouble(currentStr);
                        db.updateGoalCurrentAmount(goal.getId(), current);
                        loadGoals();
                        Toast.makeText(requireContext(), R.string.progress_updated_success, Toast.LENGTH_SHORT).show();
                    }
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
