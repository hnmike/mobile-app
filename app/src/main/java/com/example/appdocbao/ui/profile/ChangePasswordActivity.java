package com.example.appdocbao.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdocbao.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangePasswordActivity";
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnSave;
    private ImageView btnBack;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thực hiện chức năng này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                changePassword();
            }
        });
    }

    private boolean validateInputs() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (currentPassword.isEmpty()) {
            etCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            return false;
        }

        if (newPassword.isEmpty()) {
            etNewPassword.setError("Vui lòng nhập mật khẩu mới");
            return false;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Vui lòng xác nhận mật khẩu mới");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return false;
        }

        return true;
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // Reauthenticate user before changing password
        mAuth.signInWithEmailAndPassword(currentUser.getEmail(), currentPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // User re-authenticated successfully, now change password
                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    progressBar.setVisibility(View.GONE);
                                    btnSave.setEnabled(true);

                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(ChangePasswordActivity.this,
                                                "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(ChangePasswordActivity.this,
                                                "Lỗi khi đổi mật khẩu: " + updateTask.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(ChangePasswordActivity.this,
                                "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                    }
                });
    }
} 