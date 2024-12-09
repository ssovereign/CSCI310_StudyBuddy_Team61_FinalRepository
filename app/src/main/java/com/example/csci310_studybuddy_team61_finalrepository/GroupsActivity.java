package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class GroupsActivity extends AppCompatActivity {

    private static final String TAG = "GroupsActivity";

    private LinearLayout groupsContainer;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        // Initialize Firebase and UI components
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        groupsContainer = findViewById(R.id.groupsContainer);

        Button addGroupButton = findViewById(R.id.addGroupButton);

        // Load groups initially
        loadGroups();

        addGroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupsActivity.this, CreateGroup.class);
            startActivity(intent);
        });
    }

    /**
     * Function to load all groups from Firestore and add rows.
     */
    private void loadGroups() {
        String currentUserEmail = firebaseAuth.getCurrentUser().getEmail();

        // Clear the container before adding new groups
        groupsContainer.removeAllViews();

        // Query Firestore for all documents in the groups collection
        firebaseFirestore.collection("groups")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.forEach(documentSnapshot -> {
                            String groupName = documentSnapshot.getId();
                            List<String> members = (List<String>) documentSnapshot.get("members");

                            // Log the group and members for debugging
                            Log.d(TAG, "Group: " + groupName + ", Members: " + members);

                            // Check if the current user is a member of this group
                            if (members != null && members.contains(currentUserEmail)) {
                                Log.d(TAG, "User is a member of group: " + groupName);
                                addGroupRow(groupName, false); // No Join button
                            } else {
                                Log.d(TAG, "User is NOT a member of group: " + groupName);
                                addGroupRow(groupName, true); // Add Join button
                            }
                        });
                    } else {
                        Log.d(TAG, "No groups found in the database.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching groups: ", e));
    }

    /**
     * Function to add a new row to the groupsContainer for a group.
     *
     * @param groupName      The name of the group to display
     * @param showJoinButton Whether to display the Join button
     */
    private void addGroupRow(String groupName, boolean showJoinButton) {
        // Create a horizontal LinearLayout for the row
        LinearLayout groupRow = new LinearLayout(this);
        groupRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        groupRow.setOrientation(LinearLayout.HORIZONTAL);
        groupRow.setGravity(Gravity.CENTER_VERTICAL);
        groupRow.setPadding(16, 16, 16, 16);

        // Add a TextView for the group name
        TextView groupNameTextView = new TextView(this);
        groupNameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1 // Weight to fill available space
        ));
        groupNameTextView.setText(groupName);
        groupNameTextView.setTextSize(16);
        groupNameTextView.setPadding(8, 8, 8, 8);
        groupRow.addView(groupNameTextView);

        // Optionally add a Join button
        if (showJoinButton) {
            Button joinButton = new Button(this);
            joinButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            joinButton.setText("Join");
            joinButton.setOnClickListener(v -> joinGroup(groupName));
            groupRow.addView(joinButton);
        }

        // Add the row to the container
        groupsContainer.addView(groupRow);

        // Log the row addition
        Log.d(TAG, "Added row for group: " + groupName);
    }

    /**
     * Function to handle the Join button click for a specific group.
     *
     * @param groupName The name of the group to join
     */
    private void joinGroup(String groupName) {
        String currentUserEmail = firebaseAuth.getCurrentUser().getEmail();

        // Add the current user to the group's members array in Firestore
        firebaseFirestore.collection("groups").document(groupName)
                .update("members", FieldValue.arrayUnion(currentUserEmail))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully joined group: " + groupName);
                    loadGroups(); // Reload groups to update the UI
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error joining group: ", e));
    }
}
