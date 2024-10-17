//
//  CDVAppDelegate+notification.m
//  pushtest
//
//  Created by Robert Easterday on 10/26/12.
//
//

#import "CDVAppDelegate+notification.h"
#import "PushPlugin.h"
#import <objc/runtime.h>

static char launchNotificationKey;
static char coldstartKey;

@implementation CDVAppDelegate (notification)

- (id) getCommandInstance:(NSString*)className
{
    return [self.viewController getCommandInstance:className];
}

// its dangerous to override a method from within a category.
// Instead we will use method swizzling. we set this up in the load call.
+ (void)load
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        Class class = [self class];

        SEL originalSelector = @selector(init);
        SEL swizzledSelector = @selector(pushPluginSwizzledInit);

        Method original = class_getInstanceMethod(class, originalSelector);
        Method swizzled = class_getInstanceMethod(class, swizzledSelector);

        BOOL didAddMethod =
        class_addMethod(class,
                        originalSelector,
                        method_getImplementation(swizzled),
                        method_getTypeEncoding(swizzled));

        if (didAddMethod) {
            class_replaceMethod(class,
                                swizzledSelector,
                                method_getImplementation(original),
                                method_getTypeEncoding(original));
        } else {
            method_exchangeImplementations(original, swizzled);
        }
    });
}

- (CDVAppDelegate *)pushPluginSwizzledInit
{
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    center.delegate = self;

    // This actually calls the original init method over in CDVAppDelegate. Equivilent to calling super
    // on an overrided method, this is not recursive, although it appears that way. neat huh?
    return [self pushPluginSwizzledInit];
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    [NSNotificationCenter.defaultCenter postNotificationName:@"CordovaPluginPushDidRegisterForRemoteNotificationsWithDeviceToken" object:deviceToken];
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    [NSNotificationCenter.defaultCenter postNotificationName:@"CordovaPluginPushDidFailToRegisterForRemoteNotificationsWithError" object:error];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {

    // Create a dictionary with userInfo and the completionHandler
    NSDictionary *notificationInfo = @{
        @"userInfo": userInfo,
        @"completionHandler": completionHandler
    };

    [NSNotificationCenter.defaultCenter postNotificationName:@"CordovaPluginPushDidReceiveRemoteNotification" object:nil userInfo:notificationInfo];
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler {
    NSMutableDictionary *userInfo = [notification.request.content.userInfo mutableCopy];
    NSDictionary *notificationInfo = @{
        @"userInfo": userInfo,
        @"completionHandler": completionHandler
    };
    [NSNotificationCenter.defaultCenter postNotificationName:@"CordovaPluginPushWillPresentNotification" object:nil userInfo:notificationInfo];
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void(^)(void))completionHandler {
    NSLog(@"Push Plugin didReceiveNotificationResponse: actionIdentifier %@, notification: %@", response.actionIdentifier, response.notification.request.content.userInfo);
    NSDictionary *originalUserInfo = response.notification.request.content.userInfo;
    NSMutableDictionary *modifiedUserInfo = [originalUserInfo mutableCopy];
    [modifiedUserInfo setObject:response.actionIdentifier forKey:@"actionCallback"];
    NSDictionary *notificationInfo = @{
        @"actionIdentifier": response.actionIdentifier,
        @"originalUserInfo": originalUserInfo,
        @"modifiedUserInfo": modifiedUserInfo,
        @"completionHandler": completionHandler
    };
    [NSNotificationCenter.defaultCenter postNotificationName:@"CordovaPluginPushDidReceiveNotificationResponse" object:nil userInfo:notificationInfo];
}

// The accessors use an Associative Reference since you can't define a iVar in a category
// http://developer.apple.com/library/ios/#documentation/cocoa/conceptual/objectivec/Chapters/ocAssociativeReferences.html
- (NSMutableArray *)launchNotification
{
    return objc_getAssociatedObject(self, &launchNotificationKey);
}

- (void)setLaunchNotification:(NSDictionary *)aDictionary
{
    objc_setAssociatedObject(self, &launchNotificationKey, aDictionary, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (NSNumber *)coldstart
{
    return objc_getAssociatedObject(self, &coldstartKey);
}

- (void)setColdstart:(NSNumber *)aNumber
{
    objc_setAssociatedObject(self, &coldstartKey, aNumber, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (void)dealloc
{
    self.launchNotification = nil; // clear the association and release the object
    self.coldstart = nil;
}

@end
