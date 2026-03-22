package com.smartnote.organizer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddNoteActivity extends AppCompatActivity {

    EditText etSubject, etContent, etSummary;
    TextView tvFlashcards;
    Button btnGenerateSummary, btnGenerateFlashcards, btnSaveNote;

    OkHttpClient client = new OkHttpClient();
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        etSubject = findViewById(R.id.etSubject);
        etContent = findViewById(R.id.etContent);
        etSummary = findViewById(R.id.etSummary);
        tvFlashcards = findViewById(R.id.tvFlashcards);
        btnGenerateSummary = findViewById(R.id.btnGenerateSummary);
        btnGenerateFlashcards = findViewById(R.id.btnGenerateFlashcards);
        btnSaveNote = findViewById(R.id.btnSaveNote);

        token = getToken();

        btnGenerateSummary.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();

            if (content.isEmpty()) {
                Toast.makeText(this, "Enter note content first", Toast.LENGTH_SHORT).show();
            } else {
                generateSummary(content);
            }
        });

        btnGenerateFlashcards.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();

            if (content.isEmpty()) {
                Toast.makeText(this, "Enter note content first", Toast.LENGTH_SHORT).show();
            } else {
                generateFlashcards(content);
            }
        });

        btnSaveNote.setOnClickListener(v -> {
            String subject = etSubject.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            String summary = etSummary.getText().toString().trim();

            if (subject.isEmpty() || content.isEmpty() || summary.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                saveNote(subject, content, summary);
            }
        });
    }

    private void generateSummary(String content) {
        try {
            JSONObject json = new JSONObject();
            json.put("content", content);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(ApiClient.BASE_URL + "/notes/ai-summary")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(AddNoteActivity.this, "Summary failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseData = response.body().string();
                    runOnUiThread(() -> etSummary.setText(responseData));
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "JSON Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void generateFlashcards(String content) {
        try {
            JSONObject json = new JSONObject();
            json.put("content", content);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(ApiClient.BASE_URL + "/notes/ai-flashcards")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(AddNoteActivity.this, "Flashcards failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseData = response.body().string();

                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        StringBuilder builder = new StringBuilder();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String question = obj.optString("question", "");
                            String answer = obj.optString("answer", "");

                            builder.append("Q: ").append(question).append("\n");
                            builder.append("A: ").append(answer).append("\n\n");
                        }

                        runOnUiThread(() -> tvFlashcards.setText(builder.toString()));

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(AddNoteActivity.this, "Flashcard parse error", Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "JSON Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveNote(String subject, String content, String summary) {
        try {
            JSONObject json = new JSONObject();
            json.put("subject", subject);
            json.put("content", content);
            json.put("summary", summary);

            JSONArray flashcards = new JSONArray();

            JSONObject card1 = new JSONObject();
            card1.put("question", "What is the main topic?");
            card1.put("answer", subject);
            flashcards.put(card1);

            JSONObject card2 = new JSONObject();
            card2.put("question", "What does this note explain?");
            card2.put("answer", summary);
            flashcards.put(card2);

            json.put("flashcards", flashcards);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(ApiClient.BASE_URL + "/notes/save")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(AddNoteActivity.this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> {
                        Toast.makeText(AddNoteActivity.this, "Note saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "JSON Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getToken() {
        SharedPreferences prefs = getSharedPreferences("smartnote_app", MODE_PRIVATE);
        return prefs.getString("token", "");
    }
}