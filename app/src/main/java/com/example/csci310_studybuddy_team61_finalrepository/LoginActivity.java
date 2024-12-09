package com.example.csci310_studybuddy_team61_finalrepository;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = findViewById(R.id.loginButton);
        Button goToRegisterButton = findViewById(R.id.goToRegisterButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Directly navigate to the CoursesActivity
                Intent intent = new Intent(LoginActivity.this, CoursesActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Navigate to Registration screen
        goToRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
                finish(); // Close the login activity
            }
        });
    }
}
