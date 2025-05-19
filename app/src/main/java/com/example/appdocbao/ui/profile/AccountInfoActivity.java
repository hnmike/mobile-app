package com.example.appdocbao.ui.profile;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appdocbao.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountInfoActivity extends AppCompatActivity {

    private EditText etUsername, etDisplayName, etEmail;
    private ImageView btnBack;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_account_info);

        // Ánh xạ các thành phần từ XML
        etUsername = findViewById(R.id.etUsername);
        etDisplayName = findViewById(R.id.etDisplayName);
        etEmail = findViewById(R.id.etEmail);
        btnBack = findViewById(R.id.btnBack);

        // Đặt EditText ở chế độ không thể chỉnh sửa
        etUsername.setEnabled(false);
        etDisplayName.setEnabled(false);
        etEmail.setEnabled(false);

        // Lấy thông tin người dùng từ Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            usersRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
            loadAccountInfo();
        }

        // Xử lý sự kiện nút quay lại
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadAccountInfo() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String displayName = snapshot.child("displayName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    etUsername.setText(username != null ? username : "Chưa có tên đăng nhập");
                    etDisplayName.setText(displayName != null ? displayName : "Chưa có tên hiển thị");
                    etEmail.setText(email != null ? email : "Chưa có email");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });
    }
}