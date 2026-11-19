package com.example.joggingclubmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SessionManagementActivity extends AppCompatActivity {

    private LinearLayout layoutSessionList;
    private Button btnAddSession;

    private final ActivityResultLauncher<Intent> sessionFormLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            loadSessions();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_management);

        layoutSessionList = findViewById(R.id.layoutSessionList);
        btnAddSession = findViewById(R.id.btnAddSession);

        btnAddSession.setOnClickListener(v -> {
            Intent intent = new Intent(
                    SessionManagementActivity.this,
                    SessionFormActivity.class
            );
            sessionFormLauncher.launch(intent);
        });

        loadSessions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (layoutSessionList != null) {
            loadSessions();
        }
    }

    private void loadSessions() {
        layoutSessionList.removeAllViews();
        showMessage("Loading sessions from MySQL...");

        new Thread(() -> {
            try {
                JSONArray sessions = new JSONArray(
                        request("GET", "/api/sessions", null)
                );

                runOnUiThread(() -> displaySessions(sessions));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    layoutSessionList.removeAllViews();
                    showMessage(
                            "Could not load sessions.\n" +
                                    "Check that Spring Boot is running."
                    );
                });
            }
        }).start();
    }

    private void displaySessions(JSONArray sessions) {
        layoutSessionList.removeAllViews();

        if (sessions.length() == 0) {
            showMessage("No sessions found in MySQL.");
            return;
        }

        for (int i = 0; i < sessions.length(); i++) {
            try {
                JSONObject session = sessions.getJSONObject(i);

                int id = session.getInt("session_id");

                String name = session.optString(
                        "session_name", "Unnamed session");

                String category = session.optString(
                        "category", "Not specified");

                String rawDate = session.optString(
                        "session_date", "Not specified");

                // Display only YYYY-MM-DD.
                // Example:
                // 2026-10-29.
                final String date = rawDate.length() >= 10
                        ? rawDate.substring(0, 10)
                        : rawDate;


                String time = session.optString(
                        "session_time", "Not specified");

                String location = session.optString(
                        "location", "Not specified");

                int capacity = session.optInt("capacity", 0);

                String status = session.optString("status", "Open");

                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(24, 20, 24, 20);
                card.setBackgroundColor(0xFFFFFFFF);

                LinearLayout.LayoutParams cardParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                cardParams.setMargins(0, 0, 0, 20);
                card.setLayoutParams(cardParams);

                TextView details = new TextView(this);

                details.setText(
                        "Session ID: " + id +
                                "\nSession: " + name +
                                "\nCategory: " + category +
                                "\nDate: " + date +
                                "\nTime: " + time +
                                "\nLocation: " + location +
                                "\nCapacity: " + capacity +
                                "\nStatus: " + status
                );

                details.setTextSize(16);
                details.setTextColor(0xFF222222);
                card.addView(details);

                // EDIT SESSION BUTTON
                Button editButton = new Button(this);
                editButton.setText("Edit Session");
                card.addView(editButton);

                editButton.setOnClickListener(v -> {
                    Intent intent = new Intent(
                            SessionManagementActivity.this,
                            SessionFormActivity.class
                    );

                    intent.putExtra("session_id", id);
                    intent.putExtra("session_name", name);
                    intent.putExtra("category", category);
                    intent.putExtra("session_date", date);
                    intent.putExtra("session_time", time);
                    intent.putExtra("location", location);
                    intent.putExtra("capacity", capacity);

                    sessionFormLauncher.launch(intent);
                });

                // DELETE SESSION BUTTON
                Button deleteButton = new Button(this);
                deleteButton.setText("Delete Session");
                card.addView(deleteButton);

                deleteButton.setOnClickListener(v ->
                        new AlertDialog.Builder(
                                SessionManagementActivity.this)
                                .setTitle("Delete Session")
                                .setMessage(
                                        "Are you sure you want to delete \"" +
                                                name + "\"?"
                                )
                                .setPositiveButton("Delete",
                                        (dialog, which) ->
                                                deleteSession(id, name))
                                .setNegativeButton("Cancel", null)
                                .show()
                );

                // CHANGE STATUS BUTTON
                Button statusButton = new Button(this);
                statusButton.setText("Change Status");
                card.addView(statusButton);

                statusButton.setOnClickListener(
                        v -> showStatusOptions(id)
                );

                // REGISTER MEMBER BUTTON
                Button registerButton = new Button(this);
                registerButton.setText("Register Member");
                card.addView(registerButton);

                registerButton.setOnClickListener(v -> {
                    if (!"Open".equalsIgnoreCase(status)) {
                        Toast.makeText(
                                this,
                                "Only open sessions accept registrations.",
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        loadMembersForRegistration(id, name);
                    }
                });

                // VIEW PARTICIPANTS BUTTON
                Button participantsButton = new Button(this);
                participantsButton.setText("View Participants");
                card.addView(participantsButton);

                participantsButton.setOnClickListener(
                        v -> loadParticipants(id, name)
                );

                // MARK ATTENDANCE BUTTON
                Button attendanceButton = new Button(this);
                attendanceButton.setText("Mark Attendance");
                card.addView(attendanceButton);

                attendanceButton.setOnClickListener(
                        v -> loadAttendance(id, name)
                );

                layoutSessionList.addView(card);

            } catch (Exception e) {
                // Skip invalid session records.
            }
        }
    }

    // DELETE A SESSION
    private void deleteSession(int sessionId, String sessionName) {
        new Thread(() -> {
            try {
                request(
                        "DELETE",
                        "/api/sessions/" + sessionId,
                        null
                );

                runOnUiThread(() -> {
                    Toast.makeText(
                            SessionManagementActivity.this,
                            sessionName + " deleted successfully.",
                            Toast.LENGTH_LONG
                    ).show();

                    loadSessions();
                });

            } catch (Exception e) {
                String message = e.getMessage();

                runOnUiThread(() -> Toast.makeText(
                        SessionManagementActivity.this,
                        message == null
                                ? "Could not delete session."
                                : message,
                        Toast.LENGTH_LONG
                ).show());
            }
        }).start();
    }

    // LOAD MEMBERS FOR REGISTRATION
    private void loadMembersForRegistration(
            int sessionId, String sessionName) {

        new Thread(() -> {
            try {
                JSONArray members = new JSONArray(
                        request("GET", "/api/members", null)
                );

                runOnUiThread(() ->
                        showMemberChoices(sessionId, sessionName, members));

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        this,
                        "Could not load members. Check the API.",
                        Toast.LENGTH_LONG
                ).show());
            }
        }).start();
    }

    private void showMemberChoices(
            int sessionId, String sessionName, JSONArray members) {

        if (members.length() == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("No Members")
                    .setMessage(
                            "Register a member first before selecting them.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String[] memberNames = new String[members.length()];
        int[] memberIds = new int[members.length()];

        for (int i = 0; i < members.length(); i++) {
            JSONObject member = members.optJSONObject(i);

            if (member == null) {
                memberNames[i] = "Unknown member";
                memberIds[i] = -1;
            } else {
                memberIds[i] = member.optInt("member_id", -1);

                memberNames[i] = member.optString(
                        "full_name", "Unknown member"
                ) + " (" + member.optString("email", "") + ")";
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Register for " + sessionName)
                .setItems(memberNames, (dialog, which) -> {
                    int memberId = memberIds[which];

                    if (memberId > 0) {
                        confirmRegistration(
                                memberId,
                                memberNames[which],
                                sessionId,
                                sessionName
                        );
                    } else {
                        Toast.makeText(
                                this,
                                "Invalid member ID.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmRegistration(
            int memberId,
            String memberName,
            int sessionId,
            String sessionName) {

        new AlertDialog.Builder(this)
                .setTitle("Confirm Registration")
                .setMessage(
                        "Register " + memberName +
                                " for " + sessionName + "?"
                )
                .setPositiveButton("Register", (dialog, which) ->
                        registerMember(memberId, sessionId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void registerMember(int memberId, int sessionId) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("member_id", memberId);
                body.put("session_id", sessionId);

                request(
                        "POST",
                        "/api/registrations",
                        body.toString()
                );

                runOnUiThread(() -> {
                    Toast.makeText(
                            this,
                            "Member registered for the session!",
                            Toast.LENGTH_LONG
                    ).show();

                    loadSessions();
                });

            } catch (Exception e) {
                String message = e.getMessage();

                runOnUiThread(() -> Toast.makeText(
                        this,
                        message == null
                                ? "Registration failed."
                                : message,
                        Toast.LENGTH_LONG
                ).show());

                runOnUiThread(this::loadSessions);
            }
        }).start();
    }

    // LOAD PARTICIPANTS
    private void loadParticipants(int sessionId, String sessionName) {
        new Thread(() -> {
            try {
                JSONArray participants = new JSONArray(
                        request(
                                "GET",
                                "/api/sessions/" + sessionId +
                                        "/participants",
                                null
                        )
                );

                runOnUiThread(() ->
                        showParticipants(sessionName, participants));

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        this,
                        "Could not load participants.",
                        Toast.LENGTH_LONG
                ).show());
            }
        }).start();
    }

    private void showParticipants(
            String sessionName, JSONArray participants) {

        StringBuilder message = new StringBuilder();

        if (participants.length() == 0) {
            message.append("No members have registered yet.");
        } else {
            for (int i = 0; i < participants.length(); i++) {
                JSONObject participant = participants.optJSONObject(i);

                if (participant != null) {
                    message.append(i + 1)
                            .append(". ")
                            .append(participant.optString(
                                    "full_name", "Unknown member"))
                            .append("\nEmail: ")
                            .append(participant.optString(
                                    "email", "Not available"))
                            .append("\nPhone: ")
                            .append(participant.optString(
                                    "phone", "Not available"))
                            .append("\n\n");
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Participants: " + sessionName)
                .setMessage(message.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    // LOAD ATTENDANCE
    private void loadAttendance(int sessionId, String sessionName) {
        new Thread(() -> {
            try {
                JSONArray attendance = new JSONArray(
                        request(
                                "GET",
                                "/api/sessions/" + sessionId +
                                        "/attendance",
                                null
                        )
                );

                runOnUiThread(() ->
                        showAttendance(sessionId, sessionName, attendance));

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        this,
                        "Could not load attendance. Check the API.",
                        Toast.LENGTH_LONG
                ).show());
            }
        }).start();
    }

    private void showAttendance(
            int sessionId,
            String sessionName,
            JSONArray attendance) {

        if (attendance.length() == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Attendance: " + sessionName)
                    .setMessage(
                            "No members are registered for this session yet.")
                    .setPositiveButton("Close", null)
                    .show();
            return;
        }

        String[] memberChoices = new String[attendance.length()];

        for (int i = 0; i < attendance.length(); i++) {
            JSONObject member = attendance.optJSONObject(i);

            if (member == null) {
                memberChoices[i] = "Unknown member";
                continue;
            }

            String name = member.optString(
                    "full_name", "Unknown member");

            Object value = member.opt("attended");
            String status;

            if (value == null || value == JSONObject.NULL) {
                status = "Not marked";
            } else if (member.optBoolean("attended", false)) {
                status = "Present";
            } else {
                status = "Absent";
            }

            memberChoices[i] = name + " — " + status;
        }

        new AlertDialog.Builder(this)
                .setTitle("Attendance: " + sessionName)
                .setItems(memberChoices, (dialog, which) -> {
                    JSONObject selected = attendance.optJSONObject(which);

                    if (selected != null) {
                        int registrationId = selected.optInt(
                                "registration_id", -1);

                        if (registrationId > 0) {
                            showAttendanceOptions(
                                    sessionId,
                                    sessionName,
                                    registrationId,
                                    selected.optString(
                                            "full_name", "Unknown member")
                            );
                        } else {
                            Toast.makeText(
                                    this,
                                    "Invalid registration ID.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    // CHOOSE PRESENT OR ABSENT
    private void showAttendanceOptions(
            int sessionId,
            String sessionName,
            int registrationId,
            String memberName) {

        String[] options = {"Present", "Absent"};

        new AlertDialog.Builder(this)
                .setTitle("Mark Attendance: " + memberName)
                .setItems(options, (dialog, which) ->
                        saveAttendance(
                                sessionId,
                                sessionName,
                                registrationId,
                                memberName,
                                which == 0
                        ))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // SAVE ATTENDANCE
    private void saveAttendance(
            int sessionId,
            String sessionName,
            int registrationId,
            String memberName,
            boolean attended) {

        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("attended", attended);

                request(
                        "PUT",
                        "/api/attendance/" + registrationId,
                        body.toString()
                );

                runOnUiThread(() -> {
                    String status = attended ? "Present" : "Absent";

                    Toast.makeText(
                            this,
                            memberName + " marked " + status + ".",
                            Toast.LENGTH_LONG
                    ).show();

                    loadAttendance(sessionId, sessionName);
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        this,
                        "Could not save attendance: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
            }
        }).start();
    }

    // CHANGE SESSION STATUS
    private void showStatusOptions(int sessionId) {
        String[] statuses = {
                "Open", "Full", "Cancelled", "Completed"
        };

        new AlertDialog.Builder(this)
                .setTitle("Choose Session Status")
                .setItems(statuses, (dialog, which) ->
                        updateStatus(sessionId, statuses[which]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateStatus(int sessionId, String newStatus) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("status", newStatus);

                request(
                        "PUT",
                        "/api/sessions/" + sessionId + "/status",
                        body.toString()
                );

                runOnUiThread(() -> {
                    Toast.makeText(
                            this,
                            "Status updated to " + newStatus,
                            Toast.LENGTH_SHORT
                    ).show();

                    loadSessions();
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        this,
                        "Status update failed: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
            }
        }).start();
    }

    // SEND REQUEST TO SPRING BOOT API
    private String request(
            String method, String path, String requestBody)
            throws Exception {

        HttpURLConnection connection = null;

        try {
            URL url = new URL("http://192.168.8.148:8080" + path);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(method);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty(
                    "Accept", "application/json");

            if (requestBody != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type", "application/json");

                byte[] data = requestBody.getBytes(
                        StandardCharsets.UTF_8);

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(data);
                }
            }

            int responseCode = connection.getResponseCode();

            InputStream input =
                    responseCode >= 200 && responseCode < 400
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            StringBuilder response = new StringBuilder();

            if (input != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                input, StandardCharsets.UTF_8))) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            }

            if (responseCode < 200 || responseCode >= 300) {
                String error = response.toString();

                if (error.isEmpty()) {
                    error = "HTTP " + responseCode;
                }

                throw new Exception(error);
            }

            return response.toString();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void showMessage(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(16);
        layoutSessionList.addView(textView);
    }
}