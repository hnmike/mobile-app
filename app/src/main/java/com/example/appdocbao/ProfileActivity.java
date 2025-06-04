package com.example.appdocbao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Hiển thị avatar (có thể thay đổi resource nếu muốn)
        ImageView profileImage = findViewById(R.id.profileImage);
        profileImage.setImageResource(R.drawable.profile_placeholder); // hoặc thay bằng ảnh khác nếu có

        // Xử lý sự kiện Sign Out
        View cvSignOut = findViewById(R.id.cvSignOut);
        cvSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}