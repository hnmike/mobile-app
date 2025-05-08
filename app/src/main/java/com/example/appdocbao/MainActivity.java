package com.example.appdocbao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        btnGetStarted = findViewById(R.id.btnGetStarted);
        
        // Set up button click listener
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToHome();
            }
        });

        // Auto-navigate after a delay (splash screen behavior)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToHome();
            }
        }, 2000); // 2 seconds delay
    }

    private void navigateToHome() {
        // Check if user is already logged in
        boolean isLoggedIn = false; // This would come from your authentication logic
        
        if (isLoggedIn) {
            // Navigate directly to the home/categories screen
            Intent intent = new Intent(MainActivity.this, CategoriesActivity.class);
            startActivity(intent);
        } else {
            // Navigate to the Categories screen as a demo
            Intent intent = new Intent(MainActivity.this, CategoriesActivity.class);
            startActivity(intent);
        }
    }
}