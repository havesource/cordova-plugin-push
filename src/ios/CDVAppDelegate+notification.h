//
//  CDVAppDelegate+notification.h
//
//  Created by Robert Easterday on 10/26/12.
//

#import <Cordova/CDVAppDelegate.h>

@import UserNotifications;

@interface CDVAppDelegate (notification) <UNUserNotificationCenterDelegate>

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:( void (^)(UIBackgroundFetchResult))completionHandler;

@end
