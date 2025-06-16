package com.example.appdocbao.trangcanhan;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdocbao.R;

public class dieukhoan extends AppCompatActivity {
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_dieukhoan); // Đảm bảo đây đúng là tên file XML của bạn

        btnBack = findViewById(R.id.btnBack);

        // Sự kiện nhấn nút quay lại
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Quay lại màn hình trước
            }
        });
    }

}
