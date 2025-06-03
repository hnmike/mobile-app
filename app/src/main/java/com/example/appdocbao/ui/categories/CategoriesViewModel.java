package com.example.appdocbao.ui.categories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appdocbao.data.model.Category;
import com.example.appdocbao.data.repository.CategoryRepository;

import java.util.List;

public class CategoriesViewModel extends AndroidViewModel {

    private final CategoryRepository categoryRepository;
    private final LiveData<List<Category>> categories;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> errorMessage;

    /**
     * Initializes the CategoriesViewModel with application context and sets up LiveData objects for categories, loading state, and error messages from the CategoryRepository.
     *
     * @param application the Application context used to initialize the repository
     */
    public CategoriesViewModel(@NonNull Application application) {
        super(application);
        categoryRepository = CategoryRepository.getInstance(application);
        categories = categoryRepository.getCategories();
        isLoading = categoryRepository.getIsLoading();
        errorMessage = categoryRepository.getErrorMessage();
    }

    /**
     * Returns a LiveData object containing the list of categories.
     *
     * UI components can observe this LiveData to receive updates when the category data changes.
     *
     * @return LiveData holding a list of Category objects
     */
    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    /**
     * Returns a LiveData object representing the loading state of category data.
     *
     * @return LiveData that emits true when categories are being loaded, false otherwise
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Returns a LiveData object containing error messages related to category data operations.
     *
     * @return LiveData holding the current error message, or null if no error has occurred
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Initiates loading or refreshing of category data via the repository.
     *
     * Triggers the repository to fetch the latest list of categories, updating the associated LiveData objects.
     */
    public void loadCategories() {
        categoryRepository.loadCategories();
    }
} 