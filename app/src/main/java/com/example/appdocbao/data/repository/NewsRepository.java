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

    /****
     * Initializes the NewsRepository with Firestore, Retrofit service, executor service, and application context, and triggers initial category loading.
     *
     * @param context the application context used for network and resource access
     */
    private NewsRepository(Context context) {
        this.firestore = FirebaseFirestore.getInstance();
        this.vnExpressService = RetrofitClient.getVnExpressService();
        this.executorService = Executors.newFixedThreadPool(4);
        this.context = context.getApplicationContext();
        
        // Initially load categories
        loadCategories();
    }

    /**
     * Returns the singleton instance of NewsRepository, initializing it if necessary.
     *
     * @param context the application context used for initialization
     * @return the singleton NewsRepository instance
     */
    public static synchronized NewsRepository getInstance(Context context) {
        if (instance == null) {
            instance = new NewsRepository(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Returns a LiveData stream of news categories.
     *
     * The LiveData emits updates when the list of categories changes, allowing observers to reactively update the UI.
     *
     * @return LiveData containing the current list of news categories
     */
    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    /**
     * Returns a LiveData stream of the current list of articles.
     *
     * The list is updated when articles are loaded from the network or cache.
     *
     * @return LiveData containing the list of articles.
     */
    public LiveData<List<Article>> getArticles() {
        return articles;
    }
    
    /**
     * Returns a LiveData stream of articles bookmarked by the current user.
     *
     * The list is updated whenever the user's bookmarked articles change in Firestore.
     *
     * @return LiveData containing the list of bookmarked articles.
     */
    public LiveData<List<Article>> getBookmarkedArticles() {
        return bookmarkedArticles;
    }
    
    /**
     * Returns a LiveData stream representing the currently selected article.
     *
     * Observers can use this to receive updates when the selected article changes, including after loading article details or updating bookmark status.
     *
     * @return LiveData containing the selected Article, or null if none is selected
     */
    public LiveData<Article> getSelectedArticle() {
        return selectedArticle;
    }
    
    /**
     * Returns a LiveData indicating whether a data loading operation is currently in progress.
     *
     * @return LiveData that is true if loading is ongoing, false otherwise
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * Returns a LiveData stream containing the latest error message related to news data operations.
     *
     * @return LiveData emitting error messages for UI observation
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Loads news categories from Firestore and updates the observable categories list.
     *
     * If no categories are found in Firestore or the fetch fails, categories are parsed and created from the VnExpress source.
     * Sets an error message if the network is unavailable.
     */
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

    /****
     * Parses news categories from the VnExpress source and saves them to Firestore.
     *
     * Updates the categories LiveData with the parsed list and persists each category in the Firestore categories collection.
     */
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

    /**
     * Loads articles for the specified category from VnExpress and updates the articles LiveData.
     *
     * If the network is unavailable or an error occurs during fetching or parsing, attempts to load articles from the local Firestore cache. Updates loading and error state LiveData as appropriate.
     *
     * @param categoryId the identifier of the category to load articles for
     */
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
                /**
                 * Handles the Retrofit response for fetching articles by category, updating LiveData with parsed articles or error messages.
                 *
                 * On a successful response, parses the HTML to extract articles, updates the articles LiveData, checks bookmark status, and caches the articles. If parsing fails or the article list is empty, sets an error message and loads cached articles. On unsuccessful response, sets an error message and loads cached articles.
                 */
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

                /**
                 * Handles network failure when loading articles by category by updating loading and error states,
                 * logging the error, and attempting to load articles from the local cache.
                 *
                 * @param call the Retrofit call that failed
                 * @param t the throwable representing the failure
                 */
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

    /**
     * Loads articles for the specified category from the Firestore cache and updates the articles LiveData.
     *
     * If cached articles are found, updates the articles list and checks their bookmark status.
     * If no articles are found or an error occurs, sets an appropriate error message.
     *
     * @param categoryId the ID of the category for which to load cached articles
     */
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

    /**
     * Loads the latest articles from the VnExpress homepage, updates the articles LiveData, checks bookmark status, and caches the results.
     *
     * If there is no network connection, sets an error message and aborts the operation.
     */
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

    /**
     * Loads detailed information for a specific article by its ID.
     *
     * Retrieves the article from Firestore and updates the selected article LiveData. If the article exists and network is available, fetches the full content from the article's source URL. Updates loading and error state LiveData as appropriate.
     *
     * @param articleId the unique identifier of the article to load
     */
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
    
    /**
     * Retrieves the full content of an article from its source URL, parses the HTML to extract detailed content,
     * updates the selected article LiveData, and caches the updated article in Firestore.
     *
     * If the source URL is missing or invalid, the operation is aborted. Network and parsing errors are logged,
     * and the loading state is updated accordingly.
     *
     * @param article the article for which to fetch and update full content
     */
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
                /**
                 * Handles the Retrofit response for fetching full article content, parses the HTML in a background thread,
                 * updates the selected article LiveData, and caches the updated article if parsing succeeds.
                 *
                 * If the response is unsuccessful or parsing fails, logs the error and retains the original article.
                 */
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

                /**
                 * Handles network request failures when fetching article content, updating loading state and logging the error.
                 */
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
    
    /**
     * Parses the provided HTML to extract detailed article content and image URL, returning an updated copy of the original article.
     *
     * If extraction fails or the HTML is empty, returns the original article unchanged.
     *
     * @param html the HTML string containing the article's full content
     * @param originalArticle the original Article object to update
     * @return a new Article object with updated content and image URL if extraction succeeds; otherwise, the original article
     */
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

    /**
     * Caches a list of articles in Firestore by saving each article individually.
     *
     * @param articles the list of articles to cache
     */
    private void cacheArticles(List<Article> articles) {
        for (Article article : articles) {
            cacheArticle(article);
        }
    }
    
    /**
     * Saves the specified article to Firestore for caching.
     *
     * @param article the article to be cached
     */
    private void cacheArticle(Article article) {
        firestore.collection(Constants.COLLECTION_ARTICLES)
                .document(article.getId())
                .set(article)
                .addOnFailureListener(e -> Log.e(TAG, "cacheArticle: ", e));
    }

    /**
     * Loads the list of articles bookmarked by the currently authenticated user.
     *
     * If the user is not logged in, sets an error message. Otherwise, retrieves the user's bookmarked article IDs from Firestore and loads the corresponding articles. Updates the bookmarked articles LiveData or sets it to an empty list if none are found.
     */
    public void loadBookmarkedArticles() {
        String userId = getCurrentUserId();
        if (userId == null) {
            errorMessage.setValue("Bạn cần đăng nhập để xem bài viết đã lưu");
            return;
        }
        
        isLoading.setValue(true);
        
        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getBookmarkedArticles() != null && !user.getBookmarkedArticles().isEmpty()) {
                            List<String> bookmarkedIds = user.getBookmarkedArticles();
                            
                            // Load articles from Firestore by IDs
                            loadArticlesByIds(bookmarkedIds);
                        } else {
                            isLoading.setValue(false);
                            bookmarkedArticles.setValue(new ArrayList<>());
                        }
                    } else {
                        isLoading.setValue(false);
                        bookmarkedArticles.setValue(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                    Log.e(TAG, "loadBookmarkedArticles: ", e);
                });
    }

    /****
     * Loads articles from Firestore based on a list of article IDs and updates the bookmarked articles LiveData.
     *
     * Each retrieved article is marked as bookmarked. When all articles have been processed, the loading state is updated and the bookmarked articles list is set.
     */
    private void loadArticlesByIds(List<String> articleIds) {
        List<Article> articles = new ArrayList<>();
        final int[] remaining = {articleIds.size()};
        
        for (String articleId : articleIds) {
            firestore.collection(Constants.COLLECTION_ARTICLES)
                    .document(articleId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        remaining[0]--;
                        
                        if (documentSnapshot.exists()) {
                            Article article = documentSnapshot.toObject(Article.class);
                            if (article != null) {
                                article.setBookmarked(true);
                                articles.add(article);
                            }
                        }
                        
                        if (remaining[0] == 0) {
                            isLoading.setValue(false);
                            bookmarkedArticles.setValue(articles);
                        }
                    })
                    .addOnFailureListener(e -> {
                        remaining[0]--;
                        
                        Log.e(TAG, "loadArticlesByIds: ", e);
                        
                        if (remaining[0] == 0) {
                            isLoading.setValue(false);
                            bookmarkedArticles.setValue(articles);
                        }
                    });
        }
    }

    /**
     * Toggles the bookmark status of the specified article for the current user.
     *
     * If the user is not logged in, sets an error message. Otherwise, adds or removes the article from the user's bookmarked articles in Firestore and updates the article's bookmark status accordingly.
     *
     * @param article the article whose bookmark status is to be toggled
     */
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

    /**
     * Updates the bookmarked status of each article in the provided list based on the current user's bookmarked articles.
     *
     * If the user is not logged in, the method returns without making changes.
     * The articles' bookmarked flags are updated and the articles LiveData is refreshed accordingly.
     */
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

    /**
     * Updates the bookmark status of the given article for the current user and posts the result to selectedArticle LiveData.
     *
     * If the user is not logged in, the method returns without making changes.
     */
    private void checkBookmarkStatusForArticle(Article article) {
        String userId = getCurrentUserId();
        if (userId == null) return;
        
        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getBookmarkedArticles() != null) {
                            article.setBookmarked(user.hasBookmarked(article.getId()));
                            selectedArticle.setValue(article);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "checkBookmarkStatusForArticle: ", e));
    }

    /****
     * Retrieves the unique identifier of the currently authenticated user.
     *
     * @return the user ID if a user is logged in; otherwise, null
     */
    private String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    /****
     * Adds the specified article to the current user's list of bookmarked articles in Firestore.
     *
     * If the user is not logged in, sets an error message. Updates the selected article's bookmark status upon success.
     *
     * @param articleId the ID of the article to bookmark
     */
    public void addBookmark(String articleId) {
        String userId = getCurrentUserId();
        if (userId == null) {
            errorMessage.setValue("Bạn cần đăng nhập để lưu bài viết");
            return;
        }

        isLoading.setValue(true);

        // Add article ID to user's bookmarked articles
        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update("bookmarkedArticles", com.google.firebase.firestore.FieldValue.arrayUnion(articleId))
                .addOnSuccessListener(aVoid -> {
                    isLoading.setValue(false);
                    // Update local bookmark state
                    Article article = selectedArticle.getValue();
                    if (article != null) {
                        article.setBookmarked(true);
                        selectedArticle.setValue(article);
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                    Log.e(TAG, "addBookmark: ", e);
                });
    }

    /**
     * Removes the specified article from the current user's list of bookmarked articles.
     *
     * If the user is not logged in, sets an error message. Updates the bookmark status of the selected article upon success.
     *
     * @param articleId the ID of the article to remove from bookmarks
     */
    public void removeBookmark(String articleId) {
        String userId = getCurrentUserId();
        if (userId == null) {
            errorMessage.setValue("Bạn cần đăng nhập để thực hiện thao tác này");
            return;
        }

        isLoading.setValue(true);

        // Remove article ID from user's bookmarked articles
        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update("bookmarkedArticles", com.google.firebase.firestore.FieldValue.arrayRemove(articleId))
                .addOnSuccessListener(aVoid -> {
                    isLoading.setValue(false);
                    // Update local bookmark state
                    Article article = selectedArticle.getValue();
                    if (article != null) {
                        article.setBookmarked(false);
                        selectedArticle.setValue(article);
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                    Log.e(TAG, "removeBookmark: ", e);
                });
    }

    /**
     * Returns a LiveData indicating whether the currently selected article is bookmarked by the current user.
     *
     * The result is updated asynchronously based on the user's bookmarked articles stored in Firestore.
     *
     * @return LiveData<Boolean> representing the bookmark status of the selected article for the current user
     */
    public LiveData<Boolean> getIsArticleBookmarked() {
        MutableLiveData<Boolean> isBookmarked = new MutableLiveData<>(false);
        
        String userId = getCurrentUserId();
        if (userId == null) {
            return isBookmarked;
        }
        
        Article article = selectedArticle.getValue();
        if (article == null) {
            return isBookmarked;
        }
        
        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null && user.getBookmarkedArticles() != null) {
                        isBookmarked.setValue(user.getBookmarkedArticles().contains(article.getId()));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "getIsArticleBookmarked: ", e));
        
        return isBookmarked;
    }
} 