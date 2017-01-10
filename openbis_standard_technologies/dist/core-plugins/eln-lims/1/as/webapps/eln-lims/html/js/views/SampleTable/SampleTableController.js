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

function SampleTableController(parentController, title, experimentIdentifier, projectPermId, showInProjectOverview, experiment) {
	this._parentController = parentController;
	this._sampleTableModel = new SampleTableModel(title, experimentIdentifier, projectPermId, showInProjectOverview, experiment);
	this._sampleTableView = new SampleTableView(this, this._sampleTableModel);
	
	this.init = function($container) {
		var _this = this;
		Util.blockUI();
		
		var callback = function() {
			_this._sampleTableView.repaint($container);
			//Util.unblockUI();
		};
		
		if(this._sampleTableModel.experimentIdentifier || projectPermId) {
			this._loadExperimentData(callback);
		} else {
			callback();
		}
	}
	
	this._loadExperimentData = function(callback) {
		var _this = this;
		
		var properyKeyValueList = null;
		if (this._sampleTableModel.showInProjectOverview) {
			properyKeyValueList = [{ "SHOW_IN_PROJECT_OVERVIEW" : "\"true\"" }];
		}
		
		//
		var advancedSampleSearchCriteria = null;
		
		if(this._sampleTableModel.experiment) {
			advancedSampleSearchCriteria = {
					entityKind : "SAMPLE",
					logicalOperator : "AND",
					rules : { "1" : { type : "Experiment", name : "ATTR.PERM_ID", value : this._sampleTableModel.experiment.permId } }
			}
		} else if(this._sampleTableModel.projectPermId) {
			advancedSampleSearchCriteria = {
					entityKind : "SAMPLE",
					logicalOperator : "AND",
					rules : { "1" : { type : "Experiment", name : "ATTR.PROJECT_PERM_ID", value : this._sampleTableModel.projectPermId } }
			}
		}
		
		if(this._sampleTableModel.showInProjectOverview) {
			advancedSampleSearchCriteria.rules["2"] = { type : "Property", name : "SHOW_IN_PROJECT_OVERVIEW", value : "true" };
		}
		//
		callback();
		this._reloadTableWithAllSamples(advancedSampleSearchCriteria);
	}
	
	this._reloadTableWithAllSamples = function(advancedSampleSearchCriteria) {
			//Create and display table
			var dataGridController = SampleDataGridUtil.getSampleDataGrid(this._sampleTableModel.experimentIdentifier, advancedSampleSearchCriteria, null, null, null, null, null, null, true);
			
			
			var extraOptions = [];
			extraOptions.push({ name : "Delete selected", action : function(selected) {
				var grid = dataGridController._grid;
				var selected = grid.getSelected();
				if(selected != undefined && selected.length == 0){
					Util.showError("Please select at least one sample to delete!");
				} else {
					var warningText = "The next " + ELNDictionary.samples + " will be deleted: ";
					var samplePermIds = [];
					for(var sIdx = 0; sIdx < selected.length; sIdx++) {
						samplePermIds.push(selected[sIdx].permId);
						warningText += selected[sIdx].identifier + " ";
					}
					
					var modalView = new DeleteEntityController(function(reason) {
						mainController.serverFacade.deleteSamples(samplePermIds, reason, function(data) {
							if(data.error) {
								Util.showError(data.error.message);
							} else {
								Util.showSuccess("" + ELNDictionary.Sample + "/s Deleted");
								mainController.refreshView();
							}
						});
					}, true, warningText);
					modalView.init();
				}
			}});
			
			dataGridController.init(this._sampleTableView.getTableContainer(), extraOptions);
	}
}