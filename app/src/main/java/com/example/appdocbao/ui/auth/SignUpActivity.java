package com.example.appdocbao.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.example.appdocbao.R;
import com.example.appdocbao.ui.categories.CategoriesActivity;

public class SignUpActivity extends AppCompatActivity {
    
    private static final String TAG = "SignUpActivity";
    
    private EditText etUsername, etEmail, etPassword, etRepeatPassword;
    private CardView btnSignUp;
    private TextView tvSignIn;
    private View progressOverlay;
    
    private AuthViewModel viewModel;
    private boolean isRegistrationComplete = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_sign_up);
            
            Log.d(TAG, "SignUpActivity onCreate started");
            
            // Initialize views
            initializeViews();
            
            // Initialize ViewModel
            setupViewModel();
            
            // Set click listeners
            setupClickListeners();
            
            // Set up observers
            setupObservers();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in SignUpActivity onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo màn hình đăng ký", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void initializeViews() {
        try {
            etUsername = findViewById(R.id.etUsername);
            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            etRepeatPassword = findViewById(R.id.etRepeatPassword);
            btnSignUp = findViewById(R.id.btnSignUp);
            tvSignIn = findViewById(R.id.tvSignIn);
            progressOverlay = findViewById(R.id.progressOverlay);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            throw e;
        }
    }
    
    private void setupViewModel() {
        try {
            viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up ViewModel: " + e.getMessage(), e);
        }
    }
    
    private void setupClickListeners() {
        try {
            btnSignUp.setOnClickListener(v -> signUp());
            
            // Return to sign in screen when clicking "Sign In" link
            tvSignIn.setOnClickListener(v -> {
                Log.d(TAG, "Sign In link clicked, returning to SignInActivity");
                finish(); // Go back to sign in screen
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }
    
    private void setupObservers() {
        try {
            // Observe user authentication state
            viewModel.getCurrentUser().observe(this, user -> {
                try {
                    if (user != null && !isRegistrationComplete) {
                        Log.d(TAG, "User registered successfully: " + user.getEmail());
                        isRegistrationComplete = true;
                        returnToSignIn();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in user observer: " + e.getMessage(), e);
                }
            });
            
            // Observe loading state
            viewModel.getIsLoading().observe(this, isLoading -> {
                try {
                    progressOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                } catch (Exception e) {
                    Log.e(TAG, "Error updating loading state: " + e.getMessage(), e);
                }
            });
            
            // Observe error messages
            viewModel.getErrorMessage().observe(this, errorMessage -> {
                try {
                    if (errorMessage != null && !errorMessage.isEmpty()) {
                        Log.e(TAG, "Registration error: " + errorMessage);
                        Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error showing error message: " + e.getMessage(), e);
                }
            });
            
            // Observe validation errors
            viewModel.getValidationErrors().observe(this, hasErrors -> {
                try {
                    if (hasErrors != null && hasErrors) {
                        Log.d(TAG, "Form validation failed");
                        // Check and show appropriate validation errors
                        validateFields();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error handling validation: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up observers: " + e.getMessage(), e);
        }
    }
    
    private void signUp() {
        try {
            Log.d(TAG, "Sign up button clicked");
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String repeatPassword = etRepeatPassword.getText().toString().trim();
            
            Log.d(TAG, "Attempting signup with email: " + email);
            
            // Sign up with email and password through ViewModel
            viewModel.signUp(username, email, password, repeatPassword);
        } catch (Exception e) {
            Log.e(TAG, "Error during signup process: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi đăng ký: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void validateFields() {
        try {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String repeatPassword = etRepeatPassword.getText().toString().trim();
            
            if (TextUtils.isEmpty(username)) {
                etUsername.setError("Vui lòng nhập tên người dùng");
            }
            
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Vui lòng nhập email");
            } else if (!isValidEmail(email)) {
                etEmail.setError("Email không hợp lệ");
            }
            
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Vui lòng nhập mật khẩu");
            } else if (password.length() < 6) {
                etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            }
            
            if (TextUtils.isEmpty(repeatPassword)) {
                etRepeatPassword.setError("Vui lòng nhập lại mật khẩu");
            } else if (!password.equals(repeatPassword)) {
                etRepeatPassword.setError("Mật khẩu không khớp");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error validating fields: " + e.getMessage(), e);
        }
    }
    
    private boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    
    private void returnToSignIn() {
        try {
            Log.d(TAG, "Returning to SignInActivity after successful registration");
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error returning to SignInActivity: " + e.getMessage(), e);
            // Fallback - just finish this activity
            try {
                finish();
            } catch (Exception e2) {
                Log.e(TAG, "Fatal error navigating back: " + e2.getMessage(), e2);
            }
        }
    }
} 