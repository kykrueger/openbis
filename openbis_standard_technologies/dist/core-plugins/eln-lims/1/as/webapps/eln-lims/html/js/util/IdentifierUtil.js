var IdentifierUtil = new function() {
	this.isProjectSamplesEnabled = false;
	
	this.getSampleIdentifier = function(spaceCode, projectCode, sampleCode) {
		return ('/' + spaceCode + '/' + ((this.isProjectSamplesEnabled)?projectCode + '/':'') + sampleCode);
	}
	
	this.getProjectIdentifierFromExperimentIdentifier = function(experimentIdentifier) {
		return ('/' + this.getSpaceCodeFromIdentifier(experimentIdentifier) + '/' + this.getProjectCodeFromExperimentIdentifier(experimentIdentifier));
	}
	
	this.getExperimentIdentifier = function(spaceCode, projectCode, experimentCode) {
		return ('/' + spaceCode + '/' + projectCode + "/" + experimentCode);
	}
	
	this.getSpaceCodeFromIdentifier = function(identifier) {
		return identifier.split("/")[1];
	};
	
	this.getProjectCodeFromExperimentIdentifier = function(experimentIdentifier) {
		return experimentIdentifier.split("/")[2];
	};
	
	this.getExperimentCodeFromExperimentIdentifier = function(experimentIdentifier) {
		return experimentIdentifier.split("/")[3];
	};
}