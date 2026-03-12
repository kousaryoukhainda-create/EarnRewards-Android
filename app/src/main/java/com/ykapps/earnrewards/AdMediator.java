// EarnRewards/src/com/ykapps/earnrewards/AdMediator.java
// Reconstructed from APK analysis - Ad network coordination layer

package com.ykapps.earnrewards;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxBannerAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import java.util.concurrent.TimeUnit;

/**
 * AdMediator - Orchestrates ad display across multiple networks
 * Uses AppLovin MAX for header bidding and mediation
 */
public class AdMediator implements MaxAdListener, MaxRewardedAdListener {
    private static final String TAG = "AdMediator";
    
    // Ad Unit IDs (typically stored in Firebase Remote Config in production)
    private static final String BANNER_AD_UNIT_ID = "YOUR_BANNER_AD_UNIT_ID";
    private static final String MREC_AD_UNIT_ID = "YOUR_MREC_AD_UNIT_ID";
    private static final String INTERSTITIAL_AD_UNIT_ID = "YOUR_INTERSTITIAL_AD_UNIT_ID";
    private static final String REWARDED_AD_UNIT_ID = "YOUR_REWARDED_AD_UNIT_ID";
    
    private final Context context;
    private MaxBannerAdView bannerAdView;
    private MaxInterstitialAd interstitialAd;
    private MaxRewardedAd rewardedAd;
    private AdDisplayCallback adCallback;
    
    // Retry counters
    private int bannerRetryAttempt = 0;
    private int interstitialRetryAttempt = 0;
    private int rewardedRetryAttempt = 0;
    
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 3000; // 3 seconds
    
    public interface AdDisplayCallback {
        void onAdLoaded(AdType adType);
        void onAdFailedToLoad(AdType adType, String reason);
        void onAdDisplayed(AdType adType);
        void onAdClicked(AdType adType);
        void onRewardEarned(int rewardAmount);
    }
    
    public enum AdType {
        BANNER, MREC, INTERSTITIAL, REWARDED
    }
    
    public AdMediator(Context context) {
        this.context = context;
        initializeAds();
    }
    
    private void initializeAds() {
        initializeBannerAd();
        initializeInterstitialAd();
        initializeRewardedAd();
    }
    
    // ========================= BANNER ADS =========================
    
    private void initializeBannerAd() {
        bannerAdView = new MaxBannerAdView(BANNER_AD_UNIT_ID, context);
        bannerAdView.setListener(this);
        
        // Banner position (bottom of screen)
        int bannerHeight = dpToPx(50);
        bannerAdView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            bannerHeight
        ));
        
        bannerAdView.loadAd();
    }
    
    public void showBannerAd() {
        if (bannerAdView != null) {
            bannerAdView.setVisibility(android.view.View.VISIBLE);
        }
    }
    
    public void hideBannerAd() {
        if (bannerAdView != null) {
            bannerAdView.setVisibility(android.view.View.GONE);
        }
    }
    
    // ========================= INTERSTITIAL ADS =========================
    
    private void initializeInterstitialAd() {
        interstitialAd = new MaxInterstitialAd(INTERSTITIAL_AD_UNIT_ID, context);
        interstitialAd.setListener(this);
    }
    
    public void loadInterstitialAd() {
        if (interstitialAd != null && !interstitialAd.isReady()) {
            interstitialAd.loadAd();
            Log.d(TAG, "Interstitial ad loading...");
        }
    }
    
    public void showInterstitialAd() {
        if (interstitialAd != null && interstitialAd.isReady()) {
            interstitialAd.showAd("earnrewards");
            Log.d(TAG, "Interstitial ad shown");
        } else {
            Log.w(TAG, "Interstitial ad not ready");
            loadInterstitialAd();
        }
    }
    
    public boolean isInterstitialReady() {
        return interstitialAd != null && interstitialAd.isReady();
    }
    
    // ========================= REWARDED ADS =========================
    
    private void initializeRewardedAd() {
        rewardedAd = MaxRewardedAd.getInstance(REWARDED_AD_UNIT_ID, context);
        rewardedAd.setListener(this);
    }
    
    public void loadRewardedAd() {
        if (rewardedAd != null && !rewardedAd.isReady()) {
            rewardedAd.loadAd();
            Log.d(TAG, "Rewarded ad loading...");
        }
    }
    
    public void showRewardedAd() {
        if (rewardedAd != null && rewardedAd.isReady()) {
            rewardedAd.showAd("earnrewards");
            Log.d(TAG, "Rewarded ad shown");
        } else {
            Log.w(TAG, "Rewarded ad not ready");
            if (adCallback != null) {
                adCallback.onAdFailedToLoad(AdType.REWARDED, "Ad not loaded");
            }
        }
    }
    
    public boolean isRewardedAdReady() {
        return rewardedAd != null && rewardedAd.isReady();
    }
    
    // ========================= MAXADLISTENER CALLBACKS =========================
    
    @Override
    public void onAdLoaded(@NonNull MaxAd ad) {
        Log.d(TAG, "Ad loaded: " + ad.getFormat().getLabel());
        
        if (ad.getFormat().isBanner()) {
            bannerRetryAttempt = 0;
            if (adCallback != null) {
                adCallback.onAdLoaded(AdType.BANNER);
            }
        } else if (ad.getFormat().isInterstitial()) {
            interstitialRetryAttempt = 0;
            if (adCallback != null) {
                adCallback.onAdLoaded(AdType.INTERSTITIAL);
            }
        } else if (ad.getFormat().isRewarded()) {
            rewardedRetryAttempt = 0;
            if (adCallback != null) {
                adCallback.onAdLoaded(AdType.REWARDED);
            }
        }
    }
    
    @Override
    public void onAdLoadFailed(@NonNull String adUnitId, @NonNull MaxError error) {
        Log.e(TAG, "Ad load failed: " + adUnitId + " - " + error.getCode() + ": " + error.getMessage());
        
        // Retry with exponential backoff
        if (adUnitId.equals(BANNER_AD_UNIT_ID) && bannerRetryAttempt < MAX_RETRIES) {
            long delay = RETRY_DELAY_MS * (long) Math.pow(2, bannerRetryAttempt);
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                this::initializeBannerAd,
                delay
            );
            bannerRetryAttempt++;
        } else if (adUnitId.equals(INTERSTITIAL_AD_UNIT_ID) && interstitialRetryAttempt < MAX_RETRIES) {
            long delay = RETRY_DELAY_MS * (long) Math.pow(2, interstitialRetryAttempt);
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                this::initializeInterstitialAd,
                delay
            );
            interstitialRetryAttempt++;
        } else if (adUnitId.equals(REWARDED_AD_UNIT_ID) && rewardedRetryAttempt < MAX_RETRIES) {
            long delay = RETRY_DELAY_MS * (long) Math.pow(2, rewardedRetryAttempt);
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                this::initializeRewardedAd,
                delay
            );
            rewardedRetryAttempt++;
        }
        
        if (adCallback != null) {
            adCallback.onAdFailedToLoad(AdType.valueOf(adUnitId), error.getMessage());
        }
    }
    
    @Override
    public void onAdDisplayed(@NonNull MaxAd ad) {
        Log.d(TAG, "Ad displayed: " + ad.getFormat().getLabel());
        
        if (ad.getFormat().isRewarded()) {
            if (adCallback != null) {
                adCallback.onAdDisplayed(AdType.REWARDED);
            }
        }
    }
    
    @Override
    public void onAdDisplayFailed(@NonNull MaxAd ad, @NonNull MaxError error) {
        Log.e(TAG, "Ad display failed: " + error.getMessage());
        
        if (ad.getFormat().isRewarded()) {
            rewardedRetryAttempt = 0; // Reset on display failure
        }
    }
    
    @Override
    public void onAdClicked(@NonNull MaxAd ad) {
        Log.d(TAG, "Ad clicked: " + ad.getFormat().getLabel());
        
        if (adCallback != null) {
            if (ad.getFormat().isBanner()) {
                adCallback.onAdClicked(AdType.BANNER);
            } else if (ad.getFormat().isRewarded()) {
                adCallback.onAdClicked(AdType.REWARDED);
            }
        }
    }
    
    @Override
    public void onAdHidden(@NonNull MaxAd ad) {
        Log.d(TAG, "Ad hidden: " + ad.getFormat().getLabel());
        
        // Reload ads that require it
        if (ad.getFormat().isInterstitial()) {
            loadInterstitialAd();
        } else if (ad.getFormat().isRewarded()) {
            loadRewardedAd();
        }
    }
    
    // ========================= MAXREWARDEDADLISTENER CALLBACKS =========================
    
    @Override
    public void onRewardedVideoStarted(@NonNull MaxAd ad) {
        Log.d(TAG, "Rewarded video started");
    }
    
    @Override
    public void onRewardedVideoCompleted(@NonNull MaxAd ad) {
        Log.d(TAG, "Rewarded video completed");
    }
    
    @Override
    public void onUserRewarded(@NonNull MaxAd ad, @NonNull MaxReward reward) {
        Log.d(TAG, "User rewarded: " + reward.getAmount() + " " + reward.getLabel());
        
        if (adCallback != null) {
            adCallback.onRewardEarned(reward.getAmount());
        }
    }
    
    // ========================= HELPER METHODS =========================
    
    public void setAdCallback(AdDisplayCallback callback) {
        this.adCallback = callback;
    }
    
    public void destroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
