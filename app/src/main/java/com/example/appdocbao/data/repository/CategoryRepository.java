package com.example.appdocbao.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appdocbao.api.VnExpressParser;
import com.example.appdocbao.data.model.Category;
import com.example.appdocbao.utils.Constants;
import com.example.appdocbao.utils.NetworkUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {

    private static CategoryRepository instance;
    private final FirebaseFirestore firestore;
    private final ExecutorService executorService;
    private final Context context;

    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private CategoryRepository(Context context) {
        this.firestore = FirebaseFirestore.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
        this.context = context.getApplicationContext();
    }

    public static synchronized CategoryRepository getInstance(Context context) {
        if (instance == null) {
            instance = new CategoryRepository(context.getApplicationContext());
        }
        return instance;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadCategories() {
        if (NetworkUtils.isNetworkAvailable(context)) {
            loadCategoriesFromFirestore();
        } else {
            createLocalCategories();
        }
    }

    private void loadCategoriesFromFirestore() {
        isLoading.setValue(true);

        firestore.collection(Constants.COLLECTION_CATEGORIES)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    isLoading.setValue(false);

                    if (querySnapshot.isEmpty()) {
                        createCategories();
                    } else {
                        List<Category> categoryList = new ArrayList<>();
                        querySnapshot.forEach(doc -> {
                            Category category = doc.toObject(Category.class);
                            if (category != null) {
                                categoryList.add(category);
                            }
                        });
                        categories.setValue(categoryList);
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    createLocalCategories();
                });
    }

    private void createCategories() {
        executorService.execute(() -> {
            List<Category> categoryList = parseCategories();
            categories.postValue(categoryList);
            saveCategoriesAsync(categoryList);
        });
    }

    private void createLocalCategories() {
        executorService.execute(() -> {
            List<Category> categoryList = parseCategories();
            categories.postValue(categoryList);
        });
    }

    private List<Category> parseCategories() {
        try {
            return VnExpressParser.parseCategories();
        } catch (Exception e) {
            return createHardcodedCategories();
        }
    }

    private List<Category> createHardcodedCategories() {
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category("thoi-su", "Thời sự", "Thời sự mới nhất", "📰"));
        categoryList.add(new Category("the-gioi", "Thế giới", "Tin thế giới mới nhất", "🌎"));
        categoryList.add(new Category("kinh-doanh", "Kinh doanh", "Tin kinh doanh mới nhất", "💼"));
        categoryList.add(new Category("giai-tri", "Giải trí", "Tin giải trí mới nhất", "🎬"));
        categoryList.add(new Category("the-thao", "Thể thao", "Tin thể thao mới nhất", "⚽"));
        categoryList.add(new Category("suc-khoe", "Sức khỏe", "Tin sức khỏe mới nhất", "🏥"));
        return categoryList;
    }

    private void saveCategoriesAsync(List<Category> categoryList) {
        categoryList.forEach(category ->
                firestore.collection(Constants.COLLECTION_CATEGORIES)
                        .document(category.getId())
                        .set(category)
        );
    }
}