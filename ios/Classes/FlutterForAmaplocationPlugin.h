#import <Flutter/Flutter.h>
#import <AMapFoundationKit/AMapFoundationKit.h>
#import <AMapLocationKit/AMapLocationKit.h>
#import <AMapSearchKit/AMapSearchKit.h>

@interface FlutterForAmaplocationPlugin : NSObject<FlutterPlugin,AMapLocationManagerDelegate,AMapSearchDelegate>


@end
