package com.example.appdocbao.utils;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Utility class to manage Firebase Realtime Database operations
 */
public class FirebaseManager {
    
    private static final String TAG = "FirebaseManager";
    
    // Firebase Realtime Database URL for Asia Southeast 1 region
    private static final String DATABASE_URL = "https://app-6fbf1-default-rtdb.asia-southeast1.firebasedatabase.app/";
    
    private static FirebaseDatabase mDatabase;
    
    /**
     * Get instance of Firebase Database with specific region (Asia Southeast 1)
     * @return FirebaseDatabase instance
     */
    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            Log.d(TAG, "Initializing Firebase Database with URL: " + DATABASE_URL);
            mDatabase = FirebaseDatabase.getInstance(DATABASE_URL);
            
            // Enable disk persistence (optional)
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }
    
    /**
     * Get a reference to a specific path in the database
     * @param path The database path
     * @return DatabaseReference for the specified path
     */
    public static DatabaseReference getReference(String path) {
        return getDatabase().getReference(path);
    }
    
    /**
     * Get the root reference of the database
     * @return DatabaseReference for the root
     */
    public static DatabaseReference getRootReference() {
        return getDatabase().getReference();
    }
    
    /**
     * Get a reference to the users node
     * @return DatabaseReference for users
     */
    public static DatabaseReference getUsersReference() {
        return getReference("users");
    }
    
    /**
     * Get a reference to a specific user by ID
     * @param userId The user ID
     * @return DatabaseReference for the specific user
     */
    public static DatabaseReference getUserReference(String userId) {
        return getUsersReference().child(userId);
    }
    
    /**
     * Get a reference to the bookmarks node
     * @return DatabaseReference for bookmarks
     */
    public static DatabaseReference getBookmarksReference() {
        return getReference("bookmarks");
    }
    
    /**
     * Get a reference to a specific user's bookmarks
     * @param userId The user ID
     * @return DatabaseReference for the user's bookmarks
     */
    public static DatabaseReference getUserBookmarksReference(String userId) {
        return getBookmarksReference().child(userId);
    }
} 