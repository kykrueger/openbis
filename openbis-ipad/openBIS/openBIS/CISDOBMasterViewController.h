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
//  CISDOBMasterViewController.h
//  openBIS
//
//  Created by Ramakrishnan  Chandrasekhar on 10/3/12.
//  Copyright (c) 2012 ETHZ, CISD. All rights reserved.
//

#import <UIKit/UIKit.h>

@class CISDOBDetailViewController, CISDOBOpenBisModel, CISDOBIpadServiceManager;

#import <CoreData/CoreData.h>

@interface CISDOBMasterViewController : UITableViewController <NSFetchedResultsControllerDelegate>

@property (strong, nonatomic) CISDOBDetailViewController *detailViewController;
@property (strong, nonatomic) CISDOBOpenBisModel *openBisModel;

// Server Communication
- (void)didConnectServiceManager:(CISDOBIpadServiceManager *)serviceManager;

@end
