package com.example.appdocbao.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

public class NetworkUtils {
    
    private static final String TAG = "NetworkUtils";
    
    /**
     * Check if the device has an active internet connection
     * @param context Application context
     * @return true if connected, false otherwise
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null");
            return false;
        }

        try {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager == null) {
                Log.e(TAG, "ConnectivityManager is null");
                return false;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager
                        .getNetworkCapabilities(connectivityManager.getActiveNetwork());

                if (capabilities != null) {
                    boolean hasInternet = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                           capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                           capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
                    
                    Log.d(TAG, "Network available: " + hasInternet);
                    return hasInternet;
                } else {
                    Log.e(TAG, "Network capabilities are null");
                }
            } else {
                // For older Android versions
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                boolean hasInternet = activeNetworkInfo != null && activeNetworkInfo.isConnected();
                Log.d(TAG, "Network available (legacy): " + hasInternet);
                return hasInternet;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception checking network: " + e.getMessage(), e);
        }

        Log.e(TAG, "Network check failed, returning false");
        return false;
    }
} 