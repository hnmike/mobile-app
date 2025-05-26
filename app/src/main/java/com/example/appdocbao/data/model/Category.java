package com.example.appdocbao.data.model;

public class Category {
    private String id;
    private String name;
    private String description;
    private String iconEmoji;
    private int articleCount;

    // Default constructor required for Firestore
    public Category() {
    }

    public Category(String id, String name, String description, String iconEmoji) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconEmoji = iconEmoji;
        this.articleCount = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    public void setIconEmoji(String iconEmoji) {
        this.iconEmoji = iconEmoji;
    }

    public String getEmoji() {
        return iconEmoji;
    }

    public int getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }
} 