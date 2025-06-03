package com.example.appdocbao.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.appdocbao.R;
import com.example.appdocbao.ui.auth.SignInActivity;
import com.example.appdocbao.ui.bookmarks.BookmarksActivity;
import com.example.appdocbao.ui.categories.CategoriesActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    
    // UI Components
    private ImageView imgUserPhoto;
    private TextView tvUsername, tvEmail;
    private Button btnSignOut;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;
    
    // ViewModel
    private ProfileViewModel viewModel;
    
    // Flags
    private boolean isNavigating = false;
    private boolean viewsInitialized = false;

    /**
     * Initializes the profile screen, handling user authentication, UI setup, ViewModel integration, and navigation.
     *
     * If the user is not authenticated, navigates to the sign-in screen. Initializes UI components, sets up the ViewModel, loads user data, and configures observers and listeners. Handles errors during initialization and setup, providing user feedback and fallback navigation as needed.
     *
     * @param savedInstanceState the saved instance state bundle, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Set layout
            setContentView(R.layout.activity_profile);
            
            Log.d(TAG, "ProfileActivity onCreate started");
            
            // Kiểm tra đăng nhập trước khi khởi tạo giao diện
            if (!isUserLoggedIn()) {
                Log.d(TAG, "User is not logged in, navigating to login screen");
                safeNavigateToSignIn();
                return;
            }
            
            // Khởi tạo giao diện - với try-catch riêng để đảm bảo không bỏ qua phần nào
            try {
                boolean initSuccess = initializeViews();
                if (!initSuccess) {
                    Log.e(TAG, "Failed to initialize UI components");
                    safeNavigateToSignIn();
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
                safeNavigateToSignIn();
                return;
            }
            
            // Chỉ thực thi các phương thức khác khi đã khởi tạo views thành công
            try {
                // Hiển thị loading indicator ngay lập tức
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                
                setupViewModel();
                
                // Hiển thị dữ liệu người dùng hiện tại từ FirebaseAuth trước
                loadUserData();
                
                // Sau đó thiết lập các observer để cập nhật dữ liệu từ ViewModel
                setupObservers();
                
                // Thiết lập các listener và bottom navigation
                setupListeners();
                setupBottomNavigation();
                
            } catch (Exception e) {
                Log.e(TAG, "Error in ProfileActivity setup: " + e.getMessage(), e);
                // Không rời khỏi activity, chỉ hiển thị thông báo lỗi
                if (viewsInitialized) {
                    Toast.makeText(this, "Có lỗi khi tải dữ liệu, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Fatal error in ProfileActivity onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo ứng dụng", Toast.LENGTH_SHORT).show();
            safeNavigateToSignIn();
        }
    }
    
    /****
     * Checks whether a user is currently authenticated with Firebase.
     *
     * @return true if a Firebase user is logged in; false otherwise
     */
    private boolean isUserLoggedIn() {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            return auth != null && auth.getCurrentUser() != null;
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Initializes and assigns UI components for the profile screen.
     *
     * Finds and assigns references to the user photo, username, email, sign-out control, progress bar, and bottom navigation views. Handles cases where the sign-out control may be a Button or a generic View, and sets up the appropriate click listener. Marks initialization as successful only if the main views are found.
     *
     * @return true if the main profile views are successfully initialized; false otherwise
     */
    private boolean initializeViews() {
        try {
            imgUserPhoto = findViewById(R.id.profileImage);
            tvUsername = findViewById(R.id.tvName);
            tvEmail = findViewById(R.id.tvEmail);
            
            // Kiểm tra xem cvSignOut có tồn tại trong layout không
            View signOutView = findViewById(R.id.cvSignOut);
            if (signOutView != null && signOutView instanceof Button) {
                btnSignOut = (Button) signOutView;
            } else if (signOutView != null) {
                // Nếu không phải Button, sử dụng onClick cho View
                btnSignOut = null;
                signOutView.setOnClickListener(v -> signOut());
            } else {
                Log.e(TAG, "Sign out button/view not found in layout");
            }
            
            progressBar = findViewById(R.id.progressBar);
            
            // Kiểm tra bottomNavigation
            View navView = findViewById(R.id.bottomNavigation); 
            if (navView != null && navView instanceof BottomNavigationView) {
                bottomNavigationView = (BottomNavigationView) navView;
            } else {
                Log.e(TAG, "Bottom navigation view not found or has wrong type");
                bottomNavigationView = null;
            }
            
            // Đánh dấu đã khởi tạo thành công nếu các view chính đã được tìm thấy
            viewsInitialized = (imgUserPhoto != null && tvUsername != null && tvEmail != null);
            return viewsInitialized;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            viewsInitialized = false;
            return false;
        }
    }
    
    /**
     * Sets up click listeners for the sign-out controls if views are initialized.
     *
     * Assigns the sign-out action to the sign-out button or, if unavailable, to an alternative view.
     */
    private void setupListeners() {
        try {
            if (!viewsInitialized) return;
            
            // Chỉ thiết lập listener nếu btnSignOut là Button
            if (btnSignOut != null) {
                btnSignOut.setOnClickListener(v -> signOut());
            }
            
            // Tìm các view khác để thiết lập listener nếu cần
            View cvSignOut = findViewById(R.id.cvSignOut);
            if (cvSignOut != null && btnSignOut == null) {
                cvSignOut.setOnClickListener(v -> signOut());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up listeners: " + e.getMessage(), e);
        }
    }
    
    /****
     * Signs the user out of Firebase and navigates to the sign-in screen.
     *
     * Attempts to sign out the current user and, regardless of success or failure, redirects to the sign-in activity.
     */
    private void signOut() {
        try {
            FirebaseAuth.getInstance().signOut();
            safeNavigateToSignIn();
        } catch (Exception e) {
            Log.e(TAG, "Error signing out: " + e.getMessage(), e);
            // Vẫn chuyển hướng đến trang đăng nhập
            safeNavigateToSignIn();
        }
    }
    
    /**
     * Initializes the ProfileViewModel for managing user profile data and shows the loading indicator.
     *
     * If initialization fails, logs the error.
     */
    private void setupViewModel() {
        try {
            // Khởi tạo ViewModel sau
            viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
            
            // Hiển thị loading indicator trong khi chờ dữ liệu
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ViewModel: " + e.getMessage(), e);
        }
    }
    
    /**
     * Loads and displays the current user's profile information from Firebase Authentication.
     *
     * Updates the username, email, and profile photo UI components if the user is logged in.
     * Falls back to default values or images if user data is missing. Hides the progress bar if user data cannot be loaded.
     */
    private void loadUserData() {
        try {
            // Kiểm tra và hiển thị thông tin người dùng hiện tại từ Firebase Auth
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();
            
            if (currentUser != null) {
                Log.d(TAG, "Current Firebase user: " + currentUser.getEmail());
                
                // Kiểm tra từng view trước khi sử dụng để tránh NPE
                if (tvEmail != null) {
                    tvEmail.setText(currentUser.getEmail());
                }
                
                if (tvUsername != null) {
                    if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                        tvUsername.setText(currentUser.getDisplayName());
                    } else {
                        // Nếu không có display name, hiển thị email
                        String emailName = currentUser.getEmail();
                        if (emailName != null && emailName.contains("@")) {
                            tvUsername.setText(emailName.substring(0, emailName.indexOf("@")));
                        } else {
                            tvUsername.setText("Người dùng");
                        }
                    }
                }
                
                // Tải ảnh đại diện nếu có
                if (imgUserPhoto != null) {
                    if (currentUser.getPhotoUrl() != null) {
                        try {
                            Glide.with(this)
                                    .load(currentUser.getPhotoUrl())
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .circleCrop()
                                    .into(imgUserPhoto);
                        } catch (Exception e) {
                            Log.e(TAG, "Error loading Firebase user photo: " + e.getMessage());
                            imgUserPhoto.setImageResource(R.drawable.ic_profile);
                        }
                    } else {
                        imgUserPhoto.setImageResource(R.drawable.ic_profile);
                    }
                }
            } else {
                Log.e(TAG, "Current Firebase user is null even though isUserLoggedIn returned true");
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user data directly: " + e.getMessage(), e);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Sets up LiveData observers for user data, loading state, and error messages from the ViewModel.
     *
     * Updates the UI with user profile information, handles loading indicator visibility, and displays error messages as toasts.
     * Navigates to the sign-in screen if the user becomes null.
     */
    private void setupObservers() {
        try {
            if (viewModel == null) {
                Log.e(TAG, "Cannot setup observers: viewModel is null");
                return;
            }
            
            // Observe user data
            viewModel.getCurrentUser().observe(this, user -> {
                try {
                    if (user != null) {
                        // Hiển thị thông tin người dùng đầy đủ hơn
                        // Ưu tiên hiển thị displayName nếu có, nếu không thì dùng username
                        String displayedName = user.getDisplayName();
                        if (displayedName == null || displayedName.isEmpty()) {
                            displayedName = user.getUsername();
                        }
                        
                        if (tvUsername != null) {
                            tvUsername.setText(displayedName);
                        }
                        
                        if (tvEmail != null) {
                            tvEmail.setText(user.getEmail());
                        }
                        
                        Log.d(TAG, "Loading user profile: username=" + displayedName + ", email=" + user.getEmail());
                        
                        // Cải thiện hiển thị ảnh đại diện
                        if (imgUserPhoto != null) {
                            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                                try {
                                    Glide.with(this)
                                            .load(user.getPhotoUrl())
                                            .placeholder(R.drawable.ic_profile)
                                            .error(R.drawable.ic_profile)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .circleCrop()
                                            .into(imgUserPhoto);
                                    Log.d(TAG, "Loading profile image from URL: " + user.getPhotoUrl());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error loading profile image: " + e.getMessage());
                                    imgUserPhoto.setImageResource(R.drawable.ic_profile);
                                }
                            } else {
                                Log.d(TAG, "No profile image URL, using default image");
                                imgUserPhoto.setImageResource(R.drawable.ic_profile);
                            }
                        }
                    } else {
                        // User signed out
                        Log.d(TAG, "Current user is null, navigating to sign-in screen");
                        safeNavigateToSignIn();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing user data: " + e.getMessage(), e);
                } finally {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        
            // Observe loading state
            viewModel.getIsLoading().observe(this, isLoading -> {
                try {
                    if (progressBar != null) {
                        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error updating loading state: " + e.getMessage(), e);
                }
            });
        
            // Observe error messages
            viewModel.getErrorMessage().observe(this, errorMessage -> {
                try {
                    if (errorMessage != null && !errorMessage.isEmpty()) {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error showing error message: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up observers: " + e.getMessage(), e);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Configures the bottom navigation bar to handle navigation between main app sections.
     *
     * Sets the current selection to the profile screen and defines navigation actions for home, categories, and bookmarks. Finishes the current activity after navigation to another section. If the bottom navigation view is not initialized, the method exits without action.
     */
    private void setupBottomNavigation() {
        try {
            if (bottomNavigationView == null) {
                Log.e(TAG, "Cannot setup bottom navigation: bottomNavigationView is null");
                return;
            }
            
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                try {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        Intent intent = new Intent(ProfileActivity.this, com.example.appdocbao.ui.home.HomeActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_categories) {
                        startActivity(new Intent(ProfileActivity.this, CategoriesActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_bookmarks) {
                        startActivity(new Intent(ProfileActivity.this, BookmarksActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_profile) {
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    Log.e(TAG, "Error in bottom navigation: " + e.getMessage(), e);
                    return false;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up bottom navigation: " + e.getMessage(), e);
        }
    }

    /**
     * Safely navigates the user to the sign-in screen, using multiple fallback methods to ensure navigation succeeds.
     *
     * Prevents multiple navigation attempts and, if navigation to the sign-in screen fails, attempts to navigate to the home screen as a last resort.
     */
    private void safeNavigateToSignIn() {
        try {
            // Prevent multiple navigation attempts
            if (isNavigating) {
                return;
            }
            isNavigating = true;
            
            Log.d(TAG, "Navigating to SignInActivity");
            
            // Phương pháp 1: Intent thông thường
            Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error with normal intent navigation: " + e.getMessage(), e);
            
            try {
                // Phương pháp 2: Intent với tên lớp đầy đủ
                Intent intent = new Intent();
                intent.setClassName(getPackageName(), "com.example.appdocbao.ui.auth.SignInActivity");
                startActivity(intent);
                finish();
            } catch (Exception e2) {
                Log.e(TAG, "Error with explicit class name intent: " + e2.getMessage(), e2);
                
                try {
                    // Phương pháp cuối cùng: Trở về màn hình chính
                    Intent homeIntent = new Intent(this, CategoriesActivity.class);
                    startActivity(homeIntent);
                    Toast.makeText(this, "Không thể chuyển đến trang đăng nhập", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e3) {
                    Log.e(TAG, "Fatal error navigating: " + e3.getMessage(), e3);
                    // Just finish if all else fails
                    finish();
                }
            }
        }
    }
} 