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
    
    /****
     * Returns a singleton instance of the Firebase Realtime Database configured for the Asia Southeast 1 region.
     *
     * The instance is initialized with disk persistence enabled on first access and reused for subsequent calls.
     *
     * @return the singleton FirebaseDatabase instance for the specified region
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
     * Returns a reference to the specified path in the Firebase Realtime Database.
     *
     * @param path the path within the database to reference
     * @return a DatabaseReference pointing to the specified path
     */
    public static DatabaseReference getReference(String path) {
        return getDatabase().getReference(path);
    }
    
    /**
     * Returns a reference to the root node of the Firebase Realtime Database.
     *
     * @return the root DatabaseReference
     */
    public static DatabaseReference getRootReference() {
        return getDatabase().getReference();
    }
    
    /**
     * Returns a reference to the "users" node in the Firebase Realtime Database.
     *
     * @return a DatabaseReference pointing to the "users" node
     */
    public static DatabaseReference getUsersReference() {
        return getReference("users");
    }
    
    /**
     * Returns a DatabaseReference pointing to the node of a specific user by user ID.
     *
     * @param userId the unique identifier of the user
     * @return a DatabaseReference for the specified user's node under "users"
     */
    public static DatabaseReference getUserReference(String userId) {
        return getUsersReference().child(userId);
    }
    
    /**
     * Returns a reference to the "bookmarks" node in the Firebase Realtime Database.
     *
     * @return a DatabaseReference pointing to the "bookmarks" node
     */
    public static DatabaseReference getBookmarksReference() {
        return getReference("bookmarks");
    }
    
    /**
     * Returns a DatabaseReference to the bookmarks node for a specific user.
     *
     * @param userId the unique identifier of the user
     * @return a DatabaseReference pointing to the user's bookmarks in the database
     */
    public static DatabaseReference getUserBookmarksReference(String userId) {
        return getBookmarksReference().child(userId);
    }
} 