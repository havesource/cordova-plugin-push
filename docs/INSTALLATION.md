# Installation

- [Installation](#installation)
  - [Installation Requirements](#installation-requirements)
  - [Android details](#android-details)
    - [Co-existing with plugins that use Firebase](#co-existing-with-plugins-that-use-firebase)
  - [Browser details](#browser-details)
    - [Browser Support](#browser-support)
  - [iOS details](#ios-details)
    - [System \& Cordova Requirements](#system--cordova-requirements)
    - [CocoaPods](#cocoapods)
      - [Common CocoaPod Installation issues](#common-cocoapod-installation-issues)
        - [Library not found for -lPods-Appname](#library-not-found-for--lpods-appname)
        - [Library not found for -lGoogleToolboxForMac](#library-not-found-for--lgoogletoolboxformac)
  - [Additional Resources](#additional-resources)

## Installation Requirements

| Plugin version | Cordova CLI | Cordova Android | Cordova iOS | CocoaPods |
| -------------- | ----------- | --------------- | ----------- | --------- |
| 1.0.0          | 10.0.0      | 8.0.0           | 5.1.1       | 1.8.0     |
| 2.0.0          | 10.0.0      | 8.0.0           | 6.0.0       | 1.8.0     |
| 3.0.0          | 10.0.0      | 9.0.0           | 6.0.0       | 1.8.0     |
| 4.0.0          | 10.0.0      | 12.0.0          | 6.0.0       | 1.8.0     |
| 5.0.0          | 10.0.0      | 12.0.0          | 6.0.0       | 1.8.0     |
| 6.0.0          | 10.0.0      | 12.0.0          | 6.0.0       | 1.8.0     |
| 7.0.0          | 10.0.0      | 12.0.0          | 6.0.0       | 1.8.0     |

To install from the command line:

```bash
cordova plugin add @havesource/cordova-plugin-push
```

It is also possible to install via repo url directly (unstable)

or

```bash
cordova plugin add github:havesource/cordova-plugin-push
```

To configure Firebase, place your `google-services.json` (Android) and/or `GoogleService-Info.plist` in the root folder of your project and then add the following lines into your config.xml.

In the `platform` tag for Android add the following `resource-file` tag if you are using `cordova-android` 8.0 or greater:

E.g.

```xml
<platform name="android">
  <resource-file src="google-services.json" target="/app/google-services.json" />
</platform>
```

By default, on iOS, the plugin will register with APNS. If you want to use FCM on iOS, in the `platform` tag for iOS add the following `resource-file` tag:

```xml
<platform name="ios">
  <resource-file src="GoogleService-Info.plist" />
</platform>
```

## Android details

### Co-existing with plugins that use Firebase

Problems may arise when push plugin is used along plugins that implement Firebase functionality (e.g. `cordova-plugin-firebase-analytics`). Both plugins include a version of the FCM libraries.

To work together, make sure your `FCM_VERSION` variable matches the version used in the other plugin. In case of Cordova, your `package.json` contains something like this:

```json
{
  "cordova": {
    "plugins": {
      "@havesource/cordova-plugin-push": {
        "ANDROIDX_CORE_VERSION": "1.16.0",
        "FCM_VERSION": "24.1.0"
      }
    },
    "platforms": []
  }
}
```

_Note:_ This plugin's default `FCM_VERSION` is 24.1.0.

## Browser details

For the time being, push support on the browser is not supported. The original plugin used the PhoneGap push server which may no longer be active.

### Browser Support

Chrome 49+
Firefox 46+

## iOS details

### System & Cordova Requirements

**System:**

- `Xcode`: `11.0` or greater.
- `CocoaPods`: `1.8.0` or greater. Preferably `1.9.x`
- `Ruby`: `2.0.0` or greater.

**Cordova:**

- `cordova-cli`: `10.0.0` or greater.
- `cordova-ios`: `6.0.0` or greater. Preferably `6.1.x`

### CocoaPods

To install CocoaPods, please follow the installation instructions [here](https://guides.cocoapods.org/using/getting-started). Since version `1.8.0` and greater, the pod repo no longer needs to be setup or fetched. Pods specs will be fetched directly from the **CocoaPods CDN**.

If you are upgrading from an older version, it might be best to uninstall first the older version and remove the `~/.cocoapods/` directory.

This plugin uses the [Firebase/Messaging](https://cocoapods.org/pods/Firebase) library.

#### Common CocoaPod Installation issues

If you are attempting to install this plugin and you run into this error:

```log
Installing "@havesource/cordova-plugin-push" for ios
Failed to install '@havesource/cordova-plugin-push':Error: pod: Command failed with exit code 1
    at ChildProcess.whenDone (/Users/smacdona/code/push151/platforms/ios/cordova/node_modules/cordova-common/src/superspawn.js:169:23)
    at emitTwo (events.js:87:13)
    at ChildProcess.emit (events.js:172:7)
    at maybeClose (internal/child_process.js:818:16)
    at Process.ChildProcess._handle.onexit (internal/child_process.js:211:5)
Error: pod: Command failed with exit code 1
```

Please try to add the plugin again, with the `--verbose` flag. The above error is generic and can actually be caused by a number of reasons. The `--verbose` flag should help display the exact cause of the install failure.

One of the most common reason is that it is trying to fetch the podspec from the CocoaPods repo and the repo is out-of-date. It recommended to use CocoaPods CDN over the repo. If your using an older version of CocoaPods, it is recommend to upgrade with a fresh installation.

With a fresh installations, you should have one repo source which can be checked with the `pod repo` command.

```log
$ pod repo

trunk
- Type: CDN
- URL:  https://cdn.cocoapods.org/
- Path: /Users/home/.cocoapods/repos/trunk

1 repo
```

##### Library not found for -lPods-Appname

If you open the app in Xcode and you get an error like:

```log
ld: library not found for -lPods-Appname
clang: error: linker command failed with exit code 1
```

Then you are opening the .xcodeproj file when you should be opening the .xcworkspace file.

##### Library not found for -lGoogleToolboxForMac

Trying to build for iOS using the latest cocoapods (1.9.3) but failed with the following error (from terminal running cordova build ios):

```log
ld: library not found for -lGoogleToolboxForMac
```

Workarounds are to add the platform first and install the plugins later, or to manually run pod install on projectName/platforms/ios.

Another workaround is to go to build phases in your project at Link Binary Libraries and add `libPods-PROJECTNAME.a` and `libGoogleToolboxForMac.a`

## Additional Resources

The push plugin enables you to play sounds and display different icons during push (Android only). These additional resources need to be added to your projects `platforms` directory in order for them to be included into your final application binary.

You can now use the `resource-file` tag to deliver the image and sound files to your application. For example if you wanted to include an extra image file for only your Android build you would add the `resource-file` tag to your android `platform` tag:

```xml
<platform name="android">
  <resource-file src="myImage.png" target="res/drawable/myImage.png" />
</platform>
```

or if you wanted to include a sound file for iOS:

```xml
<platform name="ios">
  <resource-file src="mySound.caf" />
</platform>
```
