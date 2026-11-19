package com.example.joggingclubmanagement;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MemberListActivity extends AppCompatActivity {

    private LinearLayout layoutMemberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);

        layoutMemberList = findViewById(R.id.layoutMemberList);

        loadMembers();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (layoutMemberList != null) {
            loadMembers();
        }
    }

    private void loadMembers() {
        layoutMemberList.removeAllViews();

        TextView loadingMessage = new TextView(this);
        loadingMessage.setText("Loading members from MySQL...");
        loadingMessage.setTextSize(16);
        layoutMemberList.addView(loadingMessage);

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(
                        "http://192.168.8.148:8080/api/members"
                );

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP " + responseCode);
                }

                InputStream input = connection.getInputStream();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(input)
                );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                input.close();

                JSONArray members = new JSONArray(response.toString());

                runOnUiThread(() -> displayMembers(members));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    layoutMemberList.removeAllViews();

                    TextView errorMessage = new TextView(this);
                    errorMessage.setText(
                            "Could not load members from MySQL.\n"
                                    + "Check that Spring Boot is running."
                    );
                    errorMessage.setTextSize(16);
                    layoutMemberList.addView(errorMessage);
                });

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void displayMembers(JSONArray members) {
        layoutMemberList.removeAllViews();

        if (members.length() == 0) {
            TextView emptyMessage = new TextView(this);
            emptyMessage.setText("No members registered yet.");
            emptyMessage.setTextSize(16);
            layoutMemberList.addView(emptyMessage);
            return;
        }

        for (int i = 0; i < members.length(); i++) {
            try {
                JSONObject member = members.getJSONObject(i);

                String name = member.optString(
                        "full_name", "Not available"
                );

                String email = member.optString(
                        "email", "Not available"
                );

                String phone = member.optString(
                        "phone", "Not available"
                );

                String emergencyName = member.optString(
                        "emergency_name", "Not available"
                );

                String emergencyPhone = member.optString(
                        "emergency_phone", "Not available"
                );

                // Get the registration date from the API.
                String createdAt = member.optString(
                        "created_at", "Not available"
                );

                // Show only yyyy-MM-dd.
                if (createdAt.length() >= 10
                        && createdAt.charAt(4) == '-'
                        && createdAt.charAt(7) == '-') {
                    createdAt = createdAt.substring(0, 10);
                }

                TextView memberCard = new TextView(this);

                memberCard.setText(
                        "Member: " + name
                                + "\nEmail: " + email
                                + "\nPhone: " + phone
                                + "\nEmergency Contact: " + emergencyName
                                + "\nEmergency Phone: " + emergencyPhone
                                + "\nRegistered: " + createdAt
                );

                memberCard.setTextSize(16);
                memberCard.setTextColor(0xFF222222);
                memberCard.setPadding(24, 24, 24, 24);
                memberCard.setBackgroundColor(0xFFFFFFFF);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                params.setMargins(0, 0, 0, 16);
                memberCard.setLayoutParams(params);

                layoutMemberList.addView(memberCard);

            } catch (Exception e) {
                // Skip a member record that cannot be displayed.
            }
        }
    }
}