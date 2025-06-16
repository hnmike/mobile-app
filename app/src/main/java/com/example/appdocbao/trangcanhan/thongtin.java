package com.example.appdocbao.trangcanhan;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.appdocbao.R;
import com.example.appdocbao.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class thongtin extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView btnBack, ivAvatar;
    private TextView toolbarTitle, btnSave, tvChangePhoto;
    private EditText etUsername, etDisplayName, etEmail;
    private Button btnChangePassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_thontintt);

        // Firebase setup
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = db.collection("users").document(currentUser.getUid());

        // Ánh xạ view
        toolbar = findViewById(R.id.toolbar);
        btnBack = findViewById(R.id.btnBack);
        ivAvatar = findViewById(R.id.ivAvatar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        btnSave = findViewById(R.id.btnSave);
        tvChangePhoto = findViewById(R.id.tvChangePhoto);
        etUsername = findViewById(R.id.etUsername);
        etDisplayName = findViewById(R.id.etDisplayName);
        etEmail = findViewById(R.id.etEmail);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        progressBar = findViewById(R.id.progressBar);

        toolbarTitle.setText("Thông tin tài khoản");

        btnBack.setOnClickListener(view -> finish());

        btnSave.setOnClickListener(view -> saveUserInfo());

        tvChangePhoto.setOnClickListener(view ->
                Toast.makeText(this, "Tính năng đổi ảnh chưa hỗ trợ.", Toast.LENGTH_SHORT).show()
        );

        btnChangePassword.setOnClickListener(view -> {
            try {
                startActivity(new Intent(thongtin.this, doimatkhau.class));
            } catch (Exception e) {
                Log.e(TAG, "Lỗi chuyển trang: " + e.getMessage());
                Toast.makeText(this, "Không thể mở trang đổi mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });

        loadUserInfo();
    }

    private void loadUserInfo() {
        progressBar.setVisibility(View.VISIBLE);

        userRef.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);

            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot != null && snapshot.exists()) {
                    User user = snapshot.toObject(User.class);
                    if (user != null) {
                        // Hiển thị thông tin người dùng
                        etUsername.setText(user.getUsername() != null ? user.getUsername() : "");
                        etDisplayName.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
                        etEmail.setText(user.getEmail() != null ? user.getEmail() : "");

                        // Load ảnh đại diện với Glide
                        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                            Glide.with(this)
                                    .load(user.getPhotoUrl())
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(android.R.drawable.ic_menu_gallery) // Sử dụng ảnh hệ thống
                                    .error(android.R.drawable.ic_menu_gallery)        // Sử dụng ảnh hệ thống
                                    .into(ivAvatar);
                        } else {
                            // Sử dụng ảnh hệ thống khi không có URL
                            ivAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
                        }
                    } else {
                        Toast.makeText(this, "Dữ liệu người dùng không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Tạo dữ liệu mới nếu không tìm thấy
                    createNewUserDocument();
                }
            } else {
                Toast.makeText(this, "Lỗi khi tải thông tin: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi Firestore: ", task.getException());
            }
        });
    }

    private void createNewUserDocument() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;

        User newUser = new User(
                firebaseUser.getUid(),
                firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "",
                firebaseUser.getEmail(),
                firebaseUser.getDisplayName(),
                firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null
        );

        userRef.set(newUser)
                .addOnSuccessListener(aVoid -> {
                    // Sau khi tạo xong, load lại thông tin
                    loadUserInfo();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tạo hồ sơ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserInfo() {
        String username = etUsername.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();

        if (username.isEmpty() || displayName.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        userRef.update(
                        "username", username,
                        "displayName", displayName
                )
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đã lưu thông tin!", Toast.LENGTH_SHORT).show();

                        // Cập nhật Firebase User profile
                        updateFirebaseProfile(displayName);
                    } else {
                        Toast.makeText(this, "Lỗi khi lưu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Lỗi cập nhật Firestore: ", task.getException());
                    }
                });
    }

    private void updateFirebaseProfile(String displayName) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Tạo profile update
        com.google.firebase.auth.UserProfileChangeRequest profileUpdates =
                new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Cập nhật Firebase profile thất bại", task.getException());
                    }
                });
    }
}