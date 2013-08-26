function DefaultProfile() {
	this.skeuomorphism = true;
	//
	// DEFAULTS, TYPICALLY DON'T TOUCH IF YOU DON'T KNOW WHAT YOU DO
	//
	this.openbisUrl = 'https://localhost:8443/';
	this.dssUrl = 'https://localhost:8444/datastore_server';
	
	this.ELNExperiment = "SYSTEM_EXPERIMENT";
	this.notShowTypes = ["SYSTEM_EXPERIMENT"];
	
	this.searchType = {
		"TYPE" : "SEARCH",
		"DISPLAY_NAME" : "Search",
		"SAMPLE_TYPE_PROPERTIES" : ["TYPE", "MATCHED_TEXT", "MATCHED_FIELD"],
		"SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME" : ["Sample Type", "Matched Text", "Matching Field"],	
	};
	
	this.allTypes = [];
	this.typeGroups = {
		"OTHERS" : {
			"TYPE" : "OTHERS",
			"DISPLAY_NAME" : "Others",
			"LIST" : [] //All types not present in other groups and not in notShowTypes, is a box where everything that is not configured goes by default
		}
	};
	
	this.typePropertiesForTable = {};
	
	this.colorForInspectors = {};
	
	this.searchSorter = function(searchResults) {
		return searchResults;
	}
	//
	// Per Lab Extensions
	//
	this.inspectorContentTransformer = function(sample, propertyCode, propertyContent) {
		return {
				"isSingleColumn" : false,
				"content" : propertyContent
		};
	}
	
	this.inspectorContentExtra = function(sample, propertyContent) {
		return "";
	}
	
	//
	// Utility methods used to navigate the configuration easily
	//
	
	this.getTypeForTypeCode = function(typeCode) {
		for(var i = 0; i < this.allTypes.length; i++) {
			if(this.allTypes[i].code === typeCode) {
				return this.allTypes[i];
			}
		}
		return null;
	}
	
	this.getGroupTypeCodeForTypeCode = function(typeCode) {
		for(typeGroupCode in this.typeGroups) {
			for(var i = 0; i < this.typeGroups[typeGroupCode]["LIST"].length; i++) {
				var sampleTypeCode = this.typeGroups[typeGroupCode]["LIST"][i];
				if(sampleTypeCode === typeCode) {
					return this.typeGroups[typeGroupCode]["TYPE"];
				}
			}
		}
		return null;
	}
	
	this.getAllPropertiCodesForTypeCode = function(typeCode) {
		var allPropertiCodes = new Array();
		var sampleType = this.getTypeForTypeCode(typeCode);
		for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
			var propertyGroup = sampleType.propertyTypeGroups[i].propertyTypes;
			for(var j = 0; j < propertyGroup.length; j++) {
				var propertyType = propertyGroup[j];
				allPropertiCodes.push(propertyType.code);
			}
		}
		return allPropertiCodes;
	}
	
	this.getPropertiesDisplayNamesForTypeCode = function(sampleTypeCode, propertiesTypeCode) {
		var allPropertiDisplayNames = new Array();
		var sampleType = this.getTypeForTypeCode(sampleTypeCode);
		for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
			var propertyGroup = sampleType.propertyTypeGroups[i].propertyTypes;
			for(var j = 0; j < propertyGroup.length; j++) {
				var propertyType = propertyGroup[j];
				if($.inArray(propertyType.code, propertiesTypeCode) !== -1) {
					allPropertiDisplayNames.push(propertyType.label);
				}
			}
		}
		return allPropertiDisplayNames;
	}
	
	//
	// Initializes the Others list with all sampleType codes that are neither in typeGroups or notShowTypes
	//
	this.init = function() {
		for(var i = 0; i < this.allTypes.length; i++) {
			var sampleType = this.allTypes[i];
			if($.inArray(sampleType.code, this.notShowTypes) === -1) {
				if(this.getGroupTypeCodeForTypeCode(sampleType.code) === null) {
					this.typeGroups["OTHERS"]["LIST"].push(sampleType.code);
				}
			}
		}
	}
}

//YEASTLAB PROFILE
function YeastLabProfile() {
	//this.openbisUrl = 'https://openbis-lims.ethz.ch/';
	//this.dssUrl = 'https://openbis-lims.ethz.ch/datastore_server';
	this.notShowTypes = ["SYSTEM_EXPERIMENT", "ILLUMINA_FLOW_CELL", "ILLUMINA_FLOW_LANE", "LIBRARY", "LIBRARY_POOL", "MASTER_SAMPLE","MS_INJECTION","RAW_SAMPLE","TEMPLATE_SAMPLE", "SEARCH"];
	this.typeGroups = {
		"METHODS" : {
			"TYPE" : "METHODS",
			"DISPLAY_NAME" : "Methods",
			"LIST" : ["GENERAL_PROTOCOL", "PCR", "WESTERN_BLOTTING"]
		},
		"MATERIALS" : {
			"TYPE" : "MATERIALS",
			"DISPLAY_NAME" : "Materials",
			"LIST" : ["CHEMICAL", "ANTIBODY", "MEDIA", "SOLUTIONS_BUFFERS", "ENZYME", "OLIGO"]
		},
		"SAMPLES" : {
			"TYPE" : "SAMPLES",
			"DISPLAY_NAME" : "Samples",
			"LIST" : ["PLASMID", "YEAST", "BACTERIA"]
		},
		"OTHERS" : {
			"TYPE" : "OTHERS",
			"DISPLAY_NAME" : "Others",
			"LIST" : [] 
		}
	};
	
	this.typePropertiesForTable = {
		"GENERAL_PROTOCOL" : ["NAME", "FOR_WHAT", "PROTOCOL_TYPE", "PUBLICATION"],
		"PCR" : ["NAME", "FOR_WHAT", "TEMPLATE", "PUBLICATION"],
		"WESTERN_BLOTTING" : ["NAME", "FOR_WHAT", "STORAGE", "PUBLICATION"],
		"CHEMICAL" : ["NAME", "SUPPLIER", "ARTICLE_NUMBER", "LOCAL_ID", "STORAGE"],
		"ANTIBODY" : ["NAME", "STORAGE", "HOST", "FOR_WHAT"],
		"MEDIA" : ["NAME", "STORAGE", "FOR_WHAT", "ORGANISM"],
		"SOLUTIONS_BUFFERS" : ["NAME", "STORAGE", "FOR_WHAT"],
		"ENZYME" : ["NAME", "SUPPLIER", "ARTICLE_NUMBER", "KIT"],
		"OLIGO" : ["TARGET", "DIRECTION", "RESTRICTION_ENZYME", "PROJECT"],
		"PLASMID" : ["OWNER", "OWNER_NUMBER", "PLASMID_NAME", "BACTERIAL_ANTIBIOTIC_RESISTANCE", "YEAST_MARKER"],
		"YEAST" : ["OWNER", "OWNER_NUMBER", "YEAST_STRAIN_NAME", "PROJECT", "GENETIC_BACKGROUND", "MATING_TYPE"],
		"BACTERIA" : ["BACTERIA_STRAIN_NAME", "BACTERIA_GENOTYPE", "WHAT_FOR", "SUPPLIER", "ARTICLE_NUMBER", "COMMENTS"]
	}
	
	this.colorForInspectors = {
		"GENERAL_PROTOCOL" : "#CCFFCC",
		"PCR" : "#CCFFCC",
		"WESTERN_BLOTTING" : "#CCFFCC",
		"CHEMICAL" : "#A8A8A8",
		"ANTIBODY" : "#A8A8A8",
		"MEDIA" : "#A8A8A8",
		"SOLUTIONS_BUFFERS" : "#A8A8A8",
		"ENZYME" : "#A8A8A8",
		"BACTERIA" : "#A8A8A8",
		"OLIGO" : "#0099CC",
		"PLASMID" : "#FFCB99",
		"YEAST" : "#CCCC99"
	};
	
	this.getHTMLTableFromXML = function(xmlDocument) {
		var table_head = null;
		var table_body = "";
		var dom;
	
		if (window.DOMParser) {
		  parser = new DOMParser();
		  dom = parser.parseFromString(xmlDocument,"text/xml");
		} else {// Internet Explorer
		  dom = new ActiveXObject("Microsoft.XMLDOM");
		  dom.async = false;
		  dom.loadXML(xmlDocument); 
		} 
	
		var html = null;
		var root = dom.childNodes[0];
		var children = root.childNodes;
		for(var i = 0; i < children.length; i++) {
			var child = children[i];
			if (child.localName != null) {
				var keys = child.attributes;
				if (table_head == null) {
					table_head = "<tr>";
					var key = null;
					for (var j = 0; j < keys.length; j++) {
						key = keys[j];
						if(key.localName != "permId") {
							table_head += "<th style='text-align:left; width:" + (100/(keys.length-1)) + "%;'>"+ key.localName + "</th>";
						}
					}
					table_head += "</tr>";
				}
				table_body += "<tr>";
				for (var j = 0; j < keys.length; j++) {
					key = keys[j];
					if(key.localName != "permId") {
						if(key.localName == "date") {
							table_body += "<td style='text-align:left; width:" + (100/(keys.length-1)) + "%;'>" + key.value + "</td>";
						} else {
							table_body += "<td style='text-align:left; width:" + (100/(keys.length-1)) + "%;'>" + key.value + "</td>";
						}
					}
				}
				table_body += "</tr>";
			}
		}
		html = "<table style='font-family:helvetica; font-size:90%; width: 100%;'><thead>" + table_head + "</thead><tbody>"+ table_body + "</tbody></table>";
		return html;
	}
	
	this.inspectorContentTransformer = function(sample, propertyCode, propertyContent) {
		if(propertyContent.indexOf("<root>") != -1) {
			return {
				"isSingleColumn" : true,
				"content" : this.getHTMLTableFromXML(propertyContent)
			};
		} else {
			return {
				"isSingleColumn" : false,
				"content" : propertyContent
			};
		}
	}
	
	this.searchSorter = function(searchResults) {
		
		var getChars = function(code) {
			var theChars = code.replace(/[0-9]/g, '')
			return theChars;
		}
		
		var getNums = function(code) {
			var thenum = code.replace( /^\D+/g, '');
			if(thenum.length > 0) {
				return parseInt(thenum);
			} else {
				return 0;
			}
		}
		
		var customSort = function(sampleA, sampleB){
			var aCode = getChars(sampleA.code);
			var bCode = getChars(sampleB.code);
			
			if(aCode < bCode) {
				return -1;
			} else if(aCode > bCode) {
				return 1;
			} else {
				var aNum = getNums(sampleA.code);
				var bNum = getNums(sampleB.code);
				return aNum - bNum;
			}
		}
		
		var sortedResults = searchResults.sort(customSort);
		
		return sortedResults;
	}
	
	//TO-DO To FIX
	this.inspectorContentExtra = function(extraContainerId, sample) {
		// When requesting information about the sample, we don't need parents
		// and children, so send a copy of the saple without that information.
		var sampleToSend = $.extend({}, sample);
		delete sampleToSend.parents;
		delete sampleToSend.children; 
		
		openbisServer.listDataSetsForSample(sampleToSend, true, function(datasets) {
			for(var i = 0; i < datasets.result.length; i++) {
				if(datasets.result[i].dataSetTypeCode === "SEQ_FILE") {
					openbisServer.listFilesForDataSet(datasets.result[i].code, "/", true, function(files) {
						for(var i = 0; i < files.result.length; i++) {
							if(!files.result[i].isDirectory) {
								var pathInDataSet = files.result[i].pathInDataSet;
								var downloadUrl = profile.dssUrl + '/' + dataset.code + "/" + pathInDataSet + "?sessionID=" + openbisServer.sessionToken;
								var imageTag = "<img src='" + downloadUrl + "'/>"
								$("#" + extraContainerId).append(imageTag);
							}
						}
					});
				}
			}
		}
		);
		return "";
	}
};

YeastLabProfile.prototype = new DefaultProfile();