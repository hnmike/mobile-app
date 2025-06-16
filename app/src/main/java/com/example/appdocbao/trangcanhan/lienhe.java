package com.example.appdocbao.trangcanhan;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;

import com.example.appdocbao.R;

public class lienhe extends AppCompatActivity {
    private ImageView btnBack;
    private TextView textViewInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_lh); // ví dụ: R.layout.activity_lien_he

        // Ánh xạ view
        btnBack = findViewById(R.id.btnBack);
        textViewInfo = findViewById(R.id.textViewInfo);

        // Thiết lập sự kiện nút quay lại
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Quay lại Activity trước đó
            }
        });

        // (Không bắt buộc) Gán lại nội dung liên hệ nếu muốn động
        String lienHeText = "Tel: (024) 3 542 3555\n" +
                "Email: contact.docbao@epi.com.vn\n\n" +
                "Địa chỉ: Trường Đại học Kiến Trúc Hà Nội\n" +
                "Km 10, đường Nguyễn Trãi, quận Thanh Xuân, TP Hà Nội";

        textViewInfo.setText(lienHeText);
    }
}
