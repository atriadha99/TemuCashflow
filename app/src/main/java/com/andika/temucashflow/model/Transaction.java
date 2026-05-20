package com.andika.temucashflow.model;

public class Transaction {
    private long id;
    private String type;        // "income" or "expense"
    private double amount;
    private String category;
    private String description;
    private long date;
    private long userId;

    public Transaction() {}

    public Transaction(String type, double amount, String category, String description, long date, long userId) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.userId = userId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
}
