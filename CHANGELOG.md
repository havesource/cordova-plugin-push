# Change Log

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
