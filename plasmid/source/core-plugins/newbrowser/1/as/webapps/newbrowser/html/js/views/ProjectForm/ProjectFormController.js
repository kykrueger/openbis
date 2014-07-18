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

function ProjectFormController(mainController, mode, project) {
	this._mainController = mainController;
	this._projectFormModel = new ProjectFormModel(mode, project);
	this._projectFormView = new ProjectFormView(this, this._projectFormModel);
	
	this.init = function($container) {
		this._projectFormView.repaint($container);
	}
	
	this.createNewExperiment = function(experimentTypeCode) {
		var argsMap = {
				"experimentTypeCode" : experimentTypeCode,
				"projectIdentifier" : "/" + this._projectFormModel.project.spaceCode + "/" + this._projectFormModel.project.code
		}
		var argsMapStr = JSON.stringify(argsMap);
		
		this._mainController.changeView("showCreateExperimentPage", argsMapStr);
	}
	
	this.enableEditing = function() {
		
	}
}