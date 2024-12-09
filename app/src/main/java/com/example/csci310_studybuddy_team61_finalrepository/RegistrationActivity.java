package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private List<String> selectedCourses = new ArrayList<>(); // Stores selected courses
    private boolean[] selectedItems; // Tracks selected states

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        EditText usernameField = findViewById(R.id.usernameField);
        EditText passwordField = findViewById(R.id.passwordField);
        EditText confirmPasswordField = findViewById(R.id.confirmPasswordField);
        Button registerButton = findViewById(R.id.registerButton);
        Button loginButton = findViewById(R.id.loginButton);
        Button courseSelectionButton = findViewById(R.id.courseSelectionButton);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // List of available courses
        String[] courses = {"Math 101", "CS 201", "Physics 202"};
        selectedItems = new boolean[courses.length]; // Track which courses are selected

        // Handle course selection button click
        courseSelectionButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Courses");

            builder.setMultiChoiceItems(courses, selectedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    // Add course if selected
                    if (!selectedCourses.contains(courses[which])) {
                        selectedCourses.add(courses[which]);
                    }
                } else {
                    // Remove course if deselected
                    selectedCourses.remove(courses[which]);
                }
            });

            builder.setPositiveButton("OK", (dialog, which) -> {
                Toast.makeText(this, "Courses Selected: " + selectedCourses, Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        // Handle registration
        registerButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString();
            String password = passwordField.getText().toString();
            String confirmPassword = confirmPasswordField.getText().toString();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            } else {
                // Register user with Firebase Auth
                firebaseAuth.createUserWithEmailAndPassword(username, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Add user data to Firestore
                                Map<String, Object> user = new HashMap<>();
                                user.put("email", username);
                                user.put("courses", selectedCourses); // Save selected courses

                                firestore.collection("users")
                                        .document(firebaseAuth.getCurrentUser().getUid())
                                        .set(user)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(this, "Registration Successful! Redirecting to login...", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(this, LoginActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(this, "Failed to save user data!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Navigate to Login screen
        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
