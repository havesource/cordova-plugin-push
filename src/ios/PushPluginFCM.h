#import <Foundation/Foundation.h>
@import Firebase;

NS_ASSUME_NONNULL_BEGIN

@interface PushPluginFCM : NSObject

@property (nonatomic, assign) BOOL isFCMEnabled;

- (instancetype)initWithGoogleServicePlist;

- (void)configure;
- (void)setAPNSToken:(NSData *)token;
- (void)setTokenWithCompletion:(void (^)(NSString *token))completion;

- (void)subscribeToTopic:(NSString *)topic;
- (void)subscribeToTopics:(NSArray *)topics;

- (void)unsubscribeFromTopic:(NSString *)topic;
- (void)unsubscribeFromTopics:(NSArray *)topics;

+ (NSNotificationName)pushPluginFCMMessagingRegistrationTokenRefreshedNotification;

@end

NS_ASSUME_NONNULL_END
