package com.example.appdocbao.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.appdocbao.data.model.User;
import com.example.appdocbao.data.repository.AuthRepository;

public class ProfileViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final LiveData<User> currentUser;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> errorMessage;

    /**
     * Constructs a ProfileViewModel and initializes LiveData fields for user profile management.
     *
     * @param application the application context used to obtain repository instances
     */
    public ProfileViewModel(@NonNull Application application) {
        super(application);
        authRepository = AuthRepository.getInstance(application);
        currentUser = authRepository.getCurrentUser();
        isLoading = authRepository.getIsLoading();
        errorMessage = authRepository.getErrorMessage();
    }

    /**
     * Returns a LiveData object containing the current user's profile information.
     *
     * @return LiveData representing the current User, or null if no user is logged in
     */
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns a LiveData object representing the current loading state.
     *
     * @return LiveData that emits true when a loading operation is in progress, false otherwise
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Returns a LiveData object containing error messages related to user profile operations.
     *
     * @return LiveData holding the current error message as a String, or null if no error has occurred
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Signs out the current user by delegating the operation to the authentication repository.
     */
    public void signOut() {
        authRepository.signOut();
    }
    
    /**
     * Returns whether a user is currently logged in.
     *
     * @return true if a user is logged in; false otherwise
     */
    public boolean isUserLoggedIn() {
        return authRepository.isLoggedIn();
    }
} 