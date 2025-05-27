package com.yaga.kanboardmobile;

public class Ticket {
    private String title;
    private String description;
    private String status;
    private String createdAt;
    private String dueDate;
    private int id;

    public Ticket(String title, String description, String status, String createdAt, String dueDate) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
    }

    public Ticket(int id, String title, String description, String status, String createdAt, String dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}
