'''
@copyright:
Copyright 2013 ETH Zuerich, CISD
 
@license:
Licensed under the Apache License, Version 2.0 (the 'License');
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an 'AS IS' BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author:
Manuel Kohler

@description:
Creates the SampleSheet.csv out of values from openBIS for Demultiplexing 
used in the Illumina pipeline (configureBclToFastq.pl) 

@attention:
Runs under Jython

@note:
Takes into account to replace special characters with an underscore so that the Illumina script
does not fail

HiSeq Header Description
========================
Column Header  Description
FCID  Flow cell ID
Lane  Positive integer, indicating the lane number (1-8)
SampleID  ID of the sample
SampleRef  The reference used for alignment for the sample
Index  Index sequences. Multiple index reads are separated by a hyphen (for example, ACCAGTAA-GGACATGA).
Description  Description of the sample
Control  Y indicates this lane is a control lane, N means sample
Recipe Recipe used during sequencing
Operator Name or ID of the operator
SampleProject  The project the sample belongs to
'''

from __future__ import with_statement
import os
import logging
import re
import sys
import string
import smtplib
import operator
from ConfigParser import SafeConfigParser
from optparse import OptionParser
from datetime import *

from ch.systemsx.cisd.openbis.dss.client.api.v1 import OpenbisServiceFacadeFactory
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from java.util import EnumSet
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SampleFetchOption

lineending = {'win32':'\r\n', 'linux':'\n', 'mac':'\r'}
COMMA = ','
SAMPLETYPE = 'SAMPLETYPE'

def getLaneAncestors(service, lane, logger):
  '''
  Gets all ancestors and corresponding properties of a sample
  '''
  fetchOptions = EnumSet.of(SampleFetchOption.ANCESTORS, SampleFetchOption.PROPERTIES)
  sc = SearchCriteria();
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, lane));
  dList = service.searchForSamples(sc, fetchOptions)

  def recursiveSamples(list, codeList, propertyDict):
    '''
    Recursively walks through the sample tree and write the sample codes
    to a list and the properties into a dictionary. The sample code is
    the key, the properties are the values
    '''
       
    for element in list:
      elementCode = element.getCode()
      codeList.append(elementCode)
      elementProperties = element.getProperties()
      
      tmpDict = {}
      
      for eP in elementProperties:
        tmpDict[eP] = elementProperties[eP]
        
      tmpDict[SAMPLETYPE] = element.getSampleTypeCode()
      propertyDict[elementCode] = tmpDict

      elementParents = element.getParents()
      
      if (elementParents.size() > 0):
        recursiveSamples(elementParents, codeList, propertyDict)
 
  codeList = []
  propertyDict = {}
    
  recursiveSamples(dList, codeList, propertyDict)
  
  logger.info(codeList)
  logger.info(propertyDict)
  
  return codeList, propertyDict


def login(configDict, logger):
  logger.info('Logging into ' + configDict['openbisServer'])
  try:
    service = OpenbisServiceFacadeFactory.tryCreate(configDict['openbisUserName'],
                                                  configDict['openbisPassword'],
                                                  configDict['openbisServer'],
                                                  configDict['connectionTimeout'])
  except BaseException:
    print ('Could not connect to ' + configDict['openbisServer'] + '. Please check if the server ' +
    'address is OK, the firewall is not blocking the communication, or openBIS is down.')

  return service

def logout (service, logger):
  service.logout()
  logger.info('Logged out')

def setUpLogger(logPath, logLevel=logging.INFO):
  logFileName = 'createSampleSheet'
  d = datetime.now()
  logFileName = logFileName + '_' + d.strftime('%Y-%m-%d_%H_%M_%S') + '.log'
  logging.basicConfig(filename=logPath + logFileName,
                      format='%(asctime)s [%(levelname)s] %(message)s', level=logLevel)
  logger = logging.getLogger(logFileName)
  return logger

def parseOptions(logger):
  logger.info('Parsing command line parameters')
  parser = OptionParser(version='%prog 1.0')
  parser.add_option('-f', '--flowcell',
                  dest='flowcell',
                  help='The flowcell which is used to create the SampleSheet.csv',
                  metavar='<flowcell>')
  parser.add_option('-l', '--lineending',
                  dest='lineending',
                  type='choice',
                  action='store',
                  choices=['win32', 'linux', 'mac'],
                  default='linux',
                  help='Specify end of line separator: win32, linux, mac. Default: linux' ,
                  metavar='<lineending>')
  parser.add_option('-o', '--outdir',
                  dest='outdir',
                  default='./',
                  help='Specify the ouput directory. Default: ./' ,
                  metavar='<outdir>')
  parser.add_option('-s', '--singlelane',
                  dest='singlelane',
                  default=False,
                  action='store_true',
                  help='Creates a single Sample Sheet for each lane. Default: False')
  parser.add_option('-d', '--debug',
                  dest='debug',
                  default=False,
                  action='store_true',
                  help='Verbose debug logging. Default: False')
  parser.add_option('-v', '--verbose',
                  dest='verbose',
                  default=False,
                  action='store_true',
                  help='Write Sample Sheet to stout. Default: False')

  (options, args) = parser.parse_args()

  if options.outdir[-1] <> '/':
    options.outdir = options.outdir + '/'

  if options.flowcell is None:
    parser.print_help()
    exit(-1)
  return options

def parseConfigurationFile(logger, propertyFile='etc/createSampleSheet_Illumina_NGS.properties'):
  '''
  Parses the given config files and returns the values
  '''
  logger.info('Reading config file ' + propertyFile)
  config = SafeConfigParser()
  config.read(propertyFile)
  config.sections()
  return config

def readConfig(logger):

  GENERAL = 'GENERAL'
  OPENBIS = 'OPENBIS'
  ILLUMINA = 'ILLUMINA'

  configDict = {}

  configParameters = parseConfigurationFile(logger)

  configDict['facilityName'] = configParameters.get(GENERAL, 'facilityName')
  configDict['facilityNameShort'] = configParameters.get(GENERAL, 'facilityNameShort')
  configDict['facilityInstitution'] = configParameters.get(GENERAL, 'facilityInstitution')
  configDict['sampleSheetFileName'] = configParameters.get(GENERAL, 'sampleSheetFileName')
  configDict['separator'] = configParameters.get(GENERAL, 'separator')
  configDict['indexSeparator'] = configParameters.get(GENERAL, 'indexSeparator')

  configDict['openbisUserName'] = configParameters.get(OPENBIS, 'openbisUserName')
  configDict['openbisPassword'] = configParameters.get(OPENBIS, 'openbisPassword', raw=True)
  configDict['openbisServer'] = configParameters.get(OPENBIS, 'openbisServer')
  configDict['connectionTimeout'] = configParameters.getint(OPENBIS, 'connectionTimeout')
  configDict['illuminaFlowCellTypeName'] = configParameters.get(OPENBIS, 'illuminaFlowCellTypeName')

  configDict['illuminaFlowLaneTypeName'] = configParameters.get(OPENBIS, 'illuminaFlowLaneTypeName')
  configDict['libraryPoolTypeName'] = configParameters.get(OPENBIS, 'libraryPoolTypeName')
  configDict['libraryTypeName'] = configParameters.get(OPENBIS, 'libraryTypeName')
  configDict['rawTypeName'] = configParameters.get(OPENBIS, 'rawTypeName')
  configDict['masterTypeName'] = configParameters.get(OPENBIS, 'masterTypeName')
  configDict['index1Name'] = configParameters.get(OPENBIS, 'index1Name')
  configDict['index2Name'] = configParameters.get(OPENBIS, 'index2Name')
  configDict['operator'] = configParameters.get(OPENBIS, 'operator')
  configDict['endType'] = configParameters.get(OPENBIS, 'endType')
  configDict['lengthIndex1'] = configParameters.get(OPENBIS, 'lengthIndex1')
  configDict['lengthIndex2'] = configParameters.get(OPENBIS, 'lengthIndex2')
  
  configDict['endType'] = configParameters.get(OPENBIS, 'endType')
  configDict['cycles'] = configParameters.get(OPENBIS, 'cycles')
  configDict['runFolderName'] = configParameters.get(OPENBIS, 'runFolderName')
  configDict['ncbiOrganismTaxonomy'] = configParameters.get(OPENBIS, 'ncbiOrganismTaxonomy')
  configDict['externalSampleName'] = configParameters.get(OPENBIS, 'externalSampleName')
  
  configDict['phixNcbiCode'] = configParameters.getint(OPENBIS, 'phixNcbiCode')

  configDict['hiSeqNames'] = configParameters.get(ILLUMINA, 'hiSeqNames')
  configDict['hiSeqHeader'] = configParameters.get(ILLUMINA, 'hiSeqHeader')

  return configDict

def sanitizeString(myString):
  return re.sub('[^A-Za-z0-9]+', '_', myString)

def getVocabulary(vocabularyCode):
  ''' Returns the vocabulary terms and vocabulary labels of a vocabulary in a dictionary
      specified by the parameter vocabularyCode
      '''
  terms = []
  vocabularies = service.listVocabularies()
  vocabularyDict = {}
  for vocabulary in vocabularies:
    if (vocabulary.getCode() == vocabularyCode):
      terms = vocabulary.getTerms()
  if terms:
    for term in terms:
      vocabularyDict[term.getCode()] = term.getLabel()
  else:
    print ('No vocabulary found for ' + vocabularyCode)
  return vocabularyDict

def getFlowCell (illuminaFlowCellTypeName, flowCellName, service, logger):
  '''
  Getting the the matching FlowCell
  '''
  sc = SearchCriteria();
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, illuminaFlowCellTypeName));
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowCellName));
  foundSample = service.searchForSamples(sc)
  try:
    assert foundSample.size() == 1
  except AssertionError:
    print (str(foundSample.size()) + ' flow cells found which match.')
    exit(1)

  logger.info('Found ' + foundSample[0].getCode() + ' in openBIS')
  # Search for contained samples
  sampleSc = SearchCriteria()
  sampleSc.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(sc))
  foundContainedSamples = service.searchForSamples(sampleSc)
  
  return foundSample[0], foundContainedSamples


def getParents(sampleName, service):
  '''
  Returns a list of parents of a sample 
  '''
  sc = SearchCriteria();
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleName));
  foundSample = service.searchForSamples(sc)

  # set the criteria for getting the parents when providing the child name
  sampleSc = SearchCriteria()
  sampleSc.addSubCriteria(SearchSubCriteria.createSampleChildCriteria(sc))
  foundParentSamples = service.searchForSamples(sampleSc)

  return foundParentSamples

def getContainedSampleProperties(logger, containedSamples, service):
  '''
  Takes a java.util.ArrayList of contained samples, retrieves the parents and their properties and returns it
  as a dictionary. The key is the sample name, the value is a dictionary of the properties
  '''
  laneParentDict = {}

  for lane in containedSamples:
    parents = getParents (lane.getCode(), service)

    for parent in parents:
      parentCode = parent.getCode()
      parentProperties = parent.getProperties()

      propertyDict = {}
      for property in parentProperties:
        propertyDict[property] = parentProperties.get(property)

      propertyDict['LANE'] = lane.getCode()
      propertyDict['SAMPLE_TYPE'] = parent.getSampleTypeCode()
      myKey = sanitizeString(lane.getCode() + '_' + parentCode)
      laneParentDict[myKey] = propertyDict

  logger.info('Found ' + str(len(laneParentDict)) + ' samples on the flow cell.')
  return laneParentDict


def convertSampleToDict(foundFlowCell, configDict):
  '''
  converts <type 'ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample'> to a python dict
  '''
  flowCellDict = {}
  fcProperties = foundFlowCell.getProperties()
  for property in fcProperties:
    flowCellDict[property] = fcProperties.get(property)
  flowCellDict['Name'] = foundFlowCell.getIdentifier().split('/')[-1]
  flowCellDict['CODE'] = foundFlowCell.getCode()
  return flowCellDict

def writeSampleSheet(configDict, flowCellName, sortedSampleSheetList, myoptions, logger, fileName):
  '''
  Write the given list of class sampleSheetLine to a csv file
  '''
  newline = lineending[myoptions.lineending]
  myFile = fileName + '_' + flowCellName + '.csv'
  try:
    with open(myFile, 'w') as sampleSheetFile:
      sampleSheetFile.write(configDict['hiSeqHeader'] + newline)
      for laneElement in sortedSampleSheetList:
        for indexElement in laneElement:
          if myoptions.verbose:
            print indexElement
          sampleSheetFile.write(indexElement.__str__() + newline)
    logger.info('Writing file ' + myFile)

  except IOError:
    logger.error('File error: ' + str(err))
    print ('File error: ' + str(err))


def writeSampleSheetPerLane(configDict, flowCellName, sortedSampleSheetList, myoptions, logger, fileName):
  '''
  Write the given list of class sampleSheetLine to multiple csv files
  '''
  newline = lineending[myoptions.lineending]

  for laneElement in sortedSampleSheetList:
    lane = sortedSampleSheetList.index(laneElement) + 1
    myFile = fileName + '_' + flowCellName + '_' + str(lane) +'.csv'
    try:
      with open(myFile, 'w') as sampleSheetFile:
        sampleSheetFile.write(configDict['hiSeqHeader'] + newline)
        for indexElement in laneElement:
          if myoptions.verbose:
            print indexElement
          sampleSheetFile.write(indexElement.__str__() + newline)
    
      logger.info('Writing file ' + myFile)

    except IOError:
      logger.error('File error: ' + str(err))
      print ('File error: ' + str(err))



class sampleSheetLine:
  '''
  Represents one line in a Illumina Sample Sheet
  '''
  
  def __init__(self, fcId='', lane=0, sampleId='', sampleRef='', index='', description='',
                 control='', recipe='', operator='', sampleProject=''):
    self.fcId = fcId
    self.lane = lane
    self.sampleId = sampleId
    self.sampleRef = sampleRef
    self.index = index
    self.description = description
    self.control = control
    self.recipe = recipe
    self.operator = operator
    self.sampleProject = sampleProject

  def __repr__(self):
    return self.__str__()
      
  def __str__(self):
    return str(self.fcId) + COMMA + str(self.lane) + COMMA + str(self.sampleId) + COMMA +  \
      str(self.sampleRef) + COMMA + str(self.index) + COMMA + str(self.description) + COMMA + \
      str(self.control) + COMMA + str(self.recipe) + COMMA + str(self.operator) + COMMA + \
      str(self.sampleProject)

def main():
  '''
  Main script
  '''
  demuxPenalty = -1
  print datetime.now()

  logger = setUpLogger('log/')
  logger.info('Started Creation of Sample Sheet...')

  myoptions = parseOptions(logger)
  if myoptions.debug:
    logger.setLevel(logging.DEBUG)

  flowCellName = myoptions.flowcell
  configDict = readConfig(logger)
  service = login(configDict, logger)

  foundFlowCell, containedSamples = getFlowCell(configDict['illuminaFlowCellTypeName'], flowCellName, service, logger)
  flowCellName = foundFlowCell.getCode()
  flowCellDict = convertSampleToDict(foundFlowCell, configDict)
  
  index1ReadLength = int(flowCellDict[configDict['lengthIndex1']]) + demuxPenalty
  indexRead2Length = int(flowCellDict[configDict['lengthIndex2']]) + demuxPenalty

  laneParentDict = getContainedSampleProperties(logger, containedSamples, service)
  
  sortedParentList = laneParentDict.keys()
  sortedParentList.sort()
  
  sampleSheetList = []
  
  for laneKey in sortedParentList:
    lane = laneParentDict[laneKey]['LANE']
    logger.info('Processing Lane '+ lane)
    codeList, propertyDict = getLaneAncestors(service, lane, logger)
    
    ssLine = sampleSheetLine (operator=configDict['facilityInstitution'])
    ssLineList = []
    
    for code in codeList:
      sampleProperties = propertyDict[code]
      
      # Precedence of the EXTERNAL_SAMPLE_NAME property is:
      # RAW_SAMPLE -> LIBRARY -> LIBRARY_POOL 
      try:
        ssLine.description = sanitizeString(sampleProperties[configDict['externalSampleName']])
      except:
        pass
      
      if sampleProperties[SAMPLETYPE] == configDict['libraryTypeName']:
        
        index = ''
          
        if configDict['index1Name'] in sampleProperties and index1ReadLength > 0:
          index = sampleProperties[configDict['index1Name']][0:index1ReadLength]
        
        if configDict['index2Name'] in sampleProperties and indexRead2Length > 0:
          index = index + configDict['indexSeparator'] + sampleProperties[configDict['index2Name']][0:indexRead2Length]
          
        ssLine.fcId = flowCellName
        ssLine.lane = lane[-1]
        ssLine.sampleId = code + '_' + flowCellDict[configDict['runFolderName']] + '_' + ssLine.lane
        ssLine.index = index
        
      if sampleProperties[SAMPLETYPE] == configDict['masterTypeName']:
        ssLine.sampleRef = sampleProperties[configDict['ncbiOrganismTaxonomy']] 
        
        # Check if PhiX Control
        if int(ssLine.sampleRef) == configDict['phixNcbiCode']:
          ssLine.control = 'Y'
        else:
          ssLine.control = 'N'
        
        ssLine.recipe = flowCellDict[configDict['endType']] + '_' + flowCellDict[configDict['cycles']]
        ssLine.sampleProject = flowCellDict[configDict['runFolderName']] + '_' + ssLine.lane
        
        ssLineList.append(ssLine)
        ssLine = sampleSheetLine (operator=configDict['facilityInstitution'])

    sampleSheetList.append(ssLineList)

  if myoptions.singlelane:
    writeSampleSheetPerLane (configDict, flowCellName, sampleSheetList, myoptions, logger, fileName=myoptions.outdir +
                      configDict['sampleSheetFileName'])
  else:    
    writeSampleSheet(configDict, flowCellName, sampleSheetList, myoptions, logger, fileName=myoptions.outdir +
                      configDict['sampleSheetFileName'])
    
  logout(service, logger)
  print datetime.now()

if __name__ == "__main__":
    main()
