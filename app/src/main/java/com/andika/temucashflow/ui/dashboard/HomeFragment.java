package com.andika.temucashflow.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.andika.temucashflow.R;
import com.andika.temucashflow.adapter.TransactionAdapter;
import com.andika.temucashflow.data.DatabaseHelper;
import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.databinding.FragmentHomeBinding;
import com.andika.temucashflow.model.Transaction;
import com.andika.temucashflow.ui.transaction.AddTransactionActivity;
import com.andika.temucashflow.model.CategoryStat;
import com.andika.temucashflow.utils.CurrencyFormatter;
import com.andika.temucashflow.utils.DateUtils;
import com.andika.temucashflow.utils.FinancialHealthCalculator;
import com.andika.temucashflow.utils.InsightGenerator;
import com.andika.temucashflow.utils.TtsHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DatabaseHelper db;
    private TransactionAdapter adapter;
    private TtsHelper tts;
    private long userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());
        userId = SharedPrefManager.getInstance(requireContext()).getUserId();
        tts = new TtsHelper(requireContext());

        initViews();
        setupRecyclerView();
        setupListeners();
        loadData();
    }

    private void initViews() {
        String userName = SharedPrefManager.getInstance(requireContext()).getUserName();
        binding.tvGreeting.setText(getString(R.string.greeting_format, userName));
        binding.tvDate.setText(DateUtils.formatFullDate(System.currentTimeMillis()));
    }

    private void setupRecyclerView() {
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter(requireContext());
        adapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                Intent intent = new Intent(requireActivity(), AddTransactionActivity.class);
                intent.putExtra("transaction_id", transaction.getId());
                startActivity(intent);
            }

            @Override
            public void onTransactionLongClick(Transaction transaction) {
                showTransactionOptions(transaction);
            }
        });
        binding.rvTransactions.setAdapter(adapter);
    }

    private void showTransactionOptions(Transaction transaction) {
        String[] options = {"Baca Detail", "Hapus Transaksi"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(transaction.getDescription())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        adapter.readTransactionDetail(transaction);
                    } else {
                        showDeleteDialog(transaction);
                    }
                })
                .show();
    }

    private void setupListeners() {
        binding.tvSeeAll.setOnClickListener(v -> {
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).navigateToTransactions();
            }
        });

        binding.btnQuickIncome.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AddTransactionActivity.class);
            intent.putExtra("type", "income");
            startActivity(intent);
        });

        binding.btnQuickExpense.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AddTransactionActivity.class);
            intent.putExtra("type", "expense");
            startActivity(intent);
        });

        binding.btnGoals.setOnClickListener(v -> {
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).navigateToStatistics();
            }
        });
        
        binding.btnExport.setOnClickListener(v -> {
            // Trigger export logic (to be implemented)
            Toast.makeText(requireContext(), "Exporting data...", Toast.LENGTH_SHORT).show();
        });

        binding.btnEducation.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), com.andika.temucashflow.ui.education.EducationActivity.class);
            startActivity(intent);
        });

        binding.btnSpeakInsight.setOnClickListener(v -> {
            String text = binding.tvInsight.getText().toString();
            tts.speak(text);
        });
    }

    private void loadData() {
        double totalIncome = db.getTotalIncome(userId);
        double totalExpense = db.getTotalExpense(userId);
        double balance = totalIncome - totalExpense;

        binding.tvTotalIncome.setText(CurrencyFormatter.format(totalIncome));
        binding.tvTotalExpense.setText(CurrencyFormatter.format(totalExpense));
        binding.tvBalance.setText(CurrencyFormatter.format(balance));

        // Advanced Health Score
        FinancialHealthCalculator.HealthResult health = FinancialHealthCalculator.calculate(totalIncome, totalExpense, balance > 0 ? balance : 0);
        binding.tvHealthScoreSmall.setText(String.valueOf(health.score));
        binding.tvHealthScoreSmall.setTextColor(health.color);
        
        binding.tvHealthScore.setText(health.score + " / 100");
        binding.tvHealthStatus.setText(health.status);
        binding.tvHealthStatus.setTextColor(health.color);
        binding.tvHealthRecommendation.setText(health.recommendation);

        List<Transaction> allTransactions = db.getAllTransactions(userId);
        if (!allTransactions.isEmpty()) {
            adapter.setTransactions(allTransactions.subList(0, Math.min(allTransactions.size(), 5)));
        } else {
            adapter.setTransactions(new ArrayList<>());
        }
        
        List<CategoryStat> stats = db.getCategoryStats(userId, "expense");
        String insight = InsightGenerator.generateDailyInsight(totalIncome, totalExpense, stats);
        binding.tvInsight.setText(insight);
    }
    
    private void showDeleteDialog(Transaction transaction) {
        new MaterialAlertDialogBuilder(requireContext())
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
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) tts.shutdown();
        if (adapter != null) adapter.shutdownTts();
        binding = null;
    }
}
