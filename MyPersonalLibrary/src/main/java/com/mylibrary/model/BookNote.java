package com.mylibrary.model;

import java.time.LocalDateTime;

public class BookNote {
    private int noteId;
    private int bookId;
    private int userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Joined field
    private String bookTitle;

    public BookNote() {}

    public int getNoteId()                      { return noteId; }
    public void setNoteId(int noteId)           { this.noteId = noteId; }
    public int getBookId()                      { return bookId; }
    public void setBookId(int bookId)           { this.bookId = bookId; }
    public int getUserId()                      { return userId; }
    public void setUserId(int userId)           { this.userId = userId; }
    public String getContent()                  { return content; }
    public void setContent(String content)      { this.content = content; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime t)   { this.createdAt = t; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)   { this.updatedAt = t; }
    public String getBookTitle()                { return bookTitle; }
    public void setBookTitle(String t)          { this.bookTitle = t; }
}
