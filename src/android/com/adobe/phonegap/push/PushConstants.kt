package com.adobe.phonegap.push

/**
 * Push Constants
 *
 * @todo add docs about each constant.
 */
@Suppress("HardCodedStringLiteral")
object PushConstants {
    const val COM_ADOBE_PHONEGAP_PUSH: String = "com.adobe.phonegap.push"
    const val REGISTRATION_ID: String = "registrationId"
    const val REGISTRATION_TYPE: String = "registrationType"
    const val FOREGROUND: String = "foreground"
    const val TITLE: String = "title"
    const val NOT_ID: String = "notId"
    const val PUSH_BUNDLE: String = "pushBundle"
    const val ICON: String = "icon"
    const val ICON_COLOR: String = "iconColor"
    const val SOUND: String = "sound"
    const val SOUND_DEFAULT: String = "default"
    const val SOUND_RINGTONE: String = "ringtone"
    const val VIBRATE: String = "vibrate"
    const val ACTIONS: String = "actions"
    const val CALLBACK: String = "callback"
    const val ACTION_CALLBACK: String = "actionCallback"
    const val DRAWABLE: String = "drawable"
    const val MSGCNT: String = "msgcnt"
    const val VIBRATION_PATTERN: String = "vibrationPattern"
    const val STYLE: String = "style"
    const val SUMMARY_TEXT: String = "summaryText"
    const val PICTURE: String = "picture"
    const val GCM_N: String = "gcm.n."
    const val GCM_NOTIFICATION: String = "gcm.notification"
    const val GCM_NOTIFICATION_BODY: String = "gcm.notification.body"
    const val UA_PREFIX: String = "com.urbanairship.push"
    const val PARSE_COM_DATA: String = "data"
    const val ALERT: String = "alert"
    const val MESSAGE: String = "message"
    const val BODY: String = "body"
    const val SOUNDNAME: String = "soundname"
    const val COLOR: String = "color"
    const val LED_COLOR: String = "ledColor"
    const val PRIORITY: String = "priority"
    const val IMAGE: String = "image"
    const val STYLE_INBOX: String = "inbox"
    const val STYLE_PICTURE: String = "picture"
    const val STYLE_TEXT: String = "text"
    const val BADGE: String = "badge"
    const val INITIALIZE: String = "init"
    const val SUBSCRIBE: String = "subscribe"
    const val UNSUBSCRIBE: String = "unsubscribe"
    const val UNREGISTER: String = "unregister"
    const val EXIT: String = "exit"
    const val FINISH: String = "finish"
    const val HAS_PERMISSION: String = "hasPermission"
    const val ANDROID: String = "android"
    const val SENDER_ID: String = "senderID"
    const val CLEAR_BADGE: String = "clearBadge"
    const val CLEAR_NOTIFICATIONS: String = "clearNotifications"
    const val COLDSTART: String = "coldstart"
    const val ADDITIONAL_DATA: String = "additionalData"
    const val COUNT: String = "count"
    const val FROM: String = "from"
    const val COLLAPSE_KEY: String = "collapse_key"
    const val FORCE_SHOW: String = "forceShow"
    const val FCM: String = "FCM"
    const val CONTENT_AVAILABLE: String = "content-available"
    const val TOPICS: String = "topics"
    const val SET_APPLICATION_ICON_BADGE_NUMBER: String = "setApplicationIconBadgeNumber"
    const val GET_APPLICATION_ICON_BADGE_NUMBER: String = "getApplicationIconBadgeNumber"
    const val CLEAR_ALL_NOTIFICATIONS: String = "clearAllNotifications"
    const val VISIBILITY: String = "visibility"
    const val INLINE_REPLY: String = "inlineReply"
    const val INLINE_REPLY_LABEL: String = "replyLabel"
    const val LOC_KEY: String = "locKey"
    const val LOC_DATA: String = "locData"
    const val TWILIO_BODY: String = "twi_body"
    const val TWILIO_TITLE: String = "twi_title"
    const val TWILIO_SOUND: String = "twi_sound"
    const val AWS_PINPOINT_BODY: String = "pinpoint.notification.body"
    const val AWS_PINPOINT_PICTURE: String = "pinpoint.notification.imageUrl"
    const val AWS_PINPOINT_PREFIX: String = "pinpoint.notification"
    const val MP_MESSAGE: String = "mp_message"
    const val START_IN_BACKGROUND: String = "cdvStartInBackground"
    const val FORCE_START: String = "force-start"
    const val MESSAGE_KEY: String = "messageKey"
    const val TITLE_KEY: String = "titleKey"
    const val NO_CACHE: String = "no-cache"
    const val DISMISSED: String = "dismissed"
    const val IMAGE_TYPE: String = "image-type"
    const val IMAGE_TYPE_SQUARE: String = "square"
    const val IMAGE_TYPE_CIRCLE: String = "circle"
    const val SUBJECT: String = "subject"
    const val GOOGLE_APP_ID: String = "google_app_id"
    const val GCM_DEFAULT_SENDER_ID: String = "gcm_defaultSenderId"
    const val PUSH_DISMISSED: String = "push_dismissed"
    const val DEFAULT_CHANNEL_ID: String = "PushPluginChannel"
    const val CHANNELS: String = "channels"
    const val CHANNEL_ID: String = "id"
    const val CHANNEL_DESCRIPTION: String = "description"
    const val CHANNEL_IMPORTANCE: String = "importance"
    const val CHANNEL_LIGHT_COLOR: String = "lightColor"
    const val CHANNEL_VIBRATION: String = "vibration"
    const val ANDROID_CHANNEL_ID: String = "android_channel_id"
    const val CHANNEL_STATE: String = "state"
    const val CREATE_CHANNEL: String = "createChannel"
    const val DELETE_CHANNEL: String = "deleteChannel"
    const val ONGOING: String = "ongoing"
    const val LIST_CHANNELS: String = "listChannels"
    const val CLEAR_NOTIFICATION: String = "clearNotification"
    const val MESSAGE_ID: String = "google.message_id"
    const val IS_ENABLED: String = "isEnabled"

    const val NOTIFY_MESSAGE_TYPE_KEY = "messageType"
    const val NOTIFY_TAP_KEY = "tap"
    const val NOTIFY_NOTIFICATION_VALUE = "notification"
    const val NOTIFY_BACKGROUND_VALUE = "background"
    const val NOTIFY_FOREGROUND_VALUE = "foreground"

    const val VOIP_CALL_ID_KEY = "callId"
    const val VOIP_CALLBACK_URL_KEY = "callbackUrl"
    const val VOIP_MESSAGE_BODY_KEY = "body"
    const val VOIP_CALL_KEY = "voip"
    const val VOIP_IS_CANCEL_PUSH_KEY = "isCancelPush"
    const val VOIP_CALLER_NAME_KEY = "caller"

    const val VOIP_EXTRA_BUTTON_ACTION = "extra_button_action"
    const val VOIP_EXTRA_CALLBACK_URL = "extra_callback_url"
    const val VOIP_EXTRA_CALL_ID = "extra_call_id"

    const val VOIP_CONNECTED_KEY = "connected"
    const val VOIP_ACCEPT_KEY = "pickup"
    const val VOIP_DECLINE_KEY = "declined_callee"
}
