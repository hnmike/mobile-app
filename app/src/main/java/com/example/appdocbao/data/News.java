package com.example.appdocbao.data;

import java.util.Date;

public class News {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String url;
    private String publishDate;
    private Date publishedDate;
    private int categoryId;
    private String categoryName;
    private boolean isFeatured;

    // Constructor mặc định
    public News() {
    }

    public News(int id, String title, String description, String imageUrl,
                String publishDate, int categoryId, boolean isFeatured) {
        this.id = String.valueOf(id);
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.publishDate = publishDate;
        this.categoryId = categoryId;
        this.isFeatured = isFeatured;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public void setPublishedDate(Date publishedDate) {
        this.publishedDate = publishedDate;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }
}