package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CourseHomeActivity extends AppCompatActivity {

    private static final String TAG = "CourseHomeActivity";
    private FirebaseFirestore firebaseFirestore;

    private LinearLayout memberListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coursehome);

        Button chatBoxButton = findViewById(R.id.chatBoxButton);
        Button groupCalendarButton = findViewById(R.id.groupCalendarButton);
        Button addNewSessionButton = findViewById(R.id.addNewSessionButton);

        firebaseFirestore = FirebaseFirestore.getInstance();

        memberListContainer = findViewById(R.id.memberListContainer);

        String groupName = getIntent().getStringExtra("GroupName");

        chatBoxButton.setOnClickListener(v -> openChatPage(groupName));
        groupCalendarButton.setOnClickListener(v -> openGroupCalendar(groupName));
        addNewSessionButton.setOnClickListener(v -> openAddNewSessionPage(groupName));

        if (groupName != null) {
            loadGroupMembers(groupName);
        } else {
            Log.e(TAG, "Group name is null. Cannot load members.");
            Toast.makeText(this, "Failed to load group details.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGroupMembers(String groupName) {
        firebaseFirestore.collection("groups")
                .whereEqualTo("name", groupName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        querySnapshot.getDocuments().forEach(documentSnapshot -> {
                            List<String> members = (List<String>) documentSnapshot.get("members");
                            if (members != null && !members.isEmpty()) {
                                displayMembers(members);
                            } else {
                                Log.d(TAG, "No members found for group: " + groupName);
                                displayNoMembersMessage();
                            }
                        });
                    } else {
                        Log.e(TAG, "No group found with name: " + groupName);
                        displayNoMembersMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching group members: ", e);
                    Toast.makeText(this, "Failed to load group members.", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayMembers(List<String> members) {
        memberListContainer.removeAllViews();

        for (String member : members) {
            TextView memberTextView = new TextView(this);
            memberTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            memberTextView.setText(member);
            memberTextView.setTextSize(16);
            memberTextView.setPadding(8, 8, 8, 8);

            memberListContainer.addView(memberTextView);
        }
    }

    private void displayNoMembersMessage() {
        TextView noMembersTextView = new TextView(this);
        noMembersTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        noMembersTextView.setText("No members found.");
        noMembersTextView.setTextSize(16);
        noMembersTextView.setPadding(8, 8, 8, 8);
        noMembersTextView.setGravity(android.view.Gravity.CENTER);

        memberListContainer.addView(noMembersTextView);
    }

    private void openChatPage(String groupName) {
        Intent intent = new Intent(CourseHomeActivity.this, ChatSetupActivity.class);
        intent.putExtra("GROUP_NAME", groupName); // Pass the group name to ChatPageActivity
        startActivity(intent);
    }

    private void openGroupCalendar(String groupName) {
        Intent intent = new Intent(CourseHomeActivity.this, GroupCalendarActivity.class);
        intent.putExtra("GROUP_NAME", groupName); // Pass the group name to GroupCalendarActivity
        startActivity(intent);
    }

    private void openAddNewSessionPage(String groupName) {
        Intent intent = new Intent(CourseHomeActivity.this, AddStudySessionActivity.class);
        intent.putExtra("GROUP_NAME", groupName); // Pass the group name to AddNewSessionActivity
        startActivity(intent);
    }
}
