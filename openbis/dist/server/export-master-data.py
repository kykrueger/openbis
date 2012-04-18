#
# Generates a master data registration script for a running openBIS server.
#
# To execute it, please run the ./export-master-data.sh 
#
import codecs
import sys
import re
import os
from datetime import datetime
from java.lang import System

ENCODING = "utf-8"

OUTPUT_DIR_PROP_KEY = "master.data.output.dir"

VARNAME_PREFIXES = {
  "EXPERIMENT" : "exp_type_",
  "SAMPLE" : "samp_type_",
  "DATA_SET" : "data_set_type_",
  "MATERIAL" : "material_type_",
  "PROPERTY" : "prop_type_",
  "ASSIGNMENT" : "assignment_",
  "FILE_FORMAT" : "file_type_",
  "VOCABULARY" : "vocabulary_",
  "VOCABULARY_TERM" : "vocabulary_term_"               
}

# Those already exist by default in each openBIS installation. Therefore we want to exclude them
# in our master data export
EXISTING_FILE_TYPES = ['HDF5', 'PROPRIETARY', 'SRF', 'TIFF', 'TSV', 'XML']
EXISTING_DATASET_TYPES = ['UNKNOWN']
EXISTING_VOCABULARY = {'$STORAGE_FORMAT' : ['PROPRIETARY', 'BDS_DIRECTORY']}

def getVarName(type, var):
    # remove minuses, dots and colons 
    # they are not valid characters for Python variables
    normalized = re.sub("[\\-\\.:\\$]+", "", var)
    return VARNAME_PREFIXES[type] + normalized

def is_ascii(str):
    try:
        str.decode('ascii')
    except UnicodeDecodeError:
        return False
    
    return True
    
def strLiteral(var):
    def sanitize(dirtyString):
      return dirtyString.replace("'", "\\'")

    if var:
        if is_ascii(var):
            # ASCII string
            return ("'" + sanitize(var) + "'").replace('\n', '\\ ')
        else:
            # UNICODE string
            return "u'" + var + "'"
    else: 
        # NULL
        return 'None'
    
def codeLiteral(code):
    normalized = code
    if code[0] == "$":
        normalized = code[1:]
    return strLiteral(normalized)


def exportFileFormatType(fileType):
    var = getVarName("FILE_FORMAT", fileType.getCode())
    code = codeLiteral(fileType.getCode())
    description = strLiteral(fileType.getDescription())
    if (fileType.getCode() in EXISTING_FILE_TYPES):
        return "" 
    else:
        return """
%(var)s = tr.createNewFileFormatType(%(code)s)
%(var)s.setDescription(%(description)s)
   """ % vars()


def exportVocabulary(vocabulary):
    var = getVarName("VOCABULARY", vocabulary.getCode())
    code = codeLiteral(vocabulary.getCode())
    description = strLiteral(vocabulary.getDescription())
    urlTemplate = strLiteral(vocabulary.getUrlTemplate())
    isManagedInternally = vocabulary.isManagedInternally();
    isInternalNamespace = vocabulary.isInternalNamespace();
    isChosenFromList = vocabulary.isChosenFromList();
    if (vocabulary.getCode() in EXISTING_VOCABULARY):
        result = ""
    else:
        result = """
%(var)s = tr.createNewVocabulary(%(code)s)
%(var)s.setDescription(%(description)s)
%(var)s.setUrlTemplate(%(urlTemplate)s)
%(var)s.setManagedInternally(%(isManagedInternally)s)
%(var)s.setInternalNamespace(%(isInternalNamespace)s)
%(var)s.setChosenFromList(%(isChosenFromList)s)
""" % vars()
    
    for term in vocabulary.getTerms():
        term_var = getVarName("VOCABULARY_TERM", vocabulary.getCode() + "_" + term.getCode())
        term_code = codeLiteral(term.getCode())
        term_description = strLiteral(term.getDescription())
        term_label = strLiteral(term.getLabel())
        term_ordinal = term.getOrdinal()
        if vocabulary.getCode() in EXISTING_VOCABULARY:
            if  not(term.getCode() in EXISTING_VOCABULARY[vocabulary.getCode()]):
                result = result + """
%(term_var)s = tr.createNewVocabularyTerm(%(term_code)s)
%(term_var)s.setDescription(%(term_description)s)
%(term_var)s.setLabel(%(term_label)s)
%(term_var)s.setOrdinal(%(term_ordinal)s)
%(var)s.addTerm(%(term_var)s)
""" % vars()

    return result

        
def exportExperimentType(experimentType):
    var = getVarName("EXPERIMENT", experimentType.getCode())
    code = codeLiteral(experimentType.getCode())
    description = strLiteral(experimentType.getDescription())
    return """
%(var)s = tr.createNewExperimentType(%(code)s)
%(var)s.setDescription(%(description)s)
""" % vars()

def exportSampleType(sampleType):
    var = getVarName("SAMPLE", sampleType.getCode())
    code = codeLiteral(sampleType.getCode())
    description = strLiteral(sampleType.getDescription())
    listable = sampleType.isListable()
    isSubcodeUnique = sampleType.isSubcodeUnique()
    isAutoGeneratedCode = sampleType.isAutoGeneratedCode()
    generatedCodePrefix = strLiteral(sampleType.getGeneratedCodePrefix())
    return """
%(var)s = tr.createNewSampleType(%(code)s)
%(var)s.setDescription(%(description)s)
%(var)s.setListable(%(listable)s)
%(var)s.setSubcodeUnique(%(isSubcodeUnique)s)
%(var)s.setAutoGeneratedCode(%(isAutoGeneratedCode)s)
%(var)s.setGeneratedCodePrefix(%(generatedCodePrefix)s)
""" % vars()

def exportDataSetType(dataSetType):
    var = getVarName("DATA_SET", dataSetType.getCode())
    code = codeLiteral(dataSetType.getCode())
    description = strLiteral(dataSetType.getDescription())
    isContainerType = dataSetType.isContainerType()
    if (dataSetType.getCode() in EXISTING_DATASET_TYPES):
        return ""
    else:
        return """
%(var)s = tr.createNewDataSetType(%(code)s)
%(var)s.setDescription(%(description)s)
%(var)s.setContainerType(%(isContainerType)s)
""" % vars()

def exportMaterialType(materialType):
    var = getVarName("MATERIAL", materialType.getCode())
    code = codeLiteral(materialType.getCode())
    description = strLiteral(materialType.getDescription())
    return """
%(var)s = tr.createNewMaterialType(%(code)s)
%(var)s.setDescription(%(description)s)
""" % vars()
    
def exportPropertyType(propertyType):
    var = getVarName("PROPERTY", propertyType.getCode())
    code = codeLiteral(propertyType.getCode())
    label = strLiteral(propertyType.getLabel())
    dataType = propertyType.getDataType().name()
    specialSetters = ""
    
    if propertyType.getMaterialType():
        materialVar = getVarName("MATERIAL", propertyType.getMaterialType().getCode())
        specialSetters = "%(var)s.setMaterialType(%(materialVar)s)" % vars()
        
    if propertyType.getVocabulary():
        vocabularyVar = getVarName("VOCABULARY", propertyType.getVocabulary().getCode())
        specialSetters = specialSetters + "\n%(var)s.setVocabulary(%(vocabularyVar)s)" % vars()
        
    isManagedInternally = propertyType.isManagedInternally() 
    isInternalNamespace = propertyType.isInternalNamespace()
    
    return """
%(var)s = tr.createNewPropertyType(%(code)s, DataType.%(dataType)s)
%(var)s.setLabel(%(label)s)
%(var)s.setManagedInternally(%(isManagedInternally)s)
%(var)s.setInternalNamespace(%(isInternalNamespace)s)
%(specialSetters)s
""" % vars()

def exportAssignment(assignment):
    
    var = getVarName("ASSIGNMENT",
                     assignment.getEntityKind().name() + "_" + 
                     assignment.getEntityTypeCode() + "_" + 
                     assignment.getPropertyTypeCode())
    entityVar = getVarName(assignment.getEntityKind().name(), assignment.getEntityTypeCode())
    propertyVar = getVarName("PROPERTY", assignment.getPropertyTypeCode())
    isMandatory = assignment.isMandatory()
    section = strLiteral(assignment.getSection())
    posInForms = assignment.getPositionInForms()
    return """
%(var)s = tr.assignPropertyType(%(entityVar)s, %(propertyVar)s)
%(var)s.setMandatory(%(isMandatory)s)
%(var)s.setSection(%(section)s)
%(var)s.setPositionInForms(%(posInForms)s)
""" % vars()


def defaultOutDirName():
  now = datetime.now()
  return "./exported-master-data-" + now.strftime("%y-%m-%d-%H-%M")


#
# Main script
#

outDir = System.getProperty(OUTPUT_DIR_PROP_KEY, defaultOutDirName())
print "Exporting master data to ", outDir, "..."


tr = service.transaction()

exportedContent = (
  [exportFileFormatType(fileType) for fileType in tr.listFileFormatTypes()] + 
  [exportVocabulary(vocabulary)   for vocabulary in tr.listVocabularies()] + 
  [exportExperimentType(expType)  for expType in tr.listExperimentTypes()] + 
  [exportSampleType(sampleType)   for sampleType in tr.listSampleTypes()] + 
  [exportDataSetType(dataSetType) for dataSetType in tr.listDataSetTypes()] + 
  [exportMaterialType(materialType) for materialType in tr.listMaterialTypes()] + 
  [exportPropertyType(propertyType) for propertyType in tr.listPropertyTypes()] + 
  [exportAssignment(assignment) for assignment in tr.listPropertyAssignments()] 
);
    
if not os.path.exists(outDir):
    os.makedirs(outDir)
    
out = codecs.open(outDir + "/master-data.py", "w", ENCODING)
out.write("""# -*- coding: %(ENCODING)s -*-
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

tr = service.transaction()

""" % vars())

exportedText = ''.join(exportedContent)
out.write(exportedText.encode(ENCODING))
out.close()

print "DONE"
