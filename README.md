Rupeedesk7 User App - Full Prototype (Kotlin + Jetpack Compose)

What's included:
- Jetpack Compose app with multiple screens: Login, Dashboard, Profile, Spin, Withdraw
- SmsWorker using WorkManager with Firestore transaction to claim inventory
- SIM selection UI (reads subscription list; saving simId to user doc)
- Referral/spin system UI and prototype logic (client-side random spin)
- Withdrawal request creation (stored in 'withdrawals' collection)
- Firebase integration: place your google-services.json in app/ (already copied if provided)
- Build files and README

Important notes before production:
- This is a prototype and needs security hardening: Firestore Rules, Auth for users, server-side validation for spins and referral rewards.
- Test SMS sending only on physical devices with consent.
- For Play Store, follow SMS permissions & policy requirements.

Build:
1. Generate gradle wrapper if missing: gradle wrapper
2. chmod +x ./gradlew
3. ./gradlew assembleDebug
4. Install via adb: adb install -r app/build/outputs/apk/debug/app-debug.apk
