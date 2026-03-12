// EarnRewards/src/com/ykapps/earnrewards/models/User.java

package com.ykapps.earnrewards.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    public String userId;
    public String email;
    public String displayName;
    public long balance;
    public long createdAt;
    public long lastActive;
    public String referralCode;
    public int totalVideosWatched;
    public int totalReferrals;
    
    public User() {
        // Default constructor required for deserialization from database
    }
    
    public User(String userId, String email, String displayName) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.balance = 0;
        this.createdAt = System.currentTimeMillis();
        this.lastActive = System.currentTimeMillis();
        this.referralCode = generateReferralCode();
        this.totalVideosWatched = 0;
        this.totalReferrals = 0;
    }
    
    private String generateReferralCode() {
        return "ER" + System.currentTimeMillis() + (int)(Math.random() * 10000);
    }
}

// EarnRewards/src/com/ykapps/earnrewards/models/Reward.java

package com.ykapps.earnrewards.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Reward {
    public String rewardId;
    public String type;              // "rewarded_video", "banner_click", "referral"
    public int amount;
    public String status;            // "completed", "pending", "failed"
    public long timestamp;
    public String metadata;          // ad network, referred user, etc
    
    public Reward() {
        // Default constructor required for deserialization
    }
    
    public Reward(String type, int amount, String metadata) {
        this.rewardId = System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
        this.type = type;
        this.amount = amount;
        this.status = "completed";
        this.timestamp = System.currentTimeMillis();
        this.metadata = metadata;
    }
}

// EarnRewards/src/com/ykapps/earnrewards/models/Transaction.java

package com.ykapps.earnrewards.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Transaction {
    public String transactionId;
    public String userId;
    public String type;              // "rewarded_video", "banner_click", "referral", "withdrawal"
    public int amount;
    public String metadata;
    public long timestamp;
    public String status;            // "completed", "pending", "failed"
    
    public Transaction() {
        // Default constructor required for deserialization
    }
    
    public Transaction(String userId, String type, int amount, String metadata) {
        this.transactionId = System.currentTimeMillis() + "_" + userId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.metadata = metadata;
        this.timestamp = System.currentTimeMillis();
        this.status = "completed";
    }
}

// EarnRewards/src/com/ykapps/earnrewards/models/AdUnit.java

package com.ykapps.earnrewards.models;

public class AdUnit {
    public String adUnitId;
    public String network;           // "applovin", "facebook", "pangle", "google", etc
    public String format;            // "banner", "interstitial", "rewarded_video"
    public String status;            // "active", "inactive", "testing"
    public long createdAt;
    
    public AdUnit(String adUnitId, String network, String format) {
        this.adUnitId = adUnitId;
        this.network = network;
        this.format = format;
        this.status = "active";
        this.createdAt = System.currentTimeMillis();
    }
}
