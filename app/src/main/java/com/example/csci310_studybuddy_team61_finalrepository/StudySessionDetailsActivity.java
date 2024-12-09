package com.example.csci310_studybuddy_team61_finalrepository;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class StudySessionDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_session_details);

        TextView detailsTextView = findViewById(R.id.sessionDetailsText);

        // Get session details from intent
        HashMap<String, Object> sessionDetails = (HashMap<String, Object>) getIntent().getSerializableExtra("sessionDetails");

        // Display session details
        String details = "Title: " + sessionDetails.get("title") + "\n" +
                "Location: " + sessionDetails.get("location") + "\n" +
                "Start Time: " + sessionDetails.get("startTime") + "\n" +
                "End Time: " + sessionDetails.get("endTime") + "\n" +
                "Members: " + sessionDetails.get("members");
        detailsTextView.setText(details);
    }
}
