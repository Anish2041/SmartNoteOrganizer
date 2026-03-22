package com.smartnote.organizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvGoRegister;

    OkHttpClient client = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences prefs = getSharedPreferences("smartnote_app", MODE_PRIVATE);
        String savedToken = prefs.getString("token", null);

        if (savedToken != null && !savedToken.isEmpty()) {
            startActivity(new Intent(LoginActivity.this, NotesActivity.class));
            finish();
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(username, password);
            }
        });

        tvGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser(String username, String password) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(ApiClient.BASE_URL + "/auth/login")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseData = response.body().string();

                    try {
                        JSONObject obj = new JSONObject(responseData);
                        String message = obj.getString("message");

                        if (obj.has("token")) {
                            String token = obj.getString("token");
                            saveToken(token);

                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, NotesActivity.class));
                                finish();
                            });
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show()
                            );
                        }

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "Response parse error", Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "JSON Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveToken(String token) {
        SharedPreferences prefs = getSharedPreferences("smartnote_app", MODE_PRIVATE);
        prefs.edit().putString("token", token).apply();
    }
}