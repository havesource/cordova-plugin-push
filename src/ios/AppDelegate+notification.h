//
//  AppDelegate+notification.h
//
//  Created by Robert Easterday on 10/26/12.
//

#import "AppDelegate.h"
@import UserNotifications;

@interface AppDelegate (notification) <UNUserNotificationCenterDelegate>

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:( void (^)(UIBackgroundFetchResult))completionHandler;

@end
