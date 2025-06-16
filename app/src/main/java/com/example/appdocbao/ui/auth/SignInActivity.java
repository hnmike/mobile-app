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
import com.example.appdocbao.ui.profile.ProfileActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private EditText etEmail, etPassword;
    private CardView btnSignIn, btnGoogleSignIn, btnFacebookSignIn;
    private TextView tvForgotPassword, tvSignUp;
    private View progressOverlay;

    private AuthViewModel viewModel;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_sign_in);
    
            Log.d(TAG, "SignInActivity onCreate started");
    
            // Initialize views
            initializeViews();
            
            // Initialize ViewModel
            setupViewModel();
            
            // Set up authentication methods
            setupGoogleSignIn();
            setupFacebookSignIn();
            
            // Set click listeners
            setupClickListeners();
            
            // Set up observers
            setupObservers();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in SignInActivity onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo màn hình đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void initializeViews() {
        try {
            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            btnSignIn = findViewById(R.id.btnSignIn);
            btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
            btnFacebookSignIn = findViewById(R.id.btnFacebookSignIn);
            tvForgotPassword = findViewById(R.id.tvForgotPassword);
            tvSignUp = findViewById(R.id.tvSignUp);
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
            throw e;
        }
    }
    
    private void setupGoogleSignIn() {
        try {
            // Set up Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(this, gso);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Google Sign In: " + e.getMessage(), e);
        }
    }
    
    private void setupFacebookSignIn() {
        try {
            // Set up Facebook Sign In
            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(callbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            handleFacebookAccessToken(loginResult.getAccessToken());
                        }
    
                        @Override
                        public void onCancel() {
                            Toast.makeText(SignInActivity.this, "Đăng nhập bằng Facebook đã hủy", Toast.LENGTH_SHORT).show();
                        }
    
                        @Override
                        public void onError(FacebookException error) {
                            Toast.makeText(SignInActivity.this, "Lỗi đăng nhập: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Facebook Sign In: " + e.getMessage(), e);
        }
    }
    
    private void setupClickListeners() {
        try {
            btnSignIn.setOnClickListener(v -> signIn());
            btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
            btnFacebookSignIn.setOnClickListener(v -> signInWithFacebook());
            tvForgotPassword.setOnClickListener(v -> Toast.makeText(SignInActivity.this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show());
            tvSignUp.setOnClickListener(v -> navigateToSignUp());
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }
    
    private void setupObservers() {
        try {
            // Observe user authentication state
            viewModel.getCurrentUser().observe(this, user -> {
                if (user != null) {
                    Log.d(TAG, "User authentication successful: " + user.getEmail());
                    Toast.makeText(SignInActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    navigateAfterLogin();
                }
            });
    
            // Observe loading state
            viewModel.getIsLoading().observe(this, isLoading -> {
                progressOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            });
    
            // Observe error messages
            viewModel.getErrorMessage().observe(this, errorMessage -> {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    Toast.makeText(SignInActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
            
            // Observe validation errors
            viewModel.getValidationErrors().observe(this, hasErrors -> {
                if (hasErrors != null && hasErrors) {
                    // Check and show appropriate validation errors
                    validateFields();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up observers: " + e.getMessage(), e);
        }
    }

    private void signIn() {
        try {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
    
            // Sign in with email and password through ViewModel
            viewModel.signIn(email, password);
        } catch (Exception e) {
            Log.e(TAG, "Error during sign in: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void validateFields() {
        try {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Vui lòng nhập email");
            }
            
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Vui lòng nhập mật khẩu");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error validating fields: " + e.getMessage(), e);
        }
    }

    private void signInWithGoogle() {
        try {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } catch (Exception e) {
            Log.e(TAG, "Error starting Google sign in: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể đăng nhập bằng Google", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithFacebook() {
        try {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        } catch (Exception e) {
            Log.e(TAG, "Error starting Facebook sign in: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể đăng nhập bằng Facebook", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        try {
            viewModel.signInWithFacebook(token);
        } catch (Exception e) {
            Log.e(TAG, "Error handling Facebook token: " + e.getMessage(), e);
        }
    }

    private void navigateToSignUp() {
        try {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to sign up: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể mở trang đăng ký", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navigateAfterLogin() {
        try {
            Log.d(TAG, "Navigating after successful login");
            
            // Chuyển đến ProfileActivity sau khi đăng nhập thành công
            Intent profileIntent = new Intent(SignInActivity.this, ProfileActivity.class);
            profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(profileIntent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to profile directly: " + e.getMessage(), e);
            
            try {
                // Nếu không mở được ProfileActivity, thử mở Categories
                Log.d(TAG, "Trying to open CategoriesActivity as fallback");
                Intent intent = new Intent(this, CategoriesActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception e2) {
                Log.e(TAG, "Error navigating to categories: " + e2.getMessage(), e2);
                Toast.makeText(this, "Đăng nhập thành công nhưng không thể mở trang chủ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        try {
            // Pass the activity result to the Facebook callback
            callbackManager.onActivityResult(requestCode, resultCode, data);
    
            // Handle Google Sign In result
            if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    viewModel.signInWithGoogle(account);
                } catch (ApiException e) {
                    Toast.makeText(this, "Đăng nhập Google thất bại: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in activity result: " + e.getMessage(), e);
        }
    }
} 