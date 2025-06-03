package com.example.appdocbao.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appdocbao.api.VnExpressParser;
import com.example.appdocbao.data.model.Category;
import com.example.appdocbao.utils.Constants;
import com.example.appdocbao.utils.NetworkUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {
    private static final String TAG = "CategoryRepository";

    private static CategoryRepository instance;
    private final FirebaseFirestore firestore;
    private final ExecutorService executorService;
    private final Context context;
    
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * Initializes the CategoryRepository with Firestore, a single-threaded executor, and application context.
     *
     * @param context the Android context used to obtain the application context
     */
    private CategoryRepository(Context context) {
        this.firestore = FirebaseFirestore.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
        this.context = context.getApplicationContext();
    }

    /****
     * Returns the singleton instance of CategoryRepository, initializing it if necessary.
     *
     * @param context the application context used for initialization
     * @return the singleton CategoryRepository instance
     */
    public static synchronized CategoryRepository getInstance(Context context) {
        if (instance == null) {
            instance = new CategoryRepository(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Returns a LiveData object containing the current list of news categories.
     *
     * Observers can use this LiveData to receive updates when the category list changes.
     *
     * @return LiveData holding a list of Category objects
     */
    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    /**
     * Returns a LiveData object representing the current loading state for category data.
     *
     * @return LiveData that is true if categories are being loaded, false otherwise
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Returns a LiveData object containing the current error message related to category operations.
     *
     * Observers can use this to display error notifications or messages in the UI when category loading or updates fail.
     *
     * @return LiveData holding the latest error message, or null if no error has occurred.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Loads news categories from Firestore, falling back to local parsing or hardcoded data if necessary.
     *
     * If the device is offline, or Firestore retrieval fails, attempts to parse categories locally or use predefined categories as a last resort. Updates LiveData objects for categories, loading state, and error messages to reflect the current status.
     */
    public void loadCategories() {
        try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                // Fallback to local categories if no internet connection
                Log.w(TAG, "No internet connection, creating local categories");
                createLocalCategories();
                return;
            }
            
            isLoading.setValue(true);
            
            firestore.collection(Constants.COLLECTION_CATEGORIES)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        isLoading.setValue(false);
                        
                        if (queryDocumentSnapshots.isEmpty()) {
                            // Categories don't exist yet, create them
                            createCategories();
                        } else {
                            List<Category> categoryList = new ArrayList<>();
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                Category category = document.toObject(Category.class);
                                if (category != null) {
                                    categoryList.add(category);
                                }
                            }
                            categories.setValue(categoryList);
                        }
                    })
                    .addOnFailureListener(e -> {
                        isLoading.setValue(false);
                        errorMessage.setValue(e.getMessage());
                        Log.e(TAG, "loadCategories from Firestore failed: ", e);
                        
                        // Load from VnExpress directly if Firestore fails
                        createLocalCategories();
                    });
        } catch (Exception e) {
            isLoading.setValue(false);
            errorMessage.setValue("Failed to load categories: " + e.getMessage());
            Log.e(TAG, "loadCategories exception: ", e);
            
            // Fallback to local categories
            createLocalCategories();
        }
    }

    /****
     * Attempts to parse and load categories locally without using Firestore.
     * <p>
     * If local parsing fails, falls back to creating a predefined set of hardcoded categories.
     */
    private void createLocalCategories() {
        try {
            List<Category> categoryList = VnExpressParser.parseCategories();
            categories.postValue(categoryList);
            Log.d(TAG, "Created local categories: " + categoryList.size());
        } catch (Exception e) {
            Log.e(TAG, "createLocalCategories failed: ", e);
            // Last resort fallback - create hardcoded categories
            createHardcodedCategories();
        }
    }
    
    /**
     * Creates and posts a predefined list of hardcoded news categories as a fallback.
     *
     * If category creation fails, posts an error message to observers.
     */
    private void createHardcodedCategories() {
        try {
            List<Category> categoryList = new ArrayList<>();
            categoryList.add(new Category("thoi-su", "Thời sự", "Thời sự mới nhất", "📰"));
            categoryList.add(new Category("the-gioi", "Thế giới", "Tin thế giới mới nhất", "🌎"));
            categoryList.add(new Category("kinh-doanh", "Kinh doanh", "Tin kinh doanh mới nhất", "💼"));
            categoryList.add(new Category("giai-tri", "Giải trí", "Tin giải trí mới nhất", "🎬"));
            categoryList.add(new Category("the-thao", "Thể thao", "Tin thể thao mới nhất", "⚽"));
            categoryList.add(new Category("suc-khoe", "Sức khỏe", "Tin sức khỏe mới nhất", "🏥"));
            categories.postValue(categoryList);
            Log.d(TAG, "Created hardcoded categories: " + categoryList.size());
        } catch (Exception e) {
            Log.e(TAG, "createHardcodedCategories failed: ", e);
            errorMessage.postValue("Could not create categories");
        }
    }

    /**
     * Parses categories locally and saves them to Firestore asynchronously.
     *
     * If parsing or saving fails, falls back to creating categories using local data sources.
     */
    private void createCategories() {
        try {
            executorService.execute(() -> {
                try {
                    List<Category> categoryList = VnExpressParser.parseCategories();
                    categories.postValue(categoryList);
                    
                    // Save to Firestore
                    for (Category category : categoryList) {
                        firestore.collection(Constants.COLLECTION_CATEGORIES)
                                .document(category.getId())
                                .set(category)
                                .addOnFailureListener(e -> Log.e(TAG, "createCategories - saving to Firestore: ", e));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "createCategories in executor: ", e);
                    createLocalCategories();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "createCategories execution failed: ", e);
            createLocalCategories();
        }
    }

    /**
     * Updates the article count for a specific category in Firestore.
     *
     * @param categoryId the ID of the category to update
     * @param count the new article count to set for the category
     */
    public void updateCategoryArticleCount(String categoryId, int count) {
        firestore.collection(Constants.COLLECTION_CATEGORIES)
                .document(categoryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Category category = documentSnapshot.toObject(Category.class);
                        if (category != null) {
                            category.setArticleCount(count);
                            
                            firestore.collection(Constants.COLLECTION_CATEGORIES)
                                    .document(categoryId)
                                    .set(category)
                                    .addOnFailureListener(e -> Log.e(TAG, "updateCategoryArticleCount: ", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "updateCategoryArticleCount: ", e));
    }
} 