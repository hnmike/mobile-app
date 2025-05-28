package com.example.appdocbao.data.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String uid;
    private String username;
    private String email;
    private String displayName;
    private String photoUrl;
    private List<String> bookmarkedArticles;

    // Default constructor required for Firestore
    public User() {
        this.bookmarkedArticles = new ArrayList<>();
    }

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.bookmarkedArticles = new ArrayList<>();
    }

    public User(String uid, String username, String email, String displayName, String photoUrl) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.bookmarkedArticles = new ArrayList<>();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public List<String> getBookmarkedArticles() {
        return bookmarkedArticles;
    }

    public void setBookmarkedArticles(List<String> bookmarkedArticles) {
        this.bookmarkedArticles = bookmarkedArticles;
    }

    public void addBookmarkedArticle(String articleId) {
        if (bookmarkedArticles == null) {
            bookmarkedArticles = new ArrayList<>();
        }
        
        if (!bookmarkedArticles.contains(articleId)) {
            bookmarkedArticles.add(articleId);
        }
    }
    
    public void removeBookmarkedArticle(String articleId) {
        if (bookmarkedArticles != null) {
            bookmarkedArticles.remove(articleId);
        }
    }
    
    public boolean hasBookmarked(String articleId) {
        return bookmarkedArticles != null && bookmarkedArticles.contains(articleId);
    }
} 