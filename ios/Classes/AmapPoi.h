//
//  AmapPoi.h
//  flutter_for_amaplocation
//
//  Created by lijunlin on 2021/3/23.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AmapPoi : NSObject

@property (nonatomic,assign) double lat;
@property (nonatomic,assign) double lng;
@property (nonatomic,copy) NSString* title;

@end

NS_ASSUME_NONNULL_END
