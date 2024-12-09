package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StudySessionsActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private ListView studySessionsListView;
    private Button backButton, resourcesButton;
    private String selectedDate; // This should be passed from GroupCalendarActivity
    private String groupId; // The groupId passed from the previous activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_sessions);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        TextView selectedDateTitle = findViewById(R.id.selectedDateTitle);
        studySessionsListView = findViewById(R.id.studySessionsListView);
        backButton = findViewById(R.id.backButton); // Back button
        resourcesButton = findViewById(R.id.resourcesButton); // Resources button

        // Get the selected date and groupId from the intent
        selectedDate = getIntent().getStringExtra("selectedDate");
        groupId = getIntent().getStringExtra("groupId");
        selectedDateTitle.setText("Study Sessions for " + selectedDate);

        // Fetch study sessions
        fetchStudySessions();

        // Back button to go to the calendar
        backButton.setOnClickListener(v -> finish());

        // Resources button functionality
        resourcesButton.setOnClickListener(v -> {
            Intent intent = new Intent(StudySessionsActivity.this, ResourcesActivity.class);
            intent.putExtra("groupId", groupId); // Pass the groupId to ResourcesActivity
            startActivity(intent);
        });

        // Handle session click
        studySessionsListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            HashMap<String, Object> selectedSession = (HashMap<String, Object>) parent.getItemAtPosition(position);
            Intent intent = new Intent(StudySessionsActivity.this, StudySessionDetailsActivity.class);
            intent.putExtra("sessionDetails", selectedSession);
            startActivity(intent);
        });
    }

    private void fetchStudySessions() {
        List<HashMap<String, Object>> sessionsList = new ArrayList<>();

        firestore.collection("study_sessions")
                .whereEqualTo("date", selectedDate)
                .whereEqualTo("groupId", groupId) // Filter by groupId as well
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HashMap<String, Object> sessionData = new HashMap<>(document.getData());
                            sessionData.put("id", document.getId()); // Add document ID
                            sessionsList.add(sessionData);
                        }

                        // Adapter to display session titles
                        ArrayAdapter<HashMap<String, Object>> adapter = new ArrayAdapter<HashMap<String, Object>>(this, android.R.layout.simple_list_item_1, sessionsList) {
                            @NonNull
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

                        studySessionsListView.setAdapter(adapter);
                    } else {
                        Toast.makeText(StudySessionsActivity.this, "Failed to fetch sessions!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
