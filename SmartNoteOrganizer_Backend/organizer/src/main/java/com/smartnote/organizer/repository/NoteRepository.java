package com.smartnote.organizer.repository;

import com.smartnote.organizer.model.Note;
import com.smartnote.organizer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUser(User user);
    List<Note> findByUserAndContentContainingIgnoreCase(User user, String keyword);
}