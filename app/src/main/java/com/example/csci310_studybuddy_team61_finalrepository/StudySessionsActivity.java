package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StudySessionsActivity extends AppCompatActivity {

    private static final String TAG = "StudySessionsActivity";

    private FirebaseFirestore firestore;
    private ListView studySessionsListView;
    private Button backButton, resourcesButton;
    private String selectedDate;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_sessions);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        TextView selectedDateTitle = findViewById(R.id.selectedDateTitle);
        studySessionsListView = findViewById(R.id.studySessionsListView);
        backButton = findViewById(R.id.backButton);
        resourcesButton = findViewById(R.id.resourcesButton);

        // Get data from intent
        selectedDate = getIntent().getStringExtra("selectedDate");
        groupName = getIntent().getStringExtra("GROUP_NAME"); // Use the same key as GroupCalendarActivity

        // Debugging: Log and validate the data
        if (groupName == null || selectedDate == null) {
            Toast.makeText(this, "Error: Missing group name or selected date!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Missing groupName or selectedDate in intent.");
            finish();
            return;
        } else {
            Log.d(TAG, "Received groupName: " + groupName + ", selectedDate: " + selectedDate);
        }

        // Set the title
        selectedDateTitle.setText("Study Sessions for " + selectedDate);

        // Fetch study sessions (this is just a placeholder)
        fetchStudySessions();

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Resources button
        resourcesButton.setOnClickListener(v -> {
            Intent intent = new Intent(StudySessionsActivity.this, ResourcesActivity.class);
            intent.putExtra("GROUP_NAME", groupName); // Pass the correct group name key
            startActivity(intent);
        });
    }

    private void fetchStudySessions() {
        firestore.collection("study_sessions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "No study sessions found in Firestore!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "No documents found in Firestore.");
                        return;
                    }

                    // List to store session details after filtering
                    List<HashMap<String, Object>> sessionsList = new ArrayList<>();

                    querySnapshot.forEach(document -> {
                        HashMap<String, Object> sessionData = new HashMap<>(document.getData());
                        sessionData.put("id", document.getId());
                        Log.d(TAG, "Found session: " + sessionData);

                        // Apply manual filtering for group_id and date
                        if (sessionData.containsKey("group_id") && sessionData.containsKey("date")) {
                            String groupId = sessionData.get("group_id").toString();
                            String date = sessionData.get("date").toString();

                            if (groupId.equals(groupName) && date.equals(selectedDate)) {
                                sessionsList.add(sessionData);
                            }
                        }
                    });

                    if (sessionsList.isEmpty()) {
                        Toast.makeText(this, "No study sessions found for the selected date!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "No sessions matched group_id: " + groupName + ", date: " + selectedDate);
                        return;
                    }

                    // Adapter for the ListView
                    ArrayAdapter<HashMap<String, Object>> adapter = new ArrayAdapter<HashMap<String, Object>>(this, android.R.layout.simple_list_item_1, sessionsList) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            if (convertView == null) {
                                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
                            }

                            TextView textView = convertView.findViewById(android.R.id.text1);
                            HashMap<String, Object> session = getItem(position);
                            if (session != null && session.containsKey("title")) {
                                textView.setText(session.get("title").toString());
                            }

                            return convertView;
                        }
                    };

                    // Set the adapter to the ListView
                    studySessionsListView.setAdapter(adapter);

                    // Add OnItemClickListener for navigating to details page
                    studySessionsListView.setOnItemClickListener((parent, view, position, id) -> {
                        HashMap<String, Object> selectedSession = sessionsList.get(position);

                        // Debugging: Log the selected session
                        Log.d(TAG, "Selected session: " + selectedSession);

                        // Navigate to StudySessionDetailsActivity
                        Intent intent = new Intent(StudySessionsActivity.this, StudySessionDetailsActivity.class);
                        intent.putExtra("sessionDetails", selectedSession); // Pass session data
                        startActivity(intent);
                    });

                    Log.d(TAG, "Filtered study sessions: " + sessionsList.toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch study sessions!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching study sessions: ", e);
                });
    }
}
