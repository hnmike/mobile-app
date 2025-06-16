package com.example.appdocbao.examples;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.appdocbao.utils.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Example class demonstrating usage of the Firebase Realtime Database
 */
public class FirebaseDatabaseExample {
    
    private static final String TAG = "FirebaseDBExample";
    
    /**
     * Example method to write data to Firebase Database
     */
    public void writeUserData(String userId, String name, String email) {
        // Get a reference to the users node and the specific user
        DatabaseReference userRef = FirebaseManager.getUserReference(userId);
        
        // Create a user object
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("lastLoginAt", System.currentTimeMillis());
        
        // Write to the database
        userRef.setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User data written successfully");
                        } else {
                            Log.e(TAG, "Error writing user data", task.getException());
                        }
                    }
                });
    }
    
    /**
     * Example method to save a bookmark
     */
    public void saveBookmark(String userId, String articleId, String title, String url) {
        // Get a reference to the user's bookmarks
        DatabaseReference bookmarkRef = FirebaseManager.getUserBookmarksReference(userId).child(articleId);
        
        // Create a bookmark object
        Map<String, Object> bookmark = new HashMap<>();
        bookmark.put("title", title);
        bookmark.put("url", url);
        bookmark.put("createdAt", System.currentTimeMillis());
        
        // Save the bookmark
        bookmarkRef.setValue(bookmark)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Bookmark saved successfully");
                        } else {
                            Log.e(TAG, "Error saving bookmark", task.getException());
                        }
                    }
                });
    }
    
    /**
     * Example method to read user data from Firebase Database
     */
    public void readUserData(String userId) {
        // Get a reference to the specific user
        DatabaseReference userRef = FirebaseManager.getUserReference(userId);
        
        // Read from the database
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    
                    Log.d(TAG, "User data: name=" + name + ", email=" + email);
                } else {
                    Log.d(TAG, "User data does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user data", error.toException());
            }
        });
    }
    
    /**
     * Example method to update user data
     */
    public void updateUserData(String userId, String newName) {
        // Get a reference to the specific user
        DatabaseReference userRef = FirebaseManager.getUserReference(userId);
        
        // Update specific fields
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("updatedAt", System.currentTimeMillis());
        
        // Update the database
        userRef.updateChildren(updates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User data updated successfully");
                        } else {
                            Log.e(TAG, "Error updating user data", task.getException());
                        }
                    }
                });
    }
    
    /**
     * Example method to delete a bookmark
     */
    public void deleteBookmark(String userId, String articleId) {
        // Get a reference to the specific bookmark
        DatabaseReference bookmarkRef = FirebaseManager.getUserBookmarksReference(userId).child(articleId);
        
        // Remove the value
        bookmarkRef.removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Bookmark deleted successfully");
                        } else {
                            Log.e(TAG, "Error deleting bookmark", task.getException());
                        }
                    }
                });
    }
    
    /**
     * Example method to read all bookmarks for a user
     */
    public void readUserBookmarks(String userId) {
        // Get a reference to the user's bookmarks
        DatabaseReference bookmarksRef = FirebaseManager.getUserBookmarksReference(userId);
        
        // Read from the database
        bookmarksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through each bookmark
                for (DataSnapshot bookmarkSnapshot : dataSnapshot.getChildren()) {
                    String articleId = bookmarkSnapshot.getKey();
                    String title = bookmarkSnapshot.child("title").getValue(String.class);
                    String url = bookmarkSnapshot.child("url").getValue(String.class);
                    
                    Log.d(TAG, "Bookmark: id=" + articleId + ", title=" + title + ", url=" + url);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read bookmarks", error.toException());
            }
        });
    }
    
    /**
     * Utility method to get the current user ID
     */
    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        return null;
    }
} 