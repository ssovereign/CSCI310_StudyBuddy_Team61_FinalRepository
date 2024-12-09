package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class CourseHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coursehome);

        Button groupCalendarButton = findViewById(R.id.groupCalendarButton);
        Button addNewSessionButton = findViewById(R.id.addNewSessionButton); // New Button

        // Navigate to Group Calendar
        groupCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseHomeActivity.this, GroupCalendarActivity.class);
                intent.putExtra("groupId", groupId); // Pass the groupId
                startActivity(intent);
            }
        });

        // Navigate to Add Study Session
        addNewSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseHomeActivity.this, AddStudySessionActivity.class);
                startActivity(intent);
            }
        });
    }
}
