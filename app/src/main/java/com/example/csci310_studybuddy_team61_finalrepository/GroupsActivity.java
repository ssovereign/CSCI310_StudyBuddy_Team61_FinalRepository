package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class GroupsActivity extends AppCompatActivity {

    private LinearLayout groupsContainer;
    private HashMap<String, Integer> buttonIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        groupsContainer = findViewById(R.id.groupsContainer);
        buttonIds = new HashMap<>();
        Button addGroupButton = findViewById(R.id.addGroupButton);

        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = "Group " + (buttonIds.size() + 1);
                addGroupRow(groupName);
            }
        });
    }

    /**
     * Function to add a new row to the groupsContainer as a Button
     *
     * @param groupName The name to be displayed on the button and used as its ID
     */
    private void addGroupRow(String groupName) {
        Button groupButton = new Button(this);
        groupButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        groupButton.setText(groupName);
        groupButton.setTextSize(16);
        groupButton.setPadding(16, 16, 16, 16);
        groupButton.setBackgroundResource(android.R.color.white);

        int uniqueId = View.generateViewId();
        groupButton.setId(uniqueId);

        buttonIds.put(groupName, uniqueId);

        groupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupsActivity.this, CourseHomeActivity.class);
                intent.putExtra("GROUP_NAME", groupName);
                startActivity(intent);
            }
        });

        groupsContainer.addView(groupButton);
    }
}
