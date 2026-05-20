package com.andika.temucashflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andika.temucashflow.R;
import com.andika.temucashflow.data.DatabaseHelper;
import com.andika.temucashflow.model.CategoryStat;
import com.andika.temucashflow.utils.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;

public class CategoryStatAdapter extends RecyclerView.Adapter<CategoryStatAdapter.ViewHolder> {

    private List<CategoryStat> stats = new ArrayList<>();

    public void setStats(List<CategoryStat> stats) {
        this.stats = stats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryStat stat = stats.get(position);
        holder.tvCategory.setText(stat.getCategory());
        holder.tvTotal.setText(CurrencyFormatter.format(stat.getTotal()));
        holder.tvCount.setText(stat.getCount() + " Transaksi");
    }

    @Override
    public int getItemCount() {
        return stats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvTotal, tvCount;

        ViewHolder(View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategoryName);
            tvTotal = itemView.findViewById(R.id.tvCategoryTotal);
            tvCount = itemView.findViewById(R.id.tvTransactionCount);
        }
    }
}
