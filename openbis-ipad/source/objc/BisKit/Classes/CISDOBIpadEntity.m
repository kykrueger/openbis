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
//  CISDOBIpadEntity.m
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 10/1/12.
//
//

#import "CISDOBIpadEntity.h"
#import "CISDOBIpadService.h"


@implementation CISDOBIpadEntity

@dynamic summaryHeader;
@dynamic summary;
@dynamic identifier;
@dynamic permId;
@dynamic refconJson;
@dynamic category;
@dynamic imageUrlString;
@dynamic childrenPermIdsJson;
@dynamic propertiesJson;
@dynamic rootLevel;
@dynamic serverUrlString;
@dynamic lastUpdateDate;
@dynamic serverInfo;

- (void)setRefconJson:(NSString *)refconJson
{
    // Update the object value as well.
    [self willChangeValueForKey: @"refconJson"];
    [self setPrimitiveValue: refconJson forKey: @"refconJson"];
    [self setPrimitiveValue: nil forKey: @"refcon"];
    [self didChangeValueForKey: @"refconJson"];
}

- (id)refcon
{
    [self willAccessValueForKey: @"refcon"];
    id refcon = [self primitiveValueForKey: @"refcon"];
    [self didAccessValueForKey: @"refcon"];
    
    if (nil == refcon) {
        NSError *error;
        refcon = ObjectFromJsonData(self.refconJson, &error);
        if (!refcon) return nil;
        if (error) {
            NSLog(@"Could not deserialize refcon %@", error);
        }
        [self setPrimitiveValue: refcon forKey: @"refcon"];
    }
    
    return refcon;
}

- (NSArray *)properties
{
    [self willAccessValueForKey: @"properties"];
    NSArray *properties = [self primitiveValueForKey: @"properties"];
    [self didAccessValueForKey: @"properties"];
    
    if (nil == properties) {
        NSError *error;
        properties = ObjectFromJsonData(self.propertiesJson, &error);
        if (error) {
            NSLog(@"Could not deserialize properties %@", error);
        } else {
            [self setPrimitiveValue: properties forKey: @"properties"];
        }
    }
    
    return properties;
}

- (NSArray *)childrenPermIds
{
    [self willAccessValueForKey: @"childrenPermIds"];
    NSArray *childrenPermIds = [self primitiveValueForKey: @"childrenPermIds"];
    [self didAccessValueForKey: @"childrenPermIds"];
    
    if (nil == childrenPermIds) {
        NSError *error;
        childrenPermIds = ObjectFromJsonData(self.childrenPermIdsJson, &error);
        if (!childrenPermIds) return nil;
        if (error) {
            NSLog(@"Could not deserialize childrenPermIds %@", error);
        }
        [self setPrimitiveValue: childrenPermIds forKey: @"childrenPermIds"];
    }
    
    return childrenPermIds;
}

- (void)initializeFromRawEntity:(CISDOBIpadRawEntity *)rawEntity
{
    // These will always be non-nil
    self.permId = rawEntity.permId;
    [self updateFromRawEntity: rawEntity];
}

- (void)updateFromRawEntity:(CISDOBIpadRawEntity *)rawEntity
{
    self.refconJson = rawEntity.refcon;
    
    // Need to check if these values were transmitted with the raw entity
    if (rawEntity.category) self.category = rawEntity.category;
    if (rawEntity.summaryHeader) self.summaryHeader = rawEntity.summaryHeader;
    if (rawEntity.summary) self.summary = rawEntity.summary;
    if (rawEntity.children) {
        [self willChangeValueForKey: @"childrenPermIds"];
        self.childrenPermIdsJson = rawEntity.children;
        [self setPrimitiveValue: nil forKey: @"childrenPermIds"];
        [self didChangeValueForKey: @"childrenPermIds"];
    }
    if (rawEntity.identifier) self.identifier = rawEntity.identifier;
    if (rawEntity.imageUrl) self.imageUrlString = rawEntity.imageUrl;
    if (rawEntity.images && [rawEntity.images length] > 0) {
        NSError *error;
        NSDictionary *imageSpecs = ObjectFromJsonData(rawEntity.images, &error);
        if (!imageSpecs) {
            NSLog(@"Could not parse images %@", error);
        } else {
            NSDictionary *marqueeImage = [imageSpecs objectForKey: @"MARQUEE"];
            if (marqueeImage && [marqueeImage objectForKey: @"URL"]) {
                NSString *urlString = [marqueeImage objectForKey: @"URL"];
                if ([[NSNull null] isEqual: urlString])
                    self.imageUrlString = nil;
                else
                    self.imageUrlString = urlString;
            }
        }
    }
    if (rawEntity.properties) {
        [self willChangeValueForKey: @"properties"];
        self.propertiesJson = rawEntity.properties;
        [self setPrimitiveValue: nil forKey: @"properties"];
        [self didChangeValueForKey: @"properties"];
    }
    if (rawEntity.rootLevel) {
        BOOL rootLevel = [rawEntity.rootLevel length] > 0;
        self.rootLevel = [NSNumber numberWithBool: rootLevel];
    }
}

- (void)refreshCachedInformation
{
    [self willChangeValueForKey: @"childrenPermIds"];
    [self setPrimitiveValue: nil forKey: @"childrenPermIds"];
    [self didChangeValueForKey: @"childrenPermIds"];
    
    [self willChangeValueForKey: @"properties"];
    [self setPrimitiveValue: nil forKey: @"properties"];
    [self didChangeValueForKey: @"properties"];    
}

@end
