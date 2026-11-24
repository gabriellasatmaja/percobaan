package com.mylibrary.model;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String name;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    public User(int userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public int getUserId()                      { return userId; }
    public void setUserId(int userId)           { this.userId = userId; }
    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }
    public String getEmail()                    { return email; }
    public void setEmail(String email)          { this.email = email; }
    public String getPassword()                 { return password; }
    public void setPassword(String password)    { this.password = password; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime t)   { this.createdAt = t; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)   { this.updatedAt = t; }

    /** Returns the first letter(s) of the name for the avatar display. */
    public String getInitials() {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    /** Returns the username handle (@name before the @ in email). */
    public String getHandle() {
        if (email == null) return "";
        int at = email.indexOf('@');
        return "@" + (at > 0 ? email.substring(0, at) : email);
    }
}
