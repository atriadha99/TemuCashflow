package com.andika.temucashflow.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.andika.temucashflow.R;
import com.andika.temucashflow.adapter.TransactionAdapter;
import com.andika.temucashflow.data.DatabaseHelper;
import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.databinding.FragmentTransactionListBinding;
import com.andika.temucashflow.model.Transaction;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class TransactionListFragment extends Fragment {

    private FragmentTransactionListBinding binding;
    private DatabaseHelper db;
    private TransactionAdapter adapter;
    private long userId;
    private String currentFilter = "all";
    private String currentSort = "date";
    private String currentOrder = "desc";
    private String currentQuery = "";

    private final ActivityResultLauncher<Intent> voiceSearchLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    java.util.ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        binding.searchView.setQuery(matches.get(0), true);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());
        userId = SharedPrefManager.getInstance(requireContext()).getUserId();

        setupFilterSpinner();
        setupSortSpinner();
        setupSearch();
        setupRecyclerView();
        
        loadTransactions();
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

        binding.btnVoiceSearch.setOnClickListener(v -> startVoiceSearch());
    }

    private void startVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Sebutkan transaksi yang dicari");
        try {
            voiceSearchLauncher.launch(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(requireContext(), "Speech recognition tidak didukung", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void setupFilterSpinner() {
        String[] filters = {"Semua Transaksi", "Pemasukan", "Pengeluaran"};
        ArrayAdapter<String> adapterFilter = new ArrayAdapter<>(requireContext(),
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
        String[] sorts = {"Terbaru", "Terlama", "Terbesar", "Terkecil"};
        ArrayAdapter<String> adapterSort = new ArrayAdapter<>(requireContext(),
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

    private void loadTransactions() {
        List<Transaction> transactions = db.getTransactionsFiltered(userId, currentFilter, currentSort, currentOrder, currentQuery);
        adapter.setTransactions(transactions);
        binding.tvCount.setText(getString(R.string.msg_transactions_found_format, transactions.size()));
    }

    private void showDeleteDialog(Transaction transaction) {
        new MaterialAlertDialogBuilder(requireContext())
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
    public void onResume() {
        super.onResume();
        loadTransactions();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) adapter.shutdownTts();
        binding = null;
    }
}
