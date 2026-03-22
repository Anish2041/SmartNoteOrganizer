package com.smartnote.organizer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditNoteActivity extends AppCompatActivity {

    EditText etEditSubject, etEditContent, etEditSummary;
    Button btnUpdateNote;

    OkHttpClient client = new OkHttpClient();
    String token;
    int noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        etEditSubject = findViewById(R.id.etEditSubject);
        etEditContent = findViewById(R.id.etEditContent);
        etEditSummary = findViewById(R.id.etEditSummary);
        btnUpdateNote = findViewById(R.id.btnUpdateNote);

        token = getToken();
        noteId = getIntent().getIntExtra("id", -1);

        etEditSubject.setText(getIntent().getStringExtra("subject"));
        etEditContent.setText(getIntent().getStringExtra("content"));
        etEditSummary.setText(getIntent().getStringExtra("summary"));

        btnUpdateNote.setOnClickListener(v -> updateNote());
    }

    private void updateNote() {
        try {
            JSONObject json = new JSONObject();
            json.put("subject", etEditSubject.getText().toString().trim());
            json.put("content", etEditContent.getText().toString().trim());
            json.put("summary", etEditSummary.getText().toString().trim());

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(ApiClient.BASE_URL + "/notes/update/" + noteId)
                    .addHeader("Authorization", "Bearer " + token)
                    .put(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(EditNoteActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> {
                        Toast.makeText(EditNoteActivity.this, "Note updated successfully", Toast.LENGTH_SHORT).show();
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