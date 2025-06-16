package com.example.appdocbao.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.appdocbao.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountInfoActivity extends AppCompatActivity {

    private static final String TAG = "AccountInfoActivity";
    private EditText etUsername, etDisplayName, etEmail;
    private ImageView btnBack, ivAvatar;
    private ProgressBar progressBar;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.acitivity_account_info);
            Log.d(TAG, "AccountInfoActivity onCreate started");

            // Ánh xạ các thành phần từ XML
            initializeViews();

            // Kiểm tra người dùng đã đăng nhập
            if (!isUserLoggedIn()) {
                Log.e(TAG, "User is not logged in");
                Toast.makeText(this, "Vui lòng đăng nhập để xem thông tin", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Load thông tin người dùng
            loadAccountInfo();

            // Xử lý sự kiện nút quay lại
            setupBackButton();

            // Gán listener cho nút đổi mật khẩu
            setupChangePasswordButton();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo màn hình", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            etUsername = findViewById(R.id.etUsername);
            etDisplayName = findViewById(R.id.etDisplayName);
            etEmail = findViewById(R.id.etEmail);
            btnBack = findViewById(R.id.btnBack);
            ivAvatar = findViewById(R.id.ivAvatar);
            progressBar = findViewById(R.id.progressBar);

            // Đặt EditText ở chế độ không thể chỉnh sửa
            etUsername.setEnabled(false);
            etDisplayName.setEnabled(false);
            etEmail.setEnabled(false);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            throw e;
        }
    }

    private boolean isUserLoggedIn() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null;
    }

    private void setupBackButton() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                try {
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error in back button click: " + e.getMessage(), e);
                }
            });
        }
    }

    private void setupChangePasswordButton() {
        Button btnChangePassword = findViewById(R.id.btnChangePassword);
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChangePasswordActivity.class);
                startActivity(intent);
            });
        }
    }

    private void loadAccountInfo() {
        if (currentUser == null) {
            Log.e(TAG, "Current user is null");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        
        try {
            // Lấy email
            String email = currentUser.getEmail();
            if (etEmail != null) {
                etEmail.setText(email != null ? email : "Chưa có email");
            }

            // Lấy tên hiển thị
            String displayName = currentUser.getDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = email != null ? email.split("@")[0] : "Chưa có tên hiển thị";
            }
            if (etDisplayName != null) {
                etDisplayName.setText(displayName);
            }

            // Lấy tên đăng nhập (sử dụng email nếu không có username)
            String username = email != null ? email.split("@")[0] : "Chưa có tên đăng nhập";
            if (etUsername != null) {
                etUsername.setText(username);
            }

            // Load ảnh đại diện
            if (ivAvatar != null) {
                if (currentUser.getPhotoUrl() != null) {
                    Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop()
                        .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_profile);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading account info: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources if needed
    }
}
