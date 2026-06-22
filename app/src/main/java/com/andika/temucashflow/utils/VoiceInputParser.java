package com.andika.temucashflow.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoiceInputParser {

    public static class VoiceTransaction {
        public String type;
        public double amount;
        public String category;
        public String description;

        @Override
        public String toString() {
            return "Type: " + type + ", Amount: " + amount + ", Category: " + category;
        }
    }

    public static VoiceTransaction parse(String text) {
        String lower = text.toLowerCase();
        VoiceTransaction result = new VoiceTransaction();

        // 1. Detect Type
        if (lower.contains("pemasukan") || lower.contains("masuk") || lower.contains("terima")) {
            result.type = "income";
        } else {
            result.type = "expense"; // Default
        }

        // 2. Detect Amount
        result.amount = extractAmount(lower);

        // 3. Detect Category
        result.category = detectCategory(lower);
        
        // 4. Description (use the original text as a base if needed)
        result.description = text;

        return result;
    }

    private static double extractAmount(String text) {
        // Handle words like "sepuluh ribu", "satu juta"
        String processed = text
                .replace("seperempat", "250")
                .replace("setengah", "500")
                .replace("se", "1")
                .replace("satu", "1")
                .replace("dua", "2")
                .replace("tiga", "3")
                .replace("empat", "4")
                .replace("lima", "5")
                .replace("enam", "6")
                .replace("tujuh", "7")
                .replace("delapan", "8")
                .replace("sembilan", "9")
                .replace("sepuluh", "10")
                .replace("sebelas", "11");

        long multiplier = 1;
        if (text.contains("juta")) multiplier = 1000000;
        else if (text.contains("ribu")) multiplier = 1000;
        else if (text.contains("ratus")) multiplier = 100;

        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(processed);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1)) * multiplier;
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    private static String detectCategory(String text) {
        if (text.contains("makan") || text.contains("minum") || text.contains("kopi")) return "Makanan";
        if (text.contains("bensin") || text.contains("ojek") || text.contains("transport") || text.contains("parkir")) return "Transport";
        if (text.contains("gaji") || text.contains("honor") || text.contains("upah")) return "Gaji";
        if (text.contains("freelance") || text.contains("proyek")) return "Freelance";
        if (text.contains("belanja") || text.contains("beli")) return "Belanja";
        if (text.contains("tagihan") || text.contains("listrik") || text.contains("pdam") || text.contains("pulsa")) return "Tagihan";
        return "Lainnya";
    }
}
