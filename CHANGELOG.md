# Change Log

## 7.0.0

**Breaking Changes:**

- feat(android)!: bump library versions (#355)
  - `androidx.core:core:1.16.0`
  - `com.google.firebase:firebase-messaging:24.1.0`
- feat(ios)!: bump pod `Firebase/Messaging@11.8.0` (#356)

  **Note:** If you are using the Cordova-iOS platform version 7.x or lower, the `deployment-target` config preference must be set to at least `13.0` to work with [`Firebase/Messaging@11.8.0`](https://github.com/CocoaPods/Specs/blob/master/Specs/2/d/6/FirebaseMessaging/11.8.0/FirebaseMessaging.podspec.json#L19).

  Example `config.xml`:

  ```xml
  <platform name="ios">
    <preference name="deployment-target" value="13.0" />
  </platform>
  ```

- fix(android)!: also clear notifications when `clearBadge` is `true` (#358)

  This fix ensures that notifications in the message center are also cleared when the clearBadge flag is set to true.

  Previously, the flag was only documented to clear the badge counter (or dots, in Android’s case). However, it was not possible to clear only the badge on Android. The badge counter/dot is not fully independent of notifications—attempting to clear the badge may fail if the associated notification is not also cleared. The behavior can also vary depending on the Android version or device.

  Additionally, there was an inconsistency between iOS and Android: iOS cleared both the badge and the notification, while Android attempted to clear only the badge.

  This update aligns the behavior across platforms, making it more consistent.

- chore!: update dependencies (#360)

  While this commit is marked as a breaking change, it primarily affects plugin development, as it increases the required Node.js engine version. This should not impact plugin usage directly, but it is still recommended to use the latest supported [Node.js release](https://nodejs.org/en/about/previous-releases), preferably the Long-Term Support (LTS) release.

**Chores, CI, & Docs:**

- chore: update `package-lock.json` (#361)
- chore: move token higher and cleanup exception block (#359)
- ci: update workflow (#362)
- doc: clean & update

## 6.0.1

Forgot to update the version in `plugin.xml`.

## 6.0.0

**Security Release Notes:**

To strengthen application security, we have updated the `android:exported` flag for all relevant activities and services to `false`.

Affected components:

* `PushHandlerActivity` (Activity)
* `BackgroundHandlerActivity` (Activity)
* `com.adobe.phonegap.push.FCMService` (Service)

Although **PushHandlerActivity** had already defined `android:permission` with a `protectionLevel` of `signature`, **BackgroundHandlerActivity** — which was originally a copy of `PushHandlerActivity` with minor changes — had inadvertently omitted this permission configuration.

We have now correctly added both the `<uses-permission>` and `<permission>` declarations to **BackgroundHandlerActivity** with `android:protectionLevel="signature"`. While these permissions are likely redundant given that all components are no longer exported, it will be an added safe-guard.

- fix(android)!: set exported to false (#353) #87
- fix(android): add BackgroundHandlerActivity protectionLevel signature (#261)

**Breaking Changes:**

- feat(android)!: remove some version overrides (#351)
    Rely on the Cordova-Android platform defaults for
      - Google Services (GradlePluginGoogleServicesVersion)
      - Kotlin (GradlePluginKotlinVersion)
    App developers can still override the version in config.xml if needed.
- fix(android)!: notification audio not to be controlled by ringtone (#352)

**Features:**

- feat(ios): add forceRegister option (#337)

**Fixes:**

- fix(android): replace initialize with pluginInitialize (#347)
- fix(android): catch all Firebase exceptions (#341)

## 5.0.5

**Fixes:**

- fix(ios): correctly set `foreground` flag when `forceShow` is enabled (#338)

## 5.0.4

- Updated the plugin version in `plugin.xml`. (It was missed in the last three patches :D)

## 5.0.3

**Important Note:**

This patch release fixes the implementation of the `forceShow` option on iOS to align with Android's behavior.The original goal was to provide identical functionality, but the iOS implementation included unintended platform-specific behavior. While this might feel like a breaking change from version 5.0.0 (2024/11/21), it is classified as a patch release because it corrects a bug where notifications were processed without user interaction and did not align with Android's implementation. This update eliminates the need for developers to handle platform-specific cases, ensuring consistency with Android.

**Fixes:**

- fix(ios): dont trigger `notificationReceived` on app reload (#333)
- fix(ios): `forceShow` to display toast but trigger event until tapped (#332)

## 5.0.2

**Fixes:**

- fix(android): add missing `import androidx.core.app.ActivityCompat` (#331)

## 5.0.1

**Fixes:**

- fix(ios): revert accidental change to category "`callback`" (#328)
- fix(ios): update reset flags placement in `notificationReceived` (#329)
- fix(ios): request new permissions based on ios config changes (#326)
- fix(android): prevent permission dialog appearing when already denied (#325)
- fix(ios): exclude configure if `FIRApp` is already configured (#321)
- fix(ios): reset flags & store message after processing notification received (#319)
- fix(ios): make sure `notificationMessage` is immutable (#317)

**Others:**

- chore(*): typings - update comments & iOS `forceShow` option (#327)
- ci: replace lock app with lock-threads workflow

## 5.0.0

**Breaking:**

- chore(android)!: remove before compile hook script (#307)
- fix(ios)!: duplicate notification presentation on iOS 18.0 (#303)
- feat(ios)!: move `AppDelegate` logic to `PushPlugin.m` (#300)
- feat(ios)!: bump `firebase@10.24.0` (#294)
- feat(ios)!: extract FCM related logic to `PushPluginFCM` (#293)
- chore(ios)!: remove unused & deprecated code & styling formatting (#291)

**Features:**

- feat(ios): extract settings into `PushPluginSettings` (#292)
- feat(ios): implement `forceShow` (#276)

**Fixes:**

- fix(android): `getCircleBitmap` not displaying image (#311)
- fix(*): add `clearNotification` to typescript definitions (#309)
- fix(ios): on notification event payload & stop background task (#301)
- fix(ios): run `setApplicationIconBadgeNumber` on main thread (#302)
- fix(android): clipped notification text and expand arrow (#286)
- fix(ios): add missing `critical` to typings (#271)

**Others:**

- chore: bump plugin.xml to 5.0.0 (#310)
- chore: revert back to `AppDelegate` (#306)
- chore(ios): update all NSLog to include `[PushPlugin]` prefix (#290)
- doc: update link to iOS payload keys (#312)
- doc: formatting and including 5.0.0 (#308)

## 4.0.0

**Breaking:**

- feat(android)!: bump platform requirement cordova-android>=12.0.0 (#243)
- feat!(ios): update Firebase Messaging to ~> 8.1.1 (#152)
- fix(windows)!: remove deprecated platform (#245)

**Features:**

- feat(android): bump gradle plugin kotlin to 1.7.21 (#246)
- feat(android): bump GradlePluginGoogleServicesVersion to 4.3.15 (match w/ Cordova-Android@12.x) (#244)
- feat(android): support targetSdkVersion >= 31 (Android 12) (#185)

**Fixes:**

- fix(ios): callback not called when foreground is true #101 (#102)
- fix(android): deprecated warning Html.fromHtml (#230)
- fix(android): Ask for POST_NOTIFICATIONS permission if necessary (#238)
- fix(android): PushHandlerActivity permissions regression (#183)

**Others:**

- dep: resolve audit with rebuilt package-lock & rebuilt push.js with new packages (#248)
- dep(npm): bump all devDependencies (#241)
- ci: bump github action workflow and dependencies (#242)

## 3.0.1

**Fixes:**

* fix(android): add service exported flags w/ intent-filter (#173)
* fix(android): processPushBundle flipped returned value (#160)
* fix: doc typo (#159)

**Chores:**

* chore: correct Bower metadata to MIT license (#166)
* chore(npm): rebuilt package-lock.json & fix audit

**Docs:**

* doc: add 2.x and 3.x to PLATFORM_SUPPORT.md

## 3.0.0

**Overview:**

In this major release, there are many breaking changes to the Android platform. The primary changes in this release are dropping Android Support Library for AndroidX, updating the Google Services Gradle Plugin requirement, refactoring the code to Kotlin, and upgrading all other third-party libraries. See below for more details.

---

**Requirement Changes:**

* Cordova-Android: 9.x

---

**Libraries & Dependencie Updates and Changes:**

* Bumped `me.leolin:ShortcutBadger@1.1.22`
* Bumped `Google Services Gradle Plugin@4.3.8`
* Migrated from **Android Support Library** to **AndroidX**
  * Added `androidx.core:core@1.6.+`

---

**Breaking:**

* feat!(android): migrate source code to kotlin w/ refactors (#123)
  * kotlin@1.5.20
  * add before_compile hookscript for cordova-android 9.x support
  * converted all java files to kotlin
  * updted target-dir path for kotlin files
  * refactored with improved null checks
  * removed some duplicated code
  * updated logging (more logging & better tagging)¥
* feat!(android): bump platform requirements (#122)
* feat!(android): bump me.leolin:ShortcutBadger@1.1.22 (#121)
* feat!(android): bump Google Services Gradle Plugin@4.3.8 (#120)
* feat!(android): Migrate to AndroidX (#119)
  * feat!(android): migrate ASL to AndroidX
  * feat!(android): swap framework support-v13 w/ androidx.core
  * feat!(android): force AndroidXEnabled to true
  * feat!(android): bump androidx.core:core@1.6.+
  * doc(android): add androidx core version

**Fixes:**

* fix(docs): update TS type import to new package name (#109)

**Chores:**

* chore: rebuilt package-lock.json (#131)
* chore: bump npm dev dependencies (#132) (#133)
* chore(npm): bump devDep, rebuild package-lock & fix audit (#110)
* chore(npm): bump devDep @cordova/eslint-config@^4.0.0 w/ fixes (#144)

**CI:**

* ci: remove old travis configs (#128)
* ci: add codacy badge (#129)
* ci: add gh-actions badge to readme (#130)

**Docs:**

* doc: fixed minor typo (#98)

## 2.0.0

**Overview:**

This release contains breaking changes. One of these needed changes resolved issues of restoring the plugin from npm.

With this breaking change, the `havesource-cordova-plugin-push` package name is no longer used. Please completely uninstall the old version before installing the new version. This will ensure that the correct package name `@havesource/cordova-plugin-push` is used.

There is also an update to the installation requirements:

| | Version |
| - | - |
| Cordova CLI | 10.0.0 |
| Cordova Android | 8.0.0 |
| **Cordova iOS** | **6.0.0** |
| CocoaPods | 1.8.0 |

**Breaking:**

* breaking(ios): requirement bump [#80](https://github.com/havesource/cordova-plugin-push/pull/80)
* breaking: fixed incorrect initial cordova-cli requirement [79333b2](https://github.com/havesource/cordova-plugin-push/commit/79333b25e1ff68fea377be499da91528c82fa21f)

**Feature:**

* feat(ios): force `cocoapods` cdn [#48](https://github.com/havesource/cordova-plugin-push/pull/48)
* feat(ios): support `firebase/messaging` dep version override [#47](https://github.com/havesource/cordova-plugin-push/pull/47)

**Chore:**

* chore(`npm`): rebuilt `package-lock.json` [67e4e4b](https://github.com/havesource/cordova-plugin-push/commit/67e4e4ba185511e60b4d85cae882c41dae1c9cc0)
* chore(`android`): remove duplicate code [#81](https://github.com/havesource/cordova-plugin-push/pull/81)
* chore: bump dev dependencies [#79](https://github.com/havesource/cordova-plugin-push/pull/79)

**CI & Docs:**

* ci(gh-actions): bump dependencies [#78](https://github.com/havesource/cordova-plugin-push/pull/78)


## 1.0.0

**Breaking:**

* breaking(android): bump fcm@18.+ [#19](https://github.com/havesource/cordova-plugin-push/pull/19)
* breaking(android): drop phonegap-plugin-multidex dependency [#21](https://github.com/havesource/cordova-plugin-push/pull/21)
* breaking(android): move clearAllNotifications to destroy from pause [#13](https://github.com/havesource/cordova-plugin-push/pull/13)

**Feature:**

* feat(android): notification data pass [#31](https://github.com/havesource/cordova-plugin-push/pull/31)
* feat(ios): support critical alert notifications [#12](https://github.com/havesource/cordova-plugin-push/pull/12)
* feat(ios): increase firebase framework to 6.32.2 [#42](https://github.com/havesource/cordova-plugin-push/pull/42)
* feat: remove cordova-support-google-services dependency [#8](https://github.com/havesource/cordova-plugin-push/pull/8)

**Fix:**

* fix(android): missing channel description crash [#53](https://github.com/havesource/cordova-plugin-push/pull/53)
* fix(android): Use app name for default channel [#49](https://github.com/havesource/cordova-plugin-push/pull/49)
* fix(android): enable lights when lightColor is set [#15](https://github.com/havesource/cordova-plugin-push/pull/15)
* fix(browser): corrected path to manifest file. [#32](https://github.com/havesource/cordova-plugin-push/pull/32)

**Chore:**

* chore(android): set requirement >= 8.0.0 [#52](https://github.com/havesource/cordova-plugin-push/pull/52)
* chore(android): cleanup & format [#26](https://github.com/havesource/cordova-plugin-push/pull/26)
* chore(android): bump com.android.support:support-v13:28.0.0 [#20](https://github.com/havesource/cordova-plugin-push/pull/20)
* chore(ios): use latest firebase library [#24](https://github.com/havesource/cordova-plugin-push/pull/24)
* chore(npm): rebuilt package-lock.json [#55](https://github.com/havesource/cordova-plugin-push/pull/55)
* chore(npm): properly configure for scope package [#33](https://github.com/havesource/cordova-plugin-push/pull/33)
* chore(type-definition): Update PushNotificationStatic [#14](https://github.com/havesource/cordova-plugin-push/pull/14)
* chore(github-pages): remove config [#4](https://github.com/havesource/cordova-plugin-push/pull/4)
* chore: update ticket management [#27](https://github.com/havesource/cordova-plugin-push/pull/27)
* chore: add missing build of push.js [#22](https://github.com/havesource/cordova-plugin-push/pull/22)
* chore: match plugin.xml version w/ package.json [#10](https://github.com/havesource/cordova-plugin-push/pull/10)
* chore: update xml namespace [#9](https://github.com/havesource/cordova-plugin-push/pull/9)
* chore: update version requirements [#7](https://github.com/havesource/cordova-plugin-push/pull/7)
* chore: update npm & git ignore list [#6](https://github.com/havesource/cordova-plugin-push/pull/6)
* chore: update plugin package [#1](https://github.com/havesource/cordova-plugin-push/pull/1)
* chore: remove unused dependencies [#2](https://github.com/havesource/cordova-plugin-push/pull/2)

**Refactor & Style:**

* refactor(eslint): update dependencies w/ fixes [#3](https://github.com/havesource/cordova-plugin-push/pull/3)
* style(md): format with md all in one (vscode) [#11](https://github.com/havesource/cordova-plugin-push/pull/11)

**CI & Docs:**

* ci(gh-actions): add workflow [#23](https://github.com/havesource/cordova-plugin-push/pull/23)
* ci: update travis configs [#5](https://github.com/havesource/cordova-plugin-push/pull/5)
* doc(android): enable & set notification light with lightColor [#54](https://github.com/havesource/cordova-plugin-push/pull/54)
* doc: cleanup docs [#51](https://github.com/havesource/cordova-plugin-push/pull/51)
* doc: update various markdown docs [#28](https://github.com/havesource/cordova-plugin-push/pull/28)

## Previous Change Log

Since this repo is a fork from the original Adobe PhoneGap push plugin, you can continue to read the previous changelog here:

https://github.com/havesource/cordova-plugin-push/blob/phonegap-2.3.0/CHANGELOG.md
