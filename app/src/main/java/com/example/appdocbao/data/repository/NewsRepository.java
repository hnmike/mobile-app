package com.example.appdocbao.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appdocbao.api.RetrofitClient;
import com.example.appdocbao.api.VnExpressParser;
import com.example.appdocbao.api.VnExpressService;
import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.data.model.Category;
import com.example.appdocbao.data.model.User;
import com.example.appdocbao.data.local.BookmarkDbHelper;
import com.example.appdocbao.utils.Constants;
import com.example.appdocbao.utils.NetworkUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsRepository {
    private static final String TAG = Constants.TAG_NEWS;

    private static NewsRepository instance;
    private final FirebaseFirestore firestore;
    private final VnExpressService vnExpressService;
    private final ExecutorService executorService;
    private final Context context;
    
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<Article>> articles = new MutableLiveData<>();
    private final MutableLiveData<List<Article>> bookmarkedArticles = new MutableLiveData<>();
    private final MutableLiveData<Article> selectedArticle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private NewsRepository(Context context) {
        this.firestore = FirebaseFirestore.getInstance();
        this.vnExpressService = RetrofitClient.getVnExpressService();
        this.executorService = Executors.newFixedThreadPool(4);
        this.context = context.getApplicationContext();
        
        // Initially load categories
        loadCategories();
    }

    public static synchronized NewsRepository getInstance(Context context) {
        if (instance == null) {
            instance = new NewsRepository(context.getApplicationContext());
        }
        return instance;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<List<Article>> getArticles() {
        return articles;
    }
    
    public LiveData<List<Article>> getBookmarkedArticles() {
        return bookmarkedArticles;
    }
    
    public LiveData<Article> getSelectedArticle() {
        return selectedArticle;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Load categories from Firestore (or create them if they don't exist)
    public void loadCategories() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            errorMessage.setValue("Không có kết nối internet");
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
                    Log.e(TAG, "loadCategories: ", e);
                    
                    // Load from VnExpress directly if Firestore fails
                    createCategories();
                });
    }

    // Create categories in Firestore
    private void createCategories() {
        executorService.execute(() -> {
            List<Category> categoryList = VnExpressParser.parseCategories();
            categories.postValue(categoryList);
            
            // Save to Firestore
            for (Category category : categoryList) {
                firestore.collection(Constants.COLLECTION_CATEGORIES)
                        .document(category.getId())
                        .set(category)
                        .addOnFailureListener(e -> Log.e(TAG, "createCategories: ", e));
            }
        });
    }

    // Load articles for a specific category from VnExpress
    public void loadArticlesByCategory(String categoryId) {
        try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                Log.w(TAG, "No internet connection when loading articles");
                errorMessage.setValue("Không có kết nối internet");
                // Fallback to cache
                loadArticlesFromCache(categoryId);
                return;
            }
            
            isLoading.setValue(true);
            Log.d(TAG, "Loading articles for category: " + categoryId);
            
            String url = VnExpressParser.BASE_URL + "/" + categoryId;
            vnExpressService.getHtmlContent(url).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    isLoading.postValue(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "Got successful response with body length: " + response.body().length());
                        executorService.execute(() -> {
                            try {
                                List<Article> articleList = VnExpressParser.parseArticlesByCategory(response.body(), categoryId);
                                Log.d(TAG, "Parsed articles: " + articleList.size());
                                
                                if (articleList.isEmpty()) {
                                    errorMessage.postValue("Không tìm thấy bài viết nào");
                                    loadArticlesFromCache(categoryId);
                                    return;
                                }
                                
                                articles.postValue(articleList);
                                
                                // Check if articles are bookmarked
                                checkBookmarkedStatus(articleList);
                                
                                // Cache articles in Firestore
                                cacheArticles(articleList);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing articles: " + e.getMessage(), e);
                                errorMessage.postValue("Lỗi khi đọc bài viết: " + e.getMessage());
                                loadArticlesFromCache(categoryId);
                            }
                        });
                    } else {
                        Log.e(TAG, "Error response: " + response.code() + " - " + response.message());
                        errorMessage.postValue("Không thể tải bài viết: " + response.message());
                        loadArticlesFromCache(categoryId);
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    isLoading.postValue(false);
                    errorMessage.postValue("Lỗi kết nối: " + t.getMessage());
                    Log.e(TAG, "loadArticlesByCategory network failure: ", t);
                    
                    // Try to load from cache
                    loadArticlesFromCache(categoryId);
                }
            });
        } catch (Exception e) {
            isLoading.postValue(false);
            errorMessage.postValue("Lỗi tải bài viết: " + e.getMessage());
            Log.e(TAG, "loadArticlesByCategory exception: ", e);
            // Fallback to cache
            loadArticlesFromCache(categoryId);
        }
    }

    // Load articles from Firestore cache
    private void loadArticlesFromCache(String categoryId) {
        firestore.collection(Constants.COLLECTION_ARTICLES)
                .whereEqualTo("categoryId", categoryId)
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Article> articleList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Article article = document.toObject(Article.class);
                        if (article != null) {
                            articleList.add(article);
                        }
                    }
                    
                    if (!articleList.isEmpty()) {
                        articles.setValue(articleList);
                        checkBookmarkedStatus(articleList);
                    } else {
                        errorMessage.setValue("Không có bài viết nào trong bộ nhớ cache");
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue(e.getMessage());
                    Log.e(TAG, "loadArticlesFromCache: ", e);
                });
    }

    // Load latest articles from VnExpress homepage
    public void loadLatestArticles() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            errorMessage.setValue("Không có kết nối internet");
            return;
        }
        
        isLoading.setValue(true);
        
        executorService.execute(() -> {
            try {
                List<Article> articleList = VnExpressParser.fetchLatestArticles();
                articles.postValue(articleList);
                isLoading.postValue(false);
                
                // Check if articles are bookmarked
                checkBookmarkedStatus(articleList);
                
                // Cache articles in Firestore
                cacheArticles(articleList);
            } catch (Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue(e.getMessage());
                Log.e(TAG, "loadLatestArticles: ", e);
            }
        });
    }

    // Load article detail from VnExpress
    public void loadArticleDetail(String articleId) {
        isLoading.setValue(true);
        Log.d(TAG, "Loading article detail for ID: " + articleId);
        
        try {
            // First check if we have the article in Firestore
            firestore.collection(Constants.COLLECTION_ARTICLES)
                    .document(articleId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        try {
                            Article article = documentSnapshot.toObject(Article.class);
                            if (article != null) {
                                Log.d(TAG, "Found article in Firestore: " + article.getTitle());
                                
                                // Important: Set the selectedArticle value immediately so UI has something to show
                                selectedArticle.setValue(article);
                                checkBookmarkStatusForArticle(article);
                                
                                // If we have network, fetch the full content from VnExpress
                                if (NetworkUtils.isNetworkAvailable(context) && article.getSourceUrl() != null) {
                                    Log.d(TAG, "Fetching full content from: " + article.getSourceUrl());
                                    fetchFullArticleContent(article);
                                } else {
                                    isLoading.setValue(false);
                                    if (!NetworkUtils.isNetworkAvailable(context)) {
                                        Log.w(TAG, "No internet connection, using cached article");
                                    }
                                }
                            } else {
                                isLoading.setValue(false);
                                errorMessage.setValue("Bài viết không tồn tại");
                                Log.e(TAG, "Article not found in Firestore: " + articleId);
                            }
                        } catch (Exception e) {
                            isLoading.setValue(false);
                            errorMessage.setValue("Lỗi khi xử lý bài viết");
                            Log.e(TAG, "Error processing article: " + e.getMessage(), e);
                        }
                    })
                    .addOnFailureListener(e -> {
                        isLoading.setValue(false);
                        errorMessage.setValue("Lỗi khi tải bài viết: " + e.getMessage());
                        Log.e(TAG, "loadArticleDetail Firestore error: ", e);
                    });
        } catch (Exception e) {
            isLoading.setValue(false);
            errorMessage.setValue("Lỗi không xác định: " + e.getMessage());
            Log.e(TAG, "Unexpected error in loadArticleDetail: ", e);
        }
    }
    
    // Fetch full article content from VnExpress
    private void fetchFullArticleContent(Article article) {
        try {
            String url = article.getSourceUrl();
            if (url == null || url.isEmpty()) {
                isLoading.setValue(false);
                Log.e(TAG, "Cannot fetch article: Empty source URL");
                return;
            }
            
            // Set a timeout for the request to prevent ANR
            vnExpressService.getHtmlContent(url).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        isLoading.postValue(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            String html = response.body();
                            Log.d(TAG, "Successfully fetched full article content, length: " + html.length());
                            
                            // Process in a background thread
                            executorService.execute(() -> {
                                try {
                                    // Use the category ID from the existing article
                                    String categoryId = article.getCategoryId();
                                    
                                    // Extract article content directly instead of using VnExpressParser
                                    Article fullArticle = extractArticleContent(html, article);
                                    
                                    if (fullArticle != null) {
                                        // Update article in Firestore and UI
                                        selectedArticle.postValue(fullArticle);
                                        cacheArticle(fullArticle);
                                    } else {
                                        Log.e(TAG, "Failed to parse full article content");
                                        // Keep using the original article
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing full article: " + e.getMessage(), e);
                                    // Do not propagate the exception
                                }
                            });
                        } else {
                            Log.e(TAG, "Error fetching full article: " + response.code() + " - " + response.message());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in onResponse: " + e.getMessage(), e);
                        // Do not propagate the exception
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    try {
                        isLoading.postValue(false);
                        Log.e(TAG, "Network error fetching article: " + t.getMessage(), t);
                    } catch (Exception e) {
                        Log.e(TAG, "Error in onFailure: " + e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            isLoading.setValue(false);
            Log.e(TAG, "Error initiating network request: " + e.getMessage(), e);
        }
    }
    
    // Extract article content directly without using VnExpressParser
    private Article extractArticleContent(String html, Article originalArticle) {
        try {
            if (html == null || html.isEmpty()) {
                return originalArticle;
            }
            
            // Create a copy of the original article to avoid modifying it directly
            Article article = new Article(
                    originalArticle.getId(),
                    originalArticle.getTitle(),
                    originalArticle.getContent(),
                    originalArticle.getImageUrl(),
                    originalArticle.getSourceUrl(),
                    originalArticle.getSourceName(),
                    originalArticle.getCategoryId(),
                    originalArticle.getCategoryName(),
                    originalArticle.getPublishedAt()
            );
            
            // Set the bookmarked status
            article.setBookmarked(originalArticle.isBookmarked());
            
            // Use Jsoup to parse the HTML
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html);
            
            // Extract more detailed content if possible
            org.jsoup.select.Elements contentElements = doc.select("article.fck_detail p.Normal");
            
            if (!contentElements.isEmpty()) {
                StringBuilder contentBuilder = new StringBuilder();
                for (org.jsoup.nodes.Element p : contentElements) {
                    contentBuilder.append(p.text()).append("\n\n");
                }
                String content = contentBuilder.toString().trim();
                
                if (!content.isEmpty()) {
                    Log.d(TAG, "Extracted full content with length: " + content.length());
                    article.setContent(content);
                }
            }
            
            // Extract or update the image URL
            org.jsoup.nodes.Element imgElement = doc.selectFirst("div.fig-picture img");
            if (imgElement != null) {
                String imgUrl = imgElement.attr("data-src");
                if (imgUrl.isEmpty()) {
                    imgUrl = imgElement.attr("src");
                }
                
                if (!imgUrl.isEmpty()) {
                    if (!imgUrl.startsWith("http")) {
                        imgUrl = "https:" + imgUrl;
                    }
                    Log.d(TAG, "Updated image URL: " + imgUrl);
                    article.setImageUrl(imgUrl);
                }
            }
            
            return article;
        } catch (Exception e) {
            Log.e(TAG, "Error extracting article content: " + e.getMessage(), e);
            return originalArticle; // Return the original article if extraction fails
        }
    }

    // Cache articles in Firestore
    private void cacheArticles(List<Article> articles) {
        for (Article article : articles) {
            cacheArticle(article);
        }
    }
    
    // Cache a single article in Firestore
    private void cacheArticle(Article article) {
        firestore.collection(Constants.COLLECTION_ARTICLES)
                .document(article.getId())
                .set(article)
                .addOnFailureListener(e -> Log.e(TAG, "cacheArticle: ", e));
    }

    // Load bookmarked articles for current user
    public void loadBookmarkedArticles() {
        isLoading.setValue(true);
        Log.d(TAG, "Loading bookmarked articles from SQLite");
        
        try {
            // Get all bookmarks from SQLite
            BookmarkDbHelper dbHelper = BookmarkDbHelper.getInstance(context);
            List<Article> bookmarkedArticlesList = dbHelper.getAllBookmarks();
            
            if (bookmarkedArticlesList.isEmpty()) {
                Log.d(TAG, "No bookmarked articles found in SQLite");
            } else {
                Log.d(TAG, "Found " + bookmarkedArticlesList.size() + " bookmarked articles in SQLite");
                for (Article article : bookmarkedArticlesList) {
                    Log.d(TAG, "Loaded bookmark: " + article.getId() + " - " + article.getTitle());
                }
            }
            
            // Update LiveData with the bookmarked articles
            bookmarkedArticles.setValue(bookmarkedArticlesList);
        } catch (Exception e) {
            Log.e(TAG, "Error loading bookmarked articles from SQLite: " + e.getMessage(), e);
            errorMessage.setValue("Lỗi khi tải bài viết đã lưu: " + e.getMessage());
        } finally {
            isLoading.setValue(false);
        }
    }

    // Bookmark/unbookmark an article
    public void toggleBookmark(Article article) {
        String userId = getCurrentUserId();
        if (userId == null) {
            errorMessage.setValue("Bạn cần đăng nhập để lưu bài viết");
            return;
        }
        
        isLoading.setValue(true);
        
        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            if (user.hasBookmarked(article.getId())) {
                                // Remove bookmark
                                user.removeBookmarkedArticle(article.getId());
                                article.setBookmarked(false);
                            } else {
                                // Add bookmark
                                user.addBookmarkedArticle(article.getId());
                                article.setBookmarked(true);
                            }
                            
                            // Update user in Firestore
                            firestore.collection(Constants.COLLECTION_USERS)
                                    .document(userId)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        isLoading.setValue(false);
                                        selectedArticle.setValue(article);
                                    })
                                    .addOnFailureListener(e -> {
                                        isLoading.setValue(false);
                                        errorMessage.setValue(e.getMessage());
                                        Log.e(TAG, "toggleBookmark: ", e);
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                    Log.e(TAG, "toggleBookmark: ", e);
                });
    }

    // Check if articles are bookmarked
    private void checkBookmarkedStatus(List<Article> articleList) {
        String userId = getCurrentUserId();
        if (userId == null) return;
        
        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getBookmarkedArticles() != null) {
                            List<String> bookmarkedIds = user.getBookmarkedArticles();
                            
                            for (Article article : articleList) {
                                article.setBookmarked(bookmarkedIds.contains(article.getId()));
                            }
                            
                            articles.setValue(articleList);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "checkBookmarkedStatus: ", e));
    }

    // Check if a single article is bookmarked
    private void checkBookmarkStatusForArticle(Article article) {
        if (article == null) return;
        
        try {
            BookmarkDbHelper dbHelper = BookmarkDbHelper.getInstance(context);
            boolean isBookmarked = dbHelper.isBookmarked(article.getId());
            
            article.setBookmarked(isBookmarked);
            selectedArticle.setValue(article);
        } catch (Exception e) {
            Log.e(TAG, "Error checking bookmark status: " + e.getMessage(), e);
        }
    }

    // Get current user ID
    private String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    // Add a bookmark using SQLite instead of Firestore
    public void addBookmark(String articleId) {
        isLoading.setValue(true);
        Log.d(TAG, "Adding bookmark for article: " + articleId);
        
        Article currentArticle = selectedArticle.getValue();
        if (currentArticle == null) {
            // Try to get article from cache first
            currentArticle = com.example.appdocbao.api.VnExpressParser.getArticleFromCache(articleId);
            
            if (currentArticle == null) {
                // If still not found, try to load from Firestore
                loadArticleDetail(articleId);
                isLoading.setValue(false);
                errorMessage.setValue("Không thể lưu bài viết lúc này, hãy thử lại");
                return;
            }
        }
        
        // Make sure we have a copy before modifying it
        Article articleToSave = new Article(
            currentArticle.getId(),
            currentArticle.getTitle(),
            currentArticle.getContent(),
            currentArticle.getImageUrl(),
            currentArticle.getSourceUrl(),
            currentArticle.getSourceName(),
            currentArticle.getCategoryId(),
            currentArticle.getCategoryName(),
            currentArticle.getPublishedAt()
        );
        
        articleToSave.setBookmarked(true);
        
        // Save to SQLite database
        BookmarkDbHelper dbHelper = BookmarkDbHelper.getInstance(context);
        boolean result = dbHelper.saveBookmark(articleToSave);
        
        if (result) {
            Log.d(TAG, "Successfully added bookmark to SQLite");
            // Update UI state
            if (currentArticle == selectedArticle.getValue()) {
                currentArticle.setBookmarked(true);
                selectedArticle.setValue(currentArticle);
            }
        } else {
            Log.e(TAG, "Failed to add bookmark to SQLite");
            errorMessage.setValue("Không thể lưu bài viết, hãy thử lại");
        }
        
        isLoading.setValue(false);
    }
    
    // Remove a bookmark using SQLite
    public void removeBookmark(String articleId) {
        isLoading.setValue(true);
        Log.d(TAG, "Removing bookmark for article: " + articleId);
        
        // Remove from SQLite database
        BookmarkDbHelper dbHelper = BookmarkDbHelper.getInstance(context);
        boolean result = dbHelper.removeBookmark(articleId);
        
        if (result) {
            Log.d(TAG, "Successfully removed bookmark from SQLite");
            // Update UI state if this is the currently viewed article
            Article currentArticle = selectedArticle.getValue();
            if (currentArticle != null && currentArticle.getId().equals(articleId)) {
                currentArticle.setBookmarked(false);
                selectedArticle.setValue(currentArticle);
            }
        } else {
            Log.e(TAG, "Failed to remove bookmark from SQLite");
            errorMessage.setValue("Không thể xóa bài viết đã lưu, hãy thử lại");
        }
        
        isLoading.setValue(false);
    }
    
    // Get bookmark status for the selected article
    public LiveData<Boolean> getIsArticleBookmarked() {
        MutableLiveData<Boolean> isBookmarked = new MutableLiveData<>(false);
        
        Article article = selectedArticle.getValue();
        if (article == null) {
            return isBookmarked;
        }
        
        try {
            BookmarkDbHelper dbHelper = BookmarkDbHelper.getInstance(context);
            boolean bookmarked = dbHelper.isBookmarked(article.getId());
            isBookmarked.setValue(bookmarked);
        } catch (Exception e) {
            Log.e(TAG, "Error getting bookmark status: " + e.getMessage(), e);
        }
        
        return isBookmarked;
    }

    /**
     * Toggle bookmark status of an article
     * @param articleId The ID of the article to toggle
     * @return The new bookmark state (true = bookmarked, false = not bookmarked)
     */
    public boolean toggleBookmarkSQLite(String articleId) {
        Article article = selectedArticle.getValue();
        if (article == null || !article.getId().equals(articleId)) {
            // Try to get from cache
            article = VnExpressParser.getArticleFromCache(articleId);
            
            if (article == null) {
                // Try to get from SQLite
                BookmarkDbHelper dbHelper = BookmarkDbHelper.getInstance(context);
                article = dbHelper.getBookmark(articleId);
                
                if (article == null) {
                    Log.e(TAG, "Cannot toggle bookmark - article not found: " + articleId);
                    errorMessage.setValue("Không thể thay đổi trạng thái lưu bài viết");
                    return false;
                }
            }
        }
        
        boolean isCurrentlyBookmarked = false;
        BookmarkDbHelper dbHelper = BookmarkDbHelper.getInstance(context);
        
        // Check current bookmark status
        isCurrentlyBookmarked = dbHelper.isBookmarked(articleId);
        
        boolean result;
        if (isCurrentlyBookmarked) {
            // Remove bookmark
            result = dbHelper.removeBookmark(articleId);
            if (result) {
                Log.d(TAG, "Successfully removed bookmark: " + articleId);
                if (article != null) {
                    article.setBookmarked(false);
                }
            } else {
                Log.e(TAG, "Failed to remove bookmark: " + articleId);
            }
        } else {
            // Add bookmark
            if (article != null) {
                article.setBookmarked(true);
                result = dbHelper.saveBookmark(article);
                if (result) {
                    Log.d(TAG, "Successfully added bookmark: " + articleId);
                } else {
                    Log.e(TAG, "Failed to add bookmark: " + articleId);
                    article.setBookmarked(false);
                }
            } else {
                Log.e(TAG, "Cannot bookmark null article");
                result = false;
            }
        }
        
        // Update the UI
        if (article != null && article.getId().equals(articleId)) {
            selectedArticle.setValue(article);
        }
        
        return !isCurrentlyBookmarked && result;
    }
} 