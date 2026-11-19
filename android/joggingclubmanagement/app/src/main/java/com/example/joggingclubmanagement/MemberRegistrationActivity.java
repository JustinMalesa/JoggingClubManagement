package com.example.joggingclubmanagement;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MemberRegistrationActivity extends AppCompatActivity {

    private EditText editFullName;
    private EditText editEmail;
    private EditText editPhone;
    private EditText editEmergencyName;
    private EditText editEmergencyPhone;
    private Button btnSaveMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_registration);

        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editEmergencyName = findViewById(R.id.editEmergencyName);
        editEmergencyPhone = findViewById(R.id.editEmergencyPhone);
        btnSaveMember = findViewById(R.id.btnSaveMember);

        btnSaveMember.setOnClickListener(v -> registerMember());
    }

    private void registerMember() {
        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String emergencyName =
                editEmergencyName.getText().toString().trim();
        String emergencyPhone =
                editEmergencyPhone.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()
                || emergencyName.isEmpty() || emergencyPhone.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please complete all fields",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Enter a valid email address");
            editEmail.requestFocus();
            return;
        }

        btnSaveMember.setEnabled(false);
        btnSaveMember.setText("Registering...");

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL("http://192.168.8.148:8080/api/members");
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type", "application/json"
                );

                JSONObject member = new JSONObject();
                member.put("full_name", fullName);
                member.put("email", email);
                member.put("phone", phone);
                member.put("emergency_name", emergencyName);
                member.put("emergency_phone", emergencyPhone);

                byte[] requestData = member.toString().getBytes("UTF-8");

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(requestData);
                }

                int responseCode = connection.getResponseCode();

                InputStream responseStream =
                        responseCode >= 200 && responseCode < 400
                                ? connection.getInputStream()
                                : connection.getErrorStream();

                StringBuilder response = new StringBuilder();

                if (responseStream != null) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = responseStream.read(buffer)) != -1) {
                        response.append(
                                new String(buffer, 0, bytesRead, "UTF-8")
                        );
                    }

                    responseStream.close();
                }

                String responseMessage = response.toString();

                runOnUiThread(() -> {
                    btnSaveMember.setEnabled(true);
                    btnSaveMember.setText("Save Member");

                    if (responseCode == HttpURLConnection.HTTP_CREATED) {
                        Toast.makeText(
                                MemberRegistrationActivity.this,
                                "Member registered in MySQL successfully!",
                                Toast.LENGTH_LONG
                        ).show();

                        editFullName.setText("");
                        editEmail.setText("");
                        editPhone.setText("");
                        editEmergencyName.setText("");
                        editEmergencyPhone.setText("");

                    } else if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
                        editEmail.setError(
                                "This email address is already registered"
                        );
                        editEmail.requestFocus();

                    } else {
                        Toast.makeText(
                                MemberRegistrationActivity.this,
                                responseMessage.isEmpty()
                                        ? "Registration failed. HTTP " + responseCode
                                        : responseMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSaveMember.setEnabled(true);
                    btnSaveMember.setText("Save Member");

                    Toast.makeText(
                            MemberRegistrationActivity.this,
                            "Could not connect to the API. Check that Spring Boot is running.",
                            Toast.LENGTH_LONG
                    ).show();
                });

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}