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
//  CISDOBIpadServiceManager.m
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 10/30/12.
//
//

#import "CISDOBIpadServiceManager.h"
#import "CISDOBIpadService.h"
#import "CISDOBIpadEntity.h"
#import "CISDOBConnection.h"
#import "CISDOBAsyncCall.h"

// Internal service call that includes the private state
@interface CISDOBIpadServiceManagerCall : CISDOBAsyncCall

@property(weak) CISDOBIpadServiceManager *serviceManager;
@property(nonatomic) CISDOBAsyncCall *serviceCall;

// Initialization
- (id)initWithServiceManager:(CISDOBIpadServiceManager *)serviceManager serviceCall:(CISDOBAsyncCall *)call;

@end

static NSManagedObjectContext* GetDatabaseManagedObjectContext(NSURL* storeUrl, NSError** error)
{
	// Explicitly specify which db schema we want to use
	NSBundle* bundle = [NSBundle bundleForClass: [CISDOBIpadEntity class]];
	NSString* momPath = [bundle pathForResource: @"persistent-data-model" ofType: @"momd"];
	NSManagedObjectModel* mom = [[NSManagedObjectModel alloc] initWithContentsOfURL: [NSURL fileURLWithPath: momPath]];

	NSManagedObjectContext* moc = [[NSManagedObjectContext alloc] init];
	NSPersistentStoreCoordinator* coordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel: mom];
	[moc setPersistentStoreCoordinator: coordinator];	
	NSPersistentStore* store = 
		[coordinator 
			addPersistentStoreWithType: NSSQLiteStoreType 
			configuration: nil 
			URL: storeUrl
			options: nil 
			error: error];
	if (!store) {
		return nil;
	}
	return moc;
}


@implementation CISDOBIpadServiceManager


- (id)initWithStoreUrl:(NSURL *)storeUrl openbisUrl:(NSURL *)openbisUrl trusted:(BOOL)trusted error:(NSError **)error
{
    if (!(self = [super init])) return nil;
    
    CISDOBLiveConnection *connection = [[CISDOBLiveConnection alloc] initWithUrl: openbisUrl trusted: trusted];
    _storeUrl = [storeUrl copy];
    _service = [[CISDOBIpadService alloc] initWithConnection: connection];
    _managedObjectContext = GetDatabaseManagedObjectContext(self.storeUrl, error);
    _persistentStoreCoordinator = _managedObjectContext.persistentStoreCoordinator;
    if (!_managedObjectContext) return nil;
    
    _ipadEntityDescription = [NSEntityDescription entityForName: @"CISDOBIpadEntity" inManagedObjectContext: _managedObjectContext];
    _managedObjectModel = [_ipadEntityDescription managedObjectModel];
    
    return self;
}

- (BOOL)synchEntity:(CISDOBIpadRawEntity *)rawEntity lastUpdateDate:(NSDate *)date error:(NSError **)error
{
{
    // Create new entities in the moc, and store them.
    CISDOBIpadEntity *entity;
    NSArray *matchedEntities = [self entitiesByPermId: [NSArray arrayWithObject: rawEntity.permId] error: error];
    if (!matchedEntities) return NO;
    if ([matchedEntities count] > 0) {
        entity = [matchedEntities objectAtIndex: 0];
        [entity updateFromRawEntity: rawEntity];
    } else {
        entity = [NSEntityDescription insertNewObjectForEntityForName: @"CISDOBIpadEntity" inManagedObjectContext: self.managedObjectContext];
        [entity initializeFromRawEntity: rawEntity];
    }
    entity.lastUpdateDate = date;
    entity.serverUrlString =  [((CISDOBLiveConnection *)(self.service.connection)).url absoluteString];
    
    return YES;
}
}

- (BOOL)syncEntities:(NSArray *)rawEntities pruning:(BOOL)prune error:(NSError **)error
{
    NSDate *lastUpdateDate = [NSDate date];
    BOOL success;
    for (CISDOBIpadRawEntity *rawEntity in rawEntities) {
        success = [self synchEntity: rawEntity lastUpdateDate: lastUpdateDate error: error];
        if (!success) return NO;
    }
    // If pruning is requested, remove entities that cannot be reached from the server result set.
    // TODO : we should treat the intial results as a root set and trace out to do a gc, but the simpler implementation is just to remove everything that is not mentioned
    if (prune) {
        // Remove all entities that were not mentioned
        NSArray *entitiesToDelete = [self entitiesNotUpdatedSince: lastUpdateDate error: error];
        for (CISDOBIpadEntity *entity in entitiesToDelete) {
            [self.managedObjectContext deleteObject: entity];
        }
    }
    
    success = [self.managedObjectContext save: error];
    return success;
}

- (CISDOBIpadServiceManagerCall *)managerCallWrappingServiceCall:(CISDOBAsyncCall *)serviceCall pruning:(BOOL)prune
{
    CISDOBIpadServiceManagerCall *managerCall = [[CISDOBIpadServiceManagerCall alloc] initWithServiceManager: self serviceCall: serviceCall];
    
    serviceCall.success = ^(id result) {
        // Update the cache
        NSError *error;
        BOOL didSync = [self syncEntities: result pruning: prune error: &error];
        if (!didSync) {
            serviceCall.fail(error);
        } else if (managerCall.success) {
            managerCall.success(result);
        }
    };    
    
    serviceCall.fail = ^(NSError *error) { if (managerCall.fail) managerCall.fail(error); };
    
    return managerCall;
}

- (CISDOBIpadServiceManagerCall *)managerCallWrappingServiceCall:(CISDOBAsyncCall *)serviceCall
{
    return [self managerCallWrappingServiceCall: serviceCall pruning: NO];
}

- (CISDOBAsyncCall *)loginUser:(NSString *)user password:(NSString *)password
{
    return [self.service loginUser: user password: password];
}

- (CISDOBAsyncCall *)retrieveRootLevelEntities
{
    CISDOBAsyncCall *call = [self.service listRootLevelEntities];
        // get rid of entities not mentioned in the original call
    return [self managerCallWrappingServiceCall: call pruning: YES];
}

- (CISDOBAsyncCall *)drillOnEntity:(CISDOBIpadEntity *)entity
{
    CISDOBAsyncCall *call = [self.service drillOnEntityWithPermId: entity.permId refcon: entity.refcon];
    return [self managerCallWrappingServiceCall: call];
}

- (CISDOBAsyncCall *)detailsForEntity:(CISDOBIpadEntity *)entity
{
    CISDOBAsyncCall *call = [self.service detailsForEntityWithPermId: entity.permId refcon: entity.refcon];
    return [self managerCallWrappingServiceCall: call];
}

- (NSArray *)allIpadEntitiesOrError:(NSError **)error;
{
	NSFetchRequest* request = self.entityFetchRequest;
	return [self executeFetchRequest: request error: error];
}

- (NSArray *)entitiesByPermId:(NSArray *)permIds error:(NSError **)error
{
    NSDictionary *fetchVariables = [NSDictionary dictionaryWithObject: permIds forKey: @"PERM_IDS"];
    NSFetchRequest *request = [self.managedObjectModel fetchRequestFromTemplateWithName: @"EntitiesByPermIds" substitutionVariables: fetchVariables];
    return [self executeFetchRequest: request error: error];
}

- (NSArray *)entitiesNotUpdatedSince:(NSDate *)date error:(NSError **)error
{
    NSDictionary *fetchVariables = [NSDictionary dictionaryWithObject: date forKey: @"LAST_UPDATE_DATE"];
    NSFetchRequest *request = [self.managedObjectModel fetchRequestFromTemplateWithName: @"EntitiesNotUpdatedSince" substitutionVariables: fetchVariables];
    return [self executeFetchRequest: request error: error];
}

- (NSFetchRequest *)entityFetchRequest
{
	NSFetchRequest *request = [[NSFetchRequest alloc] init];
    [request setEntity: self.ipadEntityDescription];
    return request;
}

- (NSArray *)executeFetchRequest:(NSFetchRequest *)fetchRequest error:(NSError **)error
{
    return [self.managedObjectContext executeFetchRequest: fetchRequest error: error];
}

@end

@implementation CISDOBIpadServiceManagerCall

- (id)initWithServiceManager:(CISDOBIpadServiceManager *)serviceManager serviceCall:(CISDOBAsyncCall *)call
{
    if (!(self = [super init])) return nil;
 
    _serviceManager = serviceManager;
    _serviceCall = call;
    
    return self;
}

- (void)start
{
    [_serviceCall start];
}

@end
