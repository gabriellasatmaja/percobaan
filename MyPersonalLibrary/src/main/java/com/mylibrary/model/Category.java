package com.mylibrary.model;

import java.time.LocalDateTime;

public class Category {
    private int categoryId;
    private int userId;
    private String name;
    private String description;
    private LocalDateTime createdAt;

    public Category() {}

    public int getCategoryId()                      { return categoryId; }
    public void setCategoryId(int categoryId)       { this.categoryId = categoryId; }
    public int getUserId()                          { return userId; }
    public void setUserId(int userId)               { this.userId = userId; }
    public String getName()                         { return name; }
    public void setName(String name)                { this.name = name; }
    public String getDescription()                  { return description; }
    public void setDescription(String description)  { this.description = description; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime t)       { this.createdAt = t; }
}
