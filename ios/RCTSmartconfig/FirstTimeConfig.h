//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//



#import <Foundation/Foundation.h>
#import "FtcEncode.h"

@interface OSFailureException : NSException

@end

@interface FirstTimeConfig : NSObject {
    @private
    bool stopSending;
    NSCondition * stopSendingEvent;
    NSCondition * suspendSendingEvent;
    
    NSString * ip;
    NSString * ssid;
    NSString * key;
    int numberOfTries;

    int nSetup;
    int nSync;

    useconds_t delay;

/*    NSMutableData * sync1;
    NSMutableData * sync2;
*/
    NSData * encryptionKey;
    NSData * encryptionKeyPart1;
    NSData * encryptionKeyPart2;
    FtcEncode * ftcData;
    NSData * sockAddr;
    
    int listenSocket;
    int abortWaitForAckEvent[2];
    short listenPort;
    
    NSThread * sendingThread;
    NSCondition * stoppedSending;
    NSCondition * watchdogFinished;
    NSCondition * ackThreadFinished;
    
    bool isWatchdogRunning;
    bool isSuspended;
    
    const NSString * remoteDeviceName;
}

@property (strong) NSMutableData * sync1;
@property (strong) NSMutableData * sync2;

/**************************************************************\
 * Method name: initWithKey
 * Purpose: This method creates new FirstTimeConfig instance
 * Parameters: In (NSString *)Key - The network key
 *             In OPT (NSData *)encryptionKey - The AES key to
 *             encrypt the nework key with. Can be nil if not used
 * Exceptions: OSFailureException
 * Return value: New FirstTimeConfig instance
\**************************************************************/
- (id)initWithKey:(NSString *)Key withEncryptionKey:(NSData *)encryptionKey;

/**************************************************************\
 * Method name: stopTransmitting
 * Purpose: This method used to stop transmitting.
 * Parameters: None
 * Exceptions: OSFailureException
 * Return value: None
\**************************************************************/
- (void)stopTransmitting;

/**************************************************************\
 * Method name: transmitSettings
 * Purpose: This method begins the settings transmit. The method
 *          Creates a new thread that do the actually the sending
 *          and returns immediately
 * Parameters: None
 * Exceptions: OSFailureException
 * Return value: None
\**************************************************************/
- (void)transmitSettings;

/**************************************************************\
 * Method name: isTransmitting
 * Purpose: This method returns if the instance is transmitting
 * Parameters: None
 * Exceptions: OSFailureException
 * Return value: The method returns true if the instance is 
 *               transmitting and false otherwise
\**************************************************************/
- (bool)isTransmitting;

/*************************************************************\
 * Method name: getSSID
 * Purpose: This method retreives the SSID of the currently
 *          connected WIFI
 * Parameters: None
 * Exceptions: None
 * Return value: The SSID of the WIFI network
\**************************************************************/
+ (NSString *)getSSID;

/*************************************************************\
 * Method name: getGatewayAddress
 * Purpose: This method retreives the gateway of the currently
 *          connected network
 * Parameters: None
 * Exceptions: None
 * Return value: The IP of the network's gateway
 \**************************************************************/
+ (NSString *)getGatewayAddress;


/* The following procedure can throw an OSFailureException exception */
- (id)initWithData:(NSString *)Ip withSSID:(NSString *)Ssid withKey:(NSString *)Key withFreeData:(NSData*)freeData withEncryptionKey:(NSData *)EncryptionKey numberOfSetups:(int)numOfSetups numberOfSyncs:(int)numOfSyncs syncLength1:(int)lSync1 syncLength2:(int)lSync2 delayInMicroSeconds:(useconds_t)uDelay;
@end
