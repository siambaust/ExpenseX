package com.example.expensex;

public class Expense {

    private String name;
    private double amount;
    private long date;

    public Expense() {
    }

    public Expense(String name, double amount, long date) {
        this.name = name;
        this.amount = amount;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public long getDate() {
        return date;
    }
}
