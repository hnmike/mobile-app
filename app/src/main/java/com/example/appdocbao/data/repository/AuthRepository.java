package com.example.appdocbao.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appdocbao.data.model.User;
import com.example.appdocbao.utils.Constants;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {
    private static final String TAG = Constants.TAG_AUTH;

    private static AuthRepository instance;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final SharedPreferences sharedPreferences;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * Initializes the AuthRepository with Firebase Authentication, Firestore, and SharedPreferences.
     *
     * If a user is already authenticated, fetches their data and updates LiveData; otherwise, sets the current user to null.
     */
    private AuthRepository(Context context) {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        
        // Check if there's a current user and update LiveData
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            fetchUserData(user.getUid());
        } else {
            currentUser.setValue(null);
        }
    }

    /**
     * Returns the singleton instance of AuthRepository, initializing it if necessary.
     *
     * @param context the application context used for initialization
     * @return the singleton AuthRepository instance
     */
    public static synchronized AuthRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AuthRepository(context.getApplicationContext());
        }
        return instance;
    }

    /****
     * Returns a LiveData stream representing the currently authenticated user.
     *
     * Observers can use this LiveData to react to changes in authentication state or user profile updates.
     *
     * @return LiveData containing the current User, or null if no user is authenticated
     */
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns a LiveData object representing the current loading state of authentication operations.
     *
     * @return LiveData that is true when an authentication process is ongoing, false otherwise
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Returns a LiveData stream containing the latest authentication error message.
     *
     * Observers can use this to display error messages resulting from authentication or user data operations.
     *
     * @return LiveData emitting error messages as strings
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Registers a new user with the provided username, email, and password using Firebase Authentication.
     *
     * Initiates user creation in Firebase Authentication and, upon success, creates a corresponding user record in Firestore. Updates loading and error state LiveData to reflect operation progress and errors, including specific handling for common Firebase errors such as API key issues, email duplication, and network problems.
     *
     * @param username the display name for the new user
     * @param email the email address for registration
     * @param password the password for the new account
     */
    public void signUp(String username, String email, String password) {
        isLoading.setValue(true);
        Log.d(TAG, "Starting user registration with email: " + email);
        
        try {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            if (firebaseUser != null) {
                                Log.d(TAG, "Firebase user created successfully: " + firebaseUser.getUid());
                                // Create user in Firestore
                                User user = new User(firebaseUser.getUid(), username, email);
                                saveUserToFirestore(user);
                            } else {
                                Log.e(TAG, "Firebase user is null despite successful task");
                                isLoading.setValue(false);
                                errorMessage.setValue("Không thể tạo người dùng, vui lòng thử lại");
                            }
                        } else {
                            isLoading.setValue(false);
                            String errorMsg = "";
                            if (task.getException() != null) {
                                errorMsg = task.getException().getMessage();
                                Log.e(TAG, "Sign up failed with exception: " + errorMsg, task.getException());
                                
                                // Check for common Firebase errors
                                if (errorMsg.contains("API key")) {
                                    Log.e(TAG, "Firebase API key error detected. Please check google-services.json file.");
                                    errorMsg = "Lỗi xác thực Firebase API. Vui lòng liên hệ với nhà phát triển.";
                                } else if (errorMsg.contains("email already in use")) {
                                    Log.e(TAG, "Email already exists: " + email);
                                    errorMsg = "Email đã được sử dụng. Vui lòng thử email khác.";
                                } else if (errorMsg.contains("network error")) {
                                    Log.e(TAG, "Network error during signup");
                                    errorMsg = "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet.";
                                }
                            } else {
                                errorMsg = "Đăng ký thất bại vì lý do không xác định";
                                Log.e(TAG, errorMsg);
                            }
                            
                            errorMessage.setValue(errorMsg);
                        }
                    })
                    .addOnFailureListener(e -> {
                        isLoading.setValue(false);
                        String errorMsg = e.getMessage();
                        Log.e(TAG, "Sign up failure: " + errorMsg, e);
                        
                        // Check for API key issues
                        if (errorMsg != null && (errorMsg.contains("API key") || errorMsg.contains("authentication"))) {
                            Log.e(TAG, "Firebase authentication/API key error: " + errorMsg);
                            errorMessage.setValue("Lỗi cấu hình Firebase. Vui lòng liên hệ với nhà phát triển.");
                        } else {
                            errorMessage.setValue("Đăng ký thất bại: " + errorMsg);
                        }
                    });
        } catch (Exception e) {
            isLoading.setValue(false);
            Log.e(TAG, "Critical error during sign up process", e);
            errorMessage.setValue("Lỗi hệ thống: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user using email and password credentials.
     *
     * On successful sign-in, persists login status and retrieves user data from Firestore. Updates loading and error message LiveData to reflect operation status.
     *
     * @param email the user's email address
     * @param password the user's password
     */
    public void signIn(String email, String password) {
        isLoading.setValue(true);
        
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            // Save login status and fetch user data
                            saveLoginStatus(firebaseUser.getUid());
                            fetchUserData(firebaseUser.getUid());
                        }
                    } else {
                        errorMessage.setValue(task.getException() != null ? 
                                task.getException().getMessage() : "Sign in failed");
                        Log.e(TAG, "signIn: ", task.getException());
                    }
                });
    }

    /**
     * Authenticates a user using Google credentials and updates user data accordingly.
     *
     * Initiates sign-in with the provided Google account. On successful authentication, checks if the user exists in Firestore and creates a new user if necessary. Updates loading and error states for UI observation.
     *
     * @param account the GoogleSignInAccount used for authentication
     */
    public void signInWithGoogle(GoogleSignInAccount account) {
        isLoading.setValue(true);
        
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            // Check if user exists or create new user
                            checkUserExists(firebaseUser, account.getDisplayName());
                        }
                    } else {
                        errorMessage.setValue(task.getException() != null ? 
                                task.getException().getMessage() : "Google sign in failed");
                        Log.e(TAG, "signInWithGoogle: ", task.getException());
                    }
                });
    }

    /**
     * Authenticates a user using a Facebook access token and updates user data accordingly.
     *
     * Initiates sign-in with Facebook credentials. On successful authentication, checks if the user exists in Firestore or creates a new user entry. Updates loading and error state LiveData for UI observation.
     *
     * @param accessToken the Facebook access token used for authentication
     */
    public void signInWithFacebook(AccessToken accessToken) {
        isLoading.setValue(true);
        
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            // Check if user exists or create new user
                            checkUserExists(firebaseUser, firebaseUser.getDisplayName());
                        }
                    } else {
                        errorMessage.setValue(task.getException() != null ? 
                                task.getException().getMessage() : "Facebook sign in failed");
                        Log.e(TAG, "signInWithFacebook: ", task.getException());
                    }
                });
    }

    /**
     * Signs out the current user and clears all persisted authentication data.
     *
     * Resets the current user LiveData and removes login status from persistent storage.
     */
    public void signOut() {
        firebaseAuth.signOut();
        clearLoginStatus();
        currentUser.setValue(null);
    }

    /**
     * Persists the given user object to Firestore under the users collection.
     *
     * On successful save, updates the loading state, stores login status, and sets the current user LiveData.
     * On failure, updates the loading state and error message LiveData.
     *
     * @param user the user object to be saved to Firestore
     */
    private void saveUserToFirestore(User user) {
        firestore.collection(Constants.COLLECTION_USERS)
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User saved to Firestore successfully: " + user.getEmail());
                    isLoading.setValue(false);
                    saveLoginStatus(user.getUid());
                    currentUser.setValue(user);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                    Log.e(TAG, "saveUserToFirestore: ", e);
                });
    }

    /**
     * Checks if a user document exists in Firestore for the given Firebase user.
     *
     * If the user exists, fetches and updates user data. If not, creates a new user document in Firestore using the provided Firebase user information and display name.
     *
     * @param firebaseUser the authenticated Firebase user
     * @param displayName the display name to use if creating a new user
     */
    private void checkUserExists(FirebaseUser firebaseUser, String displayName) {
        DocumentReference userRef = firestore.collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.getUid());
                
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // User exists, fetch data
                    fetchUserData(firebaseUser.getUid());
                } else {
                    // User doesn't exist, create new user
                    String username = firebaseUser.getEmail();
                    if (username != null && username.contains("@")) {
                        username = username.substring(0, username.indexOf("@"));
                    }
                    
                    User user = new User(
                            firebaseUser.getUid(),
                            username,
                            firebaseUser.getEmail(),
                            displayName,
                            firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null
                    );
                    
                    saveUserToFirestore(user);
                }
            } else {
                isLoading.setValue(false);
                errorMessage.setValue(task.getException() != null ? 
                        task.getException().getMessage() : "Failed to check user");
                Log.e(TAG, "checkUserExists: ", task.getException());
            }
        });
    }

    /**
     * Retrieves user data from Firestore by UID and updates LiveData accordingly.
     *
     * If the user document exists in Firestore, updates the current user LiveData and saves login status.
     * If not found, attempts to create a new user from Firebase Authentication data and saves it to Firestore.
     * On Firestore retrieval failure, falls back to Firebase Authentication data to update the current user.
     */
    private void fetchUserData(String uid) {
        isLoading.setValue(true);
        
        Log.d(TAG, "Fetching user data for UID: " + uid);
        
        // Nếu Firebase user tồn tại nhưng chưa có thông tin trong Firestore
        // chúng ta sẽ tạo một User mới với thông tin từ Firebase Auth
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        
        firestore.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isLoading.setValue(false);
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        Log.d(TAG, "User data fetched successfully from Firestore: " + (user != null ? user.getEmail() : "null"));
                        currentUser.setValue(user);
                        saveLoginStatus(uid);
                    } else {
                        // Nếu không tìm thấy thông tin người dùng trong Firestore,
                        // tạo user mới từ thông tin FirebaseAuth
                        if (firebaseUser != null) {
                            Log.d(TAG, "User document does not exist, creating new user from Firebase Auth data");
                            
                            String username = uid;
                            if (firebaseUser.getEmail() != null && firebaseUser.getEmail().contains("@")) {
                                username = firebaseUser.getEmail().substring(0, firebaseUser.getEmail().indexOf("@"));
                            }
                            
                            User newUser = new User(
                                uid,
                                username,
                                firebaseUser.getEmail(),
                                firebaseUser.getDisplayName(),
                                firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null
                            );
                            
                            // Lưu thông tin người dùng mới vào Firestore
                            saveUserToFirestore(newUser);
                        } else {
                            Log.w(TAG, "User document does not exist for UID: " + uid + " and FirebaseUser is null");
                            errorMessage.setValue("User not found");
                            currentUser.setValue(null);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                    currentUser.setValue(null);
                    Log.e(TAG, "fetchUserData: ", e);
                    
                    // Fallback to FirebaseUser data if Firestore fails
                    if (firebaseUser != null) {
                        try {
                            Log.d(TAG, "Using Firebase Auth data as fallback");
                            
                            String username = uid;
                            if (firebaseUser.getEmail() != null && firebaseUser.getEmail().contains("@")) {
                                username = firebaseUser.getEmail().substring(0, firebaseUser.getEmail().indexOf("@"));
                            }
                            
                            User fallbackUser = new User(
                                uid,
                                username,
                                firebaseUser.getEmail(),
                                firebaseUser.getDisplayName(),
                                firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null
                            );
                            
                            currentUser.setValue(fallbackUser);
                            saveLoginStatus(uid);
                        } catch (Exception ex) {
                            Log.e(TAG, "Error creating fallback user: " + ex.getMessage(), ex);
                        }
                    }
                });
    }

    /**
     * Persists the user's login status and user ID in SharedPreferences.
     *
     * @param uid the unique identifier of the logged-in user
     */
    private void saveLoginStatus(String uid) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.putString(Constants.KEY_USER_ID, uid);
        editor.apply();
    }

    /**
     * Removes the user's login status and user ID from persistent storage.
     *
     * Clears the login flag and user identifier from SharedPreferences to reflect that no user is currently authenticated.
     */
    private void clearLoginStatus() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, false);
        editor.remove(Constants.KEY_USER_ID);
        editor.apply();
    }

    /****
     * Determines whether a user is currently logged in based on stored preferences.
     *
     * @return true if the user is logged in; false otherwise
     */
    public boolean isLoggedIn() {
        try {
            if (sharedPreferences == null) {
                Log.e(TAG, "isLoggedIn: SharedPreferences is null");
                return false;
            }
            return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status: " + e.getMessage(), e);
            return false;
        }
    }
} 