package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class GroupCalendarActivity extends AppCompatActivity {

    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_calendar);

        CalendarView calendarView = findViewById(R.id.groupCalendarView);
        Button confirmButton = findViewById(R.id.confirmButton);

        // Handle date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            Toast.makeText(GroupCalendarActivity.this, "Selected: " + selectedDate, Toast.LENGTH_SHORT).show();
        });

        // Handle Confirm button
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDate.isEmpty()) {
                    Toast.makeText(GroupCalendarActivity.this, "Please select a date first!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(GroupCalendarActivity.this, StudySessionsActivity.class);
                    intent.putExtra("groupId", getIntent().getStringExtra("groupId")); // Forward the groupId
                    intent.putExtra("selectedDate", selectedDate);
                    startActivity(intent);
                }
            }
        });
    }
}
