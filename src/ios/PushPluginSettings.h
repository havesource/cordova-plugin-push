//
//  PushPluginSettings.h
//  cordovaTest
//
//  Created by Erisu on 2024/09/14.
//

#import <Foundation/Foundation.h>
#import <UserNotifications/UserNotifications.h>

@interface PushPluginSettings : NSObject

@property (nonatomic, readonly) BOOL badgeEnabled;
@property (nonatomic, readonly) BOOL soundEnabled;
@property (nonatomic, readonly) BOOL alertEnabled;
@property (nonatomic, readonly) BOOL criticalEnabled;
@property (nonatomic, readonly) BOOL clearBadgeEnabled;
@property (nonatomic, readonly) BOOL forceShowEnabled;
@property (nonatomic, readonly) BOOL voipEnabled;

@property (nonatomic, readonly, strong) NSArray *fcmTopics;
@property (nonatomic, readonly, strong) NSSet<UNNotificationCategory *> *categories;

+ (instancetype)sharedInstance;

- (void)updateSettingsWithOptions:(NSDictionary *)options;

@end
