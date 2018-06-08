package com.budgetadviser.android.budgetadvisor;


import java.util.List;

public class Project {

    private String name;

    private String uid;

    public Project(String name, String uid) {
        this.name = name;
        this.uid = uid;

    }

    public Project(){

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
