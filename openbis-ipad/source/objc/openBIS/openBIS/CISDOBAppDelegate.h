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
//  CISDOBAppDelegate.h
//  openBIS
//
//  Created by Ramakrishnan  Chandrasekhar on 10/3/12.
//  Copyright (c) 2012 ETHZ, CISD. All rights reserved.
//

#import <UIKit/UIKit.h>

@class CISDOBOpenBisModel, CISDOBIpadServiceManager, CISDOBAsyncCall, CISDOBLoginViewController, CISDOBAuthenticationChallengeConfirmationDialog;
@interface CISDOBAppDelegate : UIResponder <UIApplicationDelegate> {
@private
    CISDOBAuthenticationChallengeConfirmationDialog *_challengeDialog;
}

@property (strong, nonatomic) UIWindow *window;

@property (readonly, strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (readonly, strong, nonatomic) NSManagedObjectModel *managedObjectModel;
@property (readonly, strong, nonatomic) NSPersistentStoreCoordinator *persistentStoreCoordinator;
@property (readonly, strong, nonatomic) CISDOBOpenBisModel *rootOpenBisModel;
@property (readonly, strong, nonatomic) CISDOBIpadServiceManager *serviceManager;
@property (readonly, getter=isOnline) BOOL online;

- (NSURL *)applicationDocumentsDirectory;

// User Settings
@property (copy, nonatomic) NSString *username;
@property (copy, nonatomic) NSString *password;
@property (copy, nonatomic) NSURL *openbisUrl;
- (BOOL)synchronizeUserSettings;
- (void)verifyLoginURL:(NSURL *)openbisUrl username:(NSString *)username password:(NSString *)password sender:(CISDOBLoginViewController *)controller;
- (void)loginControllerDidComplete:(CISDOBLoginViewController *)controller;

// Actions
- (void)saveContext;
- (void)goOnline;

@end
