package com.example.appdocbao.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.utils.FirebaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ArticleRepository {
    private static final String TAG = "ArticleRepository";
    private static ArticleRepository instance;
    
    private final MutableLiveData<List<Article>> trendingArticles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Article>> recentArticles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Context context;
    
    private ArticleRepository(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public static synchronized ArticleRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ArticleRepository(context);
        }
        return instance;
    }
    
    public LiveData<List<Article>> getTrendingArticles() {
        return trendingArticles;
    }
    
    public LiveData<List<Article>> getRecentArticles() {
        return recentArticles;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public void loadTrendingArticles() {
        isLoading.setValue(true);
        
        // First check Firebase for cached trending articles
        DatabaseReference articlesRef = FirebaseManager.getReference("trending_articles");
        articlesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Article> articles = new ArrayList<>();
                
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    // Data exists in Firebase, use it
                    for (DataSnapshot articleSnapshot : dataSnapshot.getChildren()) {
                        Article article = articleSnapshot.getValue(Article.class);
                        if (article != null) {
                            articles.add(article);
                        }
                    }
                    trendingArticles.setValue(articles);
                    isLoading.setValue(false);
                } else {
                    // No data in Firebase, fetch from web
                    fetchTrendingArticlesFromWeb();
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading trending articles from Firebase: " + databaseError.getMessage());
                // Fallback to web fetching
                fetchTrendingArticlesFromWeb();
            }
        });
    }
    
    private void fetchTrendingArticlesFromWeb() {
        executor.execute(() -> {
            try {
                List<Article> articles = new ArrayList<>();
                
                // Use Jsoup to fetch and parse articles from VnExpress
                Document doc = Jsoup.connect("https://vnexpress.net/").get();
                
                // Get trending articles from the featured section
                Elements featuredArticles = doc.select("article.item-news-common");
                
                // If we can't find any articles with the usual selector, try alternative selectors
                if (featuredArticles.isEmpty()) {
                    featuredArticles = doc.select("article.item-news");
                }
                
                // If we still can't find any articles, try more generic selectors
                if (featuredArticles.isEmpty()) {
                    featuredArticles = doc.select("div.width_common > article");
                }
                
                Log.d(TAG, "Found " + featuredArticles.size() + " trending articles");
                
                for (int i = 0; i < Math.min(featuredArticles.size(), 10); i++) {
                    Element article = featuredArticles.get(i);
                    
                    String title = article.select("h3.title-news > a, h2.title-news > a, h3.title > a").text();
                    String url = article.select("h3.title-news > a, h2.title-news > a, h3.title > a").attr("href");
                    String imageUrl = article.select("div.thumb-art img, picture.pic img").attr("data-src");
                    if (imageUrl.isEmpty()) {
                        imageUrl = article.select("div.thumb-art img, picture.pic img").attr("src");
                    }
                    String summary = article.select("p.description > a, p.description, p.desc").text();
                    String categoryText = article.select("span.cat-name, span.category").text().toUpperCase();
                    if (categoryText.isEmpty()) {
                        categoryText = "NEWS";
                    }
                    
                    // Skip articles with no title or URL
                    if (title.isEmpty() || url.isEmpty()) {
                        continue;
                    }
                    
                    Article newArticle = new Article();
                    newArticle.setId(String.valueOf(System.currentTimeMillis() + i));
                    newArticle.setTitle(title);
                    newArticle.setSummary(summary);
                    newArticle.setImageUrl(imageUrl);
                    newArticle.setUrl(url);
                    newArticle.setCategoryText(categoryText);
                    newArticle.setPublishedTime(new Date());
                    newArticle.setViewCount(0);
                    
                    articles.add(newArticle);
                }
                
                // Save to Firebase for caching
                DatabaseReference articlesRef = FirebaseManager.getReference("trending_articles");
                for (int i = 0; i < articles.size(); i++) {
                    articlesRef.child(String.valueOf(i)).setValue(articles.get(i));
                }
                
                trendingArticles.postValue(articles);
                isLoading.postValue(false);
                
            } catch (IOException e) {
                Log.e(TAG, "Error fetching trending articles: " + e.getMessage());
                errorMessage.postValue("Error loading trending articles: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    public void loadRecentArticles() {
        isLoading.setValue(true);
        
        // First check Firebase for cached recent articles
        DatabaseReference articlesRef = FirebaseManager.getReference("recent_articles");
        articlesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Article> articles = new ArrayList<>();
                
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    // Data exists in Firebase, use it
                    for (DataSnapshot articleSnapshot : dataSnapshot.getChildren()) {
                        Article article = articleSnapshot.getValue(Article.class);
                        if (article != null) {
                            articles.add(article);
                        }
                    }
                    recentArticles.setValue(articles);
                    isLoading.setValue(false);
                } else {
                    // No data in Firebase, fetch from web
                    fetchRecentArticlesFromWeb();
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading recent articles from Firebase: " + databaseError.getMessage());
                // Fallback to web fetching
                fetchRecentArticlesFromWeb();
            }
        });
    }
    
    private void fetchRecentArticlesFromWeb() {
        executor.execute(() -> {
            try {
                List<Article> articles = new ArrayList<>();
                
                // Use Jsoup to fetch and parse articles from VnExpress
                // Change URL to main page since it has recent articles too
                Document doc = Jsoup.connect("https://vnexpress.net/").get();
                
                // Try different selectors to find recent articles
                Elements newsArticles = doc.select(".list-news-subfolder article, .container article");
                
                // If we can't find any articles with the new selector, try the existing selectors
                if (newsArticles.isEmpty()) {
                    // Try original URL
                    doc = Jsoup.connect("https://vnexpress.net/tin-moi-nhat").get();
                    newsArticles = doc.select("article.item-news");
                    
                    if (newsArticles.isEmpty()) {
                        newsArticles = doc.select("article.item-news-common");
                    }
                    
                    if (newsArticles.isEmpty()) {
                        newsArticles = doc.select("div.width_common > article");
                    }
                }
                
                Log.d(TAG, "Found " + newsArticles.size() + " recent articles");
                
                for (int i = 0; i < Math.min(newsArticles.size(), 15); i++) {
                    Element article = newsArticles.get(i);
                    
                    String title = article.select("h3.title-news > a, h2.title-news > a, h3.title > a, .title a").text();
                    String url = article.select("h3.title-news > a, h2.title-news > a, h3.title > a, .title a").attr("href");
                    String imageUrl = article.select("div.thumb-art img, picture.pic img, .thumb-art img").attr("data-src");
                    if (imageUrl.isEmpty()) {
                        imageUrl = article.select("div.thumb-art img, picture.pic img, .thumb-art img").attr("src");
                    }
                    String summary = article.select("p.description > a, p.description, p.desc, .description, .summary").text();
                    String categoryText = article.select("span.cat-name, span.category, .category").text().toUpperCase();
                    if (categoryText.isEmpty()) {
                        categoryText = "NEWS";
                    }
                    
                    // Skip articles with no title or URL
                    if (title.isEmpty() || url.isEmpty()) {
                        continue;
                    }
                    
                    Article newArticle = new Article();
                    newArticle.setId(String.valueOf(System.currentTimeMillis() + i));
                    newArticle.setTitle(title);
                    newArticle.setSummary(summary);
                    newArticle.setImageUrl(imageUrl);
                    newArticle.setUrl(url);
                    newArticle.setCategoryText(categoryText);
                    newArticle.setPublishedTime(new Date());
                    newArticle.setViewCount(0);
                    
                    articles.add(newArticle);
                }
                
                // Save to Firebase for caching
                DatabaseReference articlesRef = FirebaseManager.getReference("recent_articles");
                for (int i = 0; i < articles.size(); i++) {
                    articlesRef.child(String.valueOf(i)).setValue(articles.get(i));
                }
                
                recentArticles.postValue(articles);
                isLoading.postValue(false);
                
            } catch (IOException e) {
                Log.e(TAG, "Error fetching recent articles: " + e.getMessage());
                errorMessage.postValue("Error loading recent articles: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    public void searchArticles(String query, MutableLiveData<List<Article>> searchResults) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                List<Article> articles = new ArrayList<>();
                
                // Use Jsoup to search articles from VnExpress
                String searchUrl = "https://timkiem.vnexpress.net/?q=" + query;
                Document doc = Jsoup.connect(searchUrl).get();
                
                // Get search results
                Elements searchArticles = doc.select("article.item-news");
                
                for (int i = 0; i < Math.min(searchArticles.size(), 20); i++) {
                    Element article = searchArticles.get(i);
                    
                    String title = article.select("h3.title-news > a").text();
                    String url = article.select("h3.title-news > a").attr("href");
                    String imageUrl = article.select("div.thumb-art img").attr("data-src");
                    if (imageUrl.isEmpty()) {
                        imageUrl = article.select("div.thumb-art img").attr("src");
                    }
                    String summary = article.select("p.description > a").text();
                    String categoryText = article.select("span.cat-name").text().toUpperCase();
                    if (categoryText.isEmpty()) {
                        categoryText = "NEWS";
                    }
                    
                    Article newArticle = new Article();
                    newArticle.setId(String.valueOf(System.currentTimeMillis() + i));
                    newArticle.setTitle(title);
                    newArticle.setSummary(summary);
                    newArticle.setImageUrl(imageUrl);
                    newArticle.setUrl(url);
                    newArticle.setCategoryText(categoryText);
                    newArticle.setPublishedTime(new Date());
                    newArticle.setViewCount(0);
                    
                    articles.add(newArticle);
                }
                
                searchResults.postValue(articles);
                isLoading.postValue(false);
                
            } catch (IOException e) {
                Log.e(TAG, "Error searching articles: " + e.getMessage());
                errorMessage.postValue("Error searching articles: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    public void loadArticlesByCategory(String categoryUrl, MutableLiveData<List<Article>> categoryArticles) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                List<Article> articles = new ArrayList<>();
                
                // Use Jsoup to fetch and parse articles from the category URL
                Document doc = Jsoup.connect(categoryUrl).get();
                
                // Get articles from the category page
                Elements categoryNewsArticles = doc.select("article.item-news, article.item-news-common");
                
                // If we still can't find any articles, try a more generic selector
                if (categoryNewsArticles.isEmpty()) {
                    categoryNewsArticles = doc.select("div.width_common > article");
                }
                
                Log.d(TAG, "Found " + categoryNewsArticles.size() + " articles in category: " + categoryUrl);
                
                for (int i = 0; i < Math.min(categoryNewsArticles.size(), 10); i++) {
                    Element article = categoryNewsArticles.get(i);
                    
                    String title = article.select("h3.title-news > a, h2.title-news > a, h3.title > a").text();
                    String url = article.select("h3.title-news > a, h2.title-news > a, h3.title > a").attr("href");
                    String imageUrl = article.select("div.thumb-art img, picture.pic img").attr("data-src");
                    if (imageUrl.isEmpty()) {
                        imageUrl = article.select("div.thumb-art img, picture.pic img").attr("src");
                    }
                    String summary = article.select("p.description > a, p.description, p.desc").text();
                    String categoryText = article.select("span.cat-name, span.category").text().toUpperCase();
                    if (categoryText.isEmpty()) {
                        // Extract category from URL
                        String[] parts = categoryUrl.split("/");
                        if (parts.length > 3) {
                            categoryText = parts[3].toUpperCase();
                        } else {
                            categoryText = "NEWS";
                        }
                    }
                    
                    // Skip articles with no title or URL
                    if (title.isEmpty() || url.isEmpty()) {
                        continue;
                    }
                    
                    Article newArticle = new Article();
                    newArticle.setId(String.valueOf(System.currentTimeMillis() + i));
                    newArticle.setTitle(title);
                    newArticle.setSummary(summary);
                    newArticle.setImageUrl(imageUrl);
                    newArticle.setUrl(url);
                    newArticle.setCategoryText(categoryText);
                    newArticle.setPublishedTime(new Date());
                    newArticle.setViewCount(0);
                    
                    articles.add(newArticle);
                }
                
                categoryArticles.postValue(articles);
                isLoading.postValue(false);
                
            } catch (IOException e) {
                Log.e(TAG, "Error fetching articles by category: " + e.getMessage());
                errorMessage.postValue("Error loading category articles: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    public static final String CATEGORY_THOI_SU = "https://vnexpress.net/thoi-su";
    public static final String CATEGORY_THE_GIOI = "https://vnexpress.net/the-gioi";
    public static final String CATEGORY_KINH_DOANH = "https://vnexpress.net/kinh-doanh";
    public static final String CATEGORY_GIAI_TRI = "https://vnexpress.net/giai-tri";
    public static final String CATEGORY_THE_THAO = "https://vnexpress.net/the-thao";
    public static final String CATEGORY_PHAP_LUAT = "https://vnexpress.net/phap-luat";
    public static final String CATEGORY_GIAO_DUC = "https://vnexpress.net/giao-duc";
    public static final String CATEGORY_SUC_KHOE = "https://vnexpress.net/suc-khoe";
    public static final String CATEGORY_DOI_SONG = "https://vnexpress.net/doi-song";
    public static final String CATEGORY_DU_LICH = "https://vnexpress.net/du-lich";
} 