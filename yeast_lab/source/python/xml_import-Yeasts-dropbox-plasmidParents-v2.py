#! /usr/bin/env python
"""
 Import sample properties from a text file.  
"""


import os
import glob
import re
import time
import shutil, sys

import xml.etree.ElementTree as ET
from xml.etree import ElementTree

from time import *
from datetime import datetime
import calendar

from ch.systemsx.cisd.openbis.generic.shared.managed_property.structured import ElementFactory
from ch.systemsx.cisd.openbis.generic.shared.managed_property.structured import XmlStructuredPropertyConverter


print "########################################################"
tz=localtime()[3]-gmtime()[3]
d=datetime.now()
print d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")



ATR_CODE = "code"
ATR_CONC = "concentration"
DATE_ATTRIBUTE = 'date'
PERSON_ATTRIBUTE = 'person'
ATR_RELATIONSHIP = "rel"
ATR_ANNOTATION = "annotation"
ATR_NAME='name'

COMMENT_ENTRY_ELEMENT_LABEL = 'commentEntry'
CONNECTION_LABEL = "connection"
LINK_LABEL = "link"
CODE_LABEL = "code"
RELATIONSHIP_LABEL = "relationship"
ANNOTATION_LABEL = "annotation"
CONC_LABEL = "concentration"
DATE_LABEL = 'Date'
PERSON_LABEL = 'Person'
COMMENT_TEXT_LABEL = 'Comment Text'
NAME_LABEL='name'

chemicals_list=[]
concentration_list=[]
buffers_list=[]
buffers_concentration_list =[]
date_list=[]
person_list=[]
comment_text_list=[]
medias_list=[]
medias_concentration_list=[]
relationship_list=[]
plasmids_list=[]
annotation_list=[]
sampleLink_list=[]
####FOR PLASMID PARENTS CODE:

"""input pattern matching one plasmid, e.g.: 
- 'FRP1 (DEL:URA3)', 
- 'FRP2 (INT)', 
- 'FRP3(MOD:URA3)', 
- 'FRP4'
"""
INPUT_PATTERN = """
                 # no '^': allow whitespace at the beginning
    ([^ (]*)     # 1st group: match code of a sample, everything before a space or '(' (e.g. 'FRP')
    (\ *\(       # start of 2nd group (matches an optional relationship type with annotation) 
                 # any spaces followed by a '('
    ([^:]*)      # 3rd group: match relationship type, any character but ':' (e.g. 'DEL', 'INT', 'MOD')
    :?           # optional ':' separator
    (.*)         # 4th group: match annotation, any text (e.g. 'URA3')
    \))?         # end of 2nd (optional) group: closing bracket of relationship details
                 # no '$': allow whitespace at the end
"""

""" due to some weird jython threading issue, we need to compile the pattern outside the function body """
inputPattern = re.compile(INPUT_PATTERN, re.VERBOSE)

"""relationship types shortcuts"""

DEL_REL_TYPE = 'DEL'
INT_REL_TYPE = 'INT'
MOD_REL_TYPE = 'MOD'

"""tuple of supported relationship types as shortcuts"""
REL_TYPES = (DEL_REL_TYPE, INT_REL_TYPE, MOD_REL_TYPE)
"""dictionary from relationship type shortcut to its 'character' representation"""
REL_TYPE_CHARS = {
    DEL_REL_TYPE: u'\u0394', # unicode '???'
#	DEL_REL_TYPE: 'D', 
    INT_REL_TYPE: '::', 
    MOD_REL_TYPE: '_' 
}
"""dictionary from relationship type shortcut to its full name/label"""
REL_TYPE_LABELS = {
    DEL_REL_TYPE: 'deletion', 
    INT_REL_TYPE: 'integration', 
    MOD_REL_TYPE: 'modification' 
}

REL_TYPE_LABEL_OTHER = '(other)'
REL_TYPE_LABELS_WITH_NONE = tuple([REL_TYPE_LABEL_OTHER] + REL_TYPE_LABELS.values())


def create_openbis_timestamp():
	tz=localtime()[3]-gmtime()[3]
	d=datetime.now().timetuple()
	timestamp = (calendar.timegm(d)*1000)
	return timestamp
   

def process(transaction):
	incomingPath = transaction.getIncoming().getAbsolutePath()

###PARSE XML FILE############################################################


	textfile = open(incomingPath, "r")
	tree = ET.parse(textfile)
	root = tree.getroot()

	
	
	for path in [ './Identifier']:
	  node = tree.find(path)
	  if node.text != "":
	    sample_name = node.text
	  else:
	    node.text = ""  
	    
	  
	for path in [ './Experiment']:
	  node = tree.find(path)
 	  if node.text != "":
		experiment_name = node.text
	  else:
	    node.text = ""  
	
	
	for path in [ './Yeast_Parents']:
	  node = tree.find(path)
 	  if node.text != "":
		yeast_parents = node.text
	  else:
	    node.text = ""  

	for path in [ './Plasmids']:
	  node = tree.find(path)
 	  if node.text != "":
		plasmids= node.text
	  else:
	    node.text = ""  
	
				
	def Plasmids():
	  for path in [ './Plasmids']:
		node = tree.find(path)
		if node.text is not None:
		  plasmids = node.text
		  tokens = plasmids.split(',')
		  for token in tokens:
		    if re.search(':', token): 
			  token = token.split(':')
			  plasmid_name = token[0][:-4]
			  plasmid_relationship= token[0][-3:]
			  plasmid_annotation=token[1][:-1]
 			  plasmids_list.append(plasmid_name)
			  relationship_list.append(plasmid_relationship)
			  annotation_list.append(plasmid_annotation)
		    else:
			  plasmid_name=token  
			  plasmid_relationship = ""
			  plasmid_annotation =""
			  plasmids_list.append(plasmid_name)
			  relationship_list.append(plasmid_relationship)
			  annotation_list.append(plasmid_annotation)
		else:
		  node.text = "" 
	  return  plasmids, plasmids_list, relationship_list, annotation_list	  
	
	Plasmids()
	 
	def Chemicals():
	  for path in [ './Chemicals']:
		node = tree.find(path)
		if node.text is not None:
		  chemicals = node.text
		  tokens = chemicals.split(',')
		  for token in tokens:
		    if re.search(":", token): 
			  token = token.split(':')
			  chemical_name = token[0]
			  chemical_concentration=token[1]
			  chemicals_list.append(chemical_name)
			  concentration_list.append(chemical_concentration)
		    else:
			  chemical_name=token  
			  chemical_concentration = "n.a."
			  chemicals_list.append(chemical_name)
			  concentration_list.append(chemical_concentration)
		else:
		  node.text = "n.a." 	  
	  return chemicals_list, concentration_list	  
	
	Chemicals()

	def Buffers():
		  for path in [ './Solutions_Buffers']:
			node = tree.find(path)
			if node.text is not None:
			  buffers = node.text
			  tokens = buffers.split(',')
			  for token in tokens:
				if re.search(":", token): 
				  token = token.split(':')
				  buffer_name = token[0]
				  buffer_concentration=token[1]
				  buffers_list.append(buffer_name)
				  buffers_concentration_list.append(buffer_concentration)
				else:
				  buffer_name=token  
				  buffer_concentration = "n.a."
				  buffers_list.append(buffer_name)
				  buffers_concentration_list.append(buffer_concentration)
			else:
			  node.text = "n.a." 	  
		  return buffers_list, buffers_concentration_list
		  
	Buffers()	  
	
	def Medias():
		  for path in [ './Media']:
			node = tree.find(path)
			if node.text is not None:
			  medias = node.text
			  tokens = medias.split(',')
			  for token in tokens:
				if re.search(":", token): 
				  token = token.split(':')
				  media_name = token[0]
				  media_concentration=token[1]
				  medias_list.append(media_name)
				  medias_concentration_list.append(media_concentration)
				else:
				  media_name=token  
				  media_concentration = "n.a."
				  medias_list.append(media_name)
				  medias_concentration_list.append(media_concentration)
			else:
			  node.text = "n.a." 	  
		  return medias_list, medias_concentration_list
		  
	Medias()	  


	
	for path in [ './XMLCOMMENTS']:
	  node = tree.find(path)
	  if node.text is not None:
		comment_text_list= node.text
	  else:
	    comment_text_list = None 


	elementFactory = ElementFactory()
	
	propertyConverter = XmlStructuredPropertyConverter(elementFactory);
    

### IMPORT YEAST PARENTS##############################################################

	def _createYeastSampleLink(yeast_parents):
		"""
		   Creates sample link XML element for sample with specified 'code'. The element will contain
		   given code as 'code' attribute apart from standard 'permId' attribute.
		   
		   If the sample doesn't exist in DB a fake link will be created with the 'code' as permId.
		   
		   @return: sample link XML element as string, e.g.:
		   - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
		   - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
		"""
		if yeast_parents is not None:
		  YPpath= "/YLAB-TEST/" + yeast_parents
		  permId =transaction.getSample(YPpath).getSample().getPermId()
		  if not permId:
			permId = yeast_parents
		  sampleLink = elementFactory.createSampleLink(permId)
		
		  sampleLink.addAttribute(ATR_CODE, yeast_parents)
		return sampleLink    
	
	
	"""
	Example input:
	
	FRY1, FRY2, FRY3, FRY4
	"""
	def updateYeastFromBatchInput(yeast_parents):
		elements = []
		input = yeast_parents
		if input is not None:
			samples = input.split(',')
			for yeast_parents in samples:
				sampleLink = _createYeastSampleLink(yeast_parents.strip())
				elements.append(sampleLink)
		return propertyConverter.convertToString(elements)
	
	
	def _getYeastCode(yeast_parents):
		elements = []
		input = yeast_parents
		if input is not None:
			samples = input.split(',')
			for yeast_parents in samples:
				yeastPath= "/YLAB-TEST/" + yeast_parents.strip()
				elements.append(yeastPath)   
		return elements          




###IMPORT PLASMID PARENTS#######################################################

	def _group(pattern, input):
		"""@return: groups returned by performing pattern search with given @pattern on given @input"""
		return pattern.search(input).groups()
	
	
	def _translateToChar(relationship_list):
		"""
		   @param relationship: relationship type as a shortcut (@see REL_TYPES), may be null
		   @return: character representation of given @relationship, 
					empty string for null
					'[<relationship>]' for unknown relationship
		"""
		if relationship_list:
			if relationship_list in REL_TYPE_CHARS:
				return REL_TYPE_CHARS[relationship_list]
			else:
				return "[" + relationship_list + "]"
		else:
			return ""
		
		
	def _translateToLabel(relationship_list):
		"""
		   @param relationship: relationship type as a shortcut (@see REL_TYPES), may be null
		   @return: full name of given @relationship, 
					empty string for null, 
					'[<relationship>]' for unknown relationship
		"""
		if relationship_list:
			if relationship_list in REL_TYPE_LABELS:
				return REL_TYPE_LABELS[relationship_list]
			else:
				return "[" + relationship_list + "]"
		else:
			return REL_TYPE_LABEL_OTHER    
	
	def _translateFromLabel(relationshipLabel):
		"""
		   @param relationshipLabel: relationship type as label (@see REL_TYPE_LABELS_WITH_NONE)
		   @return: type of given @relationshipLabel, None for REL_TYPE_LABEL_OTHER, 
		"""
		if relationshipLabel == REL_TYPE_LABEL_OTHER:
			return None
		elif relationshipLabel == 'deletion':
			return DEL_REL_TYPE
		elif relationshipLabel == 'integration':
			return INT_REL_TYPE
		elif relationshipLabel == 'modification':
			return MOD_REL_TYPE    
	
	def _createConnectionString(plasmids_list, relationship_list, annotation_list):
		"""
		   @param plasmids: code of a sample
		   @param relationship: relationship type as a shortcut (@see REL_TYPES), may be null
		   @param annotation: annotation of the relationship, may be null
		   @return: string representation of a connection with @relationship translated to a 'character'
		"""
		result = plasmids_list
		if relationship_list:
			result += _translateToChar(relationship_list)
		if annotation_list:
			result += annotation_list
		return result

	
	def _createPlasmidSampleLink(plasmids_list, relationship_list, annotation_list):
		"""
		   Creates sample link XML element for sample with specified @code. The element will contain
		   given @code as 'code' attribute apart from standard 'permId' attribute. If specified 
		   @relationship or @annotation are not null they will also be contained as attributes.
		   
		   If the sample doesn't exist in DB a fake link will be created with @code as permId.
		   
		   @param code: code of a sample
		   @param relationship: relationship type as a shortcut (@see REL_TYPES), may be null
		   @param annotation: annotation of the relationship, may be null
		   @return: sample link XML element as string, e.g.:
		   - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
		   - '<Sample code="FRP2" permId="20110309154532868-4219" relationship="DEL" annotation="URA3"/>'
		   - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
		   - '<Sample code="FRP4" permId="20110309154532868-4219" relationship="INT"/>'
		   @raise Exception: if the specified relationship type is unknown
		"""
		if plasmids is not None:
			plasmidPath= "/YLAB-TEST/" + plasmids_list.strip()
			permId =transaction.getSample(plasmidPath).getSample().getPermId()
			if not permId:
				permId = plasmids_list
			sampleLink = elementFactory.createSampleLink(permId)
			sampleLink.addAttribute(ATR_CODE, plasmids_list)
			if relationship_list:
				sampleLink.addAttribute(ATR_RELATIONSHIP, relationship_list)
				if relationship_list in REL_TYPES:
					connectionString = _createConnectionString(plasmids_list, relationship_list, annotation_list)
				else:
					raise ValidationException("Unknown relationship: '" + relationship_list + 
											  "'. Expected one of: " + REL_TYPES)
			if annotation_list:
				sampleLink.addAttribute(ATR_ANNOTATION, annotation_list)
		return sampleLink    

	""" MAIN FUNCTIONS """
	
	"""Example input:
	
	FRP1 (DEL:URA3), FRP2 (INT), FRP3 (MOD:URA3), FRP4
	
	Relationship types:
	- DEL: deletion
	- INT: integration
	- MOD: modification
	"""
	
	def updatePlasmidFromBatchInput(plasmids_list, relationship_list, annotation_list):
		elements = []
		input = plasmids
		if input is not None:
			plas = input.split(',')
			for p in plas:
 				(plasmids_list, g, relationship_list, annotation_list) = _group(inputPattern, p.strip())
  				sampleLink = _createPlasmidSampleLink(plasmids_list, relationship_list, annotation_list)
 				elements.append(sampleLink)
 		
 		parentsInput = yeast_parents
 		if parentsInput is not None:
 			parents = parentsInput.split(',')
			for parent in parents:
 				YeastPath= "/YLAB-TEST/" + parent.strip()
				parentPlasmids = transaction.getSample(YeastPath).getPropertyValue('PLASMIDS')
				print "parentPlasmids", parentPlasmids
 				if parentPlasmids is " ":
 					print "None:", parentPlasmids
 					continue
 				parentElements = list(propertyConverter.convertStringToElements(parentPlasmids))
 				print "pele", parentElements
 				for parentLink in parentElements:
 					elements.append(parentLink)     
 			
 		return propertyConverter.convertToString(elements)

 	
 	updatePlasmidFromBatchInput(plasmids_list, relationship_list, annotation_list)
	
 	
	def _getPlasmidCode(plasmids_list):
		elements = []
		input = plasmids_list
		if input is not None:
			for i in plasmids_list:
				plasmidPath= "/YLAB-TEST/" + i.strip()
				elements.append(plasmidPath)   
		return elements          




###IMPORT CHEMICALS####################################################################


	def _createChemicalsSampleLink(chemicals_list, concentration_list):
		"""
		   Creates sample link XML element for sample with specified 'code'. The element will contain
		   given code as 'code' attribute apart from standard 'permId' attribute.
		   
		   If the sample doesn't exist in DB a fake link will be created with the 'code' as permId.
		   
		   @return: sample link XML element as string, e.g.:
		   - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
		   - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
		"""
		if chemicals_list is not None:
		  chemicalPath= "/YLAB-TEST/" + chemicals_list
		  permId =transaction.getSample(chemicalPath).getSample().getPermId()
		  name = transaction.getSample(chemicalPath).getPropertyValue("NAME")
		  if not permId:
			permId = chemicals_list
		  sampleLink = elementFactory.createSampleLink(permId)
		
		  sampleLink.addAttribute(ATR_CODE, chemicals_list)
		  sampleLink.addAttribute(ATR_NAME, name)
		  sampleLink.addAttribute(ATR_CONC, concentration_list)
		 
		 
		return sampleLink    
	    
	
	"""
	Example input:
	
	FRC1: 2nM, FRC2, FRC3: 4nM, FRC4
	"""
	
	
	def updateChemicalsFromBatchInput(chemicals_list, concentration_list):
		elements = []
		input = chemicals_list
		input2 = concentration_list
		if input != "":
		   for i, j in zip(chemicals_list,concentration_list): #zip is used to iterate over two lists in parallel
				sampleLink = _createChemicalsSampleLink(i.strip(), j.strip())
				elements.append(sampleLink)
		return propertyConverter.convertToString(elements)


###IMPORT SOLUTION BUFFERS####################################################################


	def _createBuffersLink(buffers_list, buffers_concentration_list):
		"""
		   Creates sample link XML element for sample with specified 'code'. The element will contain
		   given code as 'code' attribute apart from standard 'permId' attribute.
		   
		   If the sample doesn't exist in DB a fake link will be created with the 'code' as permId.
		   
		   @return: sample link XML element as string, e.g.:
		   - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
		   - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
		"""
		if buffers_list is not None:
		  buffersPath= "/YLAB-TEST/" + buffers_list
		  permId =transaction.getSample(buffersPath).getSample().getPermId()
		  if not permId:
			permId = buffers_list
		  print "biffers permID", permId, "path", buffersPath	
		  name = transaction.getSample(buffersPath).getPropertyValue("NAME")
		  print "buffers name is:", name
		  sampleLink = elementFactory.createSampleLink(permId)
		
		  sampleLink.addAttribute(ATR_CODE, buffers_list)
		  sampleLink.addAttribute(ATR_NAME, name)
		  sampleLink.addAttribute(ATR_CONC, buffers_concentration_list)
		 
		return sampleLink    
	    
	
	"""
	Example input:
	
	FRC1: 2nM, FRC2, FRC3: 4nM, FRC4
	"""
	
	
	def updateBuffersFromBatchInput(buffers_list, buffers_concentration_list):
		elements = []
		input = buffers_list
		input2 = buffers_concentration_list
		if input != "":
		   for i, j in zip(buffers_list,buffers_concentration_list): #zip is used to iterate over two lists in parallel
				sampleLink = _createBuffersLink(i.strip(), j.strip())
				elements.append(sampleLink)
		return propertyConverter.convertToString(elements)
		
###IMPORT MEDIA####################################################################


	def _createMediasLink(medias_list, medias_concentration_list):
		"""
		   Creates sample link XML element for sample with specified 'code'. The element will contain
		   given code as 'code' attribute apart from standard 'permId' attribute.
		   
		   If the sample doesn't exist in DB a fake link will be created with the 'code' as permId.
		   
		   @return: sample link XML element as string, e.g.:
		   - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
		   - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
		"""
		if medias_list is not None:
		  mediasPath= "/YLAB-TEST/" + medias_list
		  permId =transaction.getSample(mediasPath).getSample().getPermId()
		  if not permId:
			permId = medias_list
		  mediaName = transaction.getSample(mediasPath).getPropertyValue("NAME")
		  print "medi name is:", mediaName
		  sampleLink = elementFactory.createSampleLink(permId)
		
		  sampleLink.addAttribute(ATR_CODE, medias_list)
		  sampleLink.addAttribute(ATR_NAME, mediaName)
		  sampleLink.addAttribute(ATR_CONC, medias_concentration_list)
		 		 
		return sampleLink    
	    
	
	"""
	Example input:
	
	FRC1: 2nM, FRC2, FRC3: 4nM, FRC4
	"""
	
	
	def updateMediasFromBatchInput(medias_list, medias_concentration_list):
		elements = []
		input = medias_list
		input2 = medias_concentration_list
		if input != "":
		   for i, j in zip(medias_list,medias_concentration_list): #zip is used to iterate over two lists in parallel
				sampleLink = _createMediasLink(i.strip(), j.strip())
				elements.append(sampleLink)
		return propertyConverter.convertToString(elements)			
		
		
###IMPORT COMMENTS####################################################################
	
	

	def _createCommentsSampleLink(comment_text_list):
		#if comment_text_list is not None:
		commentEntry = elementFactory.createElement(COMMENT_ENTRY_ELEMENT_LABEL)
		  		
		user = transaction.getUserId()
		commentEntry.addAttribute(PERSON_ATTRIBUTE, user)
		commentEntry.addAttribute(DATE_ATTRIBUTE,str(create_openbis_timestamp()))
		commentEntry.setData(comment_text_list)
		return commentEntry   
	
	def updateCommentsFromBatchInput(comment_text_list):
		elements = []
		input = comment_text_list
		#if input is not None:
		commentEntry = _createCommentsSampleLink(comment_text_list)
		elements.append(commentEntry)
		return propertyConverter.convertToString(elements)
	
	_createCommentsSampleLink(comment_text_list)
	

###CREATE New sample with related properties#################################################

	newSampleIdentifier="/YLAB-TEST/" + sample_name
	newSample=transaction.createNewSample(newSampleIdentifier,'YEAST')
	
	
	exp = transaction.getExperiment(experiment_name)
	newSample.setExperiment(exp)
	
	parents = _getPlasmidCode(plasmids_list) +  _getYeastCode(yeast_parents)
	print "parents =", parents
	newSample.setParentSampleIdentifiers(parents)
	
	for child in root:
 		if child.tag == "Yeast_Parents":
			newSample.setPropertyValue("YEAST_PARENTS", updateYeastFromBatchInput(yeast_parents))
  		if child.tag == "Plasmids":
  			newSample.setPropertyValue("PLASMIDS",updatePlasmidFromBatchInput(plasmids_list, relationship_list, annotation_list))
		if child.tag == "Chemicals":
			newSample.setPropertyValue("CHEMICALS",updateChemicalsFromBatchInput(chemicals_list,concentration_list))
		if child.tag == "Solutions_Buffers":
			newSample.setPropertyValue("SOLUTIONS_BUFFERS",updateBuffersFromBatchInput(buffers_list,buffers_concentration_list))
		if child.tag == "Media":
			newSample.setPropertyValue("Media",updateMediasFromBatchInput(medias_list,medias_concentration_list))
  		if child.tag == "XMLCOMMENTS":
  			newSample.setPropertyValue("XMLCOMMENTS", updateCommentsFromBatchInput(comment_text_list))
		if child.tag != "Identifier" and child.tag !="Experiment" and child.tag != "Yeast_Parents" and child.tag != "Plasmids" and child.tag != "Chemicals" and child.tag != "XMLCOMMENTS" and child.tag != "Solutions_Buffers" and child.tag != "Media":
			if child.text != None:
				newSample.setPropertyValue(child.tag, child.text)
  			else:
				child.text= ""
				newSample.setPropertyValue(child.tag, child.text)