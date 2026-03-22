package com.smartnote.organizer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    @Column(length = 10000)
    private String content;

    @Column(length = 5000)
    private String summary;

    @ElementCollection
    @CollectionTable(name = "note_flashcards", joinColumns = @JoinColumn(name = "note_id"))
    private List<FlashcardEmbeddable> flashcards;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Note() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<FlashcardEmbeddable> getFlashcards() {
        return flashcards;
    }

    public void setFlashcards(List<FlashcardEmbeddable> flashcards) {
        this.flashcards = flashcards;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}