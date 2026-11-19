package com.example.joggingclubmanagement;

public class DashboardInfo {

    private String title;
    private String description;

    public DashboardInfo(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}