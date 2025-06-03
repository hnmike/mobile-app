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
    
    /****
     * Writes user information to the Firebase Realtime Database under the specified user ID.
     *
     * @param userId the unique identifier for the user
     * @param name the user's name
     * @param email the user's email address
     *
     * The method stores the user's name, email, and the current timestamp as 'lastLoginAt'.
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
                    /**
                     * Handles the completion of a user data write operation to Firebase Realtime Database, logging the result.
                     *
                     * @param task the task representing the completion status of the write operation
                     */
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
    
    /****
     * Saves a bookmark for a user in the Firebase Realtime Database.
     *
     * @param userId the ID of the user for whom the bookmark is being saved
     * @param articleId the unique identifier for the article to bookmark
     * @param title the title of the bookmarked article
     * @param url the URL of the bookmarked article
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
                    /**
                     * Callback invoked upon completion of the bookmark save operation.
                     *
                     * Logs a success message if the bookmark is saved successfully; otherwise logs the error encountered.
                     */
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
     * Reads user data from the Firebase Realtime Database and logs the user's name and email if available.
     *
     * @param userId the unique identifier of the user whose data is to be read
     */
    public void readUserData(String userId) {
        // Get a reference to the specific user
        DatabaseReference userRef = FirebaseManager.getUserReference(userId);
        
        // Read from the database
        userRef.addValueEventListener(new ValueEventListener() {
            /**
             * Handles changes to the user's data in the database by retrieving and logging the user's name and email if available.
             *
             * Called when the user data at the specified database location is initially loaded or updated.
             *
             * @param dataSnapshot the snapshot of the current data at the user node
             */
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

            /**
             * Called when a database read operation is canceled or fails.
             *
             * @param error the error that occurred during the database read
             */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user data", error.toException());
            }
        });
    }
    
    /**
     * Updates the user's name and sets the updated timestamp in the Firebase Realtime Database.
     *
     * @param userId   the unique identifier of the user whose data will be updated
     * @param newName  the new name to set for the user
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
                    /**
                     * Handles the completion of the user data update operation, logging the result.
                     *
                     * @param task the task representing the completion status of the update operation
                     */
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
    
    /****
     * Deletes a specific bookmark for a user from the Firebase Realtime Database.
     *
     * @param userId the ID of the user whose bookmark is to be deleted
     * @param articleId the ID of the article bookmark to remove
     */
    public void deleteBookmark(String userId, String articleId) {
        // Get a reference to the specific bookmark
        DatabaseReference bookmarkRef = FirebaseManager.getUserBookmarksReference(userId).child(articleId);
        
        // Remove the value
        bookmarkRef.removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    /**
                     * Handles the completion of a bookmark deletion operation, logging the result.
                     *
                     * Logs a success message if the bookmark was deleted successfully, or logs an error with details if the operation failed.
                     */
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
     * Reads and logs all bookmarks for the specified user from the Firebase Realtime Database.
     *
     * @param userId the unique identifier of the user whose bookmarks are to be retrieved
     */
    public void readUserBookmarks(String userId) {
        // Get a reference to the user's bookmarks
        DatabaseReference bookmarksRef = FirebaseManager.getUserBookmarksReference(userId);
        
        // Read from the database
        bookmarksRef.addValueEventListener(new ValueEventListener() {
            /**
             * Handles changes to the user's bookmarks data by iterating through each bookmark and logging its details.
             *
             * @param dataSnapshot the snapshot containing the user's bookmarks data
             */
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

            /**
             * Called when the bookmark data read operation is canceled or fails.
             *
             * @param error the database error encountered during the read operation
             */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read bookmarks", error.toException());
            }
        });
    }
    
    /****
     * Retrieves the UID of the currently authenticated Firebase user.
     *
     * @return the user ID if a user is signed in; otherwise, null
     */
    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        return null;
    }
} 