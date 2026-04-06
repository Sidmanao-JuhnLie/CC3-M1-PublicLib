package com.library.model;

public class Reservation {
    private int id;
    private String resId;
    private int userId;
    private int bookId;
    private String bookTitle;    // joined field
    private String resDate;
    private int queuePosition;
    private String status;       // Waiting | Ready for Pickup! | Cancelled

    public Reservation() {}

    public int getId()              { return id; }
    public String getResId()        { return resId; }
    public int getUserId()          { return userId; }
    public int getBookId()          { return bookId; }
    public String getBookTitle()    { return bookTitle; }
    public String getResDate()      { return resDate; }
    public int getQueuePosition()   { return queuePosition; }
    public String getStatus()       { return status; }

    public void setId(int id)                       { this.id = id; }
    public void setResId(String resId)              { this.resId = resId; }
    public void setUserId(int userId)               { this.userId = userId; }
    public void setBookId(int bookId)               { this.bookId = bookId; }
    public void setBookTitle(String bookTitle)      { this.bookTitle = bookTitle; }
    public void setResDate(String resDate)          { this.resDate = resDate; }
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }
    public void setStatus(String status)            { this.status = status; }

    public String getQueueDisplay() {
        return "#" + queuePosition;
    }

    public String getTruncatedTitle(int maxLen) {
        if (bookTitle == null) return "";
        if (bookTitle.length() <= maxLen) return bookTitle;
        return bookTitle.substring(0, maxLen - 3) + "...";
    }
}
