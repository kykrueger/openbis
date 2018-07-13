var IdentifierUtil = new function() {
	this.isProjectSamplesEnabled = false;
	
	//
	// Identifier Building
	//
	
	this.getProjectIdentifier = function(spaceCode, projectCode) {
		return ('/' + spaceCode + '/' + projectCode);
	}
	
	this.getExperimentIdentifier = function(spaceCode, projectCode, experimentCode) {
		return ('/' + spaceCode + '/' + projectCode + '/' + experimentCode);
	}
	
	this.getSampleIdentifier = function(spaceCode, projectCodeOrNull, sampleCode) {
		return ('/' + spaceCode + '/' + ((projectCodeOrNull && this.isProjectSamplesEnabled)?projectCodeOrNull + '/':'') + sampleCode);
	}
	
	//
	// All Identifier Parsing
	//
	
	this.getSpaceCodeFromIdentifier = function(identifier) {
		var identifierParts = identifier.split('/');
		var spaceCode;
		if(identifierParts.length > 2) { //If has less parts, is a shared sample
			spaceCode = identifierParts[1];
		}
		return spaceCode;
	};
	
	//
	// Sample Identifier Parsing
	//
	
	this.getProjectCodeFromSampleIdentifier = function(sampleIdentifier) {
		var projectCode;
		var sampleIdentifierParts = sampleIdentifier.split('/');
		if(sampleIdentifierParts.length === 4) {
			projectCode = sampleIdentifierParts[2];
		}
		return projectCode;
	}
	
	this.getSampleCodeFromSampleIdentifier = function(sampleIdentifier) {
		var sampleIdentifierParts = sampleIdentifier.split('/');
		return sampleIdentifierParts[sampleIdentifierParts.length - 1];
	}
	
	this.getContainerSampleIdentifierFromContainedSampleIdentifier = function(sampleIdentifier) {
		var containerSampleIdentifier;
		var containerIdentifierEnd = sampleIdentifier.lastIndexOf(':');
		if(containerIdentifierEnd !== -1) {
			containerSampleIdentifier = sampleIdentifier.substring(0, containerIdentifierEnd);
		}
		return containerSampleIdentifier;
	}
	
	//
	// Experiment Identifier Parsing
	//
	
	this.getProjectIdentifierFromExperimentIdentifier = function(experimentIdentifier) {
		return ('/' + this.getSpaceCodeFromIdentifier(experimentIdentifier) + '/' + this.getProjectCodeFromExperimentIdentifier(experimentIdentifier));
	}
	
	this.getProjectCodeFromExperimentIdentifier = function(experimentIdentifier) {
		return experimentIdentifier.split('/')[2];
	};
	
	this.getExperimentCodeFromExperimentIdentifier = function(experimentIdentifier) {
		return experimentIdentifier.split('/')[3];
	};
}