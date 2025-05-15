package com.example.appdocbao.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdocbao.R;

public class ProfileTermsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_dieukhoan);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);

        if (tvTitle != null) {
            tvTitle.setText("Điều khoản sử dụng");
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }
    }
}