package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class CoursesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        Button homeScreenButton = findViewById(R.id.homeScreenButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        homeScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the Home Screen
                Intent intent = new Intent(CoursesActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        // Logout and return to Login Screen
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logic to return to Login Screen
                Intent intent = new Intent(CoursesActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
