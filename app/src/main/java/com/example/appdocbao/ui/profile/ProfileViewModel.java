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

    public ProfileViewModel(@NonNull Application application) {
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

    public void signOut() {
        authRepository.signOut();
    }
    
    /**
     * Kiểm tra xem người dùng đã đăng nhập chưa
     * @return true nếu người dùng đã đăng nhập, false nếu chưa
     */
    public boolean isUserLoggedIn() {
        return authRepository.isLoggedIn();
    }
} 