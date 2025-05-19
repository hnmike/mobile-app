package com.example.appdocbao.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.appdocbao.R;
import com.example.appdocbao.ui.auth.SignInActivity;
import com.example.appdocbao.ui.categories.CategoriesActivity;
import com.example.appdocbao.ui.bookmarks.BookmarksActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // UI Components
    private ImageView imgUserPhoto;
    private TextView tvUsername, tvEmail;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;

    private ImageView btnChevronLanguage;
    private ImageView btnChevronAccountInfo;
    private ImageView btnChevronPolicy;
    private ImageView btnChevronTerms;
    private ImageView btnLogout;

    private View signOutView;

    // ViewModel
    private ProfileViewModel viewModel;

    // Flags
    private boolean isNavigating = false;
    private boolean viewsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Log.d(TAG, "ProfileActivity onCreate started");

        if (!isUserLoggedIn()) {
            Log.d(TAG, "User is not logged in, navigating to login screen");
            safeNavigateToSignIn();
            return;
        }

        if (!initializeViews()) {
            Log.e(TAG, "Failed to initialize UI components");
            safeNavigateToSignIn();
            return;
        }

        setupViewModel();
        loadUserData();
        setupObservers();
        setupListeners();
    }

    private boolean isUserLoggedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth != null && auth.getCurrentUser() != null;
    }

    private boolean initializeViews() {
        try {
            imgUserPhoto = findViewById(R.id.profileImage);
            tvUsername = findViewById(R.id.tvName);
            tvEmail = findViewById(R.id.tvEmail);
            progressBar = findViewById(R.id.progressBar);
            bottomNavigationView = findViewById(R.id.bottomNavigation);

            btnChevronLanguage = findViewById(R.id.btnChevronLanguage);
            btnChevronAccountInfo = findViewById(R.id.btnChevronAccountInfo);
            btnChevronPolicy = findViewById(R.id.btnChevronPolicy);
            btnChevronTerms = findViewById(R.id.btnChevronTerms);
            btnLogout = findViewById(R.id.btnLogout);

            // Gán View Sign Out (có thể là CardView hoặc Layout bất kỳ)
            signOutView = findViewById(R.id.cvSignOut);

            // Kiểm tra các View chính đã được gán chưa
            viewsInitialized = (imgUserPhoto != null && tvUsername != null && tvEmail != null);
            return viewsInitialized;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            return false;
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            tvEmail.setText(currentUser.getEmail());

            String name = currentUser.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = currentUser.getEmail().split("@")[0];
            }
            tvUsername.setText(name);

            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop()
                        .into(imgUserPhoto);
            } else {
                imgUserPhoto.setImageResource(R.drawable.ic_profile);
            }
        }
    }

    private void setupObservers() {
        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                String displayedName = user.getDisplayName();
                if (displayedName == null || displayedName.isEmpty()) {
                    displayedName = user.getUsername();
                }
                tvUsername.setText(displayedName);
                tvEmail.setText(user.getEmail());
            }
        });
    }

    private void setupListeners() {
        // Gán listener cho từng icon chức năng
        btnChevronLanguage.setOnClickListener(v -> startActivity(new Intent(this, ContactActivity.class)));
        btnChevronAccountInfo.setOnClickListener(v -> startActivity(new Intent(this, AccountInfoActivity.class)));
        btnChevronPolicy.setOnClickListener(v -> startActivity(new Intent(this, ProfilePolicyActivity.class)));
        btnChevronTerms.setOnClickListener(v -> startActivity(new Intent(this, ProfileTermsActivity.class)));

        // Gán listener cho nút logout hình icon
        btnLogout.setOnClickListener(v -> signOut());

        // Gán listener cho layout hoặc button Sign Out
        if (signOutView != null) {
            signOutView.setOnClickListener(v -> signOut());
        }
    }

    private void signOut() {
        Log.d(TAG, "Signing out...");
        FirebaseAuth.getInstance().signOut();
        safeNavigateToSignIn();
    }

    private void safeNavigateToSignIn() {
        Log.d(TAG, "Navigating to SignInActivity");
        Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
