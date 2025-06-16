package com.example.appdocbao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.appdocbao.ui.auth.AuthViewModel;
import com.example.appdocbao.ui.auth.SignInActivity;
import com.example.appdocbao.ui.categories.CategoriesActivity;
import com.example.appdocbao.ui.home.HomeActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private Button btnGetStarted;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize authentication viewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        btnGetStarted = findViewById(R.id.btnGetStarted);

        // Set up button click listener
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToNextScreen();
            }
        });

        // Comment out automatic navigation after 2 seconds to let user see the splash screen
        /*
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToNextScreen();
            }
        }, 2000);
        */

        // Test Firebase Authentication
        testFirebaseAuth();

        // Kiểm tra file google-services.json
        checkGoogleServicesJson();
    }

    private void navigateToNextScreen() {
        // Chuyển đến HomeActivity thay vì CategoriesActivity
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);

        // Finish this activity
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

            // Tạo email test
            String testEmail = "test" + System.currentTimeMillis() + "@example.com";
            String testPassword = "Test123456";

            // Thử đăng ký không thực sự tạo tài khoản
            Log.d("MainActivity", "Thử kết nối Firebase Auth với email: " + testEmail);

            auth.fetchSignInMethodsForEmail(testEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("MainActivity", "Kết nối Firebase Auth thành công!");
                        } else {
                            Log.e("MainActivity", "Lỗi kết nối Firebase Auth: " +
                                    (task.getException() != null ? task.getException().getMessage() : "Không rõ"));
                        }
                    });

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