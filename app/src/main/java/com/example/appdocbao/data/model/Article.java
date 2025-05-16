package com.example.appdocbao.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Article {
    private String id;
    private String title;
    private String summary;
    private String content;
    private String imageUrl;
    private String url;
    private String categoryId;
    private String categoryText;
    private String authorName;
    private String authorAvatarUrl;
    private Date publishedTime;
    private int viewCount;
    private boolean isBookmarked;
    private String sourceName;
    
    public Article() {
        // Required empty constructor for Firebase
    }
    
    public Article(String id, String title, String summary, String content, String imageUrl, 
                  String url, String categoryId, String categoryText, String authorName, 
                  String authorAvatarUrl, Date publishedTime, int viewCount, boolean isBookmarked) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.imageUrl = imageUrl;
        this.url = url;
        this.categoryId = categoryId;
        this.categoryText = categoryText;
        this.authorName = authorName;
        this.authorAvatarUrl = authorAvatarUrl;
        this.publishedTime = publishedTime;
        this.viewCount = viewCount;
        this.isBookmarked = isBookmarked;
    }
    
    // Constructor used by VnExpressParser
    public Article(String id, String title, String content, String imageUrl, String sourceUrl, 
                  String sourceName, String categoryId, String categoryName, Date publishedTime) {
        this.id = id;
        this.title = title;
        this.summary = content;
        this.content = content;
        this.imageUrl = imageUrl;
        this.url = sourceUrl;
        this.categoryId = categoryId;
        this.categoryText = categoryName;
        this.publishedTime = publishedTime;
        this.sourceName = sourceName;
        this.viewCount = 0;
        this.isBookmarked = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryText() {
        return categoryText;
    }

    public void setCategoryText(String categoryText) {
        this.categoryText = categoryText;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public Date getPublishedTime() {
        return publishedTime;
    }

    public void setPublishedTime(Date publishedTime) {
        this.publishedTime = publishedTime;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }
    
    // Additional methods needed by other parts of the app
    public String getSourceUrl() {
        return url;
    }
    
    public String getSourceName() {
        return sourceName != null ? sourceName : "VnExpress";
    }
    
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    
    public String getCategoryName() {
        return categoryText;
    }
    
    public Date getPublishedAt() {
        return publishedTime;
    }
    
    public String getPublishedTimeFormatted() {
        if (publishedTime == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(publishedTime);
    }
} 