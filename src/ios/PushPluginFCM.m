#import "PushPluginFCM.h"
#import "PushPluginSettings.h"

@interface PushPluginFCM ()

@property (nonatomic, assign) BOOL isFCMRefreshTokenObserverAttached;
@property (nonatomic, weak) id <CDVCommandDelegate> commandDelegate;

@end

@implementation PushPluginFCM

- (instancetype)initWithGoogleServicePlist {
    self = [super init];
    if (self) {
        NSString *googleServicePath = [[NSBundle mainBundle] pathForResource:@"GoogleService-Info" ofType:@"plist"];
        NSDictionary *googleServicePlist = [[NSDictionary alloc] initWithContentsOfFile:googleServicePath];

        if (googleServicePlist != nil) {
            NSString *fcmSenderId = [googleServicePlist objectForKey:@"GCM_SENDER_ID"];
            BOOL isGcmEnabled = [[googleServicePlist valueForKey:@"IS_GCM_ENABLED"] boolValue];

            if (isGcmEnabled && fcmSenderId != nil) {
                self.isFCMEnabled = YES;
                NSLog(@"[PushPlugin] FCM is enabled and Sender ID is available.");
            } else {
                self.isFCMEnabled = NO;
                NSLog(@"[PushPlugin] FCM is not enabled or Sender ID is missing.");
            }
        } else {
            self.isFCMEnabled = NO;
            NSLog(@"[PushPlugin] Could not locate GoogleService-Info.plist.");
        }
    }
    return self;
}

- (void)configure:(id <CDVCommandDelegate>)commandDelegate {
    NSLog(@"[PushPlugin] Configuring Firebase App for FCM");
    [FIRApp configure];

    self.commandDelegate = commandDelegate;
}

- (void)configureTokens:(NSData *)token {
    NSLog(@"[PushPlugin] Setting APNS Token for Firebase App");
    [[FIRMessaging messaging] setAPNSToken:token];

    [self setFCMTokenWithCompletion];
}

- (void)setRefreshedFCMToken {
    NSLog(@"[PushPlugin] FIR has triggered a token refresh.");
    [self setFCMTokenWithCompletion];
}

- (void)setFCMTokenWithCompletion {
    #if TARGET_IPHONE_SIMULATOR
    NSLog(@"[PushPlugin] Detected simulator. Will register an FCM token but pushing to simulator is not possible.");
    #endif

    __weak __typeof(self) weakSelf = self;
    [[FIRMessaging messaging] tokenWithCompletion:^(NSString *token, NSError *error) {
        if (error != nil) {
            NSLog(@"[PushPlugin] Error getting FCM registration token: %@", error);
        } else {
            NSLog(@"[PushPlugin] FCM registration token: %@", token);
            [weakSelf subscribeToTopics:[PushPluginSettings sharedInstance].fcmTopics];
            [weakSelf sendRegistrationPluginResult:token];
            [weakSelf attachFCMTokenRefreshObserver];
        }
    }];
}

- (void)sendRegistrationPluginResult:(NSString *)token {
    NSMutableDictionary* message = [NSMutableDictionary dictionaryWithCapacity:2];
    [message setObject:token forKey:@"registrationId"];
    [message setObject:@"FCM" forKey:@"registrationType"];

    // Send result to trigger 'registration' event but keep callback
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

- (void)attachFCMTokenRefreshObserver {
    if (self.isFCMRefreshTokenObserverAttached) {
        NSLog(@"[PushPlugin] FCM token refresh observer was already attached.");
        return;
    }

    NSLog(@"[PushPlugin] Attaching FCM token refresh observer.");
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(setRefreshedFCMToken)
                                                 name:FIRMessagingRegistrationTokenRefreshedNotification
                                               object:nil];

    self.isFCMRefreshTokenObserverAttached = YES;
}

- (void)subscribeToTopics:(NSArray *)topics {
    if(!topics) {
        NSLog(@"[PushPlugin] There are no topics to subscribe to.");
        return;
    }

    for (NSString *topic in topics) {
        [self subscribeToTopic:topic];
    }
}

- (void)subscribeToTopic:(NSString *)topic {
    if (!topic) {
        NSLog(@"[PushPlugin] There is no topic to subscribe to.");
        return;
    }

    NSLog(@"[PushPlugin] Subscribing to topic: %@", topic);
    [[FIRMessaging messaging] subscribeToTopic:topic];
    NSLog(@"[PushPlugin] Successfully subscribed to topic %@", topic);
}

- (void)unsubscribeFromTopics:(NSArray *)topics {
    if(!topics) {
        NSLog(@"[PushPlugin] There are no topics to unsubscribe from.");
        return;
    }

    for (NSString *topic in topics) {
        [self unsubscribeFromTopic:topic];
    }
}

- (void)unsubscribeFromTopic:(NSString *)topic {
    if (!topic) {
        NSLog(@"[PushPlugin] There is no topic to unsubscribe from.");
        return;
    }

    NSLog(@"[PushPlugin] Unsubscribing from topic: %@", topic);
    [[FIRMessaging messaging] unsubscribeFromTopic:topic];
    NSLog(@"[PushPlugin] Successfully unsubscribed from topic %@", topic);
}

@end
