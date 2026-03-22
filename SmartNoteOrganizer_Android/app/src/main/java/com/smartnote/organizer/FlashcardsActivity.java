package com.smartnote.organizer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class FlashcardsActivity extends AppCompatActivity {

    RecyclerView recyclerFlashcards;
    FlashcardAdapter adapter;
    List<Flashcard> flashcardList;
    OkHttpClient client = new OkHttpClient();
    String token;
    int noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcards);

        recyclerFlashcards = findViewById(R.id.recyclerFlashcards);
        recyclerFlashcards.setLayoutManager(new LinearLayoutManager(this));

        flashcardList = new ArrayList<>();
        adapter = new FlashcardAdapter(flashcardList);
        recyclerFlashcards.setAdapter(adapter);

        token = getToken();
        noteId = getIntent().getIntExtra("id", -1);

        if (noteId == -1) {
            Toast.makeText(this, "Invalid note id", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadFlashcards();
    }

    private void loadFlashcards() {
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "/notes/quiz/" + noteId)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(FlashcardsActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
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

                    runOnUiThread(() -> adapter.notifyDataSetChanged());

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(FlashcardsActivity.this, "Parse error", Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private String getToken() {
        SharedPreferences prefs = getSharedPreferences("smartnote_app", MODE_PRIVATE);
        return prefs.getString("token", "");
    }
}