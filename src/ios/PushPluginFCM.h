#import <Foundation/Foundation.h>
@import Firebase;

@interface PushPluginFCM : NSObject

@property (nonatomic, assign) BOOL isFCMEnabled;

- (instancetype)initWithGoogleServicePlist;
- (void)configureFirebase;

@end
