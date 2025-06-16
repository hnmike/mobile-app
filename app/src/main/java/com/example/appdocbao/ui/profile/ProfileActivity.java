package com.example.appdocbao.ui.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.appdocbao.R;
import com.example.appdocbao.trangcanhan.chinhsach;
import com.example.appdocbao.trangcanhan.dieukhoan;
import com.example.appdocbao.trangcanhan.lienhe;
import com.example.appdocbao.trangcanhan.thongtin;
import com.example.appdocbao.ui.auth.SignInActivity;
import com.example.appdocbao.ui.bookmarks.BookmarksActivity;
import com.example.appdocbao.ui.categories.CategoriesActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int IMAGE_PICKER_REQUEST = 100;

    // UI Components
    private ImageView imgUserPhoto;
    private TextView tvUsername, tvEmail;
    private Button btnSignOut;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;

    // New UI Components
    private ImageView lienheIcon, thongtinIcon, chinhsachIcon, dieukhoaIcon;
    private CardView cvSignOut;

    // ViewModel
    private ProfileViewModel viewModel;

    // Flags
    private boolean isNavigating = false;
    private boolean viewsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_profile);
            Log.d(TAG, "ProfileActivity onCreate started");

            // Kiểm tra đăng nhập trước khi khởi tạo giao diện
            if (!isUserLoggedIn()) {
                Log.d(TAG, "User is not logged in, navigating to login screen");
                safeNavigateToSignIn();
                return;
            }

            // Khởi tạo giao diện
            if (!initializeViews()) {
                Log.e(TAG, "Failed to initialize UI components");
                safeNavigateToSignIn();
                return;
            }

            // Hiển thị loading indicator
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }

            // Setup các thành phần
            setupViewModel();
            loadUserData();
            setupObservers();
            setupListeners();
            setupBottomNavigation();

        } catch (Exception e) {
            Log.e(TAG, "Fatal error in ProfileActivity onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo ứng dụng", Toast.LENGTH_SHORT).show();
            safeNavigateToSignIn();
        }
    }

    private boolean isUserLoggedIn() {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            return auth != null && auth.getCurrentUser() != null;
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status: " + e.getMessage(), e);
            return false;
        }
    }

    private boolean initializeViews() {
        try {
            // Main UI Components
            imgUserPhoto = findViewById(R.id.profileImage);
            tvUsername = findViewById(R.id.tvName);
            tvEmail = findViewById(R.id.tvEmail);
            progressBar = findViewById(R.id.progressBar);

            // Sign out components
            View signOutView = findViewById(R.id.cvSignOut);
            if (signOutView instanceof Button) {
                btnSignOut = (Button) signOutView;
            } else if (signOutView instanceof CardView) {
                cvSignOut = (CardView) signOutView;
            }

            // New feature icons
            lienheIcon = findViewById(R.id.lienhe);
            thongtinIcon = findViewById(R.id.thongtin);
            chinhsachIcon = findViewById(R.id.chinhsach);
            dieukhoaIcon = findViewById(R.id.dieukhoan);

            // Bottom Navigation
            View navView = findViewById(R.id.bottomNavigation);
            if (navView instanceof BottomNavigationView) {
                bottomNavigationView = (BottomNavigationView) navView;
            }

            // Đánh dấu đã khởi tạo thành công
            viewsInitialized = (imgUserPhoto != null && tvUsername != null && tvEmail != null);
            return viewsInitialized;

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            return false;
        }
    }

    private void setupListeners() {
        try {
            if (!viewsInitialized) return;

            // Sign out listener
            if (btnSignOut != null) {
                btnSignOut.setOnClickListener(v -> showSignOutDialog());
            } else if (cvSignOut != null) {
                cvSignOut.setOnClickListener(v -> showSignOutDialog());
            }

            // Profile image click listener (for changing profile picture)
            if (imgUserPhoto != null) {
                imgUserPhoto.setOnClickListener(v -> openImagePicker());
            }

            // Feature icons listeners
            if (lienheIcon != null) {
                lienheIcon.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(ProfileActivity.this, lienhe.class);
                        startActivity(intent);
                        Toast.makeText(this, "Chuyển đến trang Liên hệ", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to Contact: " + e.getMessage());
                        Toast.makeText(this, "Không thể mở trang Liên hệ", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (thongtinIcon != null) {
                thongtinIcon.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(ProfileActivity.this, thongtin.class);
                        startActivity(intent);
                        Toast.makeText(this, "Chuyển đến Thông tin tài khoản", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to Account Info: " + e.getMessage());
                        Toast.makeText(this, "Không thể mở trang Thông tin tài khoản", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (chinhsachIcon != null) {
                chinhsachIcon.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(ProfileActivity.this, chinhsach.class);
                        startActivity(intent);
                        Toast.makeText(this, "Chuyển đến trang Chính sách", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to Policy: " + e.getMessage());
                        Toast.makeText(this, "Không thể mở trang Chính sách", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (dieukhoaIcon != null) {
                dieukhoaIcon.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(ProfileActivity.this, dieukhoan.class);
                        startActivity(intent);
                        Toast.makeText(this, "Chuyển đến trang Điều khoản", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to Terms: " + e.getMessage());
                        Toast.makeText(this, "Không thể mở trang Điều khoản", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up listeners: " + e.getMessage(), e);
        }
    }

    private void openImagePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICKER_REQUEST);
        } catch (Exception e) {
            Log.e(TAG, "Error opening image picker: " + e.getMessage());
            Toast.makeText(this, "Không thể mở thư viện ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSignOutDialog() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> performSignOut())
                    .setNegativeButton("Hủy", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing sign out dialog: " + e.getMessage());
            // Fallback: directly sign out
            performSignOut();
        }
    }

    private void performSignOut() {
        try {
            FirebaseAuth.getInstance().signOut();
            safeNavigateToSignIn();
        } catch (Exception e) {
            Log.e(TAG, "Error signing out: " + e.getMessage(), e);
            safeNavigateToSignIn();
        }
    }

    private void setupViewModel() {
        try {
            viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ViewModel: " + e.getMessage(), e);
        }
    }

    private void loadUserData() {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                Log.d(TAG, "Current Firebase user: " + currentUser.getEmail());

                if (tvEmail != null) {
                    tvEmail.setText(currentUser.getEmail());
                }

                if (tvUsername != null) {
                    if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                        tvUsername.setText(currentUser.getDisplayName());
                    } else {
                        String emailName = currentUser.getEmail();
                        if (emailName != null && emailName.contains("@")) {
                            tvUsername.setText(emailName.substring(0, emailName.indexOf("@")));
                        } else {
                            tvUsername.setText("Người dùng");
                        }
                    }
                }

                loadProfileImage(currentUser);

            } else {
                Log.e(TAG, "Current Firebase user is null");
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user data: " + e.getMessage(), e);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void loadProfileImage(FirebaseUser user) {
        if (imgUserPhoto != null && !isDestroyed() && !isFinishing()) {
            try {
                if (user.getPhotoUrl() != null) {
                    Glide.with(this)
                            .load(user.getPhotoUrl())
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .circleCrop()
                            .into(imgUserPhoto);
                } else {
                    imgUserPhoto.setImageResource(R.drawable.ic_profile);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading profile image: " + e.getMessage());
                imgUserPhoto.setImageResource(R.drawable.ic_profile);
            }
        }
    }

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

                        if (imgUserPhoto != null && !isDestroyed() && !isFinishing()) {
                            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                                try {
                                    Glide.with(this)
                                            .load(user.getPhotoUrl())
                                            .placeholder(R.drawable.ic_profile)
                                            .error(R.drawable.ic_profile)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .circleCrop()
                                            .into(imgUserPhoto);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error loading profile image: " + e.getMessage());
                                    imgUserPhoto.setImageResource(R.drawable.ic_profile);
                                }
                            } else {
                                imgUserPhoto.setImageResource(R.drawable.ic_profile);
                            }
                        }
                    } else {
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
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_categories) {
                        Intent intent = new Intent(ProfileActivity.this, CategoriesActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_bookmarks) {
                        Intent intent = new Intent(ProfileActivity.this, BookmarksActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
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

    private void safeNavigateToSignIn() {
        try {
            if (isNavigating) return;
            isNavigating = true;

            Log.d(TAG, "Navigating to SignInActivity");

            Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to SignIn: " + e.getMessage(), e);
            try {
                Intent intent = new Intent();
                intent.setClassName(getPackageName(), "com.example.appdocbao.ui.auth.SignInActivity");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } catch (Exception e2) {
                Log.e(TAG, "Fatal error navigating: " + e2.getMessage(), e2);
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                Uri imageUri = data.getData();
                if (imageUri != null && imgUserPhoto != null) {
                    // Option 1: Use Glide (recommended)
                    if (!isDestroyed() && !isFinishing()) {
                        Glide.with(this)
                                .load(imageUri)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .circleCrop()
                                .into(imgUserPhoto);
                    }

                    // Option 2: Use Bitmap (fallback)
                    /*
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        imgUserPhoto.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Log.e(TAG, "Error loading bitmap: " + e.getMessage());
                    }
                    */

                    Toast.makeText(this, "Ảnh đại diện đã được cập nhật", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling image result: " + e.getMessage());
                Toast.makeText(this, "Không thể cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear observers to prevent memory leaks
        if (viewModel != null) {
            try {
                viewModel.getCurrentUser().removeObservers(this);
                viewModel.getIsLoading().removeObservers(this);
                viewModel.getErrorMessage().removeObservers(this);
            } catch (Exception e) {
                Log.e(TAG, "Error clearing observers: " + e.getMessage());
            }
        }
    }
}