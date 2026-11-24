package com.mylibrary.model;

import java.time.LocalDateTime;

public class Book {
    public enum Status {
        WANT_TO_READ("Want to Read"),
        CURRENTLY_READING("Currently Reading"),
        FINISHED_READING("Finished Reading");

        private final String display;
        Status(String display) { this.display = display; }
        public String getDisplay() { return display; }

        public static Status fromString(String s) {
            for (Status st : values()) {
                if (st.display.equalsIgnoreCase(s)) return st;
            }
            return WANT_TO_READ;
        }
    }

    private int bookId;
    private int userId;
    private int authorId;
    private Integer categoryId;        // nullable
    private String title;
    private Status status;
    private String coverImage;         // relative path under uploads/covers/
    private String bookFilePath;       // absolute/relative path outside webroot
    private String originalFilename;
    private Integer currentPage;
    private Integer totalPages;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Joined fields (not in DB — populated by queries)
    private String authorName;
    private String categoryName;

    public Book() {}

    public int getBookId()                          { return bookId; }
    public void setBookId(int bookId)               { this.bookId = bookId; }
    public int getUserId()                          { return userId; }
    public void setUserId(int userId)               { this.userId = userId; }
    public int getAuthorId()                        { return authorId; }
    public void setAuthorId(int authorId)           { this.authorId = authorId; }
    public Integer getCategoryId()                  { return categoryId; }
    public void setCategoryId(Integer categoryId)   { this.categoryId = categoryId; }
    public String getTitle()                        { return title; }
    public void setTitle(String title)              { this.title = title; }
    public Status getStatus()                       { return status; }
    public void setStatus(Status status)            { this.status = status; }
    public String getCoverImage()                   { return coverImage; }
    public void setCoverImage(String coverImage)    { this.coverImage = coverImage; }
    public String getBookFilePath()                 { return bookFilePath; }
    public void setBookFilePath(String p)           { this.bookFilePath = p; }
    public String getOriginalFilename()             { return originalFilename; }
    public void setOriginalFilename(String n)       { this.originalFilename = n; }
    public Integer getCurrentPage()                 { return currentPage; }
    public void setCurrentPage(Integer p)           { this.currentPage = p; }
    public Integer getTotalPages()                  { return totalPages; }
    public void setTotalPages(Integer p)            { this.totalPages = p; }
    public String getSummary()                      { return summary; }
    public void setSummary(String summary)          { this.summary = summary; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime t)       { this.createdAt = t; }
    public LocalDateTime getUpdatedAt()             { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)       { this.updatedAt = t; }
    public String getAuthorName()                   { return authorName; }
    public void setAuthorName(String n)             { this.authorName = n; }
    public String getCategoryName()                 { return categoryName; }
    public void setCategoryName(String n)           { this.categoryName = n; }

    public boolean hasFile()  { return bookFilePath != null && !bookFilePath.isBlank(); }
    public boolean hasCover() { return coverImage  != null && !coverImage.isBlank(); }

    /** Returns the reading progress as 0–100, or -1 if not calculable. */
    public int getProgressPercent() {
        if (currentPage != null && totalPages != null && totalPages > 0) {
            return (int) Math.round((currentPage * 100.0) / totalPages);
        }
        return -1;
    }
}
