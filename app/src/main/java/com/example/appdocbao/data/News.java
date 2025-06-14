package com.example.appdocbao.data;

public class News {
    private int id;
    private String title;
    private String description;
    private String imageUrl;
    private String publishDate;
    private int categoryId;
    private boolean isFeatured;
    
    public News(int id, String title, String description, String imageUrl, 
                String publishDate, int categoryId, boolean isFeatured) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.publishDate = publishDate;
        this.categoryId = categoryId;
        this.isFeatured = isFeatured;
    }
    
    public int getId() {
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
    
    public String getPublishDate() {
        return publishDate;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public boolean isFeatured() {
        return isFeatured;
    }
}