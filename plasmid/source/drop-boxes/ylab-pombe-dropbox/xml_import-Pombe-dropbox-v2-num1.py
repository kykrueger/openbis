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


ATR_NAME="name"
ATR_CODE = "code"
ATR_CONC = "concentration"
DATE_ATTRIBUTE = 'date'
PERSON_ATTRIBUTE = 'person'
ATR_RELATIONSHIP = "rel"
ATR_ANNOTATION = "annotation"

NAME_LABEL='name'
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

pombe_parents_list=[]
date_list=[]
person_list=[]
comment_text_list=[]
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
	
	
	# for path in [ './Yeast_Parents']:
	#   node = tree.find(path)
 # 	  if node.text != "":
	# 	yeast_parents = node.text
	#   else:
	#     node.text = ""  

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
	 
	def PombeParents():
	  for path in [ './Pombe_Parents']:
		node = tree.find(path)
		if node.text is not None:
		  pombe_parents = node.text
		  tokens = pombe_parents.split(',')
		  for token in tokens:
		    if re.search(":", token): 
			  token = token.split(':')
			  pombe_name = token[0]
			  pombe_parents_list.append(pombe_name)
		    else:
			  pombe_name=token  
			  pombe_concentration = "n.a."
			  pombe_parents_list.append(pombe_name)
		else:
		  node.text = "n.a." 	  
	  return pombe_parents	  
	
	PombeParents()

	
	for path in [ './XMLCOMMENTS']:
	  node = tree.find(path)
	  if node.text is not None:
		comment_text_list= node.text
	  else:
	    comment_text_list = None 


	elementFactory = ElementFactory()
	
	propertyConverter = XmlStructuredPropertyConverter(elementFactory);




###IMPORT POMBE PARENTS####################################################################


	def _createpombe_parentsSampleLink(pombe_parents_list):

		if pombe_parents_list is not None:
		  pombePath= "/YEAST_LAB/" + pombe_parents_list
		  permId =transaction.getSample(pombePath).getSample().getPermId()
		  name = transaction.getSample(pombePath).getPropertyValue("NAME")
		  if not permId:
			permId = pombe_parents_list
		  sampleLink = elementFactory.createSampleLink(permId)
		  sampleLink.addAttribute(ATR_CODE, pombe_parents_list)
		  sampleLink.addAttribute(ATR_NAME, name)
		 
  
		return sampleLink    
	    
	
	"""
	Example input:
	
	FRSP1, FRSP2
	"""
	
	
	def updatepombe_parentsFromBatchInput(pombe_parents_list):
		elements = []
		input = pombe_parents_list
		if input != "":
		   for i in pombe_parents_list: 
				sampleLink = _createpombe_parentsSampleLink(i.strip())
				elements.append(sampleLink)
		return propertyConverter.convertToString(elements)

	def _getPombeCode(pombe_parents_list):
		elements = []
		input = pombe_parents_list
		if input is not None:
			samples = input.split(',')
			for pombe_parents_list in samples:
				ParentsPath= "/YEAST_LAB/" + pombe_parents_list.strip()
				elements.append(ParentsPath)   
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
			plasmidPath= "/YEAST_LAB/" + plasmids_list.strip()
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
 		
 		parentsInput = pombe_parents_list
 		if parentsInput is not None:
 			parents = parentsInput.split(',')
			for parent in parents:
 				ParentsPath= "/YEAST_LAB/" + parent.strip()
				parentPlasmids = transaction.getSample(ParentsPath).getPropertyValue('PLASMIDS')

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
				plasmidPath= "/YEAST_LAB/" + i.strip()
				elements.append(plasmidPath)   
		return elements          


		
		
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

	newSampleIdentifier="/YEAST_LAB/" + sample_name
	newSample=transaction.createNewSample(newSampleIdentifier,'POMBE')
	
	
	exp = transaction.getExperiment(experiment_name)
	newSample.setExperiment(exp)
	
	parents = _getPlasmidCode(plasmids_list) +  _getPombeCode(pombe_parents_list)
	print "parents =", parents
	newSample.setParentSampleIdentifiers(parents)
	
	for child in root:
 		if child.tag == "Plasmids":
  			newSample.setPropertyValue("PLASMIDS",updatePlasmidFromBatchInput(plasmids_list, relationship_list, annotation_list))
		if child.tag == "Pombe_Parents":
			newSample.setPropertyValue("POMBE-PARENTS",updatepombe_parentsFromBatchInput(pombe_parents_list))
		if child.tag == "XMLCOMMENTS":
  			newSample.setPropertyValue("XMLCOMMENTS", updateCommentsFromBatchInput(comment_text_list))
		if child.tag != "Identifier" and child.tag !="Experiment" and child.tag != "Pombe_Parents" and child.tag != "Plasmids" and child.tag != "XMLCOMMENTS":
			if child.text != None:
				newSample.setPropertyValue(child.tag, child.text)
  			else:
				child.text= ""
				newSample.setPropertyValue(child.tag, child.text)
