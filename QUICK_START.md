# 🚀 Quick Start - Build APK in 5 Minutes

## What You Have
Complete Android source code ready to build into an APK.

## What You Need to Do

### Step 1: Get Firebase Configuration (2 min)
```bash
# Go to https://console.firebase.google.com
# Download google-services.json
# Place it in the root directory of this project
```

### Step 2: Update API Keys (1 min)
Edit: `app/src/main/res/values/strings.xml`

Find and update:
```xml
<string name="applovin_sdk_key">YOUR_SDK_KEY</string>
<string name="ad_unit_rewarded">YOUR_AD_UNIT</string>
<string name="appsflyer_dev_key">YOUR_KEY</string>
```

### Step 3: Build (2 min)
```bash
# Build debug APK
./gradlew assembleDebug

# APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

### Step 4: Test on Device
```bash
# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```

## That's It! 🎉

Your EarnRewards app is now built and running!

## Next Steps

For production release:
1. Create signing keystore
2. Update build.gradle with signing config
3. Build release APK: `./gradlew assembleRelease`
4. Sign and upload to Google Play Store

See SETUP_GUIDE.md for detailed instructions.
