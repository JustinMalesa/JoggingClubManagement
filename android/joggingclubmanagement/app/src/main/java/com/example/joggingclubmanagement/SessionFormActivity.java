package com.example.joggingclubmanagement;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;

public class SessionFormActivity extends AppCompatActivity {

    private EditText editSessionName;
    private EditText editSessionDate;
    private EditText editSessionTime;
    private EditText editSessionLocation;
    private EditText editSessionCapacity;

    private Spinner spinnerSessionCategory;
    private Button btnSaveSession;
    private TextView textSessionFormTitle;

    private int sessionId = -1;
    private boolean editMode = false;
    private boolean saving = false;

    private final String baseUrl = "http://192.168.8.148:8080";

    private final String[] categories = {
            "Beginner",
            "Intermediate",
            "Advanced",
            "Family Fun Run",
            "Marathon Training"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_form);

        editSessionName = findViewById(R.id.editSessionName);
        editSessionDate = findViewById(R.id.editSessionDate);
        editSessionTime = findViewById(R.id.editSessionTime);
        editSessionLocation = findViewById(R.id.editSessionLocation);
        editSessionCapacity = findViewById(R.id.editSessionCapacity);

        spinnerSessionCategory =
                findViewById(R.id.spinnerSessionCategory);

        btnSaveSession = findViewById(R.id.btnSaveSession);

        // The title is optional in the XML layout.
        int titleId = getResources().getIdentifier(
                "textSessionFormTitle",
                "id",
                getPackageName()
        );

        if (titleId != 0) {
            textSessionFormTitle = findViewById(titleId);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerSessionCategory.setAdapter(adapter);

        // Open date picker when the date field is tapped.
        editSessionDate.setFocusable(false);
        editSessionDate.setClickable(true);
        editSessionDate.setOnClickListener(v -> showDatePicker());

        // Open time picker when the time field is tapped.
        editSessionTime.setFocusable(false);
        editSessionTime.setClickable(true);
        editSessionTime.setOnClickListener(v -> showTimePicker());

        sessionId = getIntent().getIntExtra("session_id", -1);
        editMode = sessionId > 0;

        if (editMode) {
            if (textSessionFormTitle != null) {
                textSessionFormTitle.setText("Edit Session");
            }

            btnSaveSession.setText("Update Session");
            loadExistingSession();
        } else {
            if (textSessionFormTitle != null) {
                textSessionFormTitle.setText("Add New Session");
            }

            btnSaveSession.setText("Save Session");
        }

        btnSaveSession.setOnClickListener(v -> saveSession());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        String currentDate = editSessionDate.getText().toString().trim();

        try {
            // Only use the date portion, never the timestamp portion.
            if (currentDate.length() >= 10) {
                int year = Integer.parseInt(
                        currentDate.substring(0, 4)
                );
                int month = Integer.parseInt(
                        currentDate.substring(5, 7)
                ) - 1;
                int day = Integer.parseInt(
                        currentDate.substring(8, 10)
                );

                calendar.set(year, month, day);
            }
        } catch (Exception ignored) {
            // Use today's date if the field is empty or invalid.
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );

                    editSessionDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(
                            Locale.US,
                            "%02d:%02d:00",
                            hourOfDay,
                            minute
                    );

                    editSessionTime.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        dialog.show();
    }

    private void loadExistingSession() {
        btnSaveSession.setEnabled(false);

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(
                        baseUrl + "/api/sessions"
                );

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();

                if (responseCode < 200 || responseCode >= 300) {
                    throw new Exception(
                            "Could not load sessions. HTTP " + responseCode
                    );
                }

                String response = readResponse(
                        connection.getInputStream()
                );

                org.json.JSONArray sessions =
                        new org.json.JSONArray(response);

                JSONObject selectedSession = null;

                for (int i = 0; i < sessions.length(); i++) {
                    JSONObject item = sessions.getJSONObject(i);

                    if (item.optInt("session_id", -1) == sessionId) {
                        selectedSession = item;
                        break;
                    }
                }

                if (selectedSession == null) {
                    throw new Exception(
                            "Session " + sessionId + " was not found."
                    );
                }

                JSONObject session = selectedSession;

                runOnUiThread(() -> {
                    editSessionName.setText(
                            session.optString("session_name", "")
                    );

                    // FIX: Convert an ISO timestamp into yyyy-MM-dd.
                    String rawDate = session.optString(
                            "session_date",
                            ""
                    );

                    String cleanDate = rawDate;

                    if (rawDate.length() >= 10) {
                        cleanDate = rawDate.substring(0, 10);
                    }

                    editSessionDate.setText(cleanDate);

                    String rawTime = session.optString(
                            "session_time",
                            ""
                    );

                    if (rawTime.length() >= 5) {
                        rawTime = rawTime.substring(0, 5);
                    }

                    editSessionTime.setText(rawTime);
                    editSessionLocation.setText(
                            session.optString("location", "")
                    );

                    editSessionCapacity.setText(
                            String.valueOf(
                                    session.optInt("capacity", 20)
                            )
                    );

                    String category = session.optString(
                            "category",
                            "Beginner"
                    );

                    for (int i = 0; i < categories.length; i++) {
                        if (categories[i].equalsIgnoreCase(category)) {
                            spinnerSessionCategory.setSelection(i);
                            break;
                        }
                    }

                    btnSaveSession.setEnabled(true);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSaveSession.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Could not load session: " + e.getMessage(),
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

    private void saveSession() {
        if (saving) {
            return;
        }

        String name = editSessionName.getText().toString().trim();
        String date = editSessionDate.getText().toString().trim();
        String time = editSessionTime.getText().toString().trim();
        String location = editSessionLocation.getText().toString().trim();
        String capacityText =
                editSessionCapacity.getText().toString().trim();

        if (name.isEmpty()
                || date.isEmpty()
                || time.isEmpty()
                || location.isEmpty()
                || capacityText.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please complete all fields.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Accept only yyyy-MM-dd, not a full ISO timestamp.
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            editSessionDate.setError(
                    "Select a date using the date picker."
            );
            return;
        }

        int capacity;

        try {
            capacity = Integer.parseInt(capacityText);

            if (capacity <= 0) {
                editSessionCapacity.setError(
                        "Capacity must be greater than zero."
                );
                return;
            }
        } catch (NumberFormatException e) {
            editSessionCapacity.setError(
                    "Enter a valid whole number."
            );
            return;
        }

        String category =
                spinnerSessionCategory.getSelectedItem().toString();

        JSONObject body = new JSONObject();

        try {
            body.put("session_name", name);
            body.put("category", category);
            body.put("session_date", date);
            body.put("session_time", time);
            body.put("location", location);
            body.put("capacity", capacity);
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Could not prepare session data.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        saving = true;
        btnSaveSession.setEnabled(false);

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                String endpoint = editMode
                        ? "/api/sessions/" + sessionId
                        : "/api/sessions";

                URL url = new URL(baseUrl + endpoint);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(
                        editMode ? "PUT" : "POST"
                );
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );
                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                byte[] data = body.toString().getBytes(
                        StandardCharsets.UTF_8
                );

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(data);
                    output.flush();
                }

                int responseCode = connection.getResponseCode();

                InputStream responseStream =
                        responseCode >= 200 && responseCode < 300
                                ? connection.getInputStream()
                                : connection.getErrorStream();

                String responseMessage = responseStream != null
                        ? readResponse(responseStream)
                        : "";

                if (responseCode < 200 || responseCode >= 300) {
                    throw new Exception(
                            "HTTP " + responseCode
                                    + (responseMessage.isEmpty()
                                    ? ""
                                    : ": " + responseMessage)
                    );
                }

                runOnUiThread(() -> {
                    Toast.makeText(
                            this,
                            editMode
                                    ? "Session updated successfully!"
                                    : "Session created successfully!",
                            Toast.LENGTH_LONG
                    ).show();

                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    saving = false;
                    btnSaveSession.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Save failed: " + e.getMessage(),
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

    private String readResponse(InputStream inputStream)
            throws Exception {

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        inputStream,
                        StandardCharsets.UTF_8
                )
        )) {
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }
}