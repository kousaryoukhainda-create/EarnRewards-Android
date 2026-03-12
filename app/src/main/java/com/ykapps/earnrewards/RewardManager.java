// EarnRewards/src/com/ykapps/earnrewards/RewardManager.java
// Reconstructed from APK analysis - Reward & Firebase management

package com.ykapps.earnrewards;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import android.os.Bundle;
import java.util.HashMap;
import java.util.Map;

/**
 * RewardManager - Handles user reward calculations and Firebase database operations
 */
public class RewardManager {
    private static final String TAG = "RewardManager";
    
    private final Context context;
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseRef;
    private final FirebaseAnalytics firebaseAnalytics;
    
    // Database paths
    private static final String USERS_PATH = "users";
    private static final String REWARDS_PATH = "rewards";
    private static final String TRANSACTIONS_PATH = "transactions";
    private static final String SETTINGS_PATH = "settings";
    
    // Reward configuration
    private static final int REWARDED_VIDEO_REWARD = 10; // 10 coins per video
    private static final int BANNER_AD_REWARD = 1;       // 1 coin per banner click
    private static final int REFERRAL_REWARD = 50;       // 50 coins per referral
    
    private RewardListener rewardListener;
    
    public interface RewardListener {
        void onRewardAdded(int amount, String type);
        void onBalanceUpdated(int newBalance);
        void onError(String error);
    }
    
    public RewardManager(Context context) {
        this.context = context;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }
    
    // ========================= REWARD ADDITIONS =========================
    
    /**
     * Add reward when user watches a rewarded video
     */
    public void addRewardedVideoReward(String adNetwork) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.w(TAG, "User not authenticated");
            if (rewardListener != null) {
                rewardListener.onError("User not authenticated");
            }
            return;
        }
        
        int rewardAmount = REWARDED_VIDEO_REWARD;
        
        // Record transaction
        recordTransaction(userId, "rewarded_video", rewardAmount, adNetwork);
        
        // Update user balance
        addToUserBalance(userId, rewardAmount);
        
        // Log to Firebase Analytics
        logRewardEvent("reward_earned", rewardAmount, "rewarded_video", adNetwork);
        
        // Notify listener
        if (rewardListener != null) {
            rewardListener.onRewardAdded(rewardAmount, "rewarded_video");
        }
        
        Log.d(TAG, "Rewarded video reward added: " + rewardAmount + " coins from " + adNetwork);
    }
    
    /**
     * Add reward when user clicks a banner ad
     */
    public void addBannerClickReward(String adNetwork) {
        String userId = getCurrentUserId();
        if (userId == null) return;
        
        int rewardAmount = BANNER_AD_REWARD;
        recordTransaction(userId, "banner_click", rewardAmount, adNetwork);
        addToUserBalance(userId, rewardAmount);
        logRewardEvent("reward_earned", rewardAmount, "banner_click", adNetwork);
        
        if (rewardListener != null) {
            rewardListener.onRewardAdded(rewardAmount, "banner_click");
        }
    }
    
    /**
     * Add reward for referral
     */
    public void addReferralReward(String referredUserId) {
        String userId = getCurrentUserId();
        if (userId == null) return;
        
        int rewardAmount = REFERRAL_REWARD;
        recordTransaction(userId, "referral", rewardAmount, referredUserId);
        addToUserBalance(userId, rewardAmount);
        logRewardEvent("reward_earned", rewardAmount, "referral", "user:" + referredUserId);
        
        if (rewardListener != null) {
            rewardListener.onRewardAdded(rewardAmount, "referral");
        }
    }
    
    // ========================= DATABASE OPERATIONS =========================
    
    /**
     * Update user's reward balance
     */
    private void addToUserBalance(String userId, int amount) {
        DatabaseReference userBalanceRef = databaseRef
            .child(USERS_PATH)
            .child(userId)
            .child("balance");
        
        userBalanceRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull DataSnapshot currentValue) {
                long currentBalance = currentValue.exists() ? 
                    currentValue.getValue(Long.class) : 0L;
                
                currentValue.getRef().setValue(currentBalance + amount);
                return com.google.firebase.database.Transaction.success(currentValue);
            }
            
            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentValue) {
                if (error != null) {
                    Log.e(TAG, "Balance update failed: " + error.getMessage());
                    if (rewardListener != null) {
                        rewardListener.onError(error.getMessage());
                    }
                } else {
                    long newBalance = currentValue.exists() ? 
                        currentValue.getValue(Long.class) : 0L;
                    
                    if (rewardListener != null) {
                        rewardListener.onBalanceUpdated((int) newBalance);
                    }
                    Log.d(TAG, "Balance updated: " + newBalance);
                }
            }
        });
    }
    
    /**
     * Record transaction in database
     */
    private void recordTransaction(String userId, String type, int amount, String metadata) {
        String transactionId = System.currentTimeMillis() + "_" + userId;
        
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("userId", userId);
        transaction.put("type", type);
        transaction.put("amount", amount);
        transaction.put("metadata", metadata);
        transaction.put("timestamp", com.google.firebase.database.ServerValue.TIMESTAMP);
        transaction.put("status", "completed");
        
        databaseRef
            .child(TRANSACTIONS_PATH)
            .child(userId)
            .child(transactionId)
            .setValue(transaction)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Transaction recorded: " + transactionId))
            .addOnFailureListener(e -> Log.e(TAG, "Transaction record failed: " + e.getMessage()));
    }
    
    /**
     * Get current user's balance
     */
    public void getUserBalance(BalanceCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onBalanceFetched(-1);
            return;
        }
        
        databaseRef
            .child(USERS_PATH)
            .child(userId)
            .child("balance")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long balance = snapshot.exists() ? 
                        snapshot.getValue(Long.class) : 0L;
                    callback.onBalanceFetched((int) balance);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Balance fetch failed: " + error.getMessage());
                    callback.onError(error.getMessage());
                }
            });
    }
    
    /**
     * Listen for real-time balance updates
     */
    public void listenToBalanceUpdates(BalanceCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) return;
        
        databaseRef
            .child(USERS_PATH)
            .child(userId)
            .child("balance")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long balance = snapshot.exists() ? 
                        snapshot.getValue(Long.class) : 0L;
                    callback.onBalanceFetched((int) balance);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Balance listener error: " + error.getMessage());
                    callback.onError(error.getMessage());
                }
            });
    }
    
    /**
     * Get user's reward history
     */
    public void getRewardHistory(HistoryCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) return;
        
        databaseRef
            .child(TRANSACTIONS_PATH)
            .child(userId)
            .orderByChild("timestamp")
            .limitToLast(50) // Last 50 transactions
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    callback.onHistoryFetched(snapshot);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "History fetch failed: " + error.getMessage());
                }
            });
    }
    
    /**
     * Calculate total earnings
     */
    public void calculateTotalEarnings(EarningsCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) return;
        
        databaseRef
            .child(TRANSACTIONS_PATH)
            .child(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int totalCoins = 0;
                    int videoAdsWatched = 0;
                    int referralsCount = 0;
                    
                    for (DataSnapshot transaction : snapshot.getChildren()) {
                        Integer amount = transaction.child("amount").getValue(Integer.class);
                        String type = transaction.child("type").getValue(String.class);
                        
                        if (amount != null) {
                            totalCoins += amount;
                            
                            if ("rewarded_video".equals(type)) {
                                videoAdsWatched++;
                            } else if ("referral".equals(type)) {
                                referralsCount++;
                            }
                        }
                    }
                    
                    callback.onEarningsCalculated(totalCoins, videoAdsWatched, referralsCount);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Earnings calculation failed: " + error.getMessage());
                }
            });
    }
    
    // ========================= ANALYTICS =========================
    
    private void logRewardEvent(String eventName, int amount, String type, String source) {
        Bundle params = new Bundle();
        params.putInt("reward_amount", amount);
        params.putString("reward_type", type);
        params.putString("reward_source", source);
        params.putString("timestamp", String.valueOf(System.currentTimeMillis()));
        
        firebaseAnalytics.logEvent(eventName, params);
    }
    
    // ========================= HELPER METHODS =========================
    
    private String getCurrentUserId() {
        if (firebaseAuth.getCurrentUser() != null) {
            return firebaseAuth.getCurrentUser().getUid();
        }
        return null;
    }
    
    public void setRewardListener(RewardListener listener) {
        this.rewardListener = listener;
    }
    
    // Callback interfaces
    public interface BalanceCallback {
        void onBalanceFetched(int balance);
        void onError(String error);
    }
    
    public interface HistoryCallback {
        void onHistoryFetched(DataSnapshot snapshot);
    }
    
    public interface EarningsCallback {
        void onEarningsCalculated(int totalCoins, int videosWatched, int referrals);
    }
}
