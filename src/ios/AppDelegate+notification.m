//
//  AppDelegate+notification.m
//  pushtest
//
//  Created by Robert Easterday on 10/26/12.
//
//

#import "AppDelegate+notification.h"
#import "PushPlugin.h"
#import <objc/runtime.h>

static char launchNotificationKey;
static char coldstartKey;
NSString *const pushPluginApplicationDidBecomeActiveNotification = @"pushPluginApplicationDidBecomeActiveNotification";

// store the previous notification for payload comparison to try and avoid processing duplicate payloads which occurs in iOS 18.
static const void *PreviousNotificationKey = &PreviousNotificationKey;

@implementation AppDelegate (notification)

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

- (AppDelegate *)pushPluginSwizzledInit
{
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    center.delegate = self;

    [[NSNotificationCenter defaultCenter]addObserver:self
                                            selector:@selector(pushPluginOnApplicationDidBecomeActive:)
                                                name:UIApplicationDidBecomeActiveNotification
                                              object:nil];

    // This actually calls the original init method over in AppDelegate. Equivilent to calling super
    // on an overrided method, this is not recursive, although it appears that way. neat huh?
    return [self pushPluginSwizzledInit];
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    PushPlugin *pushHandler = [self getCommandInstance:@"PushNotification"];
    [pushHandler didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    PushPlugin *pushHandler = [self getCommandInstance:@"PushNotification"];
    [pushHandler didFailToRegisterForRemoteNotificationsWithError:error];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    NSLog(@"didReceiveNotification with fetchCompletionHandler");

    // app is in the background or inactive, so only call notification callback if this is a silent push
    if (application.applicationState != UIApplicationStateActive) {

        NSLog(@"app in-active");

        // do some convoluted logic to find out if this should be a silent push.
        long silent = 0;
        id aps = [userInfo objectForKey:@"aps"];
        id contentAvailable = [aps objectForKey:@"content-available"];
        if ([contentAvailable isKindOfClass:[NSString class]] && [contentAvailable isEqualToString:@"1"]) {
            silent = 1;
        } else if ([contentAvailable isKindOfClass:[NSNumber class]]) {
            silent = [contentAvailable integerValue];
        }

        if (silent == 1) {
            NSLog(@"this should be a silent push");
            void (^safeHandler)(UIBackgroundFetchResult) = ^(UIBackgroundFetchResult result){
                dispatch_async(dispatch_get_main_queue(), ^{
                    completionHandler(result);
                });
            };

            PushPlugin *pushHandler = [self getCommandInstance:@"PushNotification"];

            if (pushHandler.handlerObj == nil) {
                pushHandler.handlerObj = [NSMutableDictionary dictionaryWithCapacity:2];
            }

            id notId = [userInfo objectForKey:@"notId"];
            if (notId != nil) {
                NSLog(@"Push Plugin notId %@", notId);
                [pushHandler.handlerObj setObject:safeHandler forKey:notId];
            } else {
                NSLog(@"Push Plugin notId handler");
                [pushHandler.handlerObj setObject:safeHandler forKey:@"handler"];
            }

            pushHandler.notificationMessage = userInfo;
            pushHandler.isInline = NO;
            [pushHandler notificationReceived];
        } else {
            NSLog(@"just put it in the shade");
            //save it for later
            self.launchNotification = userInfo;
            completionHandler(UIBackgroundFetchResultNewData);
        }

    } else {
        completionHandler(UIBackgroundFetchResultNoData);
    }
}

- (void)checkUserHasRemoteNotificationsEnabledWithCompletionHandler:(nonnull void (^)(BOOL))completionHandler
{
    [[UNUserNotificationCenter currentNotificationCenter] getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings * _Nonnull settings) {

        switch (settings.authorizationStatus)
        {
            case UNAuthorizationStatusDenied:
            case UNAuthorizationStatusNotDetermined:
                completionHandler(NO);
                break;
            case UNAuthorizationStatusAuthorized:
                completionHandler(YES);
                break;
        }
    }];
}

- (void)pushPluginOnApplicationDidBecomeActive:(NSNotification *)notification {

    NSLog(@"active");
    
    NSString *firstLaunchKey = @"firstLaunchKey";
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:@"phonegap-plugin-push"];
    if (![defaults boolForKey:firstLaunchKey]) {
        NSLog(@"application first launch: remove badge icon number");
        [defaults setBool:YES forKey:firstLaunchKey];
        [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
    }

    UIApplication *application = notification.object;

    PushPlugin *pushHandler = [self getCommandInstance:@"PushNotification"];
    if (pushHandler.clearBadge) {
        NSLog(@"PushPlugin clearing badge");
        //zero badge
        application.applicationIconBadgeNumber = 0;
    } else {
        NSLog(@"PushPlugin skip clear badge");
    }

    if (self.launchNotification) {
        pushHandler.isInline = NO;
        pushHandler.coldstart = [self.coldstart boolValue];
        pushHandler.notificationMessage = self.launchNotification;
        self.launchNotification = nil;
        self.coldstart = [NSNumber numberWithBool:NO];
        [pushHandler performSelectorOnMainThread:@selector(notificationReceived) withObject:pushHandler waitUntilDone:NO];
    }

    [[NSNotificationCenter defaultCenter] postNotificationName:pushPluginApplicationDidBecomeActiveNotification object:nil];
}

- (BOOL)isDuplicateNotification:(UNNotification *)notification {
    // Extract relevant data from the current notification
    NSDate *currentNotificationDate = notification.date;
    NSDictionary *currentPayload = notification.request.content.userInfo;

    BOOL isDuplicate = NO;

    if (self.previousNotification) {
        NSDate *previousNotificationDate = self.previousNotification.date;
        NSDictionary *previousPayload = self.previousNotification.request.content.userInfo;

        // Compare dates
        BOOL isSameDate = [currentNotificationDate isEqualToDate:previousNotificationDate];

        // Compare payloads by serializing to JSON strings for easy comparison
        NSError *error = nil;
        NSData *currentPayloadData = [NSJSONSerialization dataWithJSONObject:currentPayload options:0 error:&error];
        NSData *previousPayloadData = [NSJSONSerialization dataWithJSONObject:previousPayload options:0 error:&error];

        if (!error && currentPayloadData && previousPayloadData) {
            NSString *currentPayloadString = [[NSString alloc] initWithData:currentPayloadData encoding:NSUTF8StringEncoding];
            NSString *previousPayloadString = [[NSString alloc] initWithData:previousPayloadData encoding:NSUTF8StringEncoding];

            BOOL isSamePayload = [currentPayloadString isEqualToString:previousPayloadString];

            // Determine if the notification is a duplicate
            isDuplicate = isSameDate && isSamePayload;
        }
    }

    return isDuplicate;
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
       willPresentNotification:(UNNotification *)notification
         withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler {

    #if __IPHONE_OS_VERSION_MAX_ALLOWED >= 180000
    if ([self isDuplicateNotification:notification]) {
        NSLog(@"[PushPlugin] Duplicate notification was detected and will skip processing.");
        completionHandler(UNNotificationPresentationOptionNone);
        // Cleanup to remove previous notification to remove leaks
        [self setPreviousNotification:nil];
        return;
    }

    // If it was not duplicate, we will store it to check for the potential second notification
    [self setPreviousNotification:notification];
    #endif

    NSLog(@"[PushPlugin] NotificationCenter Handle push from foreground");

    // custom code to handle push while app is in the foreground
    PushPlugin *pushHandler = [self getCommandInstance:@"PushNotification"];
    pushHandler.notificationMessage = notification.request.content.userInfo;
    pushHandler.isInline = YES;
    [pushHandler notificationReceived];

    UNNotificationPresentationOptions presentationOption = UNNotificationPresentationOptionNone;
    if (@available(iOS 10, *)) {
        if(pushHandler.forceShow) {
            presentationOption = UNNotificationPresentationOptionAlert;
        }
    }
    completionHandler(presentationOption);
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void(^)(void))completionHandler
{
    NSLog(@"Push Plugin didReceiveNotificationResponse: actionIdentifier %@, notification: %@", response.actionIdentifier,
          response.notification.request.content.userInfo);
    NSMutableDictionary *userInfo = [response.notification.request.content.userInfo mutableCopy];
    [userInfo setObject:response.actionIdentifier forKey:@"actionCallback"];
    NSLog(@"Push Plugin userInfo %@", userInfo);

    switch ([UIApplication sharedApplication].applicationState) {
        case UIApplicationStateActive:
        {
            PushPlugin *pushHandler = [self getCommandInstance:@"PushNotification"];
            pushHandler.notificationMessage = userInfo;
            pushHandler.isInline = NO;
            [pushHandler notificationReceived];
            completionHandler();
            break;
        }
        case UIApplicationStateInactive:
        {
            NSLog(@"coldstart");
            
            if([response.actionIdentifier rangeOfString:@"UNNotificationDefaultActionIdentifier"].location == NSNotFound) {
                self.launchNotification = userInfo;
            }
            else {
                self.launchNotification = response.notification.request.content.userInfo;
            }

            self.coldstart = [NSNumber numberWithBool:YES];
            break;
        }
        case UIApplicationStateBackground:
        {
            void (^safeHandler)(void) = ^(void){
                dispatch_async(dispatch_get_main_queue(), ^{
                    completionHandler();
                });
            };

            PushPlugin *pushHandler = [self getCommandInstance:@"PushNotification"];

            if (pushHandler.handlerObj == nil) {
                pushHandler.handlerObj = [NSMutableDictionary dictionaryWithCapacity:2];
            }

            id notId = [userInfo objectForKey:@"notId"];
            if (notId != nil) {
                NSLog(@"Push Plugin notId %@", notId);
                [pushHandler.handlerObj setObject:safeHandler forKey:notId];
            } else {
                NSLog(@"Push Plugin notId handler");
                [pushHandler.handlerObj setObject:safeHandler forKey:@"handler"];
            }

            pushHandler.notificationMessage = userInfo;
            pushHandler.isInline = NO;

            [pushHandler performSelectorOnMainThread:@selector(notificationReceived) withObject:pushHandler waitUntilDone:NO];
        }
    }
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

- (void)setPreviousNotification:(UNNotification *)previousNotification {
    objc_setAssociatedObject(self, PreviousNotificationKey, previousNotification, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (UNNotification *)previousNotification {
    return objc_getAssociatedObject(self, PreviousNotificationKey);
}

- (void)dealloc
{
    self.launchNotification = nil; // clear the association and release the object
    self.coldstart = nil;

    [self setPreviousNotification:nil];
}

@end
