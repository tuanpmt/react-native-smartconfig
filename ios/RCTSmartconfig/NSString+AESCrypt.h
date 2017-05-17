//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//


#import <Foundation/Foundation.h>
#import "NSData+AESCrypt.h"

@interface NSString (AESCrypt)

- (NSData *)AES128EncryptWithKey:(NSString *)key;
+ (NSString *) stringToHex:(NSString *)str;

@end