package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        EditText usernameField = findViewById(R.id.usernameField);
        EditText passwordField = findViewById(R.id.passwordField);
        EditText confirmPasswordField = findViewById(R.id.confirmPasswordField);
        Button registerButton = findViewById(R.id.registerButton);
        Button loginButton = findViewById(R.id.loginButton);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Handle registration
        registerButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String confirmPassword = confirmPasswordField.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegistrationActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(RegistrationActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            } else {
                // Register user with Firebase Auth
                firebaseAuth.createUserWithEmailAndPassword(username, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("RegistrationActivity", "Registration successful. Firebase UID: " + firebaseAuth.getCurrentUser().getUid());

                                // Add user data to Firestore
                                Map<String, Object> user = new HashMap<>();
                                user.put("email", username);
                                user.put("courses", new ArrayList<String>()); // Use an empty ArrayList

                                firestore.collection("users")
                                        .document(firebaseAuth.getCurrentUser().getUid())
                                        .set(user)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Log.d("RegistrationActivity", "User data saved to Firestore. Redirecting to LoginActivity...");
                                                Toast.makeText(RegistrationActivity.this, "Registration Successful! Redirecting to login...", Toast.LENGTH_SHORT).show();

                                                // Delay and navigate to LoginActivity
                                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                    Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                                                    startActivity(intent);
                                                    finish(); // Close RegistrationActivity
                                                }, 2000);
                                            } else {
                                                Toast.makeText(RegistrationActivity.this, "Failed to save user data!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Log.e("RegistrationActivity", "FirebaseAuth registration failed: " + task.getException().getMessage());
                                Toast.makeText(RegistrationActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Navigate back to login screen
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
