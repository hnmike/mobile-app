package com.example.appdocbao.utils;

public class Constants {
    // Firebase Firestore collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_ARTICLES = "articles";
    public static final String COLLECTION_CATEGORIES = "categories";
    
    // SharedPreferences
    public static final String PREFS_NAME = "AppDocBaoPrefs";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_USER_ID = "userId";
    
    // Intent extras
    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";
    public static final String EXTRA_ARTICLE_ID = "extra_article_id";
    
    // Request codes
    public static final int RC_SIGN_IN = 9001;
    
    // Log tags
    public static final String TAG_AUTH = "AuthRepository";
    public static final String TAG_NEWS = "NewsRepository";
} 