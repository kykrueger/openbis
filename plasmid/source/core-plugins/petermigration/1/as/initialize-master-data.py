#
# Copyright 2014 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##
## Configuration
##
PATH_TO_MANAGE_PROPERTIES_SCRIPTS = "/Users/juanf/Documents/workspace/openbis/source/core-plugins/petermigration/1/compatibility/";
#PATH_TO_MANAGE_PROPERTIES_SCRIPTS = "/Users/barillac/openbis-peter/servers/core-plugins/petermigration/1/compatibility/";
#PATH_TO_MANAGE_PROPERTIES_SCRIPTS = None;

# MasterDataRegistrationTransaction Class
import definitions
import os
import copy
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

##
## Globals
##
vocabulariesCache = {};
propertiesCache = {};
samplesCache = {};
tr = service.transaction()

##
## API Facade
##
def createVocabularyWithTerms(vocabularyCode, terms):
    vocabulary = tr.createNewVocabulary(vocabularyCode);
    vocabulary.setChosenFromList(True);
    addTerms(vocabulary, terms);
    vocabulariesCache[vocabularyCode] = vocabulary;
    
def addTerms(vocabulary, terms):
    for term in terms:
        addTermWithLabel(vocabulary, term[0], term[1])
    
def addTermWithLabel(vocabulary, termCode, termLabel):
    newTerm = tr.createNewVocabularyTerm(termCode);
    newTerm.setLabel(termLabel);
    vocabulary.addTerm(newTerm);
    
def createSampleTypeWithProperties(sampleTypeCode, description, properties):
    newSampleType = tr.getOrCreateNewSampleType(sampleTypeCode);
    newSampleType.setDescription(description);
    newSampleType.setShowParents(True);
    newSampleType.setAutoGeneratedCode(True);
    newSampleType.setGeneratedCodePrefix(sampleTypeCode[:3]);
    addProperties(newSampleType, properties);
    samplesCache[sampleTypeCode] = newSampleType;
    
def createDataSetTypeWithProperties(dataSetCode, kind, description, properties):
    newDataSet = tr.getOrCreateNewDataSetType(dataSetCode);
    newDataSet.setDataSetKind(kind);
    newDataSet.setDescription(description);
    addProperties(newDataSet, properties);
    
def createExperimentTypeWithProperties(experimentTypeCode, description, properties):
    newExperiment = tr.getOrCreateNewExperimentType(experimentTypeCode);
    newExperiment.setDescription(description);
    addProperties(newExperiment, properties);

def addPropertiesToSamples(sampleTypeCodes, properties):
    for sampleTypeCode in sampleTypeCodes:
        sampleType = samplesCache[sampleTypeCode];
        addProperties(sampleType, properties);
    
def addProperties(entity, properties):
    for property in properties:
        addProperty(entity, property[0], property[1], property[2], property[3], property[4], property[5], property[6]);
    
def addProperty(entity, propertyCode, section, propertyLabel, dataType, vocabularyCode, propertyDescription, managedScript):
    property = None;
    
    if propertyCode in propertiesCache:
        property = propertiesCache[propertyCode];
    else:
        property = createProperty(propertyCode, dataType, propertyLabel, propertyDescription, vocabularyCode);
    
    propertyAssignment = tr.assignPropertyType(entity, property);
    propertyAssignment.setSection(section);
    if managedScript != None:
        propertyAssignment.setManaged(True);
        propertyAssignment.setShownEdit(True);
        propertyAssignment.setScriptName(managedScript);

def createProperty(propertyCode, dataType, propertyLabel, propertyDescription, vocabularyCode):
    property = tr.getOrCreateNewPropertyType(propertyCode, dataType);
    property.setDescription(propertyDescription);
    property.setLabel(propertyLabel);
    propertiesCache[propertyCode] = property;
    if dataType == DataType.CONTROLLEDVOCABULARY:
        property.setVocabulary(vocabulariesCache[vocabularyCode]);
    return property;

def addStorageGroups(numGroups, sampleType):
    storageGroup = copy.copy(definitions.storageGroupDefinition);
    
    for storageIdx in range(1,(numGroups + 1)):
        for property in storageGroup:
            property[0] = property[0].replace(str(storageIdx-1), str(storageIdx));
            property[1] = property[1].replace(str(storageIdx-1), str(storageIdx));
        addPropertiesToSamples([sampleType], storageGroup);
##
## Manage properties scripts
##
commentsScriptName = None;

if PATH_TO_MANAGE_PROPERTIES_SCRIPTS != None:
    commentsScriptName = "COMMENTS";
    commentsScriptAsString = open(PATH_TO_MANAGE_PROPERTIES_SCRIPTS + "comments.py", 'r').read();
    commentsScript = tr.getOrCreateNewScript(commentsScriptName);
    commentsScript.setName(commentsScriptName);
    commentsScript.setDescription("Comments Handler");
    commentsScript.setScript(commentsScriptAsString);
    commentsScript.setScriptType("MANAGED_PROPERTY");
    commentsScript.setEntityForScript("SAMPLE");

##
## Vocabulary Types
##
for vocabularyCode, vocabularyValues in definitions.vocacbularyDefinitions.iteritems():
    createVocabularyWithTerms(vocabularyCode, vocabularyValues)

##
## Experiment Types
##
createExperimentTypeWithProperties("ANTIBODY", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);

createExperimentTypeWithProperties("DEFAULT_EXPERIMENT", "Default Experiment", [
    ["NAME",                   "General", "Name",                     DataType.VARCHAR,             None,    "Name", None],
    ["EXPERIMENTAL_GOALS",     "General", "Experimental goals",       DataType.MULTILINE_VARCHAR,   None,    "Goal of the experiment", None],
    ["GRANT",                  "General", "Grant",                    DataType.VARCHAR,             None,    "grant name", None],
    ["START_DATE",             "General", "Start Date",               DataType.TIMESTAMP,           None,    "Start Date", None],
    ["END_DATE",               "General", "End Date",                 DataType.TIMESTAMP,           None,    "End Date", None],
    ["EXPERIMENTAL_RESULTS",   "General", "Experimental results",     DataType.MULTILINE_VARCHAR,   None,    "Brief summary of the results obtained", None],
    ["XMLCOMMENTS",            "Comments","Comments List",            DataType.XML,                 None,    "Several comments can be added by different users", commentsScriptName]
]);

##
## Sample Types
##
createSampleTypeWithProperties("ANTIBODY", "", definitions.antibodyDefinition);

addStorageGroups(definitions.numberOfStorageGroups, "ANTIBODY");
