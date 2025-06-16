package com.example.appdocbao.ui.categories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
<<<<<<< HEAD
=======
import androidx.lifecycle.MutableLiveData;
>>>>>>> origin/hnhung

import com.example.appdocbao.data.model.Category;
import com.example.appdocbao.data.repository.CategoryRepository;

import java.util.List;

public class CategoriesViewModel extends AndroidViewModel {

    private final CategoryRepository categoryRepository;
    private final LiveData<List<Category>> categories;
    private final LiveData<Boolean> isLoading;
<<<<<<< HEAD
=======
    private final LiveData<String> errorMessage;
>>>>>>> origin/hnhung

    public CategoriesViewModel(@NonNull Application application) {
        super(application);
        categoryRepository = CategoryRepository.getInstance(application);
        categories = categoryRepository.getCategories();
        isLoading = categoryRepository.getIsLoading();
<<<<<<< HEAD
=======
        errorMessage = categoryRepository.getErrorMessage();
>>>>>>> origin/hnhung
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

<<<<<<< HEAD
=======
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

>>>>>>> origin/hnhung
    public void loadCategories() {
        categoryRepository.loadCategories();
    }
} 