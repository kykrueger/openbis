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
//  BisMacDocument.m
//  BisMac
//
//  Created by cramakri on 07.08.12.
//
//

#import "BisMacDocument.h"
#import "CISDOBJsonRpcCall.h"

@implementation BisMacDocument

- (id)init
{
    self = [super init];
    if (self) {
        // Add your subclass-specific initialization here.
        // If an error occurs here, send a [self release] message and return nil.
    }
    return self;
}

- (NSString *)windowNibName
{
    // Override returning the nib file name of the document
    // If you need to use a subclass of NSWindowController or if your document supports multiple NSWindowControllers, you should remove this method and override -makeWindowControllers instead.
    return @"BisMacDocument";
}

- (void)testCall
{

    CISDOBJsonRpcCall *generalInfoServiceCall = [[CISDOBJsonRpcCall alloc] init];
    generalInfoServiceCall.url = [NSURL URLWithString: @"http://www.raboof.com/projects/jayrock/demo.ashx"];
    generalInfoServiceCall.method = @"add";
    generalInfoServiceCall.params = [NSArray arrayWithObjects: @"1", @"2", nil];
    generalInfoServiceCall.timeoutInterval = 10.0;
    SuccessBlock success = ^(id result) { 
        NSLog(@"Call returned result : %@", result); 
    };
    
    FailBlock fail = ^(NSError *error) { 
        NSLog(@"Call failed : %@", error); 
    };
    generalInfoServiceCall.success = success;
    generalInfoServiceCall.fail = fail;    
    [generalInfoServiceCall start];
}


- (void)windowControllerDidLoadNib:(NSWindowController *)aController
{
    [super windowControllerDidLoadNib:aController];
    // Add any code here that needs to be executed once the windowController has loaded the document's window.
    
    [self testCall];
    
}


@end
