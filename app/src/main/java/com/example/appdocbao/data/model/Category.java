package com.example.appdocbao.data.model;

import java.util.Objects;

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

    // ================ GETTERS ================

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    public String getEmoji() {
        return iconEmoji;
    }

    public int getArticleCount() {
        return articleCount;
    }

    // ================ SETTERS ================

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIconEmoji(String iconEmoji) {
        this.iconEmoji = iconEmoji;
    }

    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }

    // ================ OBJECT METHODS ================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Category category = (Category) o;
        
        return articleCount == category.articleCount &&
                Objects.equals(id, category.id) &&
                Objects.equals(name, category.name) &&
                Objects.equals(description, category.description) &&
                Objects.equals(iconEmoji, category.iconEmoji);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, iconEmoji, articleCount);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", iconEmoji='" + iconEmoji + '\'' +
                ", articleCount=" + articleCount +
                '}';
    }
} 