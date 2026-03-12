// EarnRewards/src/com/ykapps/earnrewards/EarnRewardsApplication.java

package com.ykapps.earnrewards;

import android.app.Application;
import android.util.Log;
import androidx.work.Configuration;
import androidx.work.WorkManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;

/**
 * EarnRewardsApplication - Application lifecycle management
 */
public class EarnRewardsApplication extends Application implements Configuration.Provider {
    
    private static final String TAG = "EarnRewardsApp";
    private static EarnRewardsApplication instance;
    
    private FirebaseAnalytics firebaseAnalytics;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        Log.d(TAG, "Application created");
        
        // Initialize Firebase
        initializeFirebase();
        
        // Initialize Work Manager
        WorkManager.initialize(this, getWorkManagerConfiguration());
        
        // Enable strict mode in debug builds
        if (BuildConfig.DEBUG) {
            enableStrictMode();
        }
    }
    
    private void initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this);
            
            firebaseAnalytics = FirebaseAnalytics.getInstance(this);
            
            // Enable persistence for Realtime Database
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            
            // Set analytics collection enabled
            firebaseAnalytics.setAnalyticsCollectionEnabled(true);
            
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed", e);
        }
    }
    
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
            .setMinimumLoggingLevel(BuildConfig.DEBUG ? Log.DEBUG : Log.INFO)
            .build();
    }
    
    private void enableStrictMode() {
        android.os.StrictMode.setThreadPolicy(
            new android.os.StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        );
        
        android.os.StrictMode.setVmPolicy(
            new android.os.StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        );
    }
    
    public static EarnRewardsApplication getInstance() {
        return instance;
    }
    
    public FirebaseAnalytics getFirebaseAnalytics() {
        return firebaseAnalytics;
    }
}
