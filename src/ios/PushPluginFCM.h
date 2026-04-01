#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>

@import Firebase;

NS_ASSUME_NONNULL_BEGIN

@interface PushPluginFCM : NSObject

@property (nonatomic, assign) BOOL isFCMEnabled;
@property (nonatomic, strong) NSString *callbackId;

- (instancetype)initWithGoogleServicePlist;

- (void)configure:(id <CDVCommandDelegate>)commandDelegate;
- (void)configureTokens:(NSData *)token;

- (void)subscribeToTopic:(NSString *)topic;

- (void)unsubscribeFromTopic:(NSString *)topic;
- (void)unsubscribeFromTopics:(NSArray *)topics;

@end

NS_ASSUME_NONNULL_END
