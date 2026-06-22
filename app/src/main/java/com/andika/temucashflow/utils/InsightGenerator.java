package com.andika.temucashflow.utils;

import com.andika.temucashflow.model.CategoryStat;
import java.util.List;

public class InsightGenerator {

    public static String generateDailyInsight(double income, double expense, List<CategoryStat> expenseStats) {
        if (income <= 0 && expense <= 0) {
            return "Mulai catat transaksi Anda untuk mendapatkan insight keuangan.";
        }

        StringBuilder sb = new StringBuilder();
        
        if (income > 0) {
            int ratio = (int) ((expense / income) * 100);
            sb.append("Pengeluaran Anda mencapai ").append(ratio).append("% dari pemasukan.\n\n");
        }

        if (expenseStats != null && !expenseStats.isEmpty()) {
            CategoryStat top = expenseStats.get(0);
            sb.append("Kategori terbesar: ").append(top.getCategory()).append(" (")
              .append((int)((top.getTotal()/expense)*100)).append("%)\n\n");
            
            sb.append("Rekomendasi: Kurangi pengeluaran ").append(top.getCategory().toLowerCase())
              .append(" sebesar 10% untuk meningkatkan saldo bulanan.");
        } else {
            sb.append("Rekomendasi: Tetap pantau pengeluaran harian Anda agar tetap sesuai anggaran.");
        }

        return sb.toString();
    }
}
