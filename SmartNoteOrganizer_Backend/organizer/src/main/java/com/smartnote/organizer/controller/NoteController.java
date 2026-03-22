package com.smartnote.organizer.controller;

import com.smartnote.organizer.model.FlashcardEmbeddable;
import com.smartnote.organizer.model.Note;
import com.smartnote.organizer.model.User;
import com.smartnote.organizer.repository.NoteRepository;
import com.smartnote.organizer.repository.UserRepository;
import com.smartnote.organizer.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.smartnote.organizer.service.AiService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notes")
@CrossOrigin
public class NoteController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiService aiService;

    @Autowired
    private NoteRepository noteRepository;

    public User getUserFromToken(String token) {
        String cleanToken = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(cleanToken);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

@GetMapping("/test")
    public String test() {
        return "Note API working";
    }

@PostMapping("/save")
    public Note saveNote(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request
    ) {
        User user = getUserFromToken(token);

        Note note = new Note();
        note.setSubject((String) request.get("subject"));
        note.setContent((String) request.get("content"));
        note.setSummary((String) request.get("summary"));
        note.setUser(user);

        List<FlashcardEmbeddable> flashcards = new ArrayList<>();

        Object flashcardsObj = request.get("flashcards");
        if (flashcardsObj instanceof List<?>) {
            List<?> flashcardList = (List<?>) flashcardsObj;

            for (Object item : flashcardList) {
                if (item instanceof Map<?, ?> cardMap) {
                    String question = (String) cardMap.get("question");
                    String answer = (String) cardMap.get("answer");
                    flashcards.add(new FlashcardEmbeddable(question, answer));
                }
            }
        }

        note.setFlashcards(flashcards);

        return noteRepository.save(note);
    }
@GetMapping("/my-notes")
    public List<Note> getMyNotes(@RequestHeader("Authorization") String token){
        User user = getUserFromToken(token);

        return noteRepository.findByUser(user);
    }
@DeleteMapping("/delete/{id}")
public String deleteNote(
        @RequestHeader("Authorization") String token,
        @PathVariable Long id) {

    User user = getUserFromToken(token);

    Note note = noteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Note not found"));

    if (!note.getUser().getId().equals(user.getId())) {
        throw new RuntimeException("Unauthorized");
    }

    noteRepository.delete(note);

    return "Note deleted successfully";
}
@GetMapping("/search")
public List<Note> searchNotes(
        @RequestHeader("Authorization") String token,
        @RequestParam String keyword) {

    User user = getUserFromToken(token);

    return noteRepository.findByUserAndContentContainingIgnoreCase(user, keyword);
}
@PostMapping("/ai-summary")
public String generateSummary(@RequestBody Map<String, String> request){
    String content = request.get("content");
    return aiService.generateSummary(content);

}
@PostMapping("/ai-explain")
public String explainNote(@RequestBody Map<String, String> request) {

    String content = request.get("content");

    return aiService.explainContent(content);
}
@PostMapping("/ai-flashcards")
public List<FlashcardEmbeddable> generateFlashcards(@RequestBody Map<String, String> request) {

    String content = request.get("content");

    return aiService.generateFlashcards(content);
}
@GetMapping("/quiz/{noteId}")
public List<FlashcardEmbeddable> getQuizFlashcards(
        @RequestHeader("Authorization") String token,
        @PathVariable Long noteId) {

    User user = getUserFromToken(token);

    Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new RuntimeException("Note not found"));

    if (!note.getUser().getId().equals(user.getId())) {
        throw new RuntimeException("Unauthorized");
    }

    return note.getFlashcards();
}
@PutMapping("/update/{id}")
public Note updateNote(
        @RequestHeader("Authorization") String token,
        @PathVariable Long id,
        @RequestBody Map<String, Object> request) {

    User user = getUserFromToken(token);

    Note note = noteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Note not found"));

    if (!note.getUser().getId().equals(user.getId())) {
        throw new RuntimeException("Unauthorized");
    }

    note.setSubject((String) request.get("subject"));
    note.setContent((String) request.get("content"));
    note.setSummary((String) request.get("summary"));

    return noteRepository.save(note);
}
}