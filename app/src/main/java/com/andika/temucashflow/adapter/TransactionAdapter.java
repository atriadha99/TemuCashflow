package com.andika.temucashflow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.andika.temucashflow.R;
import com.andika.temucashflow.model.Transaction;
import com.andika.temucashflow.utils.CurrencyFormatter;
import com.andika.temucashflow.utils.DateUtils;
import com.andika.temucashflow.utils.TtsHelper;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private Context context;
    private List<Transaction> transactions;
    private OnTransactionClickListener listener;
    private TtsHelper tts;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
        void onTransactionLongClick(Transaction transaction);
    }

    public TransactionAdapter(Context context) {
        this.context = context;
        this.transactions = new ArrayList<>();
        this.tts = new TtsHelper(context);
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(0, transaction);
        notifyItemInserted(0);
    }

    public void removeTransaction(int position) {
        transactions.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        boolean isIncome = "income".equals(transaction.getType());

        holder.tvDescription.setText(transaction.getDescription());
        holder.tvCategory.setText(String.format("%s • %s", transaction.getCategory(), DateUtils.formatDate(transaction.getDate())));
        
        String amountText = (isIncome ? "+ " : "- ") + CurrencyFormatter.format(transaction.getAmount());
        holder.tvAmount.setText(amountText);
        holder.tvAmount.setTextColor(ContextCompat.getColor(context,
                isIncome ? R.color.green_income : R.color.red_expense));

        holder.ivIcon.setImageResource(isIncome ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);
        holder.ivIcon.setBackgroundResource(isIncome ? R.drawable.circle_green : R.drawable.circle_red);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTransactionClick(transaction);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onTransactionLongClick(transaction);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void readTransactionDetail(Transaction transaction) {
        String type = "income".equals(transaction.getType()) ? "Pemasukan" : "Pengeluaran";
        String text = String.format("%s. %s. Sebesar %s. Kategori %s.",
                type,
                transaction.getDescription(),
                CurrencyFormatter.format(transaction.getAmount()),
                transaction.getCategory());
        tts.speak(text);
    }

    public void shutdownTts() {
        if (tts != null) tts.shutdown();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvDescription, tvCategory, tvAmount;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
