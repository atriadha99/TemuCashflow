package com.andika.temucashflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andika.temucashflow.R;
import com.andika.temucashflow.model.SavingsGoal;
import com.andika.temucashflow.utils.CurrencyFormatter;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private List<SavingsGoal> goals = new ArrayList<>();
    private OnGoalClickListener listener;

    public interface OnGoalClickListener {
        void onGoalClick(SavingsGoal goal);
        void onGoalLongClick(SavingsGoal goal);
    }

    public void setOnGoalClickListener(OnGoalClickListener listener) {
        this.listener = listener;
    }

    public void setGoals(List<SavingsGoal> goals) {
        this.goals = goals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_savings_goal, parent, false);

        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {

        SavingsGoal goal = goals.get(position);

        holder.tvName.setText(goal.getName());

        int progress = Math.min(goal.getProgress(), 100);

        holder.tvPercent.setText(progress + "%");

        holder.progress.setProgress(progress);

        String amountText =
                CurrencyFormatter.format(goal.getCurrentAmount())
                        + " / "
                        + CurrencyFormatter.format(goal.getTargetAmount());

        holder.tvAmount.setText(amountText);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGoalClick(goal);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {

            if (listener != null) {
                listener.onGoalLongClick(goal);
            }

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPercent, tvAmount;
        LinearProgressIndicator progress;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvGoalName);
            tvPercent = itemView.findViewById(R.id.tvGoalPercent);
            tvAmount = itemView.findViewById(R.id.tvGoalAmount);
            progress = itemView.findViewById(R.id.goalProgress);
        }
    }
}