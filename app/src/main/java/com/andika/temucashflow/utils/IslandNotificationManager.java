package com.andika.temucashflow.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.core.content.ContextCompat;

import com.andika.temucashflow.R;
import com.andika.temucashflow.databinding.ViewIslandNotificationBinding;
import com.andika.temucashflow.model.Transaction;

public class IslandNotificationManager {

    private final Activity activity;
    private ViewIslandNotificationBinding binding;
    private boolean isExpanded = false;
    private boolean isShowing = false;

    // Animation durations
    private static final long DURATION_EXPAND = 400;
    private static final long DURATION_COLLAPSE = 300;
    private static final long DURATION_SHOW = 500;
    private static final long DURATION_HIDE = 300;
    private static final long AUTO_HIDE_DELAY = 6000;

    private Runnable autoHideRunnable;

    public IslandNotificationManager(Activity activity) {
        this.activity = activity;
        init();
    }

    private void init() {
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        View islandView = LayoutInflater.from(activity)
                .inflate(R.layout.view_island_notification, rootView, false);
        rootView.addView(islandView);
        
        binding = ViewIslandNotificationBinding.bind(islandView);
        setupClickListeners();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupClickListeners() {
        // Tap collapsed to expand
        binding.islandCollapsed.setOnClickListener(v -> expand());

        // Tap expanded to collapse
        binding.islandExpanded.setOnClickListener(v -> collapse());

        // Close button
        binding.btnIslandClose.setOnClickListener(v -> hide());

        // Swipe to dismiss
        binding.islandContainer.setOnTouchListener(new View.OnTouchListener() {
            private float startY;
            
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        return true;
                    case android.view.MotionEvent.ACTION_UP:
                        v.performClick();
                        float endY = event.getY();
                        if (startY - endY > 100) { // Swipe up
                            hide();
                            return true;
                        }
                        return false;
                }
                return false;
            }
        });
    }

    public void showIncome(double amount, Transaction recentTransaction) {
        String amountText = "+ " + CurrencyFormatter.format(amount);
        show(activity.getString(R.string.label_income), amountText, 
              R.color.green_income, R.drawable.ic_arrow_down, recentTransaction, false);
    }

    public void showExpense(double amount, Transaction recentTransaction) {
        String amountText = "- " + CurrencyFormatter.format(amount);
        show(activity.getString(R.string.label_expense), amountText,
              R.color.red_expense, R.drawable.ic_arrow_up, recentTransaction, false);
    }

    public void showSummary(double income, double expense, double balance, 
                           Transaction recentTransaction) {
        updateExpandedContent(income, expense, balance, recentTransaction);
        show(activity.getString(R.string.label_balance), CurrencyFormatter.format(balance), 
             R.color.blue_balance, R.drawable.ic_wallet, recentTransaction, true);
    }

    private void show(String title, String amount, int colorRes, int iconRes, 
                     Transaction recentTransaction, boolean autoExpand) {
        if (isShowing) return;

        int color = ContextCompat.getColor(activity, colorRes);

        // Update content
        binding.tvIslandTitle.setText(title);
        binding.tvIslandAmount.setText(amount);
        binding.tvIslandAmount.setTextColor(color);
        binding.ivIslandIcon.setImageResource(iconRes);
        binding.ivIslandIcon.setColorFilter(color);

        // Update expanded content if needed
        if (recentTransaction != null) {
            updateRecentTransaction(recentTransaction);
        }

        // Show animation
        binding.islandContainer.setVisibility(View.VISIBLE);
        binding.islandCollapsed.setVisibility(View.VISIBLE);
        binding.islandCollapsed.setAlpha(0f);
        binding.islandCollapsed.setScaleX(0.5f);
        binding.islandCollapsed.setScaleY(0.5f);

        binding.islandCollapsed.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(DURATION_SHOW)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .withEndAction(() -> {
                    if (autoExpand) {
                        binding.islandContainer.postDelayed(this::expand, 500);
                    }
                })
                .start();

        isShowing = true;

        // Auto hide after delay
        resetAutoHide();
    }

    public void expand() {
        if (isExpanded || !isShowing) return;

        cancelAutoHide();

        // Animate transition
        binding.islandCollapsed.animate()
                .alpha(0f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(DURATION_EXPAND / 2)
                .withEndAction(() -> {
                    binding.islandCollapsed.setVisibility(View.GONE);
                    
                    binding.islandExpanded.setVisibility(View.VISIBLE);
                    binding.islandExpanded.setAlpha(0f);
                    binding.islandExpanded.setScaleX(0.85f);
                    binding.islandExpanded.setScaleY(0.85f);

                    binding.islandExpanded.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(DURATION_EXPAND)
                            .setInterpolator(new OvershootInterpolator(1.0f))
                            .start();
                })
                .start();

        isExpanded = true;
    }

    public void collapse() {
        if (!isExpanded) return;

        binding.islandExpanded.animate()
                .alpha(0f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(DURATION_COLLAPSE)
                .withEndAction(() -> {
                    binding.islandExpanded.setVisibility(View.GONE);
                    
                    binding.islandCollapsed.setVisibility(View.VISIBLE);
                    binding.islandCollapsed.setAlpha(0f);
                    binding.islandCollapsed.setScaleX(0.9f);
                    binding.islandCollapsed.setScaleY(0.9f);

                    binding.islandCollapsed.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(DURATION_COLLAPSE)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                })
                .start();

        isExpanded = false;
        resetAutoHide();
    }

    public void hide() {
        if (!isShowing) return;

        View targetView = isExpanded ? binding.islandExpanded : binding.islandCollapsed;

        targetView.animate()
                .alpha(0f)
                .scaleX(0.6f)
                .scaleY(0.6f)
                .translationY(-100f)
                .setDuration(DURATION_HIDE)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    binding.islandContainer.setVisibility(View.GONE);
                    binding.islandCollapsed.setVisibility(View.GONE);
                    binding.islandExpanded.setVisibility(View.GONE);
                    binding.islandCollapsed.setTranslationY(0);
                    binding.islandExpanded.setTranslationY(0);
                    isExpanded = false;
                    isShowing = false;
                })
                .start();
    }

    private void resetAutoHide() {
        cancelAutoHide();
        autoHideRunnable = this::hide;
        binding.islandContainer.postDelayed(autoHideRunnable, AUTO_HIDE_DELAY);
    }

    private void cancelAutoHide() {
        if (autoHideRunnable != null) {
            binding.islandContainer.removeCallbacks(autoHideRunnable);
            autoHideRunnable = null;
        }
    }

    public void updateExpandedContent(double income, double expense, double balance, 
                                      Transaction recent) {
        binding.tvExpandedIncome.setText(CurrencyFormatter.format(income));
        binding.tvExpandedExpense.setText(CurrencyFormatter.format(expense));
        binding.tvExpandedBalance.setText(CurrencyFormatter.format(balance));
        
        if (recent != null) {
            updateRecentTransaction(recent);
        }
    }

    private void updateRecentTransaction(Transaction transaction) {
        boolean isIncome = "income".equals(transaction.getType());
        
        binding.tvRecentDesc.setText(transaction.getDescription());
        String info = transaction.getCategory() + " • " + DateUtils.formatDate(transaction.getDate());
        binding.tvRecentCategory.setText(info);
        
        String amountText = (isIncome ? "+ " : "- ") + CurrencyFormatter.format(transaction.getAmount());
        binding.tvRecentAmount.setText(amountText);
        
        int color = ContextCompat.getColor(activity, isIncome ? R.color.green_income : R.color.red_expense);
        binding.tvRecentAmount.setTextColor(color);
        binding.ivRecentIcon.setImageResource(isIncome ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);
        binding.ivRecentIcon.setBackgroundResource(isIncome ? R.drawable.circle_green : R.drawable.circle_red);
    }

    public void destroy() {
        cancelAutoHide();
    }
}