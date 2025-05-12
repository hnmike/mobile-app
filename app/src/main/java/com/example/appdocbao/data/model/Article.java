package com.example.appdocbao.data.model;

import com.example.appdocbao.utils.DateUtils;

import java.util.Date;

public class Article {
    private String id;
    private String title;
    private String content;
    private String imageUrl;
    private String sourceUrl;
    private String source;
    private String categoryId;
    private String categoryName;
    private Date publishDate;
    private boolean isBookmarked;

    // Default constructor required for Firestore
    public Article() {
    }

    public Article(String id, String title, String content, String imageUrl, String sourceUrl, 
                  String source, String categoryId, String categoryName, Date publishDate) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.sourceUrl = sourceUrl;
        this.source = source;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.publishDate = publishDate;
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

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceName() {
        return source;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public Date getPublishedAt() {
        return publishDate;
    }

    public String getPublishedTimeFormatted() {
        return DateUtils.getTimeAgo(publishDate);
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }
} 