package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatSetupActivity extends AppCompatActivity {

    private static final String TAG = "ChatPageActivity";

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private LinearLayout chatListContainer;
    private String groupName;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatsetup);

        // Initialize Firebase
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Retrieve the group name and current user email
        groupName = getIntent().getStringExtra("GROUP_NAME");
        currentUserEmail = firebaseAuth.getCurrentUser().getEmail();

        if (groupName == null || currentUserEmail == null) {
            Log.e(TAG, "Group name or user email is null");
            Toast.makeText(this, "Failed to load chats.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        chatListContainer = findViewById(R.id.chatListContainer);

        Button groupChatButton = findViewById(R.id.groupChatButton);
        groupChatButton.setOnClickListener(v -> openGroupChat());

        // Load and display chats
        loadChats();
    }

    /**
     * Load and display the list of chats for the group.
     */
    private void loadChats() {
        firebaseFirestore.collection("groups")
                .whereEqualTo("name", groupName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        querySnapshot.getDocuments().forEach(documentSnapshot -> {
                            List<String> members = (List<String>) documentSnapshot.get("members");
                            if (members != null && !members.isEmpty()) {
                                initializeChatButtons(members);
                            } else {
                                Log.e(TAG, "No members found for group: " + groupName);
                            }
                        });
                    } else {
                        Log.e(TAG, "Group not found: " + groupName);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching group members: ", e));
    }

    /**
     * Initialize chat buttons for all members of the group.
     *
     * @param members List of group members.
     */
    private void initializeChatButtons(List<String> members) {
        for (String member : members) {
            if (!member.equals(currentUserEmail)) {
                addChatButton(member);
            }
        }
    }

    /**
     * Add a button for a chat with a specific member.
     *
     * @param member The email of the member.
     */
    private void addChatButton(String member) {
        Button chatButton = new Button(this);
        chatButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        chatButton.setText(member);
        chatButton.setOnClickListener(v -> openPrivateChat(member));

        chatListContainer.addView(chatButton);
    }

    /**
     * Open the group chat.
     */
    private void openGroupChat() {
        Intent intent = new Intent(ChatSetupActivity.this, GroupChatActivity.class);
        intent.putExtra("GROUP_NAME", groupName);
        startActivity(intent);
    }

    /**
     * Open a private chat with a specific member.
     *
     * @param member The email of the member.
     */
    private void openPrivateChat(String member) {
        Intent intent = new Intent(ChatSetupActivity.this, PrivateChatActivity.class);
        intent.putExtra("GROUP_NAME", groupName);
        intent.putExtra("MEMBER_EMAIL", member);
        startActivity(intent);
    }
}
