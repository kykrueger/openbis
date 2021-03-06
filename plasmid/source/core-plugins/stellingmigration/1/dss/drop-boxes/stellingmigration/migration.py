# some_file.py
from datetime import datetime
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria, SearchSubCriteria
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType
import xml.etree.ElementTree as ET

##
## Generic Process Method
##

definitions = {
                "GENERAL_PROTOCOL" : 
                                     { 
                                      "CHEMICALS" : {
                                                     "quantity" : "QUANTITY",
                                                     "name" : "NAME"
                                                     },
                                      "SOLUTIONS_BUFFERS" : {
                                                     "quantity" : "QUANTITY",
                                                     "name" : "NAME"
                                                     },
                                      "MEDIA" : {
                                                     "quantity" : "QUANTITY",
                                                     "name" : "NAME"
                                                     },
                                      "GENERAL_PROTOCOL" : {
                                                     "name" : "NAME"
                                                     },
                                      "ENZYMES" : {
                                                     "name" : "NAME",
                                                     "concentration" : "CONCENTRATION"
                                                     }
                                     },
                "MEDIA" : 
                                     { 
                                      "CHEMICALS" : {
                                                     "concentration" : "CONCENTRATION",
                                                     "name" : "NAME"
                                                     },
                                      "SOLUTIONS_BUFFERS" : {
                                                     "concentration" : "CONCENTRATION",
                                                     "name" : "NAME"
                                                     },
                                      "MEDIA" : {
                                                     "concentration" : "CONCENTRATION",
                                                     "name" : "NAME"
                                                     }
                                     },
                "PCR" : 
                                     { 
                                      "CHEMICALS" : {
                                                     "quantity" : "QUANTITY",
                                                     "name" : "NAME"
                                                     },
                                      "SOLUTIONS_BUFFERS" : {
                                                     "quantity" : "QUANTITY",
                                                     "name" : "NAME"
                                                     },
                                      "ENZYMES" : {
                                                     "name" : "NAME",
                                                     "concentration" : "CONCENTRATION"
                                                     }
                                     },
                "POMBE" : 
                                     { 
                                      "PLASMIDS" : {
                                                     "annotation" : "PLASMID_ANNOTATION",
                                                     "rel" : "PLASMID_RELATIONSHIP"
                                                     }
                                     },
               "READOUT" : 
                                    { 
                                     "CHEMICALS" : {
                                                    "quantity" : "QUANTITY",
                                                    "name" : "NAME"
                                                    },
                                     "SOLUTIONS_BUFFERS" : {
                                                    "quantity" : "QUANTITY",
                                                    "name" : "NAME"
                                                    },
                                     "GENERAL_PROTOCOL" : {
                                                    "name" : "NAME"
                                                    }
                                    },
               "RESULT" : 
                                    { 
                                     "ANNOTATIONS" : {
                                                    "quantity" : "QUANTITY",
                                                    "name" : "NAME",
                                                    "detail" : "DETAIL"
                                                    }
                                    },
               "SOLUTIONS_BUFFERS" : 
                                    { 
                                     "CHEMICALS" : {
                                                     "concentration" : "CONCENTRATION",
                                                     "name" : "NAME"
                                                     },
                                     "SOLUTIONS_BUFFERS" : {
                                                     "concentration" : "CONCENTRATION",
                                                     "name" : "NAME"
                                                     }
                                    },
               "WESTERN_BLOTTING" : 
                                    {
                                     "ANTIBODY" : {
                                                     "quantity" : "QUANTITY",
                                                     "name" : "NAME"
                                                     },
                                     "CHEMICALS" : {
                                                     "quantity" : "QUANTITY",
                                                     "name" : "NAME"
                                                     },
                                     "SOLUTIONS_BUFFERS" : {
                                                     "quantity" : "QUANTITY",
                                                     "name" : "NAME"
                                                     }
                                    },
               "YEAST" : 
                                    {
                                     "PLASMIDS" : {
                                                     "annotation" : "PLASMID_ANNOTATION",
                                                     "rel" : "PLASMID_RELATIONSHIP"
                                                     }
                                    }
};

currentCache = None

def process(tr):
    print "START!"
    for sampleType in definitions:
        properties = definitions[sampleType]
        samples = getSamplesByType(tr, sampleType)
        global currentCache
        currentCache = samples
        print sampleType + ": "+ str(len(samples))
        for sample in samples:
            translate(tr, sample, properties)
    print "FINISH!"

def getSamplesByType(tr, sampleType):
    criteria = SearchCriteria()
    criteria.setOperator(criteria.SearchOperator.MATCH_ANY_CLAUSES)
    criteria.addMatchClause(criteria.MatchClause.createAttributeMatch(criteria.MatchClauseAttribute.TYPE, sampleType))
    samples = tr.getSearchService().searchForSamples(criteria)
    return samples

def getSampleByPermId(tr, permId):
    criteria = SearchCriteria()
    criteria.setOperator(criteria.SearchOperator.MATCH_ANY_CLAUSES)
    criteria.addMatchClause(criteria.MatchClause.createAttributeMatch(criteria.MatchClauseAttribute.PERM_ID, permId))
    samples = tr.getSearchService().searchForSamples(criteria)
    if len(samples) is 1:
        return samples[0]
    else:
        return None

def getSampleFromCache(identifier):
    sampleToReturn = None
    for sample in currentCache:
        if sample.getSampleIdentifier() == identifier:
            return sample
    return None

def translate(tr, sample, properties):
    sampleType = sample.getSampleType()
    # Create new annotations from scratch
    newAnnotations = sample.getPropertyValue("ANNOTATIONS_STATE")
    newAnnotationsRoot = None
    newAnnotationsRoot = ET.Element("root")
    
    # Read old annotations
    for property in properties:
        propertyDefinitions = properties[property]
        oldAnnotationsRoot = None
        try:
            propertyValue = unicode(sample.getPropertyValue(property), "utf-8")
            if '<root>' in propertyValue:
                oldAnnotationsRoot = ET.fromstring(propertyValue)
        except Exception:
            print "[ERROR - PROCESSING PROPERTY_CODE] " + sample.code + " " + property
            
        if oldAnnotationsRoot is not None:
            for child in oldAnnotationsRoot:
                    try:
                        permId = child.attrib["permId"]
                        linkedSample = getSampleByPermId(tr, permId)
                        linkedSampleIdentifier = linkedSample.getSampleIdentifier()
                        if property == "GENERAL_PROTOCOL":
                            #Don't migrate them, they should be parents and contain no information.
                            #sample.getParentSampleIdentifiers().add(linkedSampleIdentifier) 
                            print "[PROTOCOL] " + sample.code + " " + linkedSampleIdentifier
                        else:
                            newAnnotationsNode = ET.SubElement(newAnnotationsRoot, "Sample")
                            print "[INFO - PROCESSING PERM_ID] " + sample.code + " " + permId
                            newAnnotationsNode.attrib["sampleType"] = linkedSample.sampleType
                            newAnnotationsNode.attrib["permId"] = permId
                            newAnnotationsNode.attrib["identifier"] = linkedSampleIdentifier
                            for oldName in propertyDefinitions:
                                newName = propertyDefinitions[oldName]
                                value = getValueOrNull(child.attrib, oldName)
                                if(value is not None):
                                    newAnnotationsNode.attrib[newName] = value
                            if property == "PLASMIDS":
                                #Is plasmid a link? it is if it's not a parent
                                isLink = True
                                for sampleParentIdentifier in sample.getParentSampleIdentifiers():
                                    if sampleParentIdentifier == linkedSampleIdentifier:
                                        isLink = False
                                if isLink: #Find parent who owns it
                                    isFound = False
                                    for sampleParentIdentifier in sample.getParentSampleIdentifiers():
                                        parentFromSample = getSampleFromCache(sampleParentIdentifier) #tr.getSample(sampleParentIdentifier)
                                        parentAnnotations = None
                                        if parentFromSample != None:
                                            parentAnnotations = parentFromSample.getPropertyValue("ANNOTATIONS_STATE")
                                        if parentAnnotations != None:
                                            if (permId in parentAnnotations):
                                                newAnnotationsNode.attrib["CONTAINED"] = sampleParentIdentifier
                                                isFound = True
                                        else:
                                            print "[WARNING - PARENTANNOTATIONS NONE] " + sample.code + " " + permId
                                    if not isFound:
                                        print "[ERROR - NOTFOUND CONTAINER] " + sample.code + " " + permId
                                    
                    except Exception, e:
                        print "[ERROR - PROCESSING PERM_ID] " + sample.code + " " + permId + str(e)
    save(tr, sample, "ANNOTATIONS_STATE", ET.tostring(newAnnotationsRoot, encoding='utf-8'))

def save(tr, sample, property, propertyValue):
    mutableSample = tr.makeSampleMutable(sample)
    mutableSample.setPropertyValue(property, propertyValue)
    
def getValueOrNull(map, key):
    if key in map:
        return map[key]
    else:
        return None