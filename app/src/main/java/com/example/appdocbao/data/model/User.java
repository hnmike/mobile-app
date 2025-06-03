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

    /****
     * Creates a new User instance with an empty list of bookmarked articles.
     * <p>
     * This no-argument constructor is required for Firestore deserialization.
     */
    public User() {
        this.bookmarkedArticles = new ArrayList<>();
    }

    /****
     * Constructs a User with the specified user ID, username, and email, initializing the bookmarked articles list as empty.
     *
     * @param uid the unique identifier for the user
     * @param username the user's username
     * @param email the user's email address
     */
    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.bookmarkedArticles = new ArrayList<>();
    }

    /****
     * Constructs a User with the specified UID, username, email, display name, and photo URL.
     * Initializes the list of bookmarked articles as empty.
     *
     * @param uid the unique identifier for the user
     * @param username the user's username
     * @param email the user's email address
     * @param displayName the user's display name
     * @param photoUrl the URL of the user's profile photo
     */
    public User(String uid, String username, String email, String displayName, String photoUrl) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.bookmarkedArticles = new ArrayList<>();
    }

    /**
     * Returns the unique identifier of the user.
     *
     * @return the user's UID
     */
    public String getUid() {
        return uid;
    }

    /****
     * Sets the unique identifier for the user.
     *
     * @param uid the user ID to assign
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Returns the username of the user.
     *
     * @return the user's username
     */
    public String getUsername() {
        return username;
    }

    /****
     * Sets the username for this user.
     *
     * @param username the username to assign
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the user's email address.
     *
     * @return the email address of the user
     */
    public String getEmail() {
        return email;
    }

    /****
     * Sets the user's email address.
     *
     * @param email the email address to assign to the user
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the display name of the user.
     *
     * @return the user's display name, or null if not set
     */
    public String getDisplayName() {
        return displayName;
    }

    /****
     * Sets the display name of the user.
     *
     * @param displayName the display name to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /****
     * Returns the user's photo URL.
     *
     * @return the photo URL, or null if not set
     */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /****
     * Sets the user's photo URL.
     *
     * @param photoUrl the URL of the user's profile photo
     */
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    /****
     * Returns the list of article IDs that the user has bookmarked.
     *
     * @return a list of bookmarked article IDs, or an empty list if none are bookmarked
     */
    public List<String> getBookmarkedArticles() {
        return bookmarkedArticles;
    }

    /****
     * Sets the list of bookmarked article IDs for the user.
     *
     * @param bookmarkedArticles a list of article IDs to assign as the user's bookmarked articles
     */
    public void setBookmarkedArticles(List<String> bookmarkedArticles) {
        this.bookmarkedArticles = bookmarkedArticles;
    }

    /**
     * Adds the specified article ID to the list of bookmarked articles if it is not already present.
     *
     * @param articleId the ID of the article to bookmark
     */
    public void addBookmarkedArticle(String articleId) {
        if (bookmarkedArticles == null) {
            bookmarkedArticles = new ArrayList<>();
        }
        
        if (!bookmarkedArticles.contains(articleId)) {
            bookmarkedArticles.add(articleId);
        }
    }
    
    /****
     * Removes the specified article ID from the list of bookmarked articles if it exists.
     *
     * @param articleId the ID of the article to remove from bookmarks
     */
    public void removeBookmarkedArticle(String articleId) {
        if (bookmarkedArticles != null) {
            bookmarkedArticles.remove(articleId);
        }
    }
    
    /**
     * Checks if the specified article ID is present in the user's list of bookmarked articles.
     *
     * @param articleId the ID of the article to check
     * @return true if the article is bookmarked by the user, false otherwise
     */
    public boolean hasBookmarked(String articleId) {
        return bookmarkedArticles != null && bookmarkedArticles.contains(articleId);
    }
} 