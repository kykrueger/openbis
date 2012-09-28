/*
 * Copyright 2012 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//
//  CISDOBIpadService.m
//  BisMac
//
//  Created by cramakri on 27.08.12.
//
//

#import "CISDOBIpadService.h"
#import "CISDOBConnection.h"
#import "CISDOBAsyncCall.h"

NSString *const CISDOBIpadServiceErrorDomain = @"CISDOBIpadServiceErrorDomain";

// Internal service call that includes the private state
@interface CISDOBIpadServiceCall : CISDOBAsyncCall {
@private
    // Internal state
    CISDOBAsyncCall     *_connectionCall;
}
@property(weak) CISDOBIpadService *service;
@property(nonatomic) CISDOBAsyncCall *connectionCall;

// Initialization
- (id)initWithService:(CISDOBIpadService *)service connectionCall:(CISDOBAsyncCall *)call;

@end


@implementation CISDOBIpadService

- (id)init
{
    self = [super init];
    if (self) {
        // Initialization code here.
    }
    
    return self;
}

- (id)initWithConnection:(CISDOBConnection *)aConn
{
    self = [super init];
    if (!self) return nil;
    
    _connection = aConn;
    _isLoggedIn = NO;

    return self;
}

- (BOOL)isIpadSupported { return _ipadReadService != nil; }

- (void)rememberIpadService:(NSArray *)services notifying:(CISDOBIpadServiceCall *)iPadCall
{    
    for (NSDictionary *service in services) {
        if ([@"ipad-read-service" isEqualToString: [service objectForKey: @"serviceKey"]]) {
            _ipadReadService = service;
            break;
        }
    }
    
    if (_ipadReadService == nil) {
        NSString *errorMessage = @"The iPad service is not installed on the selected server";
        NSDictionary *userInfo =
            [NSDictionary dictionaryWithObjectsAndKeys: errorMessage, NSLocalizedDescriptionKey, nil];
        NSError *error = [NSError errorWithDomain: CISDOBIpadServiceErrorDomain code: kCISOBIpadServiceError_NoIpadServiceAvailable userInfo: userInfo];
        if (iPadCall.fail) iPadCall.fail(error);
        return;
    }
    
    if (iPadCall.success) iPadCall.success(_connection.sessionToken);
    
}

- (void)determineIsIpadSupported:(CISDOBIpadServiceCall *)iPadCall
{
    CISDOBAsyncCall *connectionCall = [_connection listAggregationServices];
    iPadCall.connectionCall = connectionCall;
    connectionCall.success = ^(id result) { [self rememberIpadService: result notifying: iPadCall]; };
    connectionCall.fail = ^(NSError *error) { if (iPadCall.fail) iPadCall.fail(error); };
    [connectionCall start];
}

- (CISDOBAsyncCall *)loginUser:(NSString *)user password:(NSString *)password
{
    CISDOBAsyncCall *connectionCall = [_connection loginUser: user password: password];
    CISDOBIpadServiceCall *iPadCall = [[CISDOBIpadServiceCall alloc] initWithService: self connectionCall: connectionCall];
    
    connectionCall.success = ^(id result) {
        // Note that we are logged in, but wait until we figure out if the ipad is supported
        // to notify the client.
        _isLoggedIn = YES;
        [self determineIsIpadSupported: iPadCall];
    };
    connectionCall.fail = ^(NSError *error) { if (iPadCall.fail) iPadCall.fail(error); };
    
    return iPadCall;
}

- (CISDOBAsyncCall *)listAllEntities;
{
    CISDOBAsyncCall *call = [_connection
        createReportFromDataStore: [_ipadReadService objectForKey: @"dataStoreCode"]
        aggregationService: [_ipadReadService objectForKey: @"serviceKey"]
        parameters: nil];
    return call;
}

@end

@implementation CISDOBIpadServiceCall

- (id)initWithService:(CISDOBIpadService *)service connectionCall:(CISDOBAsyncCall *)call
{
    if (!(self = [super init])) return nil;
 
    _service = service;
    _connectionCall = call;
    
    return self;
}

- (void)start
{
    [_connectionCall start];
}

@end
