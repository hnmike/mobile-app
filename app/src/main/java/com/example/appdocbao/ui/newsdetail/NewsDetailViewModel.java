package com.example.appdocbao.ui.newsdetail;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.data.repository.NewsRepository;
import com.example.appdocbao.api.VnExpressParser;
import com.example.appdocbao.api.VnExpressService;
import com.example.appdocbao.api.RetrofitClient;
import com.example.appdocbao.data.local.BookmarkDbHelper;

import java.util.Date;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsDetailViewModel extends AndroidViewModel {

    private static final String TAG = "NewsDetailViewModel";
    private final NewsRepository newsRepository;
    private final LiveData<Article> article;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> errorMessage;
    private final LiveData<Boolean> isBookmarked;
    private final VnExpressService vnExpressService;

    public NewsDetailViewModel(@NonNull Application application) {
        super(application);
        newsRepository = NewsRepository.getInstance(application);
        article = newsRepository.getSelectedArticle();
        isLoading = newsRepository.getIsLoading();
        errorMessage = newsRepository.getErrorMessage();
        isBookmarked = newsRepository.getIsArticleBookmarked();
        vnExpressService = RetrofitClient.getClient().create(VnExpressService.class);
    }

    public LiveData<Article> getArticle() {
        return article;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsBookmarked() {
        return isBookmarked;
    }

    public void loadArticleDetail(String articleId) {
        try {
            // Kiểm tra xem articleId có phải số nguyên không
            int numericId = -1;
            try {
                numericId = Integer.parseInt(articleId);
            } catch (NumberFormatException e) {
                // Không phải số nguyên, có thể là UUID từ Article trong Firestore
            }
            
            // Tạo bản sao final của numericId sớm
            final int finalNumericId = numericId;
            
            // Kiểm tra xem bài viết có trong cache không
            Article cachedArticle = com.example.appdocbao.api.VnExpressParser.getArticleFromCache(articleId);
            
            if (cachedArticle != null) {
                // Nếu có trong cache, sử dụng ngay
                Log.d(TAG, "Found article in cache: " + cachedArticle.getTitle());
                ((MutableLiveData<Article>)newsRepository.getSelectedArticle()).setValue(cachedArticle);
                ((MutableLiveData<Boolean>)newsRepository.getIsLoading()).setValue(false);
                
                // Kiểm tra bookmark status
                checkBookmarkStatus(cachedArticle.getId());
                
                // Vẫn thực hiện tải nội dung đầy đủ từ URL gốc để cập nhật cache nếu có thể
                if (cachedArticle.getSourceUrl() != null && !cachedArticle.getSourceUrl().isEmpty()) {
                    loadFullArticleContent(cachedArticle);
                }
                
                return;
            }
            
            if (numericId > 0) {
                // ID là số nguyên, khả năng cao là từ News object trong HomeActivity
                Log.d(TAG, "Loading article by numeric ID: " + numericId);
                
                // Sử dụng repository để tạo Article từ News khi hiện tại không có sẵn
                ((MutableLiveData<Boolean>)newsRepository.getIsLoading()).setValue(true);
                
                // Gọi API để tải thông tin bài viết từ URL tương ứng với ID danh mục
                // Giả định: ID danh mục của bài viết có thể được xác định từ ID bài viết
                // Ví dụ: articleId = 5 thuộc thể thao, articleId = 10 thuộc giải trí
                int categoryId = finalNumericId / 1000; // Lấy categoryId từ newsId
                if (categoryId == 0) {
                    categoryId = (finalNumericId % 10) + 1; // Fallback cho ID cũ
                }
                
                // Tạo bản sao final của categoryId
                final int finalCategoryId = categoryId;
                
                // Tạo URL để lấy trang chủ hoặc danh mục cụ thể
                String url = VnExpressParser.BASE_URL;
                if (finalCategoryId > 0 && finalCategoryId <= 8) {
                    url += "/" + getCategoryPath(finalCategoryId);
                }
                
                Log.d(TAG, "Fetching from URL: " + url + " for category: " + finalCategoryId);
                
                // Tạo bản sao final của biến url
                final String finalUrl = url;
                
                vnExpressService.getHtmlContent(url).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String html = response.body();
                            
                            // Tạo một Article mẫu để hiển thị tạm thời
                            Article tempArticle = new Article(
                                UUID.randomUUID().toString(),
                                "Đang tải nội dung...",
                                "Vui lòng đợi trong giây lát...",
                                "",
                                finalUrl,
                                "VnExpress",
                                String.valueOf(finalCategoryId),
                                getCategoryName(finalCategoryId),
                                new Date()
                            );
                            
                            ((MutableLiveData<Article>)newsRepository.getSelectedArticle()).setValue(tempArticle);
                            
                            // Phân tích trang để tìm bài viết chính xác
                            java.util.concurrent.Executor executor = java.util.concurrent.Executors.newSingleThreadExecutor();
                            
                            executor.execute(() -> {
                                try {
                                    VnExpressParser parser = new VnExpressParser();
                                    java.util.List<com.example.appdocbao.data.News> newsList = parser.parseNews(html, finalCategoryId);
                                    
                                    // Kiểm tra lại cache sau khi đã phân tích HTML
                                    Article refreshedCachedArticle = com.example.appdocbao.api.VnExpressParser.getArticleFromCache(articleId);
                                    
                                    if (refreshedCachedArticle != null) {
                                        // Nếu đã có trong cache (do parseNews đã thêm vào)
                                        ((MutableLiveData<Article>)newsRepository.getSelectedArticle()).postValue(refreshedCachedArticle);
                                        ((MutableLiveData<Boolean>)newsRepository.getIsLoading()).postValue(false);
                                        
                                        // Kiểm tra bookmark status
                                        checkBookmarkStatus(refreshedCachedArticle.getId());
                                        
                                        // Tải nội dung đầy đủ nếu có URL
                                        if (refreshedCachedArticle.getSourceUrl() != null && !refreshedCachedArticle.getSourceUrl().isEmpty()) {
                                            loadFullArticleContent(refreshedCachedArticle);
                                        }
                                        return;
                                    }
                                    
                                    // Tìm bài viết có ID tương ứng
                                    com.example.appdocbao.data.News targetNews = null;
                                    for (com.example.appdocbao.data.News news : newsList) {
                                        if (news.getId().equals(String.valueOf(finalNumericId))) {
                                            targetNews = news;
                                            break;
                                        }
                                    }
                                    
                                    if (targetNews != null) {
                                        // Chuyển đổi News sang Article
                                        Article foundArticle = new Article(
                                            String.valueOf(targetNews.getId()),
                                            targetNews.getTitle(),
                                            targetNews.getDescription(),
                                            targetNews.getImageUrl(),
                                            finalUrl,
                                            "VnExpress",
                                            String.valueOf(finalCategoryId),
                                            getCategoryName(finalCategoryId),
                                            new Date()
                                        );
                                        
                                        ((MutableLiveData<Article>)newsRepository.getSelectedArticle()).postValue(foundArticle);
                                        ((MutableLiveData<Boolean>)newsRepository.getIsLoading()).postValue(false);
                                        
                                        // Kiểm tra bookmark status
                                        checkBookmarkStatus(foundArticle.getId());
                                        
                                        // Thêm vào cache cho lần sau
                                        com.example.appdocbao.api.VnExpressParser.putArticleInCache(articleId, foundArticle);
                                    } else {
                                        ((MutableLiveData<String>)newsRepository.getErrorMessage()).postValue("Không tìm thấy bài viết với ID: " + finalNumericId);
                                        ((MutableLiveData<Boolean>)newsRepository.getIsLoading()).postValue(false);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing news for ID: " + finalNumericId, e);
                                    ((MutableLiveData<String>)newsRepository.getErrorMessage()).postValue("Lỗi khi tải bài viết: " + e.getMessage());
                                    ((MutableLiveData<Boolean>)newsRepository.getIsLoading()).postValue(false);
                                }
                            });
                        } else {
                            ((MutableLiveData<String>)newsRepository.getErrorMessage()).setValue("Không thể tải nội dung: " + response.message());
                            ((MutableLiveData<Boolean>)newsRepository.getIsLoading()).setValue(false);
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.e(TAG, "Network error: " + t.getMessage(), t);
                        ((MutableLiveData<String>)newsRepository.getErrorMessage()).setValue("Lỗi kết nối: " + t.getMessage());
                        ((MutableLiveData<Boolean>)newsRepository.getIsLoading()).setValue(false);
                    }
                });
            } else {
                // Gọi phương thức thông thường cho ID dạng UUID
                newsRepository.loadArticleDetail(articleId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadArticleDetail: " + e.getMessage(), e);
            ((MutableLiveData<String>)newsRepository.getErrorMessage()).setValue("Lỗi xử lý: " + e.getMessage());
            ((MutableLiveData<Boolean>)newsRepository.getIsLoading()).setValue(false);
        }
    }
    
    private void loadFullArticleContent(Article article) {
        try {
            String url = article.getSourceUrl();
            if (url != null && !url.isEmpty()) {
                vnExpressService.getHtmlContent(url).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String html = response.body();
                            
                            java.util.concurrent.Executor executor = java.util.concurrent.Executors.newSingleThreadExecutor();
                            executor.execute(() -> {
                                try {
                                    // Phân tích nội dung đầy đủ của bài viết
                                    Article fullArticle = VnExpressParser.parseArticleDetail(html, article.getCategoryId());
                                    
                                    if (fullArticle != null) {
                                        // Cập nhật thông tin để giữ ID ban đầu
                                        fullArticle.setId(article.getId());
                                        
                                        // Cập nhật UI và cache
                                        ((MutableLiveData<Article>)newsRepository.getSelectedArticle()).postValue(fullArticle);
                                        com.example.appdocbao.api.VnExpressParser.putArticleInCache(article.getId(), fullArticle);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing full article: " + e.getMessage(), e);
                                }
                            });
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.e(TAG, "Failed to load full article content: " + t.getMessage(), t);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading full article content: " + e.getMessage(), e);
        }
    }
    
    private String getCategoryPath(int categoryId) {
        switch (categoryId) {
            case 1: return "thoi-su";
            case 2: return "the-gioi";
            case 3: return "kinh-doanh";
            case 4: return "giai-tri";
            case 5: return "the-thao";
            case 6: return "phap-luat";
            case 7: return "giao-duc";
            case 8: return "suc-khoe";
            case 9: return "doi-song";
            case 10: return "du-lich";
            case 11: return "khoa-hoc";
            case 12: return "so-hoa";
            case 13: return "xe";
            case 14: return "y-kien";
            case 15: return "tam-su";
            default: return "";
        }
    }
    
    private String getCategoryName(int categoryId) {
        switch (categoryId) {
            case 1: return "Thời sự";
            case 2: return "Thế giới";
            case 3: return "Kinh doanh";
            case 4: return "Giải trí";
            case 5: return "Thể thao";
            case 6: return "Pháp luật";
            case 7: return "Giáo dục";
            case 8: return "Sức khỏe";
            case 9: return "Đời sống";
            case 10: return "Du lịch";
            case 11: return "Khoa học";
            case 12: return "Số hóa";
            case 13: return "Xe";
            case 14: return "Ý kiến";
            case 15: return "Tâm sự";
            default: return "Tin tức";
        }
    }

    public void addBookmark(String articleId) {
        newsRepository.addBookmark(articleId);
    }

    public void removeBookmark(String articleId) {
        newsRepository.removeBookmark(articleId);
    }

    public boolean toggleBookmark(String articleId) {
        return newsRepository.toggleBookmarkSQLite(articleId);
    }

    private void checkBookmarkStatus(String articleId) {
        try {
            // Sử dụng phương thức mới từ NewsRepository
            newsRepository.updateBookmarkStatus(articleId);
            Log.d(TAG, "Checking bookmark status for article: " + articleId);
        } catch (Exception e) {
            Log.e(TAG, "Error checking bookmark status: " + e.getMessage(), e);
            // Mặc định là false nếu có lỗi
            ((MutableLiveData<Boolean>)newsRepository.getIsArticleBookmarked()).postValue(false);
        }
    }

    // Check and update article information when internet is restored
    public void checkAndUpdateArticleInfo(String articleId) {
        try {
            // Check if this is a numeric ID (from HomeActivity)
            int numericId = -1;
            try {
                numericId = Integer.parseInt(articleId);
            } catch (NumberFormatException e) {
                // Not a numeric ID, skip
                return;
            }
            
            if (numericId > 0) {
                // Check if we have a minimal article (placeholder title)
                Article currentArticle = newsRepository.getSelectedArticle().getValue();
                if (currentArticle != null && 
                    currentArticle.getId().equals(articleId) && 
                    "Bài viết từ trang chủ".equals(currentArticle.getTitle())) {
                    
                    Log.d(TAG, "Found minimal article, updating with full info: " + articleId);
                    newsRepository.updateArticleInfoWhenOnline(articleId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking article info update: " + e.getMessage(), e);
        }
    }
} 