//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//


#import <Foundation/Foundation.h>

@interface FtcEncode : NSObject {
    @protected
    NSData * mKey;
    NSString * mSsid;
    NSData * mFree;
    NSMutableArray * mData;
    BOOL mEncryption;
}

- (id)initWithSsid:(NSString *)ssid withKey:(NSData *)key withFreeData:(NSData *)freeData withEncryption:(BOOL)encryption;
- (NSArray *)getPackets;

@end
