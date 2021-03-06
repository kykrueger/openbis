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
//  CISDOBIpadServiceTest.h
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 9/26/12.
//
//

#import "CISDOBAsyncTest.h"
#import "CISDOBIpadService.h"

/**
 * Test the ipad service. This test is designed to run against an instance
 * of openBIS that has the ipad-ui module installed.
 */
@interface CISDOBIpadServiceTest : CISDOBAsyncTest {
@private
    CISDOBIpadService *_service;
}

@end
