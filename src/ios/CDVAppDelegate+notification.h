//
//  CDVAppDelegate+notification.h
//  pushtest
//
//  Created by Robert Easterday on 10/26/12.
//
//

#import <Cordova/CDVAppDelegate.h>

@import UserNotifications;

@interface CDVAppDelegate (notification) <UNUserNotificationCenterDelegate>

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:( void (^)(UIBackgroundFetchResult))completionHandler;

- (id) getCommandInstance:(NSString*)className;

@property (nonatomic, retain) NSDictionary  *launchNotification;
@property (nonatomic, retain) NSNumber  *coldstart;

@end
