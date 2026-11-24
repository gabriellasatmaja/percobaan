package com.mylibrary.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReadingGoal {
    public enum GoalType {
        BOOKS_COUNT("books_count"),
        FINISH_BOOK("finish_book");

        private final String dbValue;
        GoalType(String dbValue) { this.dbValue = dbValue; }
        public String getDbValue() { return dbValue; }

        public static GoalType fromString(String s) {
            for (GoalType t : values()) {
                if (t.dbValue.equalsIgnoreCase(s)) return t;
            }
            return BOOKS_COUNT;
        }
    }

    private int goalId;
    private int userId;
    private String title;
    private GoalType goalType;
    private Integer targetBooks;   // used when BOOKS_COUNT
    private Integer bookId;        // used when FINISH_BOOK, nullable
    private LocalDate deadline;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Joined field
    private String bookTitle;

    public ReadingGoal() {}

    public int getGoalId()                          { return goalId; }
    public void setGoalId(int goalId)               { this.goalId = goalId; }
    public int getUserId()                          { return userId; }
    public void setUserId(int userId)               { this.userId = userId; }
    public String getTitle()                        { return title; }
    public void setTitle(String title)              { this.title = title; }
    public GoalType getGoalType()                   { return goalType; }
    public void setGoalType(GoalType t)             { this.goalType = t; }
    public Integer getTargetBooks()                 { return targetBooks; }
    public void setTargetBooks(Integer t)           { this.targetBooks = t; }
    public Integer getBookId()                      { return bookId; }
    public void setBookId(Integer bookId)           { this.bookId = bookId; }
    public LocalDate getDeadline()                  { return deadline; }
    public void setDeadline(LocalDate deadline)     { this.deadline = deadline; }
    public boolean isCompleted()                    { return completed; }
    public void setCompleted(boolean completed)     { this.completed = completed; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime t)       { this.createdAt = t; }
    public LocalDateTime getUpdatedAt()             { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)       { this.updatedAt = t; }
    public String getBookTitle()                    { return bookTitle; }
    public void setBookTitle(String t)              { this.bookTitle = t; }
}
