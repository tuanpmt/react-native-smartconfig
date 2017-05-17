//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//


#import <Foundation/Foundation.h>

@interface NSData (AESCrypt)

- (NSData *)AES128EncryptWithKey:(NSString *)key noLength:(BOOL)noLength first16Bytes:(BOOL)first16Bytes withLength:(int)keyLength;

- (NSString *)base64Encoding;

@end