# -*- coding: utf-8 -*-
'''
Copyright 2016 ETH Zuerich, CISD
  
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Manuel Kohler

@description:
Creates Excel-based invoices for the Quantitative Genomics Facility,
D-BSSE, ETH Zurich

@attention:
Runs under Jython

@note:

'''

import os
import re
import sys
import logging
from datetime import *
from ConfigParser import SafeConfigParser
from optparse import OptionParser
from java.io import FileOutputStream
from org.apache.poi.hssf.usermodel import HSSFWorkbook
from org.apache.poi.hssf.usermodel import HeaderFooter
from org.apache.poi.poifs.filesystem import POIFSFileSystem
from org.apache.poi.xssf.usermodel import XSSFWorkbook
from ch.systemsx.cisd.openbis.dss.client.api.v1 import OpenbisServiceFacadeFactory
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from java.util import EnumSet
from java.util import TreeMap
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SampleFetchOption

excelFormats = {"xls": "HSSFWorkbook()" , "xlsx": "XSSFWorkbook()"}

# This list is imply used to keep the order of elements of the 'columnHeadersMap'
columnHeadersList = ["EXTERNAL_SAMPLE_NAME", "BARCODE", "INDEX2", "CONTACT_PERSON_NAME",
                     "BIOLOGICAL_SAMPLE_ARRIVED", "PREPARED_BY", "KIT", "QC_AT_DBSSE",
                     "NOTES"]

columnHeadersMap = {"EXTERNAL_SAMPLE_NAME": "Sample Name",
                    "BARCODE": "Index",
                    "INDEX2": "Index2",
                    "PREPARED_BY" : "Prepared by",
                    "KIT" : "Kit",
                    "QC_AT_DBSSE" : "QC at D-BSSE",
                    "CONTACT_PERSON_NAME" : "Contact Person",
                    "NOTES" : "Notes",
                    "BIOLOGICAL_SAMPLE_ARRIVED": "Received",
                    }

 
class uniqueRow():
    '''
    Little helper class which ensures the unique use of a row 
    '''
    def __init__(self):
        self.row = -1
    
    def getNextRow (self):
        self.row += 1
        return self.row
    
    def setRow(self, rowNumber):
        self.row = rowNumber
        return self.row

class uniqueColumn():
    '''
    Little helper class which ensures the unique use of a column 
    '''
    def __init__(self):
        self.column = -1
    
    def getCurrentColumn(self):
        return self.column
    
    def getNextColumn (self):
        self.column += 1
        return self.column
    
    def setColumn(self, columnNumber):
        self.column = columnNumber
        return self.column

def getDate():
    d = datetime.now()
    return d.strftime("%A, %d-%B-%Y")


def setFont(wb, configMap, fontSize=10, boldness=False, underline=False):
    font = wb.createFont()
    font.setFontHeightInPoints(fontSize)
    font.setFontName(configMap["defaultFonts"])
    font.setItalic(False)
    font.setStrikeout(False)
    font.setBold(boldness)
    if underline:
        font.setUnderline(font.U_SINGLE);
    # Fonts are set into a style so create a new one to use.
    style = wb.createCellStyle()
    style.setFont(font)
    return style


def getVocabulary(service, vocabularyCode):
    ''' Returns the vocabulary term and vocabulary label of a vocabulary specified by the parameter
    vocabularyCode in a map'''
    
    vocabularies = service.listVocabularies()
    vocabularyMap = {}
    for vocabulary in vocabularies:
        if (vocabulary.getCode() == vocabularyCode):
            terms = vocabulary.getTerms()
    for term in terms:
        vocabularyMap[term.getCode()] = term.getLabel()
    return vocabularyMap


def identify_sequencer(sequencer_code):
    sequencer_map = {"M": "MiSeq", "NS": "NextSeq"}
    if (sequencer_code[0] in sequencer_map):
        return sequencer_mapp[sequencer_code[0]]
    else:
        return "HiSeq"


def merge_two_dicts(x, y):
    '''Given two dicts, merge them into a new dict as a shallow copy.'''
    z = x.copy()
    z.update(y)
    return z

def writeHeader(sheet, myRows, wb, configMap):
  # Write header
  row = sheet.createRow(myRows.getNextRow())
  row.createCell(0).setCellValue(configMap["facilityName"] + ", " + configMap["facilityInstitution"])
  row.getCell(0).setCellStyle(setFont(wb, configMap, 10, True))
  row1 = sheet.createRow(myRows.getNextRow())
  row1.createCell(0).setCellValue(getDate())
  row1.getCell(0).setCellStyle(setFont(wb, configMap, 10, True))


def createRow(configMap, wb, sheet, myRows, key="", value="", rowNumber=0, fontSize=10, boldness=False, underline=False, columnspace=0):
    if rowNumber == 0:
      row = sheet.createRow(myRows.getNextRow())
    else:
      row = rowNumber
    row.createCell(0).setCellValue(key)
    row.createCell(1+columnspace).setCellValue(value)
    row.getCell(0).setCellStyle(setFont(wb, configMap, fontSize, boldness, underline))
    row.getCell(1+columnspace).setCellStyle(setFont(wb, configMap, fontSize, boldness, underline))
    return row


def writeXLSHeader (configMap, service, sheet):
    header = sheet.getHeader()
    header.setCenter(configMap["facilityName"] + "\n" + datetime.now().strftime("%d-%B-%Y"))


def writeXLSFooter(service, sheet):
    footer = sheet.getFooter()
    footer.setCenter( "Page " + HeaderFooter.page() + " of " + HeaderFooter.numPages() )
    #footer.setRight("generated on " + datetime.now().strftime("%H:%M - %d.%m.%Y"))


def write_flowcell_details(configMap, flowCellProperties, myRows, sequencerVocabulary, runModeVocabulary, flowcell, wb, sheet, createRow):
    createRow(configMap, wb, sheet, myRows)
    createRow(configMap, wb, sheet, myRows)
    createRow(configMap, wb, sheet, myRows)
    createRow(configMap, wb, sheet, myRows)
    createRow(configMap, wb, sheet, myRows, "Flow Cell Details", "", 0, 14, columnspace=1)
    createRow(configMap, wb, sheet, myRows, "Flow Cell", flowcell, columnspace=1)
    for property in flowCellProperties:
        if (property == "SEQUENCER"):
            val = sequencerVocabulary[flowCellProperties[property]]
        else:
            val = flowCellProperties[property]
        if (property == "RUN_MODE"):
            val = runModeVocabulary[flowCellProperties[property]]
        else:
            val = flowCellProperties[property]
        createRow(configMap, wb, sheet, myRows, property, val, columnspace=1)
    
    createRow(configMap, wb, sheet, myRows)



def write_samples(configMap, myRows, wb, sheet, setFont, sample, sampleValues):
    singleSampleColumns = uniqueColumn()
    rowN = sheet.createRow(myRows.getNextRow())
    rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(configMap['sampleCodePrefix'] + sample)
    rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configMap, 10))
    for column in columnHeadersList:
        if (column == 'BIOLOGICAL_SAMPLE_ARRIVED'):
            try:
#                 value = sampleValues[column].split(" ")[0]
                value = sampleValues[column]
            except:
                value = ""
        else:
            try:
                value = sampleValues[column]
            except:
                value = ""
        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(value)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configMap, 10))
    
    return value

def writeExcel(myoptions, configMap, service, piName, laneDict, sampleDict, piDict,
               piSpace, flowCellProperties, flowcellName, logger,format="xls"):
    '''
    Writes out all data to an Excel file
    '''
    accumulated_pi_dict = {}
    sample_pools = {}
    regular_samples = {}
    myRows = uniqueRow()
    sequencerVocabulary = getVocabulary(service, "SEQUENCER")
    runModeVocabulary = getVocabulary(service, "RUN_MODE_VOCABULARY")
    setOfFlowcells = set ()
    try:
        # expecting the old running folder name
        runDate, seqId, runningNumber, flowcell = flowCellProperties["RUN_NAME_FOLDER"].split("_")
        flowcell = flowcell[1:]
    except:
        flowcell = flowcellName
    
    wb = (eval(excelFormats[format]))
    createHelper = wb.getCreationHelper()
    sheet = wb.createSheet(configMap["facilityNameShort"])
    # 4/3 = 133 percent magnification when opening the workbook
    sheet.setZoom(4, 3)
    
    writeHeader(sheet, myRows, wb, configMap)
    createRow(configMap, wb, sheet, myRows, "Principal Investigator", piName.replace("_", " "), 0, 10, True, columnspace=1)
    createRow(configMap, wb, sheet, myRows,"Data Space", piSpace, 0, 10, True, columnspace=1)
    createRow(configMap, wb, sheet, myRows)
    createRow(configMap, wb, sheet, myRows,"Samples", "", 0, 10, True, True)
    
    myColumns = uniqueColumn()
    
    sampleHeader = sheet.createRow(myRows.getNextRow())
    #   sampleHeader.createCell(myColumns.getNextColumn()).setCellValue("Flow Cell:Lane")
    #   sampleHeader.getCell(myColumns.getCurrentColumn()).setCellStyle(setFont(wb, configMap, 10, True))
    sampleHeader.createCell(myColumns.getNextColumn()).setCellValue("Sample Code")
    sampleHeader.getCell(myColumns.getCurrentColumn()).setCellStyle(setFont(wb, configMap, 10, True))
    for c in columnHeadersList:
        sampleHeader.createCell(myColumns.getNextColumn()).setCellValue(columnHeadersMap[c])
        sampleHeader.getCell(myColumns.getCurrentColumn()).setCellStyle(setFont(wb, configMap, 10, True))
    
    listofLanes = piDict[piName]
    listofLanes.sort()
    logger.debug(listofLanes)
       
    for lane in listofLanes:
        accumulated_pi_dict = merge_two_dicts(accumulated_pi_dict, sampleDict[lane])

    # sort the dictionary by keys and taking the key as an integer
    for sample in sorted(accumulated_pi_dict.iterkeys(), key=int):
        sampleValues = accumulated_pi_dict[sample]
        logger.debug(sampleValues['PRINCIPAL_INVESTIGATOR_NAME'])
        logger.debug(piName)
        logger.debug(sample)
        # if there is a shared lane do not mix them 
        if (sanitizeString(sampleValues['PRINCIPAL_INVESTIGATOR_NAME']) != sanitizeString(piName)):
            continue
        
        # Find the Pools:
        try:
            if sampleValues['BARCODE'] != 'NOINDEX':
                regular_samples[sample] = sampleValues
            else:
                sample_pools[sample] = sampleValues
        except:
            if int(sampleValues['NCBI_ORGANISM_TAXONOMY']) != 10847:
                sample_pools[sample] = sampleValues
            else:
                regular_samples[sample] = sampleValues
                           
    print("Found {0} samples.".format(len(regular_samples)))
    print("Found {0} pools.".format(len(sample_pools)))
    
    for reg_sample in regular_samples:     
        value = write_samples(configMap, myRows, wb, sheet, setFont, reg_sample, regular_samples[reg_sample])
  
    createRow(configMap, wb, sheet, myRows)
    createRow(configMap, wb, sheet, myRows)
    createRow(configMap, wb, sheet, myRows)
    sequencer = identify_sequencer(flowCellProperties["SEQUENCER"])
    createRow(configMap, wb, sheet, myRows, key=sequencer + " sequencing", value="", rowNumber=0, fontSize=10, boldness=True, underline=True, columnspace=0 )
    createRow(configMap, wb, sheet, myRows, "Run Folder Name", flowCellProperties["RUN_NAME_FOLDER"], 0, 10, True)
    
    createRow(configMap, wb, sheet, myRows)
    createRow(configMap, wb, sheet, myRows)
    
    # needed to start on the first column again
    pool_columns = uniqueColumn()
    sample_pool_header = sheet.createRow(myRows.getNextRow())
    sample_pool_header.createCell(pool_columns.getNextColumn()).setCellValue("Sample Code")
    sample_pool_header.getCell(pool_columns.getCurrentColumn()).setCellStyle(setFont(wb, configMap, 10, True))
    for c in columnHeadersList:
        sample_pool_header.createCell(pool_columns.getNextColumn()).setCellValue(columnHeadersMap[c])
        sample_pool_header.getCell(pool_columns.getCurrentColumn()).setCellStyle(setFont(wb, configMap, 10, True))
        
    for pool in sample_pools:
        value = write_samples(configMap, myRows, wb, sheet, setFont, pool, sample_pools[pool])       
    
    write_flowcell_details(configMap, flowCellProperties, myRows, sequencerVocabulary, runModeVocabulary, flowcell, wb, sheet, createRow)
    
    # adjust width
    for i in range(0, 20):
        sheet.autoSizeColumn(i)
    
    # set layout to landscape
    #sheet.getPrintSetup().setLandscape(True)
    
    #left_margin_inches = sheet.getMargin(sheet.LeftMargin)
    #right_margin_inches = sheet.getMargin(sheet.RightMargin)
    
    sheet.setMargin(sheet.LeftMargin, 0.5)
    sheet.setMargin(sheet.RightMargin, 0.5)
    
    writeXLSFooter(service, sheet)
    writeXLSHeader(configMap, service, sheet)
    
    # sanitizeString(piName) + datetime.now().strftime("_%d_%m_%Y.") + format
    #  Write the output to a file
    fileName = myoptions.outdir + configMap["facilityNameShort"] + "_" + flowcell + "_" + \
              sanitizeString(piName) + "." + format
    fileOut = FileOutputStream(fileName)
    # need this print for use as an openBIS webapp
    print fileName
    
    wb.write(fileOut);
    fileOut.close();


def sanitizeString(myString):
    myString = myString.replace(u'ä', 'ae')
    myString = myString.replace(u'ü', 'ue')
    myString = myString.replace(u'ö', 'oe')
    return re.sub('[^A-Za-z0-9]+', '_', myString.strip())


def setUpLogger(logPath, logLevel=logging.INFO):
    logFileName = 'createInvoices'
    d = datetime.now()
    logFileName = logFileName + '_' + d.strftime('%Y-%m-%d_%H_%M_%S') + '.log'
    logging.basicConfig(filename=logPath + logFileName,
                      format='%(asctime)s [%(levelname)s] %(message)s', level=logLevel)
    logger = logging.getLogger(logFileName)
    return logger


def parseConfigurationFile(propertyFile='etc/service.properties'):
    '''
    Parses the given config files and returns the values
    '''
    config = SafeConfigParser()
    config.read(propertyFile)
    config.sections()
    return config

def readConfig(logger):
    GENERAL = 'GENERAL'
    OPENBIS = 'OPENBIS'
    EXCEL = 'EXCEL'
    
    logger.info('Reading config file')
    configMap = {}
    
    configParameters = parseConfigurationFile()
    configMap['facilityName'] = configParameters.get(GENERAL, 'facilityName')
    configMap['facilityNameShort'] = configParameters.get(GENERAL, 'facilityNameShort')
    configMap['facilityInstitution'] = configParameters.get(GENERAL, 'facilityInstitution')
    configMap['mailList'] = configParameters.get(GENERAL, 'mailList')
    configMap['mailFrom'] = configParameters.get(GENERAL, 'mailFrom')
    configMap['smptHost'] = configParameters.get(GENERAL, 'smptHost')
    configMap['separator'] = configParameters.get(GENERAL, 'separator')
    configMap['indexSeparator'] = configParameters.get(GENERAL, 'indexSeparator')
    
    configMap['openbisUserName'] = configParameters.get(OPENBIS, 'openbisUserName')
    configMap['openbisPassword'] = configParameters.get(OPENBIS, 'openbisPassword', raw=True)
    configMap['openbisServer'] = configParameters.get(OPENBIS, 'openbisServer')
    configMap['connectionTimeout'] = configParameters.getint(OPENBIS, 'connectionTimeout')
    configMap['pIPropertyName'] = configParameters.get(OPENBIS, 'pIPropertyName')
    configMap['sampleCodePrefix'] = configParameters.get(OPENBIS, 'sampleCodePrefix')
    
    configMap['defaultFonts'] = configParameters.get(EXCEL, 'defaultFonts')
    
    return configMap

def login(logger, configMap):
    logger.info('Logging into ' + configMap['openbisServer'])
    service = OpenbisServiceFacadeFactory.tryCreate(configMap['openbisUserName'],
                                                    configMap['openbisPassword'],
                                                    configMap['openbisServer'],
                                                    configMap['connectionTimeout'])
    return service

def parseOptions(logger):
    logger.info('Parsing command line parameters')
    parser = OptionParser(version='%prog 1.0')
    parser.add_option('-f', '--flowcell',
                    dest='flowcell',
                    help='The flowcell which is used to create the SampleSheet.csv',
                    metavar='<flowcell>')
    parser.add_option('-o', '--outdir',
                    dest='outdir',
                    default='./',
                    help='Specify the ouput directory. Default: ./' ,
                    metavar='<outdir>')
    parser.add_option('-d', '--debug',
                    dest='debug',
                    default=False,
                    action='store_true',
                    help='Verbose debug logging. Default: False')
    
    (options, args) = parser.parse_args()
    
    if options.outdir[-1] <> '/':
        options.outdir = options.outdir + '/'
    
    if options.flowcell is None:
        parser.print_help()
        exit(-1)
    return options


def getFLowcellData(service, configMap, flowcell, logger):

    laneCodeList = []
    fetchOptions = EnumSet.of(SampleFetchOption.ANCESTORS, SampleFetchOption.PROPERTIES)
    
    sc = SearchCriteria();
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowcell));  
    fcList = service.searchForSamples(sc, fetchOptions)
    if not fcList:
        print("No flowcell found with code: " + flowcell)
        sys.exit()
    
    scContained = SearchCriteria()
    scContained.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowcell));
    
    scContainedMain = SearchCriteria();
    scContainedMain.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(scContained))
    
    containedSamples = service.searchForSamples(scContainedMain)
    
    for lane in containedSamples:
        laneCode = lane.getCode()
        laneCodeList.append(laneCode)
        logger.debug("Found lane " + laneCode + " in flow cell")
    logger.info("All lanes found: " + str(laneCodeList))
    
    for p in fcList:
        flowCellProperties = p.getProperties()
      
    numberOfLanes = int(flowCellProperties['LANECOUNT'])
    
    foundLaneNumber = containedSamples.size()
    if (foundLaneNumber > numberOfLanes):
        numberOfLanes = foundLaneNumber
        logger.info("Found " + str(foundLaneNumber) + " lanes! This differs from the lane number " +
             "registered in the flow cell. --> Lanes got splitted!")
    
    laneDict = {}
    sampleDict = {}
    piDict = {}
    spaceDict = {}
    invoiceDict = {}
    
    for lane in laneCodeList:
        myLane = lane
        laneSc = SearchCriteria();
        laneSc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, myLane));
        laneList = service.searchForSamples(laneSc, fetchOptions)
        logger.debug("Length of laneList for " + myLane + " is " + str(len(laneList)))
        
        for l in laneList:
            laneProperties = l.getProperties()
            laneDict[lane] = laneProperties
            laneParents = l.getParents()
            
            s = {}
            for samples in laneParents:
                sampleCode = samples.getCode()
                sampleProperties = samples.getProperties()
                s[sampleCode.split("-")[-1]] = sampleProperties
                sampleDict[lane] = s
                pi = sanitizeString(sampleProperties[configMap["pIPropertyName"]])
                invoiceProperty = sampleProperties['INVOICE']
                # if sample got created via Excel upload, the property could be not set, which is represented by None
                if (invoiceProperty is None):
                    invoiceProperty = 'false'
                sentInvoice = {'true': True, 'false': False}.get(invoiceProperty.lower())
                logger.debug("PI for " + sampleCode + ": " + pi)
                logger.debug("Invoice sent for " + sampleCode + ": " + str(sentInvoice))
                
                if piDict.has_key(pi):
                    piDict[pi].append(lane)
                    # Making the lanes unique
                    piDict[pi] = list(set(piDict[pi]))
                else:
                    piDict[pi] = [lane]
                
                    if not sentInvoice:
                        if invoiceDict.has_key(pi):
                            invoiceDict[pi].append(lane)
                            # Making the lanes unique
                            invoiceDict[pi] = list(set(invoiceDict[pi]))
                        else:
                            invoiceDict[pi] = [lane]
                
                    spaceDict[pi] = l.getSpaceCode()
    
    logger.debug(spaceDict)
    
    logger.info("Found the following PIs on the lanes: ")
    logger.info(piDict)
    logger.info("Found the following PIs with non-invoiced samples : ")
    logger.info(invoiceDict)
    
    # simply sort the hashmap
    treeMap = TreeMap (flowCellProperties)
    return laneDict, sampleDict, piDict, treeMap, spaceDict, invoiceDict


'''
Main script
'''

def main():

    # for now setting the format by hand
    format = "xlsx"
    magicString = "@Invoice@"
    
    logger = setUpLogger('log/')
    logger.info('Started Creation Invoices...')
    
    myoptions = parseOptions(logger)
    configMap = readConfig(logger)
    if myoptions.debug:
        logger.setLevel(logging.DEBUG)
    
    service = login(logger, configMap)
    flowcellName = myoptions.flowcell
    laneDict, sampleDict, piDict, flowCellProperties, spaceDict, invoiceDict = getFLowcellData(service, configMap, flowcellName, logger)
    
    for piName in piDict:
        # create an Excel file for each PI
        writeExcel(myoptions, configMap, service, piName, laneDict, sampleDict,
                 piDict, spaceDict[piName], flowCellProperties,
                 flowcellName, logger, format)
    
    for invoicePi in invoiceDict:
        print (magicString + invoicePi)
    
    service.logout()

if __name__ == "__main__":
    main()
