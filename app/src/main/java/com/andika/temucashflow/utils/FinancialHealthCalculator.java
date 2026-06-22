package com.andika.temucashflow.utils;

public class FinancialHealthCalculator {

    public static class HealthResult {
        public int score;
        public String status;
        public String recommendation;
        public int color;

        public HealthResult(int score, String status, String recommendation, int color) {
            this.score = score;
            this.status = status;
            this.recommendation = recommendation;
            this.color = color;
        }
    }

    public static HealthResult calculate(double totalIncome, double totalExpense, double savings) {
        int score = 0;

        if (totalIncome > 0) {
            double expenseRatio = (totalExpense / totalIncome) * 100;
            double savingsRatio = (savings / totalIncome) * 100;

            // Score based on expense ratio (50 points max)
            if (expenseRatio <= 50) score += 50;
            else if (expenseRatio <= 70) score += 35;
            else if (expenseRatio <= 90) score += 15;
            else score += 5;

            // Score based on savings ratio (50 points max)
            if (savingsRatio >= 20) score += 50;
            else if (savingsRatio >= 10) score += 30;
            else if (savingsRatio > 0) score += 15;
            else score += 0;
        }

        String status;
        String recommendation;
        int color; // Resource ID or Hex

        if (score <= 40) {
            status = "Needs Improvement";
            recommendation = "Pengeluaran Anda melebihi batas ideal. Kurangi pengeluaran konsumtif dan tingkatkan tabungan untuk memperbaiki kondisi keuangan.";
            color = 0xFFFF4444; // Danger
        } else if (score <= 60) {
            status = "Fair";
            recommendation = "Kondisi keuangan Anda cukup stabil, namun masih banyak ruang untuk penghematan. Coba tinjau kembali kategori pengeluaran terbesar Anda.";
            color = 0xFFFFBB33; // Warning
        } else if (score <= 80) {
            status = "Good";
            recommendation = "Bagus! Anda mengelola keuangan dengan baik. Pertahankan rasio tabungan Anda dan pertimbangkan untuk mulai berinvestasi.";
            color = 0xFF00C851; // Success
        } else {
            status = "Excellent";
            recommendation = "Luar biasa! Keuangan Anda sangat sehat. Anda memiliki kontrol penuh atas pengeluaran dan tabungan Anda.";
            color = 0xFF007E33; // Success Dark
        }

        return new HealthResult(score, status, recommendation, color);
    }
}
