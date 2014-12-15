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

# MasterDataRegistrationTransaction Class
import definitions
import definitionsVoc
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
        addProperty(entity, property[0], property[1], property[2], property[3], property[4], property[5], property[6], property[7], property[8]);
    
def addProperty(entity, propertyCode, section, propertyLabel, dataType, vocabularyCode, propertyDescription, managedScript, dynamicScript, isMandatory):
    property = None;
    
    if propertyCode in propertiesCache:
        property = propertiesCache[propertyCode];
    else:
        property = createProperty(propertyCode, dataType, propertyLabel, propertyDescription, vocabularyCode);
    
    propertyAssignment = tr.assignPropertyType(entity, property);
    propertyAssignment.setSection(section);
    propertyAssignment.setMandatory(isMandatory);
    if managedScript != None:
        propertyAssignment.setManaged(True);
        propertyAssignment.setShownEdit(True);
        propertyAssignment.setScriptName(managedScript);
    if dynamicScript != None:
        propertyAssignment.setDynamic(True);
        propertyAssignment.setShownEdit(True);
        propertyAssignment.setScriptName(dynamicScript);

def createProperty(propertyCode, dataType, propertyLabel, propertyDescription, vocabularyCode):
    property = tr.getOrCreateNewPropertyType(propertyCode, dataType);
    property.setDescription(propertyDescription);
    property.setLabel(propertyLabel);
    propertiesCache[propertyCode] = property;
    if dataType == DataType.CONTROLLEDVOCABULARY:
        property.setVocabulary(vocabulariesCache[vocabularyCode]);
    return property;

def addStorageGroups(numGroups, sampleType):
    for storageIdx in range(1,(numGroups + 1)):
        storageGroup = definitions.getStorageGroupDefinition();
        for property in storageGroup:
            property[0] = property[0] + "_" + str(storageIdx);
            property[1] = property[1] + "_" + str(storageIdx);
            property[5] = property[5] + "_" + str(storageIdx);
        addPropertiesToSamples([sampleType], storageGroup);

#Valid Script Types: DYNAMIC_PROPERTY, MANAGED_PROPERTY, ENTITY_VALIDATION 
def createScript(path, name, description, scriptType, entityType):
    scriptAsString = open(path, 'r').read();
    script = tr.getOrCreateNewScript(name);
    script.setName(name);
    script.setDescription(description);
    script.setScript(scriptAsString);
    script.setScriptType(scriptType);
    script.setEntityForScript(entityType);
    return script;
    
##
## Managed properties scripts
##
commentsScript = createScript(PATH_TO_MANAGE_PROPERTIES_SCRIPTS + "comments.py",
                                  definitions.commentsScriptName,
                                  "Comments Handler",
                                  "MANAGED_PROPERTY",
                                  "SAMPLE");

##
## Dynamic properties scripts
##
adenosineScript = createScript(PATH_TO_MANAGE_PROPERTIES_SCRIPTS + "adenosine_count.py",
                                  definitions.adenosineScriptName,
                                  "Count number of adenosine in sequence",
                                  "DYNAMIC_PROPERTY",
                                  "SAMPLE");

cytosineScript = createScript(PATH_TO_MANAGE_PROPERTIES_SCRIPTS + "cytosine_count.py",
                                  definitions.cytosineScriptName,
                                  "Count number of cytosine in sequence",
                                  "DYNAMIC_PROPERTY",
                                  "SAMPLE");

gcScript = createScript(PATH_TO_MANAGE_PROPERTIES_SCRIPTS + "gc.py",
                                  definitions.gcScriptName,
                                  "(guanosine+cytosine)*100/length",
                                  "DYNAMIC_PROPERTY",
                                  "SAMPLE");

guanosineScript = createScript(PATH_TO_MANAGE_PROPERTIES_SCRIPTS + "guanosine_count.py",
                                  definitions.guanosineScriptName,
                                  "Count number of guanosine in sequence",
                                  "DYNAMIC_PROPERTY",
                                  "SAMPLE");

lengthScript = createScript(PATH_TO_MANAGE_PROPERTIES_SCRIPTS + "length.py",
                                  definitions.lengthScriptName,
                                  "Count total length of sequence (A+C+T+G+O_nucleotide)",
                                  "DYNAMIC_PROPERTY",
                                  "SAMPLE");

nucelotideScript = createScript(PATH_TO_MANAGE_PROPERTIES_SCRIPTS + "nucleotide_count.py",
                                  definitions.nucelotideScriptName,
                                  "Count number of r,y,m,k,s,w,h,b,d,x,N in sequence",
                                  "DYNAMIC_PROPERTY",
                                  "SAMPLE");

thymidineScript = createScript(PATH_TO_MANAGE_PROPERTIES_SCRIPTS + "thymidine_count.py",
                                  definitions.thymidineScriptName,
                                  "Count number of thymidine in sequence",
                                  "DYNAMIC_PROPERTY",
                                  "SAMPLE");

tmScript = createScript(PATH_TO_MANAGE_PROPERTIES_SCRIPTS + "tm.py",
                                  definitions.tmScriptName,
                                  "68.3 + (0.41 * gc) - (600/length)",
                                  "DYNAMIC_PROPERTY",
                                  "SAMPLE");

##
## Vocabulary Types
##
for vocabularyCode, vocabularyValues in definitionsVoc.vocacbularyDefinitions.iteritems():
    createVocabularyWithTerms(vocabularyCode, vocabularyValues)

##
## Experiment Types
##
createExperimentTypeWithProperties("DEFAULT_EXPERIMENT", "Default Experiment", definitions.experimentDefinition);
createExperimentTypeWithProperties("ANTIBODY", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("CELL", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("STRAIN", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("PLASMID", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("CHEMICAL", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("SIRNA", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("OLIGO", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
##
## Sample Types
##
createSampleTypeWithProperties("ANTIBODY", "", definitions.antibodyDefinition);
addStorageGroups(definitions.numberOfStorageGroups, "ANTIBODY");
createSampleTypeWithProperties("CELL", "", definitions.cellDefinition);
addStorageGroups(definitions.numberOfStorageGroups, "CELL");
createSampleTypeWithProperties("STRAIN", "", definitions.strainDefinition);
createSampleTypeWithProperties("PLASMID", "", definitions.plasmidDefinition);
createSampleTypeWithProperties("CHEMICAL", "", definitions.chemicalDefinition);
createSampleTypeWithProperties("SIRNA", "", definitions.siRNADefinition);
createSampleTypeWithProperties("OLIGO", "", definitions.oligoDefinition);

##
## Data set Types
##
createDataSetTypeWithProperties("DOCUMENT", "PHYSICAL", "", definitions.documentDefinition);
