# EarnRewards APK Build Guide

## Prerequisites
- Android Studio (2023.1+)
- JDK 11+
- Android SDK 34

## Quick Build Steps

### 1. Download google-services.json
1. Go to Firebase Console
2. Download google-services.json
3. Place in root directory of this project

### 2. Update API Keys
1. Open `app/src/main/res/values/strings.xml`
2. Replace placeholder API keys:
   - `applovin_sdk_key`
   - `ad_unit_rewarded`
   - `firebase_project_id`
   - `appsflyer_dev_key`

### 3. Build Debug APK
```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### 4. Build Release APK
```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

## Gradle Wrapper
Gradle 8.0 is included via the wrapper. Just run `./gradlew` or `gradlew.bat`

## Directory Structure
```
EarnRewards_APK_Build/
├── app/                              # Main app module
│   ├── src/main/
│   │   ├── java/                     # Java source code
│   │   ├── res/                      # Resources (strings, colors, layouts)
│   │   └── AndroidManifest.xml       # App manifest
│   ├── build.gradle                  # App build config
│   └── proguard-rules.pro           # Code obfuscation rules
├── gradle/                           # Gradle configuration
├── settings.gradle                   # Project settings
└── README_BUILD.md                   # This file
```

## Dependencies
All dependencies are configured in `app/build.gradle`:
- Firebase (Analytics, Database, Auth, Messaging)
- AppLovin MAX (Ad mediation)
- Google Play Services
- AppsFlyer SDK
- And more...

## Troubleshooting

**Build fails with "google-services.json not found"**
→ Download from Firebase Console and place in root directory

**Cannot resolve symbol 'R'**
→ Run `./gradlew clean` then rebuild

**Gradle timeout**
→ Increase timeout in gradle.properties:
   org.gradle.jvmargs=-Xmx4096m

## Support
See SETUP_GUIDE.md for detailed instructions
