package com.mylibrary.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReadingLog {
    private int logId;
    private int bookId;
    private int userId;
    private int pagesRead;
    private LocalDate logDate;
    private String notes;
    private LocalDateTime createdAt;

    // Joined field
    private String bookTitle;

    public ReadingLog() {}

    public int getLogId()                       { return logId; }
    public void setLogId(int logId)             { this.logId = logId; }
    public int getBookId()                      { return bookId; }
    public void setBookId(int bookId)           { this.bookId = bookId; }
    public int getUserId()                      { return userId; }
    public void setUserId(int userId)           { this.userId = userId; }
    public int getPagesRead()                   { return pagesRead; }
    public void setPagesRead(int pagesRead)     { this.pagesRead = pagesRead; }
    public LocalDate getLogDate()               { return logDate; }
    public void setLogDate(LocalDate logDate)   { this.logDate = logDate; }
    public String getNotes()                    { return notes; }
    public void setNotes(String notes)          { this.notes = notes; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime t)   { this.createdAt = t; }
    public String getBookTitle()                { return bookTitle; }
    public void setBookTitle(String t)          { this.bookTitle = t; }
}
