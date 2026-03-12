// EarnRewards/src/com/ykapps/earnrewards/platform/RewardMethodChannel.java

package com.ykapps.earnrewards.platform;

import android.util.Log;
import androidx.annotation.NonNull;
import com.ykapps.earnrewards.RewardManager;
import com.google.firebase.database.DataSnapshot;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.plugin.common.MethodChannel;

/**
 * RewardMethodChannel - Handles Flutter ↔ Java communication for reward operations
 */
public class RewardMethodChannel {
    
    private static final String CHANNEL_NAME = "com.ykapps.earnrewards/rewards";
    private static final String TAG = "RewardMethodChannel";
    
    private final MethodChannel methodChannel;
    private final RewardManager rewardManager;
    
    public RewardMethodChannel(DartExecutor dartExecutor, RewardManager rewardManager) {
        this.rewardManager = rewardManager;
        this.methodChannel = new MethodChannel(dartExecutor, CHANNEL_NAME);
        setupMethodHandler();
        setupRewardListener();
    }
    
    private void setupMethodHandler() {
        methodChannel.setMethodCallHandler((call, result) -> {
            try {
                switch (call.method) {
                    case "getBalance":
                        handleGetBalance(result);
                        break;
                    
                    case "getHistory":
                        handleGetHistory(result);
                        break;
                    
                    case "calculateEarnings":
                        handleCalculateEarnings(result);
                        break;
                    
                    case "addRewardedVideoReward":
                        String network = call.argument("network");
                        handleAddRewardedVideoReward(network, result);
                        break;
                    
                    case "addBannerClickReward":
                        String adNetwork = call.argument("network");
                        handleAddBannerClickReward(adNetwork, result);
                        break;
                    
                    case "addReferralReward":
                        String referredUserId = call.argument("referredUserId");
                        handleAddReferralReward(referredUserId, result);
                        break;
                    
                    case "listenToBalanceUpdates":
                        handleListenToBalanceUpdates(result);
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
    
    private void setupRewardListener() {
        rewardManager.setRewardListener(new RewardManager.RewardListener() {
            @Override
            public void onRewardAdded(int amount, String type) {
                invokeFlutterMethod("onRewardAdded", createMap("amount", amount, "type", type));
            }
            
            @Override
            public void onBalanceUpdated(int newBalance) {
                invokeFlutterMethod("onBalanceUpdated", newBalance);
            }
            
            @Override
            public void onError(String error) {
                invokeFlutterMethod("onRewardError", error);
            }
        });
    }
    
    private void handleGetBalance(MethodChannel.Result result) {
        rewardManager.getUserBalance(new RewardManager.BalanceCallback() {
            @Override
            public void onBalanceFetched(int balance) {
                result.success(balance);
            }
            
            @Override
            public void onError(String error) {
                result.error("FETCH_FAILED", error, null);
            }
        });
    }
    
    private void handleGetHistory(MethodChannel.Result result) {
        rewardManager.getRewardHistory(new RewardManager.HistoryCallback() {
            @Override
            public void onHistoryFetched(DataSnapshot snapshot) {
                // Convert Firebase DataSnapshot to a list
                // This would need proper serialization in production
                result.success("History retrieved successfully");
            }
        });
    }
    
    private void handleCalculateEarnings(MethodChannel.Result result) {
        rewardManager.calculateTotalEarnings(new RewardManager.EarningsCallback() {
            @Override
            public void onEarningsCalculated(int totalCoins, int videosWatched, int referrals) {
                java.util.HashMap<String, Object> earnings = new java.util.HashMap<>();
                earnings.put("totalCoins", totalCoins);
                earnings.put("videosWatched", videosWatched);
                earnings.put("referrals", referrals);
                result.success(earnings);
            }
        });
    }
    
    private void handleAddRewardedVideoReward(String network, MethodChannel.Result result) {
        rewardManager.addRewardedVideoReward(network);
        result.success(true);
    }
    
    private void handleAddBannerClickReward(String network, MethodChannel.Result result) {
        rewardManager.addBannerClickReward(network);
        result.success(true);
    }
    
    private void handleAddReferralReward(String referredUserId, MethodChannel.Result result) {
        rewardManager.addReferralReward(referredUserId);
        result.success(true);
    }
    
    private void handleListenToBalanceUpdates(MethodChannel.Result result) {
        rewardManager.listenToBalanceUpdates(new RewardManager.BalanceCallback() {
            @Override
            public void onBalanceFetched(int balance) {
                invokeFlutterMethod("onBalanceUpdated", balance);
            }
            
            @Override
            public void onError(String error) {
                invokeFlutterMethod("onBalanceError", error);
            }
        });
        result.success(true);
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
    
    private java.util.HashMap<String, Object> createMap(String key1, Object value1, 
                                                         String key2, Object value2) {
        java.util.HashMap<String, Object> map = new java.util.HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }
}
