package com.example.appdocbao.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.data.repository.ArticleRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeViewModel extends AndroidViewModel {

    private final ArticleRepository articleRepository;
    
    private final LiveData<List<Article>> trendingArticles;
    private final LiveData<List<Article>> recentArticles;
    
    private final Map<String, MutableLiveData<List<Article>>> categoryArticlesMap = new HashMap<>();
    
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> errorMessage;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        
        articleRepository = ArticleRepository.getInstance(application);
        
        trendingArticles = articleRepository.getTrendingArticles();
        recentArticles = articleRepository.getRecentArticles();
        
        isLoading = articleRepository.getIsLoading();
        errorMessage = articleRepository.getErrorMessage();
        
        // Initialize category LiveData objects
        initializeCategories();
    }
    
    private void initializeCategories() {
        categoryArticlesMap.put("THOI_SU", new MutableLiveData<>());
        categoryArticlesMap.put("THE_GIOI", new MutableLiveData<>());
        categoryArticlesMap.put("KINH_DOANH", new MutableLiveData<>());
        categoryArticlesMap.put("GIAI_TRI", new MutableLiveData<>());
        categoryArticlesMap.put("THE_THAO", new MutableLiveData<>());
        categoryArticlesMap.put("PHAP_LUAT", new MutableLiveData<>());
        categoryArticlesMap.put("GIAO_DUC", new MutableLiveData<>());
        categoryArticlesMap.put("SUC_KHOE", new MutableLiveData<>());
        categoryArticlesMap.put("DOI_SONG", new MutableLiveData<>());
        categoryArticlesMap.put("DU_LICH", new MutableLiveData<>());
    }

    public LiveData<List<Article>> getTrendingArticles() {
        return trendingArticles;
    }

    public LiveData<List<Article>> getRecentArticles() {
        return recentArticles;
    }
    
    public LiveData<List<Article>> getCategoryArticles(String category) {
        return categoryArticlesMap.get(category);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadTrendingArticles() {
        articleRepository.loadTrendingArticles();
    }

    public void loadRecentArticles() {
        articleRepository.loadRecentArticles();
    }
    
    public void loadArticlesByCategory(String category) {
        String categoryUrl = "";
        
        switch (category) {
            case "THOI_SU":
                categoryUrl = ArticleRepository.CATEGORY_THOI_SU;
                break;
            case "THE_GIOI":
                categoryUrl = ArticleRepository.CATEGORY_THE_GIOI;
                break;
            case "KINH_DOANH":
                categoryUrl = ArticleRepository.CATEGORY_KINH_DOANH;
                break;
            case "GIAI_TRI":
                categoryUrl = ArticleRepository.CATEGORY_GIAI_TRI;
                break;
            case "THE_THAO":
                categoryUrl = ArticleRepository.CATEGORY_THE_THAO;
                break;
            case "PHAP_LUAT":
                categoryUrl = ArticleRepository.CATEGORY_PHAP_LUAT;
                break;
            case "GIAO_DUC":
                categoryUrl = ArticleRepository.CATEGORY_GIAO_DUC;
                break;
            case "SUC_KHOE":
                categoryUrl = ArticleRepository.CATEGORY_SUC_KHOE;
                break;
            case "DOI_SONG":
                categoryUrl = ArticleRepository.CATEGORY_DOI_SONG;
                break;
            case "DU_LICH":
                categoryUrl = ArticleRepository.CATEGORY_DU_LICH;
                break;
        }
        
        if (!categoryUrl.isEmpty() && categoryArticlesMap.containsKey(category)) {
            MutableLiveData<List<Article>> categoryArticles = categoryArticlesMap.get(category);
            articleRepository.loadArticlesByCategory(categoryUrl, categoryArticles);
        }
    }
    
    public void loadAllCategories() {
        for (String category : categoryArticlesMap.keySet()) {
            loadArticlesByCategory(category);
        }
    }
} 