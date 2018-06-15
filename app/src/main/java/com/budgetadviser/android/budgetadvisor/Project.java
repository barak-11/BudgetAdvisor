package com.budgetadviser.android.budgetadvisor;

import java.util.Date;

public class Project {

    private String name;
    private int budget;
    private String createdDate;
    private String uid;
    private String currency;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Project(String name, int budget, String createdDate, String uid, String currency) {

        this.name = name;
        this.budget = budget;
        this.createdDate = createdDate;
        this.uid = uid;
        this.currency=currency;
    }

    public Project() {

    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
