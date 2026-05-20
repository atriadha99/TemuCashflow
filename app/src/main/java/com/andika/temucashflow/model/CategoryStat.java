package com.andika.temucashflow.model;

public class CategoryStat {
    private String category;
    private double total;
    private int count;
    private int colorResId;
    private int iconResId;

    public CategoryStat(String category, double total, int count) {
        this.category = category;
        this.total = total;
        this.count = count;
    }

    // Getters & Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    
    public int getColorResId() { return colorResId; }
    public void setColorResId(int colorResId) { this.colorResId = colorResId; }
    
    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
}
