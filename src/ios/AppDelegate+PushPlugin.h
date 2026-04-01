//
//  AppDelegate+PushPlugin.h
//
//  Created by Robert Easterday on 10/26/12.
//

#import "AppDelegate.h"

@import UserNotifications;

@interface AppDelegate (PushPlugin) <UNUserNotificationCenterDelegate>

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;
- (void)application:(UIApplication *)application
    didReceiveRemoteNotification:(NSDictionary *)userInfo
          fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler;

@end
