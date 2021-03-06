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

function DataSetFormModel(mode, entity, dataSet, isMini, dataSetV3) {
	this.mode = mode;
	this.isMini = isMini;
	this.isAutoUpload = true;
	this.isFormDirty = false;
	
	this.entity = entity;
	
	this.isExperiment = function() {
		return this.entity && this.entity["@type"] === "as.dto.experiment.Experiment";
	}
	
	if(!dataSet) {
		this.dataSet = { properties : {} };
	} else {
		this.dataSet = dataSet;
		this.dataSetV3 = dataSetV3;
	}
	this.linkedData = null;
	
	this.dataSetTypes = null;
	this.files = [];
	
	this.datasetParentsComponent = null; 
}
