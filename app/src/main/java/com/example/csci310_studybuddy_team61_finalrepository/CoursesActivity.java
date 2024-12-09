package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CoursesActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private ListView coursesListView;
    private ArrayAdapter<String> coursesAdapter;
    private List<String> coursesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        coursesListView = findViewById(R.id.coursesListView);

        Button homeScreenButton = findViewById(R.id.homeScreenButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        // Initialize the courses list and adapter
        coursesList = new ArrayList<>();
        coursesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, coursesList);
        coursesListView.setAdapter(coursesAdapter);

        // Fetch and display the user's enrolled courses
        fetchEnrolledCourses();

        // Navigate to the Home Screen
        homeScreenButton.setOnClickListener(v -> {
            Intent intent = new Intent(CoursesActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        // Logout and return to Login Screen
        logoutButton.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(CoursesActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Handle back button click
    }


    private void fetchEnrolledCourses() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> courses = (List<String>) documentSnapshot.get("courses");
                        if (courses != null) {
                            coursesList.clear();
                            coursesList.addAll(courses);
                            coursesAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(CoursesActivity.this, "No courses enrolled.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CoursesActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CoursesActivity", "Error fetching courses", e);
                    Toast.makeText(CoursesActivity.this, "Failed to fetch courses.", Toast.LENGTH_SHORT).show();
                });
    }
}
