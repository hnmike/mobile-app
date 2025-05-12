package com.example.appdocbao;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class App extends Application {
    
    private static final String TAG = "AppDocBao";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            // Thêm log trước khi khởi tạo
            Log.d(TAG, "Bắt đầu khởi tạo Firebase...");
            
            // Kiểm tra xem Firebase đã được khởi tạo chưa
            if (FirebaseApp.getApps(this).isEmpty()) {
                // Initialize Firebase
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase đã được khởi tạo thành công");
            } else {
                Log.d(TAG, "Firebase đã được khởi tạo trước đó");
            }
            
            // Debug chi tiết cấu hình Firebase
            try {
                FirebaseOptions options = FirebaseApp.getInstance().getOptions();
                Log.d(TAG, "Firebase project ID: " + options.getProjectId());
                Log.d(TAG, "Firebase API Key: " + options.getApiKey());
                Log.d(TAG, "Firebase App ID: " + options.getApplicationId());
                Log.d(TAG, "Firebase Database URL: " + options.getDatabaseUrl());
                Log.d(TAG, "Firebase Storage Bucket: " + options.getStorageBucket());
                
                // Validate API key format
                String apiKey = options.getApiKey();
                if (apiKey == null || apiKey.isEmpty()) {
                    Log.e(TAG, "API Key trống hoặc null!");
                } else if (apiKey.equals("AIzaSyA1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q") || apiKey.startsWith("YOUR_API_KEY")) {
                    Log.e(TAG, "API Key là giá trị mẫu, không phải API key thực!");
                } else if (apiKey.length() < 30) {
                    Log.e(TAG, "API Key có vẻ quá ngắn: " + apiKey.length() + " ký tự");
                } else {
                    Log.d(TAG, "API Key có định dạng hợp lệ");
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi đọc FirebaseOptions: " + e.getMessage(), e);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo Firebase: " + e.getMessage(), e);
        }
    }
} 