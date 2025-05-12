package com.example.appdocbao.ui.auth;

import android.app.Application;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appdocbao.data.model.User;
import com.example.appdocbao.data.repository.AuthRepository;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final LiveData<User> currentUser;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> errorMessage;
    private final MutableLiveData<Boolean> validationErrors = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = AuthRepository.getInstance(application);
        currentUser = authRepository.getCurrentUser();
        isLoading = authRepository.getIsLoading();
        errorMessage = authRepository.getErrorMessage();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getValidationErrors() {
        return validationErrors;
    }

    public void signIn(String email, String password) {
        // Validate inputs
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            validationErrors.setValue(true);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            validationErrors.setValue(true);
            return;
        }

        // Call repository for authentication
        authRepository.signIn(email, password);
    }

    public void signUp(String username, String email, String password, String confirmPassword) {
        // Clear previous validation errors
        validationErrors.setValue(false);
        
        // Validate inputs
        boolean hasError = false;
        
        if (TextUtils.isEmpty(username)) {
            hasError = true;
            Log.d("AuthViewModel", "Username validation failed: empty");
        }
        
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            hasError = true;
            Log.d("AuthViewModel", "Email validation failed: " + (TextUtils.isEmpty(email) ? "empty" : "invalid format"));
        }
        
        if (TextUtils.isEmpty(password)) {
            hasError = true;
            Log.d("AuthViewModel", "Password validation failed: empty");
        } else if (password.length() < 6) {
            hasError = true;
            Log.d("AuthViewModel", "Password validation failed: too short");
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            hasError = true;
            Log.d("AuthViewModel", "Confirm password validation failed: empty");
        } else if (!password.equals(confirmPassword)) {
            hasError = true;
            Log.d("AuthViewModel", "Confirm password validation failed: passwords don't match");
        }
        
        if (hasError) {
            validationErrors.setValue(true);
            return;
        }
        
        // All validations passed
        Log.d("AuthViewModel", "Validation passed, proceeding with signup");
        
        // Call repository for user creation
        authRepository.signUp(username, email, password);
    }

    public void signInWithGoogle(GoogleSignInAccount account) {
        if (account != null) {
            authRepository.signInWithGoogle(account);
        }
    }

    public void signInWithFacebook(AccessToken accessToken) {
        if (accessToken != null) {
            authRepository.signInWithFacebook(accessToken);
        }
    }

    public void signOut() {
        authRepository.signOut();
    }

    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }
} 