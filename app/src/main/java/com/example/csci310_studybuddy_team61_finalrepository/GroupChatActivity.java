package com.example.csci310_studybuddy_team61_finalrepository;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity {

    private static final String TAG = "GroupChatActivity";
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private LinearLayout messagesContainer;
    private EditText messageInput;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchat);

        // Initialize Firebase
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Retrieve the group name
        groupName = getIntent().getStringExtra("GROUP_NAME");
        if (groupName == null) {
            Log.e(TAG, "Group name is null");
            Toast.makeText(this, "Failed to load group chat.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messagesContainer = findViewById(R.id.messagesContainer);
        messageInput = findViewById(R.id.messageInput);
        ImageButton sendButton = findViewById(R.id.sendButton);

        // Back Button
        TextView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Close the current activity and go back

        loadGroupMessages();

        sendButton.setOnClickListener(v -> sendMessage());
    }

    /**
     * Load existing group messages from Firestore.
     */
    private void loadGroupMessages() {
        firebaseFirestore.collection("groupMessages")
                .whereEqualTo("room", groupName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.forEach(documentSnapshot -> {
                        String text = documentSnapshot.getString("text");
                        String sender = documentSnapshot.getString("sender");
                        addMessageToUI(sender, text);
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading messages: ", e));
    }

    /**
     * Add a message to the UI.
     *
     * @param sender The sender's email.
     * @param text   The message text.
     */
    private void addMessageToUI(String sender, String text) {
        TextView messageTextView = new TextView(this);
        messageTextView.setText(sender + ": " + text);

        messageTextView.setTextSize(18);
        messageTextView.setTypeface(null, android.graphics.Typeface.BOLD);

        messageTextView.setPadding(8, 8, 8, 8);
        messagesContainer.addView(messageTextView);
    }

    /**
     * Send a message to the group.
     */
    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String sender = firebaseAuth.getCurrentUser().getEmail();
        Map<String, Object> message = new HashMap<>();
        message.put("room", groupName);
        message.put("sender", sender);
        message.put("text", text);
        message.put("createdAt", System.currentTimeMillis());

        firebaseFirestore.collection("groupMessages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    addMessageToUI(sender, text);
                    messageInput.setText("");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error sending message: ", e));
    }
}
