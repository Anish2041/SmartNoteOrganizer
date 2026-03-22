package com.smartnote.organizer.service;

import com.smartnote.organizer.model.FlashcardEmbeddable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiService {

    // Generate summary
    public String generateSummary(String content) {
    if (content == null || content.trim().isEmpty()) {
        return "No content available for summary.";
    }

    String cleaned = content.trim();

    String[] sentences = cleaned.split("\\.");

    if (sentences.length >= 2) {
        return "Summary: " + sentences[0].trim() + ". " + sentences[1].trim() + ".";
    } else {
        String[] words = cleaned.split("\\s+");

        if (words.length > 12) {
            StringBuilder shortSummary = new StringBuilder("Summary: ");
            for (int i = 0; i < 12; i++) {
                shortSummary.append(words[i]).append(" ");
            }
            shortSummary.append("...");
            return shortSummary.toString().trim();
        } else {
            return "Summary: " + cleaned;
        }
    }
}

    // Generate explanation
    public String explainContent(String content) {

        return "Simple Explanation:\n\n"
                + content
                + "\n\nThis concept is important. It means the system ensures proper communication and reliability.";
    }

    // Generate flashcards
    public List<FlashcardEmbeddable> generateFlashcards(String content) {

        List<FlashcardEmbeddable> flashcards = new ArrayList<>();

        String[] words = content.split(" ");

        if (words.length > 5) {

            flashcards.add(
                    new FlashcardEmbeddable(
                            "What is the main topic?",
                            words[0] + " concept"
                    )
            );

            flashcards.add(
                    new FlashcardEmbeddable(
                            "What does this concept ensure?",
                            "Reliable communication"
                    )
            );
        }

        return flashcards;
    }
}