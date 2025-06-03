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

    /**
     * Initializes the AuthViewModel and sets up LiveData fields for authentication state and operations.
     *
     * @param application the application context used to initialize the authentication repository
     */
    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = AuthRepository.getInstance(application);
        currentUser = authRepository.getCurrentUser();
        isLoading = authRepository.getIsLoading();
        errorMessage = authRepository.getErrorMessage();
    }

    /**
     * Returns a LiveData object representing the currently authenticated user.
     *
     * @return LiveData containing the current User, or null if no user is authenticated
     */
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns a LiveData indicating whether an authentication operation is currently in progress.
     *
     * @return LiveData that is true if an authentication process is ongoing, false otherwise
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Returns a LiveData object containing error messages from authentication operations.
     *
     * @return LiveData holding the latest authentication error message, or null if no error.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns a LiveData indicating whether there are validation errors in the authentication input fields.
     *
     * @return LiveData that is true if validation errors are present, false otherwise
     */
    public LiveData<Boolean> getValidationErrors() {
        return validationErrors;
    }

    /**
     * Attempts to sign in a user with the provided email and password.
     *
     * Performs input validation for non-empty fields and valid email format before delegating authentication to the repository.
     * Sets validation error state if input is invalid.
     *
     * @param email the user's email address
     * @param password the user's password
     */
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

    /**
     * Attempts to register a new user with the provided username, email, password, and confirmation password.
     *
     * Performs input validation for all fields. If validation fails, sets a validation error flag and aborts the sign-up process. If validation succeeds, delegates user creation to the authentication repository.
     *
     * @param username the desired username for the new account
     * @param email the user's email address
     * @param password the user's chosen password
     * @param confirmPassword confirmation of the chosen password
     */
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

    /**
     * Initiates sign-in using a Google account if the provided account is not null.
     *
     * @param account the GoogleSignInAccount to authenticate with
     */
    public void signInWithGoogle(GoogleSignInAccount account) {
        if (account != null) {
            authRepository.signInWithGoogle(account);
        }
    }

    /**
     * Initiates sign-in using a Facebook access token if the token is not null.
     *
     * @param accessToken the Facebook access token to authenticate the user
     */
    public void signInWithFacebook(AccessToken accessToken) {
        if (accessToken != null) {
            authRepository.signInWithFacebook(accessToken);
        }
    }

    /****
     * Signs out the currently authenticated user.
     */
    public void signOut() {
        authRepository.signOut();
    }

    /**
     * Returns whether a user is currently authenticated.
     *
     * @return true if a user is logged in; false otherwise
     */
    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }
} 