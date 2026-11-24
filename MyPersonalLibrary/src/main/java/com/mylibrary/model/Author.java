package com.mylibrary.model;

import java.time.LocalDateTime;

public class Author {
    private int authorId;
    private int userId;
    private String name;
    private String bio;
    private LocalDateTime createdAt;

    public Author() {}

    public int getAuthorId()                    { return authorId; }
    public void setAuthorId(int authorId)       { this.authorId = authorId; }
    public int getUserId()                      { return userId; }
    public void setUserId(int userId)           { this.userId = userId; }
    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }
    public String getBio()                      { return bio; }
    public void setBio(String bio)              { this.bio = bio; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime t)   { this.createdAt = t; }
}
