#import "PushPluginFCM.h"

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

@end
