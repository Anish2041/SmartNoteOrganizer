package com.smartnote.organizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
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

public class NotesActivity extends AppCompatActivity {

    RecyclerView recyclerNotes;
    com.google.android.material.floatingactionbutton.FloatingActionButton btnAddNoteFromNotes;
    android.widget.ImageButton btnLogout;
    android.widget.EditText etSearchNotes;
    NoteAdapter adapter;
    List<Note> noteList;
    OkHttpClient client = new OkHttpClient();
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        etSearchNotes = findViewById(R.id.etSearchNotes);
        btnLogout = findViewById(R.id.btnLogout);

        recyclerNotes = findViewById(R.id.recyclerNotes);
        btnAddNoteFromNotes = findViewById(R.id.btnAddNoteFromNotes);

        recyclerNotes.setLayoutManager(new LinearLayoutManager(this));

        noteList = new ArrayList<>();
        adapter = new NoteAdapter(this, noteList);
        recyclerNotes.setAdapter(adapter);
        btnLogout.setOnClickListener(v -> logoutUser());

        token = getToken();

        btnAddNoteFromNotes.setOnClickListener(v ->
                startActivity(new Intent(NotesActivity.this, AddNoteActivity.class)));
        etSearchNotes.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    loadNotes();
                } else {
                    searchNotes(keyword);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences("smartnote_app", MODE_PRIVATE);
        prefs.edit().remove("token").apply();

        Intent intent = new Intent(NotesActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void searchNotes(String keyword) {
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "/notes/search?keyword=" + keyword)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(NotesActivity.this, "Search failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();

                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    noteList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);

                        int id = obj.getInt("id");
                        String subject = obj.optString("subject", "");
                        String content = obj.optString("content", "");
                        String summary = obj.optString("summary", "");

                        noteList.add(new Note(id, subject, content, summary));
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(NotesActivity.this, "Search parse error", Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "/notes/my-notes")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(NotesActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();

                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    noteList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);

                        int id = obj.getInt("id");
                        String subject = obj.optString("subject", "");
                        String content = obj.optString("content", "");
                        String summary = obj.optString("summary", "");

                        noteList.add(new Note(id, subject, content, summary));
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(NotesActivity.this, "Parse error", Toast.LENGTH_LONG).show()
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