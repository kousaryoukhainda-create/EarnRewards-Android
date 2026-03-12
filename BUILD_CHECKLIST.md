# EarnRewards Build Checklist

## Pre-Build Setup

- [ ] Download `google-services.json` from Firebase Console
- [ ] Place `google-services.json` in project root directory
- [ ] Open `app/src/main/res/values/strings.xml`
- [ ] Update AppLovin SDK key
- [ ] Update ad unit IDs
- [ ] Update AppsFlyer dev key
- [ ] Update Firebase configuration (if using custom Firebase project)
- [ ] Install Android SDK 34 (if not already installed)
- [ ] Have JDK 11 or higher installed

## Build Steps

### Debug Build (for testing)

```bash
# Step 1: Verify gradle is executable
chmod +x gradlew

# Step 2: Clean project
./gradlew clean

# Step 3: Build debug APK
./gradlew assembleDebug

# Step 4: Check output
# Look in: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build (for Play Store)

```bash
# Step 1: Create keystore
keytool -genkey -v -keystore earnrewards.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias earnrewards

# Step 2: Update app/build.gradle with signing config
# (See SETUP_GUIDE.md for details)

# Step 3: Build release APK
./gradlew assembleRelease

# Step 4: Check output
# Look in: app/build/outputs/apk/release/app-release.apk

# Step 5: Build App Bundle (recommended for Play Store)
./gradlew bundleRelease

# Look in: app/build/outputs/bundle/release/app-release.aab
```

## Troubleshooting

### Build Fails: "google-services.json not found"
**Solution:** Download from Firebase Console and place in root directory

### Build Fails: "Cannot resolve symbol 'R'"
**Solution:** Run `./gradlew clean` and rebuild

### Build Fails: "Out of memory"
**Solution:** Increase Gradle heap size in gradle.properties:
```
org.gradle.jvmargs=-Xmx4096m
```

### Build Hangs: Gradle downloading dependencies
**Solution:** 
- Check internet connection
- Try: `./gradlew --offline` (if dependencies are cached)
- Or: `./gradlew --refresh-dependencies`

## Testing on Device

### Install Debug APK
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### View Logs
```bash
adb logcat | grep EarnRewards
```

### Uninstall
```bash
adb uninstall com.ykapps.earnrewards.debug
```

## File Structure Verification

Make sure these files exist before building:

- ✅ `app/build.gradle` - Build configuration
- ✅ `app/src/main/AndroidManifest.xml` - App manifest
- ✅ `app/src/main/res/values/strings.xml` - String resources
- ✅ `app/src/main/res/values/colors.xml` - Color resources
- ✅ `app/src/main/java/com/ykapps/earnrewards/MainActivity.java` - Main activity
- ✅ `google-services.json` - Firebase config (must download)
- ✅ `settings.gradle` - Project settings
- ✅ `gradle/wrapper/gradle-wrapper.properties` - Gradle wrapper

## Post-Build

### APK Location
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`
- Bundle: `app/build/outputs/bundle/release/app-release.aab`

### APK Size
- Debug: ~28 MB
- Release: ~26 MB (after optimization)

### Test Features
- [ ] App launches successfully
- [ ] No crashes on startup
- [ ] Firebase initializes
- [ ] Ad networks initialize
- [ ] Balance displays
- [ ] Ads load and display

## Next Steps

1. Successfully build debug APK
2. Test on device/emulator
3. Configure for production (signing)
4. Build release APK
5. Submit to Google Play Store

---
See SETUP_GUIDE.md for detailed instructions
