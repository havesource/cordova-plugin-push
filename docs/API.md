# API

- [API](#api)
  - [PushNotification.init(options)](#pushnotificationinitoptions)
    - [Returns](#returns)
    - [Parameters](#parameters)
      - [Android](#android)
      - [Browser](#browser)
      - [iOS](#ios)
      - [iOS GCM support](#ios-gcm-support)
        - [How GCM on iOS works.](#how-gcm-on-ios-works)
      - [iOS VoIP Notifications](#ios-voip-notifications)
    - [Example](#example)
  - [PushNotification.hasPermission(successHandler)](#pushnotificationhaspermissionsuccesshandler)
    - [Parameters](#parameters-1)
    - [Callback parameters](#callback-parameters)
      - [`successHandler`](#successhandler)
    - [Example](#example-1)
  - [PushNotification.createChannel(successHandler, failureHandler, channel)](#pushnotificationcreatechannelsuccesshandler-failurehandler-channel)
    - [Parameters](#parameters-2)
    - [Example](#example-2)
    - [Channel properties](#channel-properties)
  - [PushNotification.deleteChannel(successHandler, failureHandler, channelId)](#pushnotificationdeletechannelsuccesshandler-failurehandler-channelid)
    - [Parameters](#parameters-3)
    - [Example](#example-3)
  - [PushNotification.listChannels(successHandler)](#pushnotificationlistchannelssuccesshandler)
    - [Parameters](#parameters-4)
    - [Callback parameters](#callback-parameters-1)
      - [`successHandler`](#successhandler-1)
    - [Example](#example-4)
  - [push.on(event, callback)](#pushonevent-callback)
    - [Parameters](#parameters-5)
  - [push.on('registration', callback)](#pushonregistration-callback)
    - [Callback parameters](#callback-parameters-2)
    - [Example](#example-5)
    - [Common Problems](#common-problems)
      - [Got JSON Exception TIMEOUT](#got-json-exception-timeout)
  - [push.on('notification', callback)](#pushonnotification-callback)
    - [Callback parameters](#callback-parameters-3)
    - [Example](#example-6)
  - [push.on('error', callback)](#pushonerror-callback)
    - [Callback parameters](#callback-parameters-4)
    - [Example](#example-7)
  - [push.off(event, callback)](#pushoffevent-callback)
    - [Parameters](#parameters-6)
    - [Example](#example-8)
  - [push.unregister(successHandler, errorHandler, topics)](#pushunregistersuccesshandler-errorhandler-topics)
    - [Parameters](#parameters-7)
    - [Example](#example-9)
  - [push.subscribe(topic, successHandler, errorHandler)](#pushsubscribetopic-successhandler-errorhandler)
    - [Parameters](#parameters-8)
    - [Example](#example-10)
  - [push.unsubscribe(topic, successHandler, errorHandler)](#pushunsubscribetopic-successhandler-errorhandler)
    - [Parameters](#parameters-9)
    - [Example](#example-11)
  - [push.setApplicationIconBadgeNumber(successHandler, errorHandler, count) - iOS \& Android only](#pushsetapplicationiconbadgenumbersuccesshandler-errorhandler-count---ios--android-only)
    - [Parameters](#parameters-10)
    - [Example](#example-12)
  - [push.getApplicationIconBadgeNumber(successHandler, errorHandler) - iOS \& Android only](#pushgetapplicationiconbadgenumbersuccesshandler-errorhandler---ios--android-only)
    - [Parameters](#parameters-11)
    - [Callback parameters](#callback-parameters-5)
      - [`successHandler`](#successhandler-2)
    - [Example](#example-13)
  - [push.finish(successHandler, errorHandler, id) - iOS only](#pushfinishsuccesshandler-errorhandler-id---ios-only)
    - [Parameters](#parameters-12)
    - [Example](#example-14)
  - [push.clearAllNotifications(successHandler, errorHandler) - iOS \& Android only](#pushclearallnotificationssuccesshandler-errorhandler---ios--android-only)
    - [Parameters](#parameters-13)
    - [Example](#example-15)
  - [push.clearNotification(id, successHandler, errorHandler) - iOS \& Android only](#pushclearnotificationid-successhandler-errorhandler---ios--android-only)
    - [Parameters](#parameters-14)
    - [Example](#example-16)

## PushNotification.init(options)

Initializes the plugin on the native side.

**Note:** like all plugins you must wait until you receive the [`deviceready`](https://cordova.apache.org/docs/en/5.4.0/cordova/events/events.deviceready.html) event before calling `PushNotification.init()`.

**Note:** you will want to call `PushNotification.init()` each time your app starts. The remote push service can periodically reset your registration ID so this ensures you have the correct value.

### Returns

* Instance of `PushNotification`.

### Parameters

| Parameter | Type     | Default | Description                                                              |
| --------- | -------- | ------- | ------------------------------------------------------------------------ |
| `options` | `Object` | `{}`    | An object describing relevant specific options for all target platforms. |

All available option attributes are described bellow.

#### Android

| Attribute                    | Type      | Default     | Description                                                                                                                                                                                                                                                                                                                                                                                                                    |
| ---------------------------- | --------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `android.icon`               | `string`  |             | Optional. The name of a drawable resource to use as the small-icon. The name should not include the extension.                                                                                                                                                                                                                                                                                                                 |
| `android.iconColor`          | `string`  |             | Optional. Sets the background color of the small icon on Android 5.0 and greater. [Supported Formats](<http://developer.android.com/reference/android/graphics/Color.html#parseColor(java.lang.String)>)                                                                                                                                                                                                                       |
| `android.sound`              | `boolean` | `true`      | Optional. If `true` it plays the sound specified in the push data or the default system sound.                                                                                                                                                                                                                                                                                                                                 |
| `android.vibrate`            | `boolean` | `true`      | Optional. If `true` the device vibrates on receipt of notification.                                                                                                                                                                                                                                                                                                                                                            |
| `android.clearBadge`         | `boolean` | `false`     | Optional. If `true` the icon badge will be cleared on init and before push messages are processed.                                                                                                                                                                                                                                                                                                                             |
| `android.clearNotifications` | `boolean` | `true`      | Optional. If `true` the app clears all pending notifications when it is closed.                                                                                                                                                                                                                                                                                                                                                |
| `android.forceShow`          | `boolean` | `false`     | Optional. Controls the behavior of the notification when app is in foreground. If `true` and app is in foreground, it will show a notification in the notification drawer, the same way as when the app is in background (and `on('notification')` callback will be called _only when the user clicks the notification_). When `false` and app is in foreground, the `on('notification')` callback will be called immediately. |
| `android.topics`             | `array`   | `[]`        | Optional. If the array contains one or more strings each string will be used to subscribe to a FcmPubSub topic.                                                                                                                                                                                                                                                                                                                |
| `android.messageKey`         | `string`  | `'message'` | Optional. The key to search for text of notification.                                                                                                                                                                                                                                                                                                                                                                          |
| `android.titleKey`           | `string`  | `'title'`   | Optional. The key to search for title of notification.                                                                                                                                                                                                                                                                                                                                                                         |

#### Browser

| Attribute                      | Type     | Default                                                      | Description                                             |
| ------------------------------ | -------- | ------------------------------------------------------------ | ------------------------------------------------------- |
| `browser.pushServiceURL`       | `string` | `http://push.api.phonegap.com/v1/push`                       | Optional. URL for the push server you want to use.      |
| `browser.applicationServerKey` | `string` |                                                              | Optional. Your GCM API key if you are using VAPID keys. |

#### iOS

All iOS boolean options can also be specified as `string`

| Attribute        | Type      | Default | Description                                                                                                                                                                                                                                                                                                                                                                       |
| ---------------- | --------- | ------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ios.voip`       | `boolean` | `false` | Optional. If `true` the device will be set up to receive VoIP Push notifications and the other options will be ignored since VoIP notifications are silent notifications that should be handled in the "notification" event.                                                                                                                                                      |
| `ios.alert`      | `boolean` | `false` | Optional. If `true` the device shows an alert on receipt of notification. **Note:** the value you set this option to the first time you call the init method will be how the application always acts. Once this is set programmatically in the init method it can only be changed manually by the user in Settings>Notifications>`App Name`. This is normal iOS behaviour.        |
| `ios.badge`      | `boolean` | `false` | Optional. If `true` the device sets the badge number on receipt of notification. **Note:** the value you set this option to the first time you call the init method will be how the application always acts. Once this is set programmatically in the init method it can only be changed manually by the user in Settings>Notifications>`App Name`. This is normal iOS behaviour. |
| `ios.sound`      | `boolean` | `false` | Optional. If `true` the device plays a sound on receipt of notification. **Note:** the value you set this option to the first time you call the init method will be how the application always acts. Once this is set programmatically in the init method it can only be changed manually by the user in Settings>Notifications>`App Name`. This is normal iOS behaviour.         |
| `ios.clearBadge` | `boolean` | `false` | Optional. If `true` the badge will be cleared on app startup.                                                                                                                                                                                                                                                                                                                     |
| `ios.categories` | `Object`  | `{}`    | Optional. The data required in order to enable Action Buttons for iOS. See [Action Buttons on iOS](https://github.com/phonegap/phonegap-plugin-push/blob/master/docs/PAYLOAD.md#action-buttons-1) for more details.                                                                                                                                                              |
| `ios.critical`   | `boolean` | `false` | Optional. If `true` the device can show up critical alerts. (Possible since iOS 12 with a special entitlement) **Note:** the value you set this option to the first time you call the init method will be how the application always acts. Once this is set programmatically in the init method it can only be changed manually by the user in Settings > Notifications > `App Name`. This is normal iOS behaviour.         |
| `ios.forceShow`  | `boolean` | `false` | Optional. Controls the behavior of the notification when app is in foreground. If `true` and app is in foreground, it will show a notification in the notification drawer, the same way as when the app is in background (and `on('notification')` callback will be called _only when the user clicks the notification_). When `false` and app is in foreground, the `on('notification')` callback will be called immediately. |
| `ios.forceRegister`  | `boolean` | `false` | Optional. If set to true, the app will continue to register an APNs token for remote notifications. Notification permissions only control disruptive user-facing notifications, such as alerts, sounds, badges, and critical notifications. Even if the user denies these permissions, background push notifications can still be received. If the user disables "Background App Refresh", silent push notifications will be blocked. |

#### iOS GCM support

The following properties are used if you want use GCM on iOS.

| Attribute        | Type      | Default | Description                                                    |
| ---------------- | --------- | ------- | -------------------------------------------------------------- |
| `ios.topics`     | `array`   | `[]`    | Optional. If the array contains one or more strings each string will be used to subscribe to a FcmPubSub topic. |

##### How GCM on iOS works.

First it is kind of a misnomer as GCM does not send push messages directly to devices running iOS.

What happens is on the device side is that it registers with APNS, then that registration ID is sent to GCM which returns a different GCM specific ID. That is the ID you get from the push plugin `registration` event.

When you send a message to GCM using that ID, what it does is look up the APNS registration ID on it's side and forward the message you sent to GCM on to APSN to deliver to your iOS device.

Unlike GCM, FCM automatically determines whether to use the sandbox or production environment based on factors such as the app's provisioning profile, APNs certificate, and distribution method (e.g., App Store, TestFlight, or simulator).

> Note: The integration between GCM and APNS is a bit finicky. Personally, I feel it is much better to send pushes to Android using GCM and pushes to iOS using APNS which this plugin does support.

#### iOS VoIP Notifications

It is possible to receive VoIP Notifications in iOS that can execute the "notification" event also when the application is in background or closed.

This type of notifications consist only of payload data, so the developer is the responsible of handling the event and do whatever the aplication should do when receiving one of them. The cordova-plugin-local-notifications is a good complement for the VoIP feature.

In order to use the VoIP Notifications, you have to create a VoIP Services Certificate. There are a lot of tutorials on the web to achieve this. Once created, you must use this certificate in order to communicate with the APN Service.

To set up the VoIP Notification in iOS do:

```javascript
const push = PushNotification.init({
  ios: {
    voip: true
  }
});
```

Once set up the voip parameter to true, the rest of the options will be ignored.

You will also need to setup your app to receive `voip` messages in the apps pList. In your apps config.xml add the following in the `<platform name="ios">` tag. Only do this if you are setup to receive `voip` messages. If your app does not use `voip` messages the Apple App Store will reject your app.

```xml
<config-file target="*-Info.plist" parent="UIBackgroundModes">
  <array>
    <string>voip</string>
  </array>
</config-file>
```

The "hasPermission" success callback will return data.isEnabled to false since there is no need to approve to use this type of notifications.

The "finish" method has not use too when the VoIP notifications are enabled.

### Example

```javascript
const push = PushNotification.init({
  android: {},
  browser: {
    pushServiceURL: 'http://push.api.phonegap.com/v1/push'
  },
  ios: {
    alert: 'true',
    badge: true,
    sound: 'false'
  }
});
```

## PushNotification.hasPermission(successHandler)

Checks whether the push notification permission has been granted.

### Parameters

| Parameter        | Type       | Default | Description                                                                  |
| ---------------- | ---------- | ------- | ---------------------------------------------------------------------------- |
| `successHandler` | `Function` |         | Is called when the api successfully retrieves the details on the permission. |

### Callback parameters

#### `successHandler`

| Parameter        | Type      | Description                                                     |
| ---------------- | --------- | --------------------------------------------------------------- |
| `data.isEnabled` | `Boolean` | Whether the permission for push notifications has been granted. |

### Example

```javascript
PushNotification.hasPermission(data => {
  if (data.isEnabled) {
    console.log('isEnabled');
  }
});
```

## PushNotification.createChannel(successHandler, failureHandler, channel)

Create a new notification channel for Android O and above.

### Parameters

| Parameter        | Type       | Default | Description                                            |
| ---------------- | ---------- | ------- | ------------------------------------------------------ |
| `successHandler` | `Function` |         | Is called when the api successfully creates a channel. |
| `failureHandler` | `Function` |         | Is called when the api fails to create a channel.      |
| `channel`        | `Object`   |         | The options for the channel.                           |

### Example

```javascript
PushNotification.createChannel(
  () => {
    console.log('success');
  },
  () => {
    console.log('error');
  },
  {
    id: 'testchannel1',
    description: 'My first test channel',
    importance: 3,
    vibration: true
  }
);
```

The above will create a channel for your app. You'll need to provide the `id`, `description` and `importance` properties.

A default channel with the id "PushPluginChannel" is created automatically. To make changes to the default channel's settings, create a channel with the id "PushPluginChannel" before calling the PushNotification.init function.

### Channel properties

| Property                         | Type      | Description                                                                                                                                                                                                                         |
| -------------------------------- | --------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `id`                             | `String`  | The id of the channel. Must be unique per package. The value may be truncated if it is too long.                                                                                                                                    |
| `description`                    | `String`  | The user visible name of the channel. The recommended maximum length is 40 characters; the value may be truncated if it is too long.                                                                                                |
| `importance`                     | `Int`     | The importance of the channel. This controls how interruptive notifications posted to this channel are. The importance property goes from 1 = Lowest, 2 = Low, 3 = Normal, 4 = High and 5 = Highest.                                |
| `sound`                          | `String`  | The name of the sound file to be played upon receipt of the notification in this channel. Empty string to disable sound. Cannot be changed after channel is created.                                                                                               |
| `vibration`                      | `Boolean` or `Array` | Boolean sets whether notification posted to this channel should vibrate. Array sets custom vibration pattern. Example - vibration: `[2000, 1000, 500, 500]`. Cannot be changed after channel is created.                 |
| `visibility`                     | `Int`     | Sets whether notifications posted to this channel appear on the lockscreen or not, and if so, whether they appear in a redacted form. 0 = Private, 1 = Public, -1 = Secret.                                                         |
| `lightColor`                     | `Int`     | Sets and enables the color of the notification light. The default value, `-1`, disables the notification light. (**Android Only**) |

## PushNotification.deleteChannel(successHandler, failureHandler, channelId)

Delete a notification channel for Android O and above.

### Parameters

| Parameter        | Type       | Default | Description                                            |
| ---------------- | ---------- | ------- | ------------------------------------------------------ |
| `successHandler` | `Function` |         | Is called when the api successfully creates a channel. |
| `failureHandler` | `Function` |         | Is called when the api fails to create a channel.      |
| `channelId`      | `String`   |         | The ID of the channel.                                 |

### Example

```javascript
PushNotification.deleteChannel(
  () => {
    console.log('success');
  },
  () => {
    console.log('error');
  },
  'testchannel1'
);
```

## PushNotification.listChannels(successHandler)

Returns a list of currently configured channels.

### Parameters

| Parameter        | Type       | Default | Description                                                         |
| ---------------- | ---------- | ------- | ------------------------------------------------------------------- |
| `successHandler` | `Function` |         | Is called when the api successfully retrieves the list of channels. |

### Callback parameters

#### `successHandler`

| Parameter  | Type         | Description              |
| ---------- | ------------ | ------------------------ |
| `channels` | `JSONArrary` | List of channel objects. |

### Example

```javascript
PushNotification.listChannels(channels => {
  for (let channel of channels) {
    console.log(`ID: ${channel.id} Description: ${channel.description}`);
  }
});
```

## push.on(event, callback)

### Parameters

| Parameter  | Type       | Default | Description                                                        |
| ---------- | ---------- | ------- | ------------------------------------------------------------------ |
| `event`    | `string`   |         | Name of the event to listen to. See below for all the event names. |
| `callback` | `Function` |         | Is called when the event is triggered.                             |

## push.on('registration', callback)

The event `registration` will be triggered on each successful registration with the 3rd party push service.

### Callback parameters

| Parameter               | Type     | Description                                                                     |
| ----------------------- | -------- | ------------------------------------------------------------------------------- |
| `data.registrationId`   | `string` | The registration ID provided by the 3rd party remote push service.              |
| `data.registrationType` | `string` | The registration type of the 3rd party remote push service. Either FCM or APNS. |

### Example

```javascript
push.on('registration', data => {
  console.log(data.registrationId);
  console.log(data.registrationType);
});
```

For APNS users: the `registrationId` you will get will be a production or sandbox id according to how the app was built. ([Source](https://developer.apple.com/library/ios/technotes/tn2265/_index.html))

> Note: There is a separate persistent connection to the push service for each environment. The operating system establishes a persistent connection to the sandbox environment for development builds; ad hoc and distribution builds connect to the production environment.

### Common Problems

#### Got JSON Exception TIMEOUT

If you run this plugin on older versions of Android and you get an error:

```
E/PushPlugin(20077): execute: Got JSON Exception TIMEOUT
```

It means you are running an older version of Google Play Services. You will need to open the Google Play Store app and update your version of Google Play Services.

## push.on('notification', callback)

The event `notification` will be triggered each time a push notification is received by a 3rd party push service on the device.

### Callback parameters

| Parameter                        | Type      | Description                                                                                                                                                                                                                         |
| -------------------------------- | --------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `data.message`                   | `string`  | The text of the push message sent from the 3rd party service.                                                                                                                                                                       |
| `data.title`                     | `string`  | The optional title of the push message sent from the 3rd party service.                                                                                                                                                             |
| `data.count`                     | `string`  | The number of messages to be displayed in the badge in iOS/Android or message count in the notification shade in Android. |
| `data.sound`                     | `string`  | The name of the sound file to be played upon receipt of the notification.                                                                                                                                                           |
| `data.image`                     | `string`  | The path of the image file to be displayed in the notification.                                                                                                                                                                     |
| `data.additionalData`            | `Object`  | An optional collection of data sent by the 3rd party push service that does not fit in the above properties.                                                                                                                        |
| `data.additionalData.foreground` | `boolean` | Whether the notification was received while the app was in the foreground                                                                                                                                                           |
| `data.additionalData.coldstart`  | `boolean` | Will be `true` if the application is started by clicking on the push notification, `false` if the app is already started.                                                                                                           |
| `data.additionalData.dismissed`  | `boolean` | Is set to `true` if the notification was dismissed by the user                                                                                                                                                                      |

### Example

```javascript
push.on('notification', data => {
  console.log(data.message);
  console.log(data.title);
  console.log(data.count);
  console.log(data.sound);
  console.log(data.image);
  console.log(data.additionalData);
});
```

Android quirk: Please note that some payloads may cause this event not to be always fired: [data vs notification payloads](https://github.com/phonegap/phonegap-plugin-push/blob/master/docs/PAYLOAD.md#notification-vs-data-payloads)

## push.on('error', callback)

The event `error` will trigger when an internal error occurs and the cache is aborted.

### Callback parameters

| Parameter | Type    | Description                                                |
| --------- | ------- | ---------------------------------------------------------- |
| `e`       | `Error` | Standard JavaScript error object that describes the error. |

### Example

```javascript
push.on('error', e => {
  console.log(e.message);
});
```

## push.off(event, callback)

Removes a previously registered callback for an event.

### Parameters

| Parameter  | Type       | Default | Description                                                                                  |
| ---------- | ---------- | ------- | -------------------------------------------------------------------------------------------- |
| `event`    | `string`   |         | Name of the event type. The possible event names are the same as for the `push.on` function. |
| `callback` | `Function` |         | The same callback used to register with `push.on`.                                           |

### Example

```javascript
const callback = data => {
  /*...*/
};

//Adding handler for notification event
push.on('notification', callback);

//Removing handler for notification event
push.off('notification', callback);
```

**WARNING**: As stated in the example, you will have to store your event handler if you are planning to remove it.

## push.unregister(successHandler, errorHandler, topics)

The unregister method is used when the application no longer wants to receive push notifications. Beware that this cleans up all event handlers previously registered, so you will need to reinitialize the plugin's API (through `.init()`) if you want them to function again without an application reload.

If you provide a list of topics as an optional parameter then the application will unsubscribe from these topics but continue to receive other push messages.

### Parameters

| Parameter        | Type       | Default | Description                                                     |
| ---------------- | ---------- | ------- | --------------------------------------------------------------- |
| `successHandler` | `Function` |         | Is called when the api successfully unregisters.                |
| `errorHandler`   | `Function` |         | Is called when the api encounters an error while unregistering. |
| `topics`         | `Array`    |         | A list of topics to unsubscribe from.                           |

### Example

```javascript
push.unregister(
  () => {
    console.log('success');
  },
  () => {
    console.log('error');
  }
);
```

## push.subscribe(topic, successHandler, errorHandler)

The subscribe method is used when the application wants to subscribe a new topic to receive push notifications.

### Parameters

| Parameter        | Type       | Default | Description                                                   |
| ---------------- | ---------- | ------- | ------------------------------------------------------------- |
| `topic`          | `String`   |         | Topic to subscribe to.                                        |
| `successHandler` | `Function` |         | Is called when the api successfully subscribes.               |
| `errorHandler`   | `Function` |         | Is called when the api encounters an error while subscribing. |

### Example

```javascript
push.subscribe(
  'my-topic',
  () => {
    console.log('success');
  },
  e => {
    console.log('error:', e);
  }
);
```

## push.unsubscribe(topic, successHandler, errorHandler)

The unsubscribe method is used when the application no longer wants to receive push notifications from a specific topic but continue to receive other push messages.

### Parameters

| Parameter        | Type       | Default | Description                                                     |
| ---------------- | ---------- | ------- | --------------------------------------------------------------- |
| `topic`          | `String`   |         | Topic to unsubscribe from.                                      |
| `successHandler` | `Function` |         | Is called when the api successfully unsubscribe.                |
| `errorHandler`   | `Function` |         | Is called when the api encounters an error while unsubscribing. |

### Example

```javascript
push.unsubscribe(
  'my-topic',
  () => {
    console.log('success');
  },
  e => {
    console.log('error:', e);
  }
);
```

## push.setApplicationIconBadgeNumber(successHandler, errorHandler, count) - iOS & Android only

Set the badge count visible when the app is not running

> Note: badges are not supported on all Android devices. See [our payload documentation](PAYLOAD.md#badges) for more details.

### Parameters

| Parameter        | Type       | Default | Description                                                                                                                                                                                      |
| ---------------- | ---------- | ------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `successHandler` | `Function` |         | Is called when the api successfully sets the icon badge number.                                                                                                                                  |
| `errorHandler`   | `Function` |         | Is called when the api encounters an error while trying to set the icon badge number.                                                                                                            |
| `count`          | `number`   |         | Indicates what number should show up in the badge. Passing 0 will clear the badge. Each `notification` event contains a `data.count` value which can be used to set the badge to correct number. |

### Example

```javascript
push.setApplicationIconBadgeNumber(
  () => {
    console.log('success');
  },
  () => {
    console.log('error');
  },
  2
);
```

## push.getApplicationIconBadgeNumber(successHandler, errorHandler) - iOS & Android only

Get the current badge count visible when the app is not running

### Parameters

| Parameter        | Type       | Default | Description                                                                                |
| ---------------- | ---------- | ------- | ------------------------------------------------------------------------------------------ |
| `successHandler` | `Function` |         | Is called when the api successfully retrieves the icon badge number.                       |
| `errorHandler`   | `Function` |         | Is called when the api encounters an error while trying to retrieve the icon badge number. |

### Callback parameters

#### `successHandler`

| Parameter | Type     | Description                                  |
| --------- | -------- | -------------------------------------------- |
| `n`       | `number` | An integer which is the current badge count. |

### Example

```javascript
push.getApplicationIconBadgeNumber(
  n => {
    console.log('success', n);
  },
  () => {
    console.log('error');
  }
);
```

## push.finish(successHandler, errorHandler, id) - iOS only

Tells the OS that you are done processing a background push notification.

### Parameters

| Parameter        | Type       | Default | Description                                                                                     |
| ---------------- | ---------- | ------- | ----------------------------------------------------------------------------------------------- |
| `successHandler` | `Function` |         | Is called when the api successfully completes background push processing.                       |
| `errorHandler`   | `Function` |         | Is called when the api encounters an error while processing and completing the background push. |
| `id`             | `String`   |         | Tells the OS which background process is complete.                                              |

### Example

```javascript
push.finish(
  () => {
    console.log('success');
  },
  () => {
    console.log('error');
  },
  'push-1'
);
```

## push.clearAllNotifications(successHandler, errorHandler) - iOS & Android only

Tells the OS to clear all notifications from the Notification Center

### Parameters

Parameter | Type | Default | Description
--------- | ---- | ------- | -----------
`successHandler` | `Function` | | Is called when the api successfully clears the notifications.
`errorHandler` | `Function` | | Is called when the api encounters an error when attempting to clear the notifications.

### Example

```javascript
push.clearAllNotifications(
  () => {
    console.log('success');
  },
  () => {
    console.log('error');
  }
);
```

## push.clearNotification(id, successHandler, errorHandler) - iOS & Android only

Tells the OS to clear the notification that corresponds to the id argument, from the Notification Center

### Parameters

Parameter | Type | Default | Description
--------- | ---- | ------- | -----------
`successHandler` | `Function` | | Is called when the api successfully clears the notification.
`errorHandler` | `Function` | | Is called when the api encounters an error when attempting to clear the notification.
`id` | `number` | | The ID of the notification that will be cleared. |

### Example

```javascript
push.clearNotification(() => {
	console.log('success');
}, () => {
	console.log('error');
}, 145);
```
