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

# MasterDataRegistrationTransaction Class
import os
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

##
## Vocabulary Types
##
createVocabularyWithTerms("ALL_LAB_MEMBERS", [
                                        ["ANNA_DEPLAZES",   "Anna Deplazes"],
                                        ["ANNE-CHRISTINE_BUTTY",   "Anne-Christine Butty"],
                                        ["BRIAN_LUKE",   "Brian Luke"],
                                        ["CAROLINE_ZBINDEN",   "Caroline Zbinden"],
                                        ["CELINE_LAFOURCADE",   "Celine Lafourcade"],
                                        ["CHRISTINE_RUPP",   "Christine Rupp"],
                                        ["CLAUDINE_KRAFT",   "Claudine Kraft"],
                                        ["EDYTA_SIERGIEJUK",   "Edyta Siergiejuk"],
                                        ["FABIAN_HERZOG",   "Fabian Herzog"],
                                        ["FABIAN_RUDOLF",   "Fabian Rudolf"],
                                        ["FRANK_VAN_DROGEN",   "Frank Van Drogen"],
                                        ["GERDA_JORRITSMA",   "Gerda Jorritsma"],
                                        ["GWEN_RABUT",   "Gwen Rabut"],
                                        ["INGRID_STOFFEL-STUDER",   "Ingrid Stoffel-Studer"],
                                        ["IRAM_ZAIDI",   "Iram Zaidi"],
                                        ["IZABELA_SUMARA",   "Izabela Sumara"],
                                        ["LIONEL_PINTARD",   "Lionel Pintard"],
                                        ["MALIKAJAQUENOUD",   "MalikaJaquenoud"],
                                        ["MARC_SOHRMANN",   "Marc Sohrmann"],
                                        ["MARIE-LYN_HECHT",   "Marie-Lyn Hecht"],
                                        ["MARIE-PIERRE_GULLI",   "Marie-Pierre Gulli"],
                                        ["MATTHIAS_PETER",   "Matthias Peter"],
                                        ["MICHAEL_OLMA",   "Michael Olma"],
                                        ["MICHAEL_SCHNEIDER",   "Michael Schneider"],
                                        ["MICHELE_KNAUS",   "Michele Knaus"],
                                        ["MONIKA_GERSBACH",   "Monika Gersbach"],
                                        ["NATHALIE_MEYER-SCHALLER",   "Nathalie Meyer-Schaller"],
                                        ["NATHALIE_PERRINJAQUET",   "Nathalie Perrinjaquet"],
                                        ["NICOLAS_DARD",   "Nicolas Dard"],
                                        ["NICOLAS_PAGE",   "Nicolas Page"],
                                        ["PATRICK_PEDRIOLI",   "Patrick Pedrioli"],
                                        ["PHILIPPE_WIGET",   "Philippe Wiget"],
                                        ["PIERRE_MAILLARD",   "Pierre Maillard"],
                                        ["REINHARD_DECHANT",   "Reinhard Dechant"],
                                        ["SANDER_SPRINGER",   "Sander Springer"],
                                        ["SARAH_GLASER",   "Sarah Glaser"],
                                        ["SARAH_MAERKI",   "Sarah Maerki"],
                                        ["SEBASTIAN_LEIDEL",   "Sebastian Leidel"],
                                        ["SERGE_PELET",   "Serge Pelet"],
                                        ["STEFAN_MUELLER",   "Stefan Müller"],
                                        ["TANIA_ROBERTS",   "Tania Roberts"],
                                        ["THIMO_KURZ",   "Thimo Kurz"],
                                        ["TOBIAS_BRAUN",   "Tobias Braun"],
                                        ["VANESSA_REZGUI",   "Vanessa Rezgui"],
                                        ["WOJCIECH_PIWKO",   "Wojciech Piwko"],
                                        ["YUKIKO_SHIMADA",   "Yukiko Shimada"],
                                        ["YVONNE_GLOOR",   "Yvonne Gloor"],
                                        ["JOCHEN_BECK",   "Jochen Beck"],
                                        ["MONIKA_KIJANSKA",   "Monika Kijanska"]

                                    ]);

createVocabularyWithTerms("BLOCK", [
                                ["1BSA_01TRITON-X100",  "1% BSA; 0.1% Triton X100"],                               
                                    ]);

createVocabularyWithTerms("CLASS", [
                                ["IGM", "IgM"],
                                ["IGG1",    "IgG1"],
                                ["IGG2",    "IgG2"]
                                    ]);


createVocabularyWithTerms("COMPANY", [
                                    ["ABCAM", "abcam"],
                                    ["CELL_SIGNALING", "cell signaling"],
                                    ["STRATAGEN", "stratagene"],
                                    ["CLONTECH", "clontech"],
                                    ["UBI", "ubi"],
                                    ["SOUTHERN_BIOTECH", "southern biotech"],
                                    ["PIERCE", "pierce"],
                                    ["PHARMIGEN", "pharmingen"],
                                    ["MOLECULAR_PROBES", "molecular probes"],
                                    ["PHARMACIA", "pharmacia"],
                                    ["BIORAD", "biorad"],
                                    ["EUROGENETIC", "eurogentec"],
                                    ["HOMEMADE", "homemade"],
                                    ["BD_BIOSCIENCE", "BD bioscience"],
                                    ["OBTAINED_FROM", "obtained from "],
                                    ["ZYMED", "Zymed"],
                                    ["UNKNOWN", "Unknown"],
                                    ["SANTA_CRUZ", "Santa Cruz"],
                                    ["BIOLEGEND", "Biolegend"],
                                    ["BETHYL", "Bethyl"],
                                    ["UPSTATE", "Upstate"],
                                    ["ROCKLAND", "Rockland"],
                                    ["NOVUS_BIOLOGICALS", "Novus Biologicals"],
                                    ["CHEMICON_INTERNATIONAL", "Chemicon International"],
                                    ["SIGMA", "Sigma"],
                                    ["ALPHA_DIAGNOSTIC_INTERNATIONAL", "Alpha Diagnostic International"],
                                    ["MILLIPORE", "Millipore"],
                                    ["ACTIVE_MOTIF", "Active motif"],
                                    ["NEB", "NEB"],
                                    ["NEOMARKERS", "Neomarkers"],
                                    ["LUBIOSCIENCE", "LubioScience"],
                                    ["EBIOSCIENCE", "eBioscience"],
                                    ["INVITROGEN", "Invitrogen"],
                                    ["STRESSGEN", "Stressgen"],
                                    ["COVANCE", "Covance"],
                                    ["ENZO_LIFE_SCIENCES", "Enzo Life Sciences"],
                                    ["CALBIOCHEM", "Calbiochem"],
                                    ["ROCHE", "Roche"],
                                    ["CYTOSKELETON", "Cytoskeleton"],
                                    ["OPEN_BIOSYSTEMS", "Open Biosystems"],
                                    ["BD_TRANSDUCTION", "BD Transduction"],
                                    ["ATLAS_ANTIBODIES_AB", "Atlas Antibodies AB"],
                                    ["GENENTECH", "Genentech"],
                                    ["TRANSDUCTION_LABS", "Transduction Labs"],
                                    ["ANTIBODIES_INCORPORATED", "Antibodies Incorporated"],
                                    ["BABCO", "BAbCO"],
                                    ["ERICH_NIGG_LAB_BASEL", "Erich Nigg lab (Basel)"],
                                    ["FRANCIS_BARR", "Francis Barr"],
                                    ["AVIVA_SYSTEMS_BIOLOGY", "Aviva Systems Biology"],
                                    ["LUCERNACHEM", "LucernaChem"],
                                    ["LI-COR", "LI-COR"],
                                    ["ABGENT", "Abgent"],
                                    ["CHROMOTEK", "Chromotek"],
                                    ["ABNOVA", "Abnova"],
                                    ["UNKNOWN", "Unknown"],
                                    ["ABSEA", "Absea"],
                                    ["GE_HEALTHCARE", "GE Healthcare"],
                                    ["GRAMSCH_LABORATORIES", "Gramsch Laboratories"],
                                    ["ADIPOGEN", "Adipogen"],
                                    ["COSMO_BIO_CO", "Cosmo bio co."],
                                    ["BECKTON_DICKINSON", "Beckton Dickinson"],
                                    ["BIOTIUM", "Biotium"]
                                    ]);

createVocabularyWithTerms("CONCENTRATION_FACS", [
                                            ["1_TO_10", "0:10"],
                                            ["1_TO_20", "0:20"],
                                            ["1_TO_30", "0:30"],
                                            ["1_TO_40", "0:40"],
                                            ["1_TO_50", "0:50"],
                                            ["1_TO_100", "0:100"],
                                            ["1_TO_200", "0:200"],
                                            ["1_TO_500", "0:500"],
                                            ["1_TO_1000", "0:1000"],
                                            ["1_TO_2000", "0:2000"],
                                            ["1_TO_3000", "0:3000"],
                                            ["NOT_TESTED", "not tested"],
                                            ["NOT_WORKING", "not working"]
                                    ]);


createVocabularyWithTerms("CONCENTRATION_IP", [
                                            ["1UL", "1 µl"],
                                            ["5UL", "5 µl"],
                                            ["10UL", "10 µl"],
                                            ["15UL", "15 µl"],
                                            ["20UL", "20 µl"],
                                            ["NOT_TESTED", "not tested"],
                                            ["NOT_WORKING", "not working"]
                                    ]);

createVocabularyWithTerms("CONCENTRATION_WESTERN", [
                                                ["1_TO_100", "0:100"],
                                                ["1_TO_500", "0:500"],
                                                ["1_TO_1000", "0:1000"],
                                                ["1_TO_2000", "0:2000"],
                                                ["1_TO_3000", "0:3000"],
                                                ["1_TO_5000", "0:5000"],
                                                ["1_TO_10000", "0:10000"],
                                                ["1_TO_20000", "0:20000"],
                                                ["1_TO_25000", "0:25000"],
                                                ["NOT_TESTED", "not tested"],
                                                ["NOT_WORKING", "not working"]
                                    ]);

createVocabularyWithTerms("CURRENT_LAB_MEMBERS", [
                                            ["CAROLINE_ZBINDEN", "Caroline Zbinden"],
                                            ["FRANK_VAN_DROGEN", "Frank Van Drogen"],
                                            ["INGRID_STOFFEL-STUDER", "Ingrid Stoffel-Studer"],
                                            ["MATTHIAS_PETER", "Matthias Peter"],
                                            ["REINHARD_DECHANT", "Reinhard Dechant"],
                                            ["WOJCIECH_PIWKO", "Wojciech Piwko"],
                                            ["JOCHEN_BECK", "Jochen Beck"]
                                    ]);

createVocabularyWithTerms("FIX", [
                                ["1H-RT", "1h, RT"],
                                ["PTEMF", "PTEMF"],
                                ["PFA", "PFA"],
                                ["MEOH", "MeOH"]
                                    ]);

createVocabularyWithTerms("LABEL", [
                                ["HRP", "hrp"],
                                ["AP", "ap"],
                                ["FITC", "fitc"],
                                ["CY3", "Cy3"],
                                ["ALEXA647", "Alexa 647"]
                                    ]);

createVocabularyWithTerms("LOCATION", [
                                    ["SMALL_PETER_LAB", "Small Peter Lab"],
                                    ["BIG_PETER_LAB", "Big Peter Lab"],
                                    ["NEW_PETER_LAB", "New Peter Lab"],
                                    ["MINUS20_C-FLOOR", "-20C C-Floor"]
                                    ]);

createVocabularyWithTerms("MONO-POLYCLONAL", [
                                            ["MONOCLONAL",  "Monoclonal"],
                                            ["POLYCLONAL",  "Polyclonal"],
                                            ["UNKNOWN", "Unknown"]
                                    ]);

createVocabularyWithTerms("SOURCE", [
                                ["MOUSE",  "mouse"],
                                ["RABBIT",  "rabbit"],
                                ["HAMSTER",  "hamster"],
                                ["GOAT",  "goat"],
                                ["RAT",  "rat"],
                                ["CHICKEN",  "chicken"],
                                ["SHEEP",  "sheep"],
                                ["HUMAN",  "human"],
                                ["GUINEA_PIG",  "guinea pig"],
                                ["DONEKY",  "donkey"],
                                ["UNKNOWN",  "unknown"]
                                    ]);

createVocabularyWithTerms("FREEZER", [
                                ["G9_FRIDGE-1",  "G9 Fridge 1"],
                                ["G9_FRIDGE-2",  "G9 Fridge 2"],
                                ["G9_FREEZER-A",  "G9 Freezer A"],
                                ["G9_FREEZER-B",  "G9 Freezer B"],
                                ["G9_FREEZER-C",  "G9 Freezer C"],
                                ["G10_FREEZER-D",  "G10 Freezer D"],
                                ["G10_FREEZER-E",  "G10 Freezer E"],
                                ["G10_FREEZER-F",  "G10 Freezer F"],
                                ["G10_FREEZER-G",  "G10 Freezer G"],
                                ["G10_FREEZER-H",  "G10 Freezer H"],
                                ["G10_FREEZER-I",  "G10 Freezer I"],
                                ["G10_FRIDGE-3",  "G10 Fridge 3"],
                                ["G10_FRIDGE-4",  "G10 Fridge 4"],
                                ["G10_FRIDGE-5",  "G10 Fridge 5"],
                                ["G10_FRIDGE-6",  "G10 Fridge 6"],
                                ["G10_FRIDGE-7",  "G10 Fridge 7"],
                                ["G11_FRIDGE-8",  "G10 Fridge 8"],
                                ["G14_FREEZER-J",  "G14 Freezer J"],
                                ["G14_FREEZER-K",  "G14 Freezer K"],
                                ["G14_FREEZER-L",  "G14 Freezer L"],
                                ["G14_FREEZER-M",  "G14 Freezer M"],
                                ["G14_FRIDGE-9",  "G14 Fridge 9"],
                                ["G14_FRIDGE-10",  "G14 Fridge 10"],
                                ["G17_FRIDGE-11",  "G17 Fridge 11"],
                                ["G17_FRIDGE-12",  "G17 Fridge 12"],
                                ["G17_FREEZER-N",  "G17 Freezer N"],
                                ["USER_BENCH-20",  "Bench -20ºC"],
                                ["USER_BENCH-80",  "Bench -80ºC"],
                                ["USER_BENCH-RT",  "Bench RT"],
                                ["USER_BENCH",  "Bench"]

                                    ]);

##
## Experiment Types
##
createExperimentTypeWithProperties("ANTIBODY", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);

createExperimentTypeWithProperties("DEFAULT_EXPERIMENT", "Default Experiment", [
    ["NAME",                 "General", "Name",                     DataType.VARCHAR,             None,    "Name", None],
    ["EXPERIMENTAL_GOALS",     "General", "Experimental goals",     DataType.MULTILINE_VARCHAR, None,    "Goal of the experiment", None],
    ["GRANT",                 "General", "Grant",                 DataType.VARCHAR,            None,    "grant name", None],
    ["START_DATE",             "General", "Start Date",             DataType.TIMESTAMP,         None,    "Start Date", None],
    ["END_DATE",             "General", "End Date",                 DataType.TIMESTAMP,            None,    "End Date", None],
    ["EXPERIMENTAL_RESULTS","General", "Experimental results",     DataType.MULTILINE_VARCHAR, None,    "Brief summary of the results obtained", None],
    ["XMLCOMMENTS",            "Comments","Comments List",            DataType.XML,                None,    "Several comments can be added by different users", commentsScriptName]
]);

##
## Sample Types
##
createSampleTypeWithProperties("ANTIBODY", "", [
    ["NAME",                           "General",                "antibody ID",                        DataType.VARCHAR,                    None,  "", None],
    ["ANTIBODY_ID_NR",                 "General",                "antibody_id_nr",                     DataType.VARCHAR,                    None,  "", None],
    ["ANTIGEN",                        "General",                "antigen",                            DataType.VARCHAR,                    None,  "", None],
    ["BARCODE",                        "General",                "barcode",                            DataType.VARCHAR,                    None,  "", None],
    ["BARCODE_LABEL",                  "General",                "barcode label",                      DataType.VARCHAR,                    None,  "", None],
    ["ANTIBODY_FACS_BLOCK",            "General",                "block facs",                         DataType.CONTROLLEDVOCABULARY,       "BLOCK",  "", None],
    ["ANTIBODY_IF_BLOCK",              "General",                "block IFF",                          DataType.CONTROLLEDVOCABULARY,       "BLOCK",  "", None],
    ["ANTIBODY_WB_BLOCK",              "General",                "block western",                      DataType.CONTROLLEDVOCABULARY,       "BLOCK",  "", None],
    ["BOX",                            "General",                "box",                                DataType.VARCHAR,                    None,  "", None],
    ["CATALOGUE_NUMBER",               "General",                "catalog id #",                       DataType.VARCHAR,                    None,  "", None],
    ["CLASS",                          "General",                "class",                              DataType.CONTROLLEDVOCABULARY,       "CLASS",  "", None],
    ["CLONE",                          "General",                "clone",                              DataType.VARCHAR,                    None,  "", None],
    ["COMPANY",                        "General",                "company",                            DataType.CONTROLLEDVOCABULARY,       "COMPANY",  "", None],
    ["ANTIBODY_FACS_CONC",             "General",                "conc facs",                          DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_FACS",  "", None],
    ["ANTIBODY_IF_CONC",               "General",                "conc IFF",                           DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_FACS",  "", None],
    ["ANTIBODY_IP_CONC",               "General",                "conc ip",                            DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_IP",  "", None],
    ["ANTIBODY_WB_CONC",               "General",                "conc western",                       DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_WESTERN",  "", None],
    ["ANTIBODY_CONCENTRATION",         "General",                "concentration",                      DataType.VARCHAR,                    None,  "", None],
    ["ANTIBODY_CROSSREACTIVITY",       "General",                "crossreactivity",                    DataType.VARCHAR,                    None,  "", None],
    ["ANTIBODY_FACS_FIX",              "General",                "fix facs",                           DataType.CONTROLLEDVOCABULARY,       "FIX",  "", None],
    ["ANTIBODY_IF_FIX",                "General",                "fix IFF",                            DataType.CONTROLLEDVOCABULARY,       "FIX",  "", None],
    ["ANTIBODY_WB_FIX",                "General",                "fix western",                        DataType.CONTROLLEDVOCABULARY,       "FIX",  "", None],
    ["COMMENTS",                       "General",                "info",                               DataType.VARCHAR,                    None,  "", None],
    ["INVESTIGATOR",                   "General",                "investigator",                       DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None],
    ["LABEL",                          "General",                "label",                              DataType.CONTROLLEDVOCABULARY,       "LABEL",  "", None],
    ["LOTNUMBER",                      "General",                "lotnumber",                          DataType.VARCHAR,                    None,  "", None],
    ["MODIFIED_BY",                    "General",                "modified by",                        DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None],
    ["MONO_POLYCLONAL",                "General",                "mono/polyclonal",                    DataType.CONTROLLEDVOCABULARY,       "MONO-POLYCLONAL",  "", None],
    ["ANTIBODY_FACS_NOTES",            "General",                "notes FACS",                         DataType.VARCHAR,                    None,  "", None],
    ["ANTIBODY_IF_NOTES",              "General",                "notes IFF",                          DataType.VARCHAR,                    None,  "", None],
    ["ANTIBODY_IP_NOTES",              "General",                "notes IP",                           DataType.VARCHAR,                    None,  "", None],
    ["ANTIBODY_WB_NOTES",              "General",                "notes western",                      DataType.VARCHAR,                    None,  "", None],
    ["RACK",                           "General",                "rack",                               DataType.VARCHAR,                    None,  "", None],
    ["PUBLISHED_IN",                   "General",                "reference",                          DataType.VARCHAR,                    None,  "", None],
    ["SERIAL_NUMBER",                  "General",                "serial number",                      DataType.INTEGER,                    None,  "", None],
    ["ANTIGEN_SIZE",                   "General",                "size",                               DataType.VARCHAR,                    None,  "", None],
    ["SOURCE",                         "General",                "source",                             DataType.CONTROLLEDVOCABULARY,       "SOURCE",  "", None],
    ["CREATION_DATE",                  "General",                "creation date",                      DataType.TIMESTAMP,                  None,  "", None],
    ["MODIFICATION_DATE",              "General",                "modification date",                  DataType.TIMESTAMP,                  None,  "", None],
    ["FROZEN",                         "General",                "frozen",                             DataType.TIMESTAMP,                  None,  "", None],
    ["BOX_NUMBER",                     "General",                "antibody ID data box::box label",    DataType.VARCHAR,                    None,  "", None],
    ["FROZEN_BY",                      "General",                "antibody ID data box::frozen by",    DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None],
    ["FREEZER_NAME",                   "General",                "antibody ID data box::location",     DataType.CONTROLLEDVOCABULARY,       "LOCATION",  "", None],
    ["BOX_POSITION",                   "General",                "antibody ID data box::position",     DataType.VARCHAR,                    None,  "", None]
]);