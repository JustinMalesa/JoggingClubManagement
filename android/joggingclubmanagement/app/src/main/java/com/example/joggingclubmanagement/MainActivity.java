package com.example.joggingclubmanagement;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView textApiStatus;
    private GridLayout layoutDashboardSessions;

    private SessionViewModel sessionViewModel;

    private ExecutorService executor;

    private static final String API_URL =
            "http://192.168.8.148:8080/api/sessions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLifecycle().addObserver(
                new DashboardLifecycleObserver()
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.dashboardFragmentContainer,
                            new DashboardInfoFragment()
                    )
                    .commit();
        }

        textApiStatus = findViewById(R.id.textApiStatus);

        layoutDashboardSessions =
                findViewById(R.id.layoutDashboardSessions);

        executor = Executors.newSingleThreadExecutor();

        sessionViewModel =
                new ViewModelProvider(this)
                        .get(SessionViewModel.class);

        sessionViewModel.getSessions().observe(this, sessions -> {
            if (sessions != null) {
                displaySessions(sessions);
            }
        });

        sessionViewModel.getStatus().observe(this, status -> {
            if (status != null) {
                textApiStatus.setText(status);
            }
        });

        Button btnManageSessions =
                findViewById(R.id.btnManageSessions);

        Button btnMemberRegistration =
                findViewById(R.id.btnMemberRegistration);

        Button btnViewMembers =
                findViewById(R.id.btnViewMembers);

        btnManageSessions.setOnClickListener(v ->
                startActivity(new Intent(
                        MainActivity.this,
                        SessionManagementActivity.class
                )));

        btnMemberRegistration.setOnClickListener(v ->
                startActivity(new Intent(
                        MainActivity.this,
                        MemberRegistrationActivity.class
                )));

        btnViewMembers.setOnClickListener(v ->
                startActivity(new Intent(
                        MainActivity.this,
                        MemberListActivity.class
                )));

        // Load data only if the ViewModel has no sessions yet.
        JSONArray savedSessions =
                sessionViewModel.getSessions().getValue();

        if (savedSessions == null || savedSessions.length() == 0) {
            loadDashboardSessions();
        }
    }

    private void loadDashboardSessions() {

        sessionViewModel.setStatus("Loading sessions...");

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {
                URL url = new URL(API_URL);

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                int responseCode = connection.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception(
                            "API returned HTTP " + responseCode
                    );
                }

                StringBuilder response = new StringBuilder();

                try (
                        InputStream inputStream =
                                connection.getInputStream();

                        BufferedReader reader =
                                new BufferedReader(
                                        new InputStreamReader(
                                                inputStream,
                                                StandardCharsets.UTF_8
                                        )
                                )
                ) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                JSONArray sessions =
                        new JSONArray(response.toString());

                runOnUiThread(() -> {
                    sessionViewModel.setSessions(sessions);

                    sessionViewModel.setStatus(
                            "Connected to database successfully"
                    );
                });

            } catch (Exception e) {

                runOnUiThread(() -> {
                    sessionViewModel.setStatus(
                            "Could not load sessions. Check API connection."
                    );

                    layoutDashboardSessions.removeAllViews();

                    TextView errorText = new TextView(this);

                    errorText.setText(
                            "Unable to load sessions.\n\n"
                                    + e.getMessage()
                                    + "\n\nMake sure the Spring Boot API "
                                    + "is running on port 8080."
                    );

                    errorText.setTextColor(Color.RED);
                    errorText.setTextSize(15);
                    errorText.setPadding(16, 16, 16, 16);

                    layoutDashboardSessions.addView(errorText);
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void displaySessions(JSONArray sessions) {

        layoutDashboardSessions.removeAllViews();
        layoutDashboardSessions.setColumnCount(1);

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
        ).format(new Date());

        List<JSONObject> upcomingSessions = new ArrayList<>();

        for (int i = 0; i < sessions.length(); i++) {

            JSONObject session = sessions.optJSONObject(i);

            if (session == null) {
                continue;
            }

            String date = session.optString(
                    "session_date",
                    ""
            );

            if (date.length() >= 10) {
                date = date.substring(0, 10);
            }

            if (date.matches("\\d{4}-\\d{2}-\\d{2}")
                    && date.compareTo(today) >= 0) {

                upcomingSessions.add(session);
            }
        }

        Collections.sort(upcomingSessions, (first, second) -> {

            String firstDate =
                    first.optString("session_date", "");

            String secondDate =
                    second.optString("session_date", "");

            if (firstDate.length() >= 10) {
                firstDate = firstDate.substring(0, 10);
            }

            if (secondDate.length() >= 10) {
                secondDate = secondDate.substring(0, 10);
            }

            int dateResult = firstDate.compareTo(secondDate);

            if (dateResult != 0) {
                return dateResult;
            }

            return first.optString("session_time", "")
                    .compareTo(
                            second.optString("session_time", "")
                    );
        });

        if (upcomingSessions.size() > 10) {
            upcomingSessions = new ArrayList<>(
                    upcomingSessions.subList(0, 10)
            );
        }

        if (upcomingSessions.isEmpty()) {

            TextView emptyText = new TextView(this);

            emptyText.setText(
                    "No upcoming sessions available."
            );

            emptyText.setTextSize(16);
            emptyText.setPadding(16, 16, 16, 16);

            layoutDashboardSessions.addView(emptyText);
            return;
        }

        Map<String, List<JSONObject>> groupedSessions =
                new LinkedHashMap<>();

        groupedSessions.put("Beginner", new ArrayList<>());
        groupedSessions.put("Intermediate", new ArrayList<>());
        groupedSessions.put("Advanced", new ArrayList<>());
        groupedSessions.put("Family Fun Run", new ArrayList<>());
        groupedSessions.put("Marathon Training", new ArrayList<>());

        for (JSONObject session : upcomingSessions) {

            String category = session.optString(
                    "category",
                    ""
            ).trim().toLowerCase(Locale.ROOT);

            String categoryName = null;

            if (category.contains("beginner")) {
                categoryName = "Beginner";
            } else if (category.contains("intermediate")) {
                categoryName = "Intermediate";
            } else if (category.contains("advanced")) {
                categoryName = "Advanced";
            } else if (category.contains("family")) {
                categoryName = "Family Fun Run";
            } else if (category.contains("marathon")) {
                categoryName = "Marathon Training";
            }

            if (categoryName != null) {
                groupedSessions.get(categoryName).add(session);
            }
        }

        int screenWidthDp = getResources()
                .getConfiguration().screenWidthDp;

        int columnCount = screenWidthDp >= 600 ? 2 : 1;

        for (String categoryName : groupedSessions.keySet()) {

            List<JSONObject> categorySessions =
                    groupedSessions.get(categoryName);

            if (categorySessions == null ||
                    categorySessions.isEmpty()) {
                continue;
            }

            android.widget.LinearLayout section =
                    new android.widget.LinearLayout(this);

            section.setOrientation(
                    android.widget.LinearLayout.VERTICAL
            );

            GridLayout.LayoutParams sectionParams =
                    new GridLayout.LayoutParams();

            sectionParams.width =
                    GridLayout.LayoutParams.MATCH_PARENT;

            sectionParams.height =
                    GridLayout.LayoutParams.WRAP_CONTENT;

            sectionParams.columnSpec = GridLayout.spec(0, 1f);

            sectionParams.setMargins(
                    dpToPx(6), dpToPx(12),
                    dpToPx(6), dpToPx(8)
            );

            section.setLayoutParams(sectionParams);

            TextView heading = new TextView(this);

            heading.setText(categoryName);
            heading.setTextSize(20);
            heading.setTypeface(null, Typeface.BOLD);
            heading.setTextColor(Color.rgb(30, 55, 90));

            heading.setPadding(
                    dpToPx(8), dpToPx(8),
                    dpToPx(8), dpToPx(10)
            );

            section.addView(heading);

            GridLayout categoryGrid = new GridLayout(this);
            categoryGrid.setColumnCount(columnCount);

            for (int i = 0; i < categorySessions.size(); i++) {

                JSONObject session = categorySessions.get(i);

                String name = session.optString(
                        "session_name",
                        "Unnamed session"
                );

                String date = session.optString(
                        "session_date",
                        "Not specified"
                );

                String time = session.optString(
                        "session_time",
                        "Not specified"
                );

                String location = session.optString(
                        "location",
                        "Not specified"
                );

                String status = session.optString(
                        "status",
                        "Unknown"
                );

                int capacity = session.optInt("capacity", 0);

                int registeredCount = session.optInt(
                        "registered_count",
                        0
                );

                if (date.length() > 10) {
                    date = date.substring(0, 10);
                }

                if (time.length() >= 5) {
                    time = time.substring(0, 5);
                }

                android.widget.LinearLayout card =
                        new android.widget.LinearLayout(this);

                card.setOrientation(
                        android.widget.LinearLayout.VERTICAL
                );

                int padding = dpToPx(16);

                card.setPadding(
                        padding, padding, padding, padding
                );

                card.setBackgroundColor(Color.WHITE);
                card.setElevation(dpToPx(3));

                GridLayout.LayoutParams cardParams =
                        new GridLayout.LayoutParams(
                                GridLayout.spec(
                                        i / columnCount, 1, 1f
                                ),
                                GridLayout.spec(
                                        i % columnCount, 1, 1f
                                )
                        );

                cardParams.width = 0;

                cardParams.height =
                        GridLayout.LayoutParams.WRAP_CONTENT;

                int margin = dpToPx(6);

                cardParams.setMargins(
                        margin, margin, margin, margin
                );

                card.setLayoutParams(cardParams);

                TextView title = new TextView(this);

                title.setText(name);
                title.setTextSize(18);
                title.setTypeface(null, Typeface.BOLD);
                title.setTextColor(Color.rgb(30, 55, 90));

                title.setPadding(
                        0, 0, 0, dpToPx(10)
                );

                TextView details = new TextView(this);

                details.setText(
                        "Date: " + date
                                + "\nTime: " + time
                                + "\nMeeting location: " + location
                                + "\nParticipants: "
                                + registeredCount + " / " + capacity
                                + "\nStatus: " + status
                );

                details.setTextSize(14);
                details.setTextColor(Color.DKGRAY);

                details.setLineSpacing(
                        dpToPx(3), 1.0f
                );

                card.addView(title);
                card.addView(details);

                categoryGrid.addView(card);
            }

            section.addView(categoryGrid);
            layoutDashboardSessions.addView(section);
        }
    }

    private int dpToPx(int dp) {

        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    @Override
    protected void onDestroy() {
        if (executor != null) {
            executor.shutdownNow();
        }

        super.onDestroy();
    }
}