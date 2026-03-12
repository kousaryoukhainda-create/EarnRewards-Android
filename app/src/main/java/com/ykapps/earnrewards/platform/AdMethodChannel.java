// EarnRewards/src/com/ykapps/earnrewards/platform/AdMethodChannel.java

package com.ykapps.earnrewards.platform;

import android.util.Log;
import androidx.annotation.NonNull;
import com.ykapps.earnrewards.AdMediator;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.plugin.common.MethodChannel;

/**
 * AdMethodChannel - Handles Flutter ↔ Java communication for ad operations
 */
public class AdMethodChannel {
    
    private static final String CHANNEL_NAME = "com.ykapps.earnrewards/ads";
    private static final String TAG = "AdMethodChannel";
    
    private final MethodChannel methodChannel;
    private final AdMediator adMediator;
    
    public AdMethodChannel(DartExecutor dartExecutor, AdMediator adMediator) {
        this.adMediator = adMediator;
        this.methodChannel = new MethodChannel(dartExecutor, CHANNEL_NAME);
        setupMethodHandler();
    }
    
    private void setupMethodHandler() {
        methodChannel.setMethodCallHandler((call, result) -> {
            try {
                switch (call.method) {
                    case "loadRewardedAd":
                        handleLoadRewardedAd(result);
                        break;
                    
                    case "showRewardedAd":
                        handleShowRewardedAd(result);
                        break;
                    
                    case "isRewardedAdReady":
                        handleIsRewardedAdReady(result);
                        break;
                    
                    case "loadInterstitialAd":
                        handleLoadInterstitialAd(result);
                        break;
                    
                    case "showInterstitialAd":
                        handleShowInterstitialAd(result);
                        break;
                    
                    case "isInterstitialAdReady":
                        handleIsInterstitialAdReady(result);
                        break;
                    
                    case "showBannerAd":
                        handleShowBannerAd(result);
                        break;
                    
                    case "hideBannerAd":
                        handleHideBannerAd(result);
                        break;
                    
                    case "getAdNetworks":
                        handleGetAdNetworks(result);
                        break;
                    
                    default:
                        result.notImplemented();
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling method: " + call.method, e);
                result.error("EXCEPTION", e.getMessage(), null);
            }
        });
    }
    
    private void handleLoadRewardedAd(MethodChannel.Result result) {
        adMediator.loadRewardedAd();
        adMediator.setAdCallback(new AdMediator.AdDisplayCallback() {
            @Override
            public void onAdLoaded(AdMediator.AdType adType) {
                if (adType == AdMediator.AdType.REWARDED) {
                    invokeFlutterMethod("onRewardedAdLoaded", null);
                }
            }
            
            @Override
            public void onAdFailedToLoad(AdMediator.AdType adType, String reason) {
                if (adType == AdMediator.AdType.REWARDED) {
                    invokeFlutterMethod("onRewardedAdFailedToLoad", reason);
                }
            }
            
            @Override
            public void onAdDisplayed(AdMediator.AdType adType) {}
            
            @Override
            public void onAdClicked(AdMediator.AdType adType) {}
            
            @Override
            public void onRewardEarned(int rewardAmount) {
                invokeFlutterMethod("onRewardEarned", rewardAmount);
            }
        });
        result.success(true);
    }
    
    private void handleShowRewardedAd(MethodChannel.Result result) {
        if (adMediator.isRewardedAdReady()) {
            adMediator.showRewardedAd();
            result.success(true);
        } else {
            result.error("NOT_READY", "Rewarded ad is not loaded", null);
        }
    }
    
    private void handleIsRewardedAdReady(MethodChannel.Result result) {
        result.success(adMediator.isRewardedAdReady());
    }
    
    private void handleLoadInterstitialAd(MethodChannel.Result result) {
        adMediator.loadInterstitialAd();
        result.success(true);
    }
    
    private void handleShowInterstitialAd(MethodChannel.Result result) {
        if (adMediator.isInterstitialReady()) {
            adMediator.showInterstitialAd();
            result.success(true);
        } else {
            result.error("NOT_READY", "Interstitial ad is not loaded", null);
        }
    }
    
    private void handleIsInterstitialAdReady(MethodChannel.Result result) {
        result.success(adMediator.isInterstitialReady());
    }
    
    private void handleShowBannerAd(MethodChannel.Result result) {
        adMediator.showBannerAd();
        result.success(true);
    }
    
    private void handleHideBannerAd(MethodChannel.Result result) {
        adMediator.hideBannerAd();
        result.success(true);
    }
    
    private void handleGetAdNetworks(MethodChannel.Result result) {
        String[] networks = {
            "AppLovin",
            "Facebook",
            "Pangle",
            "Google AdMob",
            "Adjoe",
            "Ogury",
            "Snap",
            "IronSource"
        };
        result.success(networks);
    }
    
    public void invokeFlutterMethod(String methodName, Object argument) {
        methodChannel.invokeMethod(methodName, argument, new MethodChannel.Result() {
            @Override
            public void success(Object result) {
                Log.d(TAG, "Flutter method " + methodName + " succeeded");
            }
            
            @Override
            public void error(String errorCode, String errorMessage, Object errorDetails) {
                Log.e(TAG, "Flutter method " + methodName + " failed: " + errorMessage);
            }
            
            @Override
            public void notImplemented() {
                Log.w(TAG, "Flutter method " + methodName + " not implemented");
            }
        });
    }
}
