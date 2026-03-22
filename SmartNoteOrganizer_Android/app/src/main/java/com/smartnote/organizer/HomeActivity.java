package com.smartnote.organizer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    Button btnAddNote, btnMyNotes, btnSummary, btnFlashcards, btnQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnAddNote = findViewById(R.id.btnAddNote);
        btnMyNotes = findViewById(R.id.btnMyNotes);
        btnSummary = findViewById(R.id.btnSummary);
        btnFlashcards = findViewById(R.id.btnFlashcards);
        btnQuiz = findViewById(R.id.btnQuiz);

        btnAddNote.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, AddNoteActivity.class)));

        btnMyNotes.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, NotesActivity.class)));

        btnSummary.setOnClickListener(v ->
                Toast.makeText(this, "AI Summary clicked", Toast.LENGTH_SHORT).show());

        btnFlashcards.setOnClickListener(v ->
                Toast.makeText(this, "Flashcards clicked", Toast.LENGTH_SHORT).show());

        btnQuiz.setOnClickListener(v ->
                Toast.makeText(this, "Quiz Mode clicked", Toast.LENGTH_SHORT).show());
    }
}