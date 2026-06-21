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
import com.andika.temucashflow.utils.CurrencyFormatter;
import com.andika.temucashflow.utils.DateUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DatabaseHelper db;
    private TransactionAdapter adapter;
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
                showDeleteDialog(transaction);
            }
        });
        binding.rvTransactions.setAdapter(adapter);
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
    }

    private void loadData() {
        double totalIncome = db.getTotalIncome(userId);
        double totalExpense = db.getTotalExpense(userId);
        double balance = totalIncome - totalExpense;

        binding.tvTotalIncome.setText(CurrencyFormatter.format(totalIncome));
        binding.tvTotalExpense.setText(CurrencyFormatter.format(totalExpense));
        binding.tvBalance.setText(CurrencyFormatter.format(balance));

        // Simplified Health Score for Home
        int score = calculateBasicScore(totalIncome, totalExpense);
        binding.tvHealthScoreSmall.setText(String.valueOf(score));

        List<Transaction> allTransactions = db.getAllTransactions(userId);
        if (!allTransactions.isEmpty()) {
            adapter.setTransactions(allTransactions.subList(0, Math.min(allTransactions.size(), 5)));
        } else {
            adapter.setTransactions(new ArrayList<>());
        }
        
        generateInsight(allTransactions, totalIncome, totalExpense);
    }
    
    private int calculateBasicScore(double income, double expense) {
        if (income <= 0) return 0;
        double ratio = (expense / income) * 100;
        if (ratio <= 50) return 85;
        if (ratio <= 80) return 65;
        return 40;
    }

    private void generateInsight(List<Transaction> transactions, double income, double expense) {
        if (income > 0 && expense > income * 0.8) {
            binding.tvInsight.setText("Pengeluaran Anda mencapai " + (int)((expense/income)*100) + "% dari pemasukan.");
        } else {
            binding.tvInsight.setText(R.string.advice_ideal);
        }
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
        binding = null;
    }
}
