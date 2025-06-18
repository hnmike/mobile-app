package com.example.appdocbao.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appdocbao.api.RetrofitClient;
import com.example.appdocbao.api.VnExpressParser;
import com.example.appdocbao.api.VnExpressService;
import com.example.appdocbao.data.News;
import com.example.appdocbao.data.model.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends AndroidViewModel {

    private static final String TAG = "HomeViewModel";

    // LiveData for UI updates
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Map<Integer, List<News>>> categoryNewsMap = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Services and utilities
    private final VnExpressService vnExpressService;
    private final VnExpressParser vnExpressParser;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        vnExpressService = RetrofitClient.getClient().create(VnExpressService.class);
        vnExpressParser = new VnExpressParser();
    }

    // Getters for LiveData
    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Map<Integer, List<News>>> getCategoryNewsMap() {
        return categoryNewsMap;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Load all data from API including categories and news for each category
     */
    public void loadDataFromApi() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // Create categories first (including featured category)
        List<Category> allCategories = createCategories();
        categories.setValue(allCategories);

        // Initialize category news map
        Map<Integer, List<News>> newsMap = new HashMap<>();
        categoryNewsMap.setValue(newsMap);

        // Load news for each category
        loadCategoriesNewsFromApi(allCategories, newsMap);
    }

    /**
     * Load news for all categories
     */
    private void loadCategoriesNewsFromApi(List<Category> allCategories, Map<Integer, List<News>> categoryNewsMap) {
        final int totalCategories = allCategories.size();
        final int[] processedCategoriesCount = {0};
        
        for (Category category : allCategories) {
            try {
                int categoryId = Integer.parseInt(category.getId());
                String url = getCategoryUrl(categoryId);
                loadNewsForCategory(url, categoryId, category, categoryNewsMap, processedCategoriesCount, totalCategories);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid category ID: " + category.getId());
                processedCategoriesCount[0]++;
                checkAndUpdateUIIfComplete(categoryNewsMap, processedCategoriesCount[0], totalCategories);
            }
        }
    }

    /**
     * Load news for a specific category
     */
    private void loadNewsForCategory(String url,
                                   int categoryId,
                                   Category category,
                                   Map<Integer, List<News>> categoryNewsMap,
                                   final int[] processedCategoriesCount,
                                   final int totalCategories) {

        vnExpressService.getHtmlContent(url).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String html = response.body();
                    
                    executor.execute(() -> {
                        try {
                            List<News> newsList = vnExpressParser.parseNews(html, categoryId);
                            
                            // Update the map on main thread
                            categoryNewsMap.put(categoryId, newsList);
                            HomeViewModel.this.categoryNewsMap.postValue(new HashMap<>(categoryNewsMap));
                            
                            // Log success
                            Log.d(TAG, "Loaded " + newsList.size() + " news for category: " + category.getName());
                            
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing news for category " + category.getName() + ": " + e.getMessage(), e);
                            errorMessage.postValue("Lỗi khi tải tin tức cho " + category.getName());
                        }
                        
                        // Check if all categories are processed
                        processedCategoriesCount[0]++;
                        checkAndUpdateUIIfComplete(categoryNewsMap, processedCategoriesCount[0], totalCategories);
                    });
                } else {
                    Log.e(TAG, "Error response for category " + category.getName() + ": " + response.code());
                    errorMessage.postValue("Lỗi khi tải tin tức cho " + category.getName());
                    
                    processedCategoriesCount[0]++;
                    checkAndUpdateUIIfComplete(categoryNewsMap, processedCategoriesCount[0], totalCategories);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error for category " + category.getName() + ": " + t.getMessage(), t);
                errorMessage.postValue("Lỗi kết nối khi tải tin tức cho " + category.getName());
                
                processedCategoriesCount[0]++;
                checkAndUpdateUIIfComplete(categoryNewsMap, processedCategoriesCount[0], totalCategories);
            }
        });
    }

    /**
     * Check if all categories are processed and update UI accordingly
     */
    private void checkAndUpdateUIIfComplete(Map<Integer, List<News>> categoryNewsMap,
                                          int processedCount, int totalCount) {
        if (processedCount >= totalCount) {
            isLoading.postValue(false);
            
            // Log statistics
            logCategoryNewsStats(categories.getValue(), categoryNewsMap);
        }
    }

    /**
     * Log statistics about loaded news
     */
    private void logCategoryNewsStats(List<Category> categoriesToListLog, Map<Integer, List<News>> categoryNewsMap) {
        if (categoriesToListLog == null || categoryNewsMap == null) return;
        
        Log.d(TAG, "=== CATEGORY NEWS STATISTICS ===");
        for (Category category : categoriesToListLog) {
            List<News> newsList = categoryNewsMap.get(category.getId());
            int newsCount = newsList != null ? newsList.size() : 0;
            Log.d(TAG, category.getName() + ": " + newsCount + " articles");
        }
        Log.d(TAG, "=== END STATISTICS ===");
    }

    /**
     * Get URL for a specific category
     */
    private String getCategoryUrl(int categoryId) {
        String baseUrl = "https://vnexpress.net";
        
        switch (categoryId) {
            case 0: return baseUrl + "/tin-tuc-24h"; // Featured articles
            case 1: return baseUrl + "/thoi-su";
            case 2: return baseUrl + "/the-gioi";
            case 3: return baseUrl + "/kinh-doanh";
            case 4: return baseUrl + "/giai-tri";
            case 5: return baseUrl + "/the-thao";
            case 6: return baseUrl + "/phap-luat";
            case 7: return baseUrl + "/giao-duc";
            case 8: return baseUrl + "/suc-khoe";
            case 9: return baseUrl + "/doi-song";
            case 10: return baseUrl + "/du-lich";
            case 11: return baseUrl + "/khoa-hoc";
            case 12: return baseUrl + "/so-hoa";
            case 13: return baseUrl + "/xe";
            case 14: return baseUrl + "/y-kien";
            case 15: return baseUrl + "/tam-su";
            default: return baseUrl;
        }
    }

    /**
     * Create list of categories
     */
    private List<Category> createCategories() {
        List<Category> categoryList = new ArrayList<>();
        
        // Add featured articles category first
        categoryList.add(new Category("0", "Bài viết nổi bật", "Các bài viết nổi bật trên hệ thống", "🔥"));
        
        // Add regular categories
        categoryList.add(new Category("1", "Thời sự", "Tin tức thời sự trong nước", "📰"));
        categoryList.add(new Category("2", "Thế giới", "Tin tức quốc tế", "🌎"));
        categoryList.add(new Category("3", "Kinh doanh", "Tin tức kinh tế, tài chính", "💼"));
        categoryList.add(new Category("4", "Giải trí", "Tin tức giải trí, showbiz", "🎭"));
        categoryList.add(new Category("5", "Thể thao", "Tin tức thể thao", "⚽"));
        categoryList.add(new Category("6", "Pháp luật", "Tin tức pháp luật", "⚖️"));
        categoryList.add(new Category("7", "Giáo dục", "Tin tức giáo dục", "🎓"));
        categoryList.add(new Category("8", "Sức khỏe", "Tin tức y tế, sức khỏe", "🏥"));
        categoryList.add(new Category("9", "Đời sống", "Tin tức đời sống", "🏠"));
        categoryList.add(new Category("10", "Du lịch", "Tin tức du lịch", "✈️"));
        categoryList.add(new Category("11", "Khoa học", "Tin tức khoa học", "🔬"));
        categoryList.add(new Category("12", "Số hóa", "Tin tức công nghệ", "💻"));
        
        return categoryList;
    }

    /**
     * Refresh data
     */
    public void refreshData() {
        loadDataFromApi();
    }
}
