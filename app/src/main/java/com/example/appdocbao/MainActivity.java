package com.example.appdocbao;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.appdocbao.ui.auth.AuthViewModel;
import com.example.appdocbao.ui.auth.SignInActivity;
import com.example.appdocbao.ui.bookmarks.BookmarksActivity;
import com.example.appdocbao.ui.categories.CategoriesActivity;
import com.example.appdocbao.ui.home.HomeActivity;
import com.example.appdocbao.ui.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Initialize views first
            bottomNavigationView = findViewById(R.id.bottomNavigation);
            
            if (bottomNavigationView == null) {
                Log.e("MainActivity", "BottomNavigationView not found in layout");
                return;
            }

            // Set up bottom navigation
            setupBottomNavigation();
            
            // Set default selection to Home
            bottomNavigationView.setSelectedItemId(R.id.nav_home);

            // Test Firebase Authentication
            testFirebaseAuth();

            // Initialize authentication viewModel later to avoid crashes
            try {
                authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
                // Check authentication status - but don't redirect immediately
                checkAuthenticationStatus();
            } catch (Exception e) {
                Log.e("MainActivity", "Error initializing AuthViewModel: " + e.getMessage(), e);
            }

            // Tự động chuyển đến HomeActivity sau khi khởi tạo xong
            navigateToHome();

        } catch (Exception e) {
            Log.e("MainActivity", "Error in onCreate: " + e.getMessage(), e);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Intent intent = null;

            if (item.getItemId() == R.id.nav_home) {
                // Kiểm tra xem có đang ở MainActivity không, nếu có thì chuyển đến HomeActivity
                intent = new Intent(this, HomeActivity.class);
            } else if (item.getItemId() == R.id.nav_categories) {
                intent = new Intent(this, CategoriesActivity.class);
            } else if (item.getItemId() == R.id.nav_bookmarks) {
                intent = new Intent(this, BookmarksActivity.class);
            } else if (item.getItemId() == R.id.nav_profile) {
                // Kiểm tra trạng thái đăng nhập
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    intent = new Intent(this, ProfileActivity.class);
                } else {
                    intent = new Intent(this, SignInActivity.class);
                }
            }

            if (intent != null) {
                // Thêm flags để tránh tạo nhiều instance
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void checkAuthenticationStatus() {
        try {
            // Observe authentication state - but with delay to avoid immediate redirect
            authViewModel.getCurrentUser().observe(this, user -> {
                // Log authentication status
                Log.d("MainActivity", "Authentication status checked, user: " + (user != null ? "logged in" : "not logged in"));

                // Don't redirect immediately on app start - let user use the app
                // Only redirect to sign in when user explicitly tries to access profile
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Error checking authentication status: " + e.getMessage(), e);
        }
    }

    private void redirectToSignIn() {
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Chuyển đến HomeActivity làm trang chủ chính
     */
    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }



    // Phương thức test Firebase Authentication
    private void testFirebaseAuth() {
        try {
            Log.d("MainActivity", "Bắt đầu kiểm tra Firebase Authentication...");

            // Kiểm tra API key
            String apiKey = FirebaseApp.getInstance().getOptions().getApiKey();
            Log.d("MainActivity", "Firebase API Key: " + apiKey);

            // Kiểm tra Firebase Auth instance
            FirebaseAuth auth = FirebaseAuth.getInstance();
            Log.d("MainActivity", "FirebaseAuth đã được khởi tạo: " + (auth != null));

            // Log current user status
            if (auth.getCurrentUser() != null) {
                Log.d("MainActivity", "Người dùng đã đăng nhập: " + auth.getCurrentUser().getEmail());
            } else {
                Log.d("MainActivity", "Chưa có người dùng đăng nhập");
            }

            // Kiểm tra file google-services.json
            checkGoogleServicesJson();

        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi khi test Firebase Auth: " + e.getMessage(), e);
        }
    }

    // Thêm phương thức kiểm tra file google-services.json
    private void checkGoogleServicesJson() {
        try {
            InputStream is = getAssets().open("google-services.json");
            Log.e("MainActivity", "Không thể mở file google-services.json từ assets");
        } catch (IOException e) {
            // Expected, file không nằm trong assets
            Log.d("MainActivity", "File google-services.json không nằm trong assets (đây là bình thường)");
        }
        
        try {
            File googleServicesFile = new File(getApplicationContext().getFilesDir().getParentFile(), "app/google-services.json");
            if (googleServicesFile.exists()) {
                Log.d("MainActivity", "Tìm thấy file google-services.json trong " + googleServicesFile.getAbsolutePath());
            } else {
                Log.e("MainActivity", "Không tìm thấy file google-services.json trong " + googleServicesFile.getAbsolutePath());
            }
            
            // Kiểm tra trong thư mục app
            File appDir = new File(getApplicationContext().getFilesDir().getParentFile().getParentFile(), "app");
            File appGoogleServicesFile = new File(appDir, "google-services.json");
            if (appGoogleServicesFile.exists()) {
                Log.d("MainActivity", "Tìm thấy file google-services.json trong " + appGoogleServicesFile.getAbsolutePath());
            } else {
                Log.e("MainActivity", "Không tìm thấy file google-services.json trong " + appGoogleServicesFile.getAbsolutePath());
            }
            
            FirebaseOptions options = FirebaseApp.getInstance().getOptions();
            String apiKey = options.getApiKey();
            if ("AIzaSyA1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q".equals(apiKey)) {
                Log.e("MainActivity", "⚠️ CẢNH BÁO: Đang sử dụng API key mẫu! Cần thay thế google-services.json");
            }
            
        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi khi kiểm tra google-services.json: " + e.getMessage(), e);
        }
    }
}