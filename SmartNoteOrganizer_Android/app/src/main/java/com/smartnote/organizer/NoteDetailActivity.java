package com.smartnote.organizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NoteDetailActivity extends AppCompatActivity {

    TextView tvDetailSubject, tvDetailContent, tvDetailSummary;
    Button btnViewFlashcards, btnStartQuiz, btnDeleteNote, btnEditNote;

    OkHttpClient client = new OkHttpClient();
    String token;
    int noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        tvDetailSubject = findViewById(R.id.tvDetailSubject);
        tvDetailContent = findViewById(R.id.tvDetailContent);
        tvDetailSummary = findViewById(R.id.tvDetailSummary);
        btnViewFlashcards = findViewById(R.id.btnViewFlashcards);
        btnStartQuiz = findViewById(R.id.btnStartQuiz);
        btnDeleteNote = findViewById(R.id.btnDeleteNote);

        token = getToken();
        noteId = getIntent().getIntExtra("id", -1);

        String subject = getIntent().getStringExtra("subject");
        String content = getIntent().getStringExtra("content");
        String summary = getIntent().getStringExtra("summary");

        tvDetailSubject.setText(subject);
        tvDetailContent.setText(content);
        tvDetailSummary.setText(summary);

        btnViewFlashcards.setOnClickListener(v -> {
            Intent intent = new Intent(NoteDetailActivity.this, FlashcardsActivity.class);
            intent.putExtra("id", noteId);
            startActivity(intent);
        });

        btnStartQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(NoteDetailActivity.this, QuizActivity.class);
            intent.putExtra("id", noteId);
            startActivity(intent);
        });

        btnDeleteNote.setOnClickListener(v -> showDeleteDialog());
        btnEditNote = findViewById(R.id.btnEditNote);
        btnEditNote.setOnClickListener(v -> {
            Intent intent = new Intent(NoteDetailActivity.this, EditNoteActivity.class);
            intent.putExtra("id", noteId);
            intent.putExtra("subject", tvDetailSubject.getText().toString());
            intent.putExtra("content", tvDetailContent.getText().toString());
            intent.putExtra("summary", tvDetailSummary.getText().toString());
            startActivity(intent);
        });
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes", (dialog, which) -> deleteNote())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteNote() {
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "/notes/delete/" + noteId)
                .addHeader("Authorization", "Bearer " + token)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(NoteDetailActivity.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    Toast.makeText(NoteDetailActivity.this, "Note deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private String getToken() {
        SharedPreferences prefs = getSharedPreferences("smartnote_app", MODE_PRIVATE);
        return prefs.getString("token", "");
    }
}