#import <Foundation/Foundation.h>
@import Firebase;

@interface PushPluginFCM : NSObject

@property (nonatomic, assign) BOOL isFCMEnabled;

- (instancetype)initWithGoogleServicePlist;

- (void)configureFirebase;
- (void)setTokenWithCompletion:(void (^)(NSString *token))completion;

- (void)subscribeToTopic:(NSString *)topic;
- (void)subscribeToTopics:(NSArray *)topics;

- (void)unsubscribeFromTopic:(NSString *)topic;
- (void)unsubscribeFromTopics:(NSArray *)topics;

@end
