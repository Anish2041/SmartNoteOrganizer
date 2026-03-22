package com.smartnote.organizer;

import android.content.Intent;
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
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuizActivity extends AppCompatActivity {

    TextView tvQuizScore, tvQuizQuestion, tvQuizResult;
    EditText etQuizAnswer;
    Button btnSubmitAnswer, btnBackToNote;

    List<Flashcard> flashcardList = new ArrayList<>();
    OkHttpClient client = new OkHttpClient();

    String token;
    int noteId;

    int currentIndex = 0;
    int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuizScore = findViewById(R.id.tvQuizScore);
        tvQuizQuestion = findViewById(R.id.tvQuizQuestion);
        tvQuizResult = findViewById(R.id.tvQuizResult);
        etQuizAnswer = findViewById(R.id.etQuizAnswer);
        btnSubmitAnswer = findViewById(R.id.btnSubmitAnswer);
        btnBackToNote = findViewById(R.id.btnBackToNote);

        token = getToken();
        noteId = getIntent().getIntExtra("id", -1);

        if (noteId == -1) {
            Toast.makeText(this, "Invalid note id", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnSubmitAnswer.setOnClickListener(v -> checkAnswer());

        btnBackToNote.setOnClickListener(v -> {
            finish();
        });

        loadQuizFlashcards();
    }

    private void loadQuizFlashcards() {
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "/notes/quiz/" + noteId)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(QuizActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();

                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    flashcardList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String question = obj.optString("question", "");
                        String answer = obj.optString("answer", "");

                        flashcardList.add(new Flashcard(question, answer));
                    }

                    runOnUiThread(() -> {
                        if (flashcardList.isEmpty()) {
                            Toast.makeText(QuizActivity.this, "No flashcards found", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            showQuestion();
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(QuizActivity.this, "Parse error", Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private void showQuestion() {
        if (currentIndex < flashcardList.size()) {
            Flashcard flashcard = flashcardList.get(currentIndex);
            tvQuizQuestion.setText(flashcard.getQuestion());
            tvQuizScore.setText("Score: " + score + "/" + flashcardList.size());
            etQuizAnswer.setText("");
            tvQuizResult.setText("");
            etQuizAnswer.setEnabled(true);
            btnSubmitAnswer.setEnabled(true);
            btnSubmitAnswer.setText("Submit Answer");
            btnBackToNote.setVisibility(Button.GONE);
        }
    }

    private void checkAnswer() {
        if (currentIndex >= flashcardList.size()) return;

        String userAnswer = etQuizAnswer.getText().toString().trim();
        String correctAnswer = flashcardList.get(currentIndex).getAnswer().trim();

        if (userAnswer.isEmpty()) {
            Toast.makeText(this, "Enter an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCorrect = isAnswerCorrect(userAnswer, correctAnswer);

        if (isCorrect) {
            score++;
            tvQuizResult.setText("Correct!");
        } else {
            tvQuizResult.setText("Wrong! Correct answer: " + correctAnswer);
        }

        tvQuizScore.setText("Score: " + score + "/" + flashcardList.size());

        currentIndex++;

        if (currentIndex < flashcardList.size()) {
            btnSubmitAnswer.setText("Next Question");
            btnSubmitAnswer.setOnClickListener(v -> {
                btnSubmitAnswer.setOnClickListener(v2 -> checkAnswer());
                showQuestion();
            });
        } else {
            showFinalResult();
        }
    }

    private void showFinalResult() {
        tvQuizQuestion.setText("Quiz Finished!");
        etQuizAnswer.setEnabled(false);
        btnSubmitAnswer.setEnabled(false);
        tvQuizScore.setText("Final Score: " + score + "/" + flashcardList.size());
        btnBackToNote.setVisibility(Button.VISIBLE);

        String oldFeedback = tvQuizResult.getText().toString();
        tvQuizResult.setText(oldFeedback + "\n\nGood job! You can now go back to the note.");
    }

    private boolean isAnswerCorrect(String userAnswer, String correctAnswer) {
        String user = userAnswer.toLowerCase().trim();
        String correct = correctAnswer.toLowerCase().trim();

        String[] correctWords = correct.split("\\s+");
        int totalKeywords = 0;
        int matchedKeywords = 0;

        for (String word : correctWords) {
            word = word.replaceAll("[^a-zA-Z0-9]", "");

            if (word.length() <= 2) {
                continue;
            }

            totalKeywords++;

            if (user.contains(word)) {
                matchedKeywords++;
            }
        }

        if (totalKeywords == 0) {
            return user.equalsIgnoreCase(correct);
        }

        double matchPercentage = (double) matchedKeywords / totalKeywords;
        return matchPercentage >= 0.6;
    }

    private String getToken() {
        SharedPreferences prefs = getSharedPreferences("smartnote_app", MODE_PRIVATE);
        return prefs.getString("token", "");
    }
}