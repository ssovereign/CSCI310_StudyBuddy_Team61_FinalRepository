package com.example.csci310_studybuddy_team61_finalrepository;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddStudySessionActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private List<String> groupMembers = new ArrayList<>();
    private List<String> selectedMembers = new ArrayList<>();

    private EditText titleField, dateField, startTimeField, endTimeField, locationField;
    private Button addMembersButton, confirmButton;

    private String groupId; // To store the group ID passed from the previous activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_study_session);

        firestore = FirebaseFirestore.getInstance();

        // Retrieve group ID from intent
        groupId = getIntent().getStringExtra("GROUP_NAME");
        if (groupId == null || groupId.isEmpty()) {
            Toast.makeText(this, "Group ID not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        titleField = findViewById(R.id.sessionTitleField);
        dateField = findViewById(R.id.sessionDateField);
        startTimeField = findViewById(R.id.sessionStartTimeField);
        endTimeField = findViewById(R.id.sessionEndTimeField);
        locationField = findViewById(R.id.sessionLocationField);
        addMembersButton = findViewById(R.id.addMembersButton);
        confirmButton = findViewById(R.id.confirmAddSessionButton);

        // Fetch group members
        fetchGroupMembers();

        // Add Members button logic
        addMembersButton.setOnClickListener(v -> showAddMembersDialog());

        // Confirm button logic
        confirmButton.setOnClickListener(v -> addStudySession());
    }

    private void fetchGroupMembers() {
        firestore.collection("groups")
                .document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> members = (List<String>) documentSnapshot.get("members");
                        if (members != null) {
                            groupMembers.addAll(members);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch members!", Toast.LENGTH_SHORT).show());
    }

    private void showAddMembersDialog() {
        boolean[] checkedItems = new boolean[groupMembers.size()];
        String[] membersArray = groupMembers.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Members");
        builder.setMultiChoiceItems(membersArray, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                selectedMembers.add(membersArray[which]);
            } else {
                selectedMembers.remove(membersArray[which]);
            }
        });
        builder.setPositiveButton("Done", (dialog, which) -> {
            Toast.makeText(this, "Members selected: " + selectedMembers.size(), Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void addStudySession() {
        String title = titleField.getText().toString().trim();
        String date = dateField.getText().toString().trim();
        String startTime = startTimeField.getText().toString().trim();
        String endTime = endTimeField.getText().toString().trim();
        String location = locationField.getText().toString().trim();

        // Check for empty fields
        if (title.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || location.isEmpty() || selectedMembers.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select members!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate date format (dd-MM-yyyy)
        if (!date.matches("\\d{2}-\\d{2}-\\d{4}")) {
            Toast.makeText(this, "Invalid date format! Use dd-MM-yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate time format (HH:mm)
        if (!startTime.matches("\\d{2}:\\d{2}") || !endTime.matches("\\d{2}:\\d{2}")) {
            Toast.makeText(this, "Invalid time format! Use HH:mm (24-hour format)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate time ranges and start-end consistency
        try {
            String[] startSplit = startTime.split(":");
            String[] endSplit = endTime.split(":");

            int startHour = Integer.parseInt(startSplit[0]);
            int startMinute = Integer.parseInt(startSplit[1]);
            int endHour = Integer.parseInt(endSplit[0]);
            int endMinute = Integer.parseInt(endSplit[1]);

            // Check if the time values are valid (24-hour format)
            if (startHour < 0 || startHour > 23 || startMinute < 0 || startMinute > 59 ||
                    endHour < 0 || endHour > 23 || endMinute < 0 || endMinute > 59) {
                Toast.makeText(this, "Invalid time values! Ensure times are in 24-hour format.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ensure the end time is after the start time
            if (endHour < startHour || (endHour == startHour && endMinute <= startMinute)) {
                Toast.makeText(this, "End time must be after start time!", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid time format!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate location (optional: add custom logic)
        if (location.length() < 3) {
            Toast.makeText(this, "Location must be at least 3 characters long!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add session data to Firestore
        HashMap<String, Object> sessionData = new HashMap<>();
        sessionData.put("title", title);
        sessionData.put("date", date);
        sessionData.put("start_time", startTime);
        sessionData.put("end_time", endTime);
        sessionData.put("location", location);
        sessionData.put("members", selectedMembers);
        sessionData.put("group_id", groupId); // Add the group ID to the session data

        // Save to Firestore
        firestore.collection("study_sessions")
                .add(sessionData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Study Session Added Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add study session!", Toast.LENGTH_SHORT).show());
    }
}
