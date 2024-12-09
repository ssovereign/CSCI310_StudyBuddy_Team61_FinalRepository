package com.example.csci310_studybuddy_team61_finalrepository;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PrivateChatActivity extends AppCompatActivity {

    private static final String TAG = "PrivateChatActivity";
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private ScrollView messagesScrollView;
    private LinearLayout messagesContainer;
    private EditText messageInput;
    private String groupName;
    private String memberEmail;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privatechat);

        // Initialize Firebase
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserEmail = firebaseAuth.getCurrentUser().getEmail();

        // Retrieve the group name and member email
        groupName = getIntent().getStringExtra("GROUP_NAME");
        memberEmail = getIntent().getStringExtra("MEMBER_EMAIL");

        if (groupName == null || memberEmail == null || currentUserEmail == null) {
            Log.e(TAG, "Group name, member email, or current user email is null");
            Toast.makeText(this, "Failed to load private chat.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messagesScrollView = findViewById(R.id.messagesScrollView);
        messagesContainer = findViewById(R.id.messagesContainer);
        messageInput = findViewById(R.id.messageInput);
        ImageButton sendButton = findViewById(R.id.sendButton);

        loadPrivateMessages();

        sendButton.setOnClickListener(v -> sendMessage());
    }

    /**
     * Load existing private messages from Firestore.
     */
    private void loadPrivateMessages() {
        firebaseFirestore.collection("privateMessages")
                .whereEqualTo("room", groupName)
                .whereIn("sender", Arrays.asList(currentUserEmail, memberEmail))
                .whereIn("receiver", Arrays.asList(currentUserEmail, memberEmail))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.forEach(documentSnapshot -> {
                        String text = documentSnapshot.getString("text");
                        String sender = documentSnapshot.getString("sender");
                        addMessageToUI(sender, text);
                    });
                    scrollToBottom();
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
     * Scroll to the bottom of the messages container.
     */
    private void scrollToBottom() {
        messagesScrollView.post(() -> messagesScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    /**
     * Send a message to the private chat.
     */
    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String sender = currentUserEmail;
        Map<String, Object> message = new HashMap<>();
        message.put("room", groupName);
        message.put("sender", sender);
        message.put("receiver", memberEmail);
        message.put("text", text);
        message.put("createdAt", System.currentTimeMillis());

        firebaseFirestore.collection("privateMessages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    addMessageToUI(sender, text);
                    messageInput.setText("");
                    scrollToBottom(); // Scroll to the latest message after sending
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error sending message: ", e));
    }
}
