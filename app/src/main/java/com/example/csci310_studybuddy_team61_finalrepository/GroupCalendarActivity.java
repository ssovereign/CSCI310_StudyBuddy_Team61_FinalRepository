package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class GroupCalendarActivity extends AppCompatActivity {

    private static final String TAG = "GroupCalendarActivity";
    private String selectedDate = "";
    private String groupName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_calendar);

        // Retrieve groupName from the intent
        groupName = getIntent().getStringExtra("GROUP_NAME");

        // Debugging: Log and show a Toast with the groupName
        if (groupName != null && !groupName.isEmpty()) {
            Log.d(TAG, "Received groupName: " + groupName);
            Toast.makeText(this, "Group Name: " + groupName, Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Group name is null or empty!");
            Toast.makeText(this, "Group name not passed!", Toast.LENGTH_SHORT).show();
        }

        CalendarView calendarView = findViewById(R.id.groupCalendarView);
        Button confirmButton = findViewById(R.id.confirmButton);

        // Handle date selection
// Handle date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Ensure day and month are zero-padded to match dd-MM-yyyy format
            String formattedDay = String.format("%02d", dayOfMonth); // Zero-pad day
            String formattedMonth = String.format("%02d", month + 1); // Zero-pad month (month is zero-based)
            selectedDate = formattedDay + "-" + formattedMonth + "-" + year; // Concatenate into dd-MM-yyyy format

            // Log and display the formatted date
            Log.d(TAG, "Selected date: " + selectedDate);
            Toast.makeText(GroupCalendarActivity.this, "Selected: " + selectedDate, Toast.LENGTH_SHORT).show();
        });

        // Handle Confirm button
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDate.isEmpty()) {
                    Toast.makeText(GroupCalendarActivity.this, "Please select a date first!", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Attempted to confirm without selecting a date.");
                } else {
                    Log.d(TAG, "Navigating to StudySessionsActivity with groupName: " + groupName + ", selectedDate: " + selectedDate);
                    Intent intent = new Intent(GroupCalendarActivity.this, StudySessionsActivity.class);
                    intent.putExtra("GROUP_NAME", groupName); // Pass the group name
                    intent.putExtra("selectedDate", selectedDate);
                    startActivity(intent);
                }
            }
        });
    }
}
