package com.smartnote.organizer;

public class Note {
    private int id;
    private String subject;
    private String content;
    private String summary;

    public Note(int id, String subject, String content, String summary) {
        this.id = id;
        this.subject = subject;
        this.content = content;
        this.summary = summary;
    }

    public int getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public String getSummary() {
        return summary;
    }
}