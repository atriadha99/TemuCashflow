package com.andika.temucashflow.model;

public class Category {
    private int id;
    private String name;
    private String type;        // "income" or "expense"
    private int iconResId;
    private int colorResId;

    public Category(int id, String name, String type, int iconResId, int colorResId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.iconResId = iconResId;
        this.colorResId = colorResId;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getIconResId() { return iconResId; }
    public int getColorResId() { return colorResId; }
}
