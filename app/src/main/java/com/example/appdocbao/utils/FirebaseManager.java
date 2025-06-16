package com.example.appdocbao.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseManager {
    
    private static FirebaseDatabase database;
    
    static {
        // Initialize Firebase Database
        try {
            database = FirebaseDatabase.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get DatabaseReference for a given path
     * @param path The path to the data
     * @return DatabaseReference object
     */
    public static DatabaseReference getReference(String path) {
        if (database != null) {
            return database.getReference(path);
        }
        return null;
    }
    
    /**
     * Get the root DatabaseReference
     * @return DatabaseReference object
     */
    public static DatabaseReference getReference() {
        if (database != null) {
            return database.getReference();
        }
        return null;
    }
    
    /**
     * Check if Firebase is properly initialized
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return database != null;
    }
} 