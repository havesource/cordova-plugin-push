//
//  AppDelegate+PushPlugin.m
//
//  Created by Robert Easterday on 10/26/12.
//

#import "AppDelegate+PushPlugin.h"
#import "PushPlugin.h"
#import "PushPluginConstants.h"
#import <objc/runtime.h>

@implementation AppDelegate (PushPlugin)

// its dangerous to override a method from within a category.
// Instead we will use method swizzling. we set this up in the load call.
+ (void)load {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        Class class = [self class];

        SEL originalSelector = @selector(init);
        SEL swizzledSelector = @selector(pushPluginSwizzledInit);

        Method original = class_getInstanceMethod(class, originalSelector);
        Method swizzled = class_getInstanceMethod(class, swizzledSelector);

        BOOL didAddMethod = class_addMethod(class, originalSelector, method_getImplementation(swizzled), method_getTypeEncoding(swizzled));

        if (didAddMethod) {
            class_replaceMethod(class, swizzledSelector, method_getImplementation(original), method_getTypeEncoding(original));
        } else {
            method_exchangeImplementations(original, swizzled);
        }
    });
}

- (AppDelegate *)pushPluginSwizzledInit {
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    center.delegate = self;
    // This actually calls the original init method over in AppDelegate. Equivilent to calling super
    // on an overrided method, this is not recursive, although it appears that way. neat huh?
    return [self pushPluginSwizzledInit];
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    [NSNotificationCenter.defaultCenter postNotificationName:PluginDidRegisterForRemoteNotificationsWithDeviceToken object:deviceToken];
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    [NSNotificationCenter.defaultCenter postNotificationName:PluginDidFailToRegisterForRemoteNotificationsWithError object:error];
}

- (void)application:(UIApplication *)application
didReceiveRemoteNotification:(NSDictionary *)userInfo
fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    NSDictionary *notificationInfo = @{@"userInfo" : userInfo, @"completionHandler" : completionHandler};
    [NSNotificationCenter.defaultCenter postNotificationName:PluginDidReceiveRemoteNotification object:nil userInfo:notificationInfo];
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
       willPresentNotification:(UNNotification *)notification
         withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler {
    NSDictionary *notificationInfo = @{@"notification" : notification, @"completionHandler" : completionHandler};
    [NSNotificationCenter.defaultCenter postNotificationName:PluginWillPresentNotification object:nil userInfo:notificationInfo];
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void (^)(void))completionHandler {
    NSDictionary *notificationInfo = @{@"response" : response, @"completionHandler" : completionHandler};
    [NSNotificationCenter.defaultCenter postNotificationName:PluginDidReceiveNotificationResponse object:nil userInfo:notificationInfo];
}

@end
