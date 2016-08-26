/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function LinksModel(title, sampleTypeHints, isDisabled, showInfo, disableAddAnyType) {
	this.title = title;
	this.sampleTypeHints = sampleTypeHints;
	this.isDisabled = isDisabled;
	this.showInfo = showInfo;
	this.disableAddAnyType = disableAddAnyType;
	this.samplesRemoved = [];
	this.samplesByType = {};
	this.stateObj = {};
	
 	this.writeState = function(sample, propertyTypeCode, propertyValue, isDelete) {
		this.loadState();
		
		if(isDelete) {
			FormUtil.deleteAnnotationsFromPermId(this.stateObj, sample.permId);
		} else {
			FormUtil.writeAnnotationForSample(this.stateObj, sample, propertyTypeCode, propertyValue);
		}
		
		var xmlDoc = FormUtil.getXMLFromAnnotations(this.stateObj);
		mainController.currentView._sampleFormModel.sample.properties["ANNOTATIONS_STATE"] = xmlDoc;
	}
	
	this.readState = function(permId, propertyTypeCode) {
		if(this.stateObj[permId] && this.stateObj[permId][propertyTypeCode]) {
			return this.stateObj[permId][propertyTypeCode];
		}
		return null;
	}
	
	this.loadState = function() {
		var isStateFieldAvailable = false;
		
		if(mainController.currentView._sampleFormModel.sample) {
			var availableFields = profile.getAllPropertiCodesForTypeCode(mainController.currentView._sampleFormModel.sample.sampleTypeCode);
			var pos = $.inArray("ANNOTATIONS_STATE", availableFields);
			isStateFieldAvailable = (pos !== -1);
		}
		
		if(!isStateFieldAvailable) {
			if(this.sampleTypeHints && this.sampleTypeHints.length !== 0) { //Indicates annotations are needed
				Util.showError("You need a property with code ANNOTATIONS_STATE on this entity to store the state of the annotations.");
			}
		} else {
			//Update Values
			this.stateObj = FormUtil.getAnnotationsFromSample(mainController.currentView._sampleFormModel.sample);
		}
	}
	
	this.loadState();
}