package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PrivateChatActivity extends AppCompatActivity {

    private static final String TAG = "PrivateChatActivity";
    private static final int FILE_SELECT_CODE = 100;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;

    private ScrollView messagesScrollView;
    private LinearLayout messagesContainer;
    private EditText messageInput;
    private ImageButton uploadButton, sendButton, backButton;

    private String memberEmail;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privatechat);

        // Initialize the back button
        TextView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Close the current activity and go back

        // Initialize Firebase and other UI elements
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        currentUserEmail = firebaseAuth.getCurrentUser().getEmail();

        memberEmail = getIntent().getStringExtra("MEMBER_EMAIL");

        if (memberEmail == null || currentUserEmail == null) {
            Log.e(TAG, "Member email or current user email is null");
            Toast.makeText(this, "Failed to load private chat.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messagesScrollView = findViewById(R.id.messagesScrollView);
        messagesContainer = findViewById(R.id.messagesContainer);
        messageInput = findViewById(R.id.messageInput);
        uploadButton = findViewById(R.id.uploadButton);
        sendButton = findViewById(R.id.sendButton);

        loadPrivateMessages();

        sendButton.setOnClickListener(v -> sendMessage());
        uploadButton.setOnClickListener(v -> openFilePicker());
    }

    /**
     * Open file picker to select a file.
     */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow all file types
        startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_SELECT_CODE);
    }

    private void navigateToCourseHome() {
        finish(); // Go back to the previous activity
    }

    /**
     * Handle the result of the file picker.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                uploadFileToFirebase(fileUri);
            }
        }
    }

    /**
     * Upload the selected file to Firebase Storage.
     */
    private void uploadFileToFirebase(Uri fileUri) {
        String fileName = System.currentTimeMillis() + "_" + fileUri.getLastPathSegment();
        StorageReference storageRef = firebaseStorage.getReference().child("chatFiles/" + fileName);

        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String fileUrl = uri.toString();
                            sendMessageWithFile(fileUrl);
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Error getting file URL", e)))
                .addOnFailureListener(e -> Log.e(TAG, "File upload failed", e));
    }

    /**
     * Load existing private messages from Firestore.
     */
    private void loadPrivateMessages() {
        firebaseFirestore.collection("privateMessages")
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
     * Send a message containing text.
     */
    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String sender = currentUserEmail;
        Map<String, Object> message = new HashMap<>();
        message.put("sender", sender);
        message.put("receiver", memberEmail);
        message.put("text", text);
        message.put("createdAt", System.currentTimeMillis());

        firebaseFirestore.collection("privateMessages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    addMessageToUI(sender, text);
                    messageInput.setText("");
                    scrollToBottom();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error sending message: ", e));
    }

    /**
     * Send a message containing a file URL.
     */
    private void sendMessageWithFile(String fileUrl) {
        String sender = currentUserEmail;
        Map<String, Object> message = new HashMap<>();
        message.put("sender", sender);
        message.put("receiver", memberEmail);
        message.put("text", "File: " + fileUrl);
        message.put("createdAt", System.currentTimeMillis());

        firebaseFirestore.collection("privateMessages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    addMessageToUI(sender, "File: " + fileUrl);
                    scrollToBottom();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error sending file message: ", e));
    }

    /**
     * Add a message to the UI.
     */
    private void addMessageToUI(String sender, String text) {
        TextView messageTextView = new TextView(this);

        if (text.startsWith("File: ")) {
            String fileUrl = text.substring(6);
            messageTextView.setText(sender + ": [File] Click to open");
            messageTextView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
                startActivity(intent);
            });
        } else {
            messageTextView.setText(sender + ": " + text);
        }

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
}
