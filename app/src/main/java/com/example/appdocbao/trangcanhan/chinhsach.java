package com.example.appdocbao.trangcanhan;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdocbao.R;

public class chinhsach extends AppCompatActivity {
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_chinhsach); // thay bằng đúng tên file layout XML của bạn

        // Ánh xạ các view
        btnBack = findViewById(R.id.btnBack);

        // Sự kiện nút quay lại
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Quay về màn hình trước đó
            }
        });

        // Nếu bạn muốn set nội dung văn bản trong Java (không bắt buộc), có thể dùng:
        /*
        TextView textView2 = findViewById(R.id.textView2);
        textView2.setText("Nội dung chính sách...");
        */
    }
}
