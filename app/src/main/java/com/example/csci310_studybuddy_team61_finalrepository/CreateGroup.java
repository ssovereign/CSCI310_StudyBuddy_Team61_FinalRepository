package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class CreateGroup extends AppCompatActivity {

    private static final String TAG = "CreateGroupActivity";

    private EditText groupNameInput;
    private Spinner membersDropdown;
    private Button addMemberButton, submitButton;

    private ArrayList<String> membersList; // Dropdown options
    private ArrayList<String> selectedMembers; // Members to add to the group
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creategroup);

        // Initialize UI components
        groupNameInput = findViewById(R.id.groupNameInput);
        membersDropdown = findViewById(R.id.membersDropdown);
        addMemberButton = findViewById(R.id.addMemberButton);
        submitButton = findViewById(R.id.submitButton);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        // Initialize data structures
        membersList = new ArrayList<>();
        selectedMembers = new ArrayList<>();

        // Load all members except the current user
        loadAllMembers();

        // Add member to the group
        addMemberButton.setOnClickListener(v -> {
            String selectedMember = membersDropdown.getSelectedItem().toString();
            if (!selectedMembers.contains(selectedMember)) {
                selectedMembers.add(selectedMember);
                Toast.makeText(CreateGroup.this, selectedMember + " added to group", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CreateGroup.this, selectedMember + " is already in the group", Toast.LENGTH_SHORT).show();
            }
        });

        // Submit group logic
        submitButton.setOnClickListener(v -> createGroup());
    }

    private void loadAllMembers() {
        String currentUserId = firebaseAuth.getCurrentUser().getUid();

        firebaseFirestore.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    queryDocumentSnapshots.forEach(documentSnapshot -> {
                        String userEmail = documentSnapshot.getString("email"); // Fetch email
                        if (userEmail != null) {
                            membersList.add(userEmail);
                        }
                    });

                    // Populate the dropdown
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, membersList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    membersDropdown.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading members: ", e));
    }

    private void createGroup() {
        String groupName = groupNameInput.getText().toString().trim();

        if (groupName.isEmpty()) {
            Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedMembers.isEmpty()) {
            Toast.makeText(this, "You must add at least one member to the group", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add the current user's email to the group
        String currentUserEmail = firebaseAuth.getCurrentUser().getEmail();
        if (currentUserEmail != null && !selectedMembers.contains(currentUserEmail)) {
            selectedMembers.add(currentUserEmail);
        }

        // Create the group data
        HashMap<String, Object> groupData = new HashMap<>();
        groupData.put("name", groupName);
        groupData.put("members", selectedMembers);

        // Add the group to the Firestore 'groups' collection
        firebaseFirestore.collection("groups").document(groupName)
                .set(groupData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Group created successfully in Firestore");

                    // Create corresponding folder in Firebase Storage
                    createGroupFolder(groupName);

                    // Return the new group name to GroupsActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("NEW_GROUP_NAME", groupName);
                    setResult(RESULT_OK, resultIntent);
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating group: ", e);
                    Toast.makeText(this, "Failed to create group. Try again.", Toast.LENGTH_SHORT).show();
                });
        Intent intent = new Intent(CreateGroup.this, GroupsActivity.class);
        startActivity(intent);
    }

    /**
     * Create a folder in Firebase Storage for the group.
     *
     * @param groupName The name of the group (used as the folder name)
     */
    private void createGroupFolder(String groupName) {
        // Use a dummy file to create a folder-like structure
        StorageReference groupFolderRef = firebaseStorage.getReference().child("groups/" + groupName + "/.placeholder");

        // Upload an empty file to simulate folder creation
        groupFolderRef.putBytes(new byte[0]) // Empty file
                .addOnSuccessListener(taskSnapshot -> Log.d(TAG, "Folder created successfully in Firebase Storage"))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating folder in Firebase Storage: ", e));
    }
}
