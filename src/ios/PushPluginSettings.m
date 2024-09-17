//
//  PushPluginSettings.m
//  cordovaTest
//
//  Created by Erisu on 2024/09/14.
//

#import "PushPluginSettings.h"

@interface PushPluginSettings ()

@property (nonatomic, strong) NSMutableDictionary *settingsDictionary;

@end

@implementation PushPluginSettings

+ (instancetype)sharedInstance {
    static PushPluginSettings *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
      sharedInstance = [[self alloc] initWithDefaults];
    });
    return sharedInstance;
}

- (instancetype)initWithDefaults {
    self = [super init];
    if (self) {
        self.settingsDictionary = [@{
            @"badge" : @(NO),
            @"sound" : @(NO),
            @"alert" : @(NO),
            @"critical" : @(NO),
            @"clearBadge" : @(NO),
            @"forceShow" : @(NO),
            @"voip" : @(NO),
            @"fcmTopics" : @[],
            @"categories" : [NSSet set]
        } mutableCopy];
    }
    return self;
}

- (void)updateSettingsWithOptions:(NSDictionary *)options {
    for (NSString *key in options) {
        if ([self.settingsDictionary objectForKey:key]) {
            // Overrides the default setting if defined and apply the correct formatting based on the key.
            if ([key isEqualToString:@"fcmTopics"]) {
                self.settingsDictionary[key] = [self parseArrayOption:key fromOptions:options withDefault:nil];
            } else if ([key isEqualToString:@"categories"]) {
                self.settingsDictionary[key] = [self parseCategoriesFromOptions:options[key]];
            } else {
                self.settingsDictionary[key] = @([self parseOption:key fromOptions:options withDefault:NO]);
            }
        } else {
            NSLog(@"[PushPlugin] Settings: Invalid option key: %@", key);
        }
    }

    NSLog(@"[PushPlugin] Settings: %@", self.settingsDictionary);
}

- (BOOL)parseOption:(NSString *)key fromOptions:(NSDictionary *)options withDefault:(BOOL)defaultValue {
    id option = [options objectForKey:key];
    if ([option isKindOfClass:[NSString class]]) {
        return [option isEqualToString:@"true"];
    }
    if ([option respondsToSelector:@selector(boolValue)]) {
        return [option boolValue];
    }
    return defaultValue;
}

- (NSArray *)parseArrayOption:(NSString *)key fromOptions:(NSDictionary *)options withDefault:(NSArray *)defaultValue {
    id option = [options objectForKey:key];
    if ([option isKindOfClass:[NSArray class]]) {
        return (NSArray *)option;
    }
    return defaultValue;
}

- (NSSet<UNNotificationCategory *> *)parseCategoriesFromOptions:(NSDictionary *)categoryOptions {
    NSMutableSet<UNNotificationCategory *> *categoriesSet = [[NSMutableSet alloc] init];
    if (categoryOptions != nil && [categoryOptions isKindOfClass:[NSDictionary class]]) {
        for (id key in categoryOptions) {
            NSDictionary *category = [categoryOptions objectForKey:key];
            UNNotificationCategory *notificationCategory = [self createCategoryFromDictionary:category withIdentifier:key];
            if (notificationCategory) {
                [categoriesSet addObject:notificationCategory];
            }
        }
    }
    return categoriesSet;
}

- (UNNotificationCategory *)createCategoryFromDictionary:(NSDictionary *)category withIdentifier:(NSString *)identifier {
    NSMutableArray<UNNotificationAction *> *actions = [[NSMutableArray alloc] init];

    UNNotificationAction *yesAction = [self createActionFromDictionary:[category objectForKey:@"yes"]];
    if (yesAction)
        [actions addObject:yesAction];

    UNNotificationAction *noAction = [self createActionFromDictionary:[category objectForKey:@"no"]];
    if (noAction)
        [actions addObject:noAction];

    UNNotificationAction *maybeAction = [self createActionFromDictionary:[category objectForKey:@"maybe"]];
    if (maybeAction)
        [actions addObject:maybeAction];

    return [UNNotificationCategory categoryWithIdentifier:identifier actions:actions intentIdentifiers:@[] options:UNNotificationCategoryOptionNone];
}

- (UNNotificationAction *)createActionFromDictionary:(NSDictionary *)dictionary {
    if (![dictionary isKindOfClass:[NSDictionary class]]) {
        return nil;
    }

    NSString *identifier = [dictionary objectForKey:@"identifier"];
    NSString *title = [dictionary objectForKey:@"title"];

    if (!title || !identifier) {
        return nil;
    }

    UNNotificationActionOptions options = UNNotificationActionOptionNone;
    id foreground = [dictionary objectForKey:@"foreground"];
    if (foreground != nil && (([foreground isKindOfClass:[NSString class]] && [foreground isEqualToString:@"true"]) || [foreground boolValue])) {
        options |= UNNotificationActionOptionForeground;
    }

    id destructive = [dictionary objectForKey:@"destructive"];
    if (destructive != nil && (([destructive isKindOfClass:[NSString class]] && [destructive isEqualToString:@"true"]) || [destructive boolValue])) {
        options |= UNNotificationActionOptionDestructive;
    }

    return [UNNotificationAction actionWithIdentifier:identifier title:title options:options];
}

#pragma mark - Getters for individual settings

- (BOOL)badgeEnabled {
    return [self.settingsDictionary[@"badge"] boolValue];
}

- (BOOL)soundEnabled {
    return [self.settingsDictionary[@"sound"] boolValue];
}

- (BOOL)alertEnabled {
    return [self.settingsDictionary[@"alert"] boolValue];
}

- (BOOL)criticalEnabled {
    return [self.settingsDictionary[@"critical"] boolValue];
}

- (BOOL)clearBadgeEnabled {
    return [self.settingsDictionary[@"clearBadge"] boolValue];
}

- (BOOL)forceShowEnabled {
    return [self.settingsDictionary[@"forceShow"] boolValue];
}

- (BOOL)voipEnabled {
    return [self.settingsDictionary[@"voip"] boolValue];
}

- (NSArray *)fcmTopics {
    return self.settingsDictionary[@"fcmTopics"];
}

- (NSSet<UNNotificationCategory *> *)categories {
    return self.settingsDictionary[@"categories"];
}

@end
