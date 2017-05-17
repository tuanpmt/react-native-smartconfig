//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//


#import <Foundation/Foundation.h>
#import <SystemConfiguration/CaptiveNetwork.h>
#import <SystemConfiguration/SystemConfiguration.h>
@interface NetworkHelper : NSObject

+ (NSString *)getSSID;

+ (NSString *)getGatewayAddress;

@end
