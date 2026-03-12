// EarnRewards/src/com/ykapps/earnrewards/MainActivity.java
// Reconstructed from APK decompilation analysis
// This is what the Java wrapper likely contains

package com.ykapps.earnrewards;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.attribution.AppsFlyerRequestListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.mediation.MaxAdapterInitializationManager;
import com.facebook.ads.AudienceNetworkAds;
import com.pangle.ads.api.PAGSdk;
import com.android.billingclient.api.BillingClient;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {
    private static final String TAG = "EarnRewards";
    
    private AppLovinSdkConfiguration appLovinConfig;
    private FirebaseAnalytics firebaseAnalytics;
    private AdMediator adMediator;
    private RewardManager rewardManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "MainActivity onCreate called");
        
        // Initialize Flutter plugins
        GeneratedPluginRegistrant.registerGeneratedPlugins(this);
        
        // Initialize Firebase
        initializeFirebase();
        
        // Initialize Ad Networks
        initializeAdNetworks();
        
        // Initialize AppsFlyer
        initializeAppsFlyer();
        
        // Initialize Reward Manager
        rewardManager = new RewardManager(this);
        
        // Initialize Mediation Controller
        adMediator = new AdMediator(this);
    }
    
    @Override
    public void configureFlutterEngine(FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        
        // Setup method channels for Flutter → Java communication
        new com.ykapps.earnrewards.platform.AdMethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), adMediator);
        new com.ykapps.earnrewards.platform.RewardMethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), rewardManager);
        new com.ykapps.earnrewards.platform.AnalyticsMethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), firebaseAnalytics);
    }
    
    private void initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this);
            firebaseAnalytics = FirebaseAnalytics.getInstance(this);
            
            // Enable Firebase Realtime Database
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed", e);
        }
    }
    
    private void initializeAdNetworks() {
        try {
            // Initialize AppLovin MAX
            initializeAppLovinMax();
            
            // Initialize Facebook Audience Network
            initializeFacebookAds();
            
            // Initialize Pangle (ByteDance)
            initializePangle();
            
            // Initialize Google AdMob
            com.google.android.gms.ads.MobileAds.initialize(this);
            
            Log.d(TAG, "All ad networks initialized");
        } catch (Exception e) {
            Log.e(TAG, "Ad network initialization failed", e);
        }
    }
    
    private void initializeAppLovinMax() {
        // AppLovin MAX configuration
        String appLovinSdkKey = "YOUR_APPLOVIN_SDK_KEY"; // Fetched from Firebase Remote Config or config.xml
        
        AppLovinSdk.getInstance(appLovinSdkKey, this, initialized -> {
            Log.d(TAG, "AppLovin MAX SDK initialized");
            
            // Load test devices
            AppLovinSdk.getInstance().getSettings().setTestDeviceAdvertisingIds(
                new java.util.ArrayList<>(java.util.Collections.singletonList("YOUR_TEST_DEVICE_ID"))
            );
        });
    }
    
    private void initializeFacebookAds() {
        AudienceNetworkAds.initialize(this);
        Log.d(TAG, "Facebook Audience Network initialized");
    }
    
    private void initializePangle() {
        PAGSdk.Config config = new PAGSdk.Config.Builder()
            .appId("YOUR_PANGLE_APP_ID")
            .appName(getApplicationInfo().loadLabel(getPackageManager()).toString())
            .useTextureView(true)
            .build();
        
        PAGSdk.init(this, config, new PAGSdk.ISdkInitListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Pangle SDK initialized");
            }
            
            @Override
            public void onError(int code, String msg) {
                Log.e(TAG, "Pangle init error: " + msg);
            }
        });
    }
    
    private void initializeAppsFlyer() {
        AppsFlyerLib appsFlyer = AppsFlyerLib.getInstance();
        
        // Set AppsFlyer credentials
        appsFlyer.init("YOUR_APPSFLYER_DEV_KEY", new AppsFlyerRequestListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "AppsFlyer initialized");
            }
            
            @Override
            public void onError(int code, String desc) {
                Log.e(TAG, "AppsFlyer error: " + desc);
            }
        }, this);
        
        // Start session tracking
        appsFlyer.start(this);
    }
}
