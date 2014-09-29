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

function ExperimentFormView(experimentFormController, experimentFormModel) {
	this._experimentFormController = experimentFormController;
	this._experimentFormModel = experimentFormModel;
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		
		var $form = $("<div>", { "class" : "row"});
		
		var $formColumn = $("<form>", { 
			"class" : FormUtil.formColumClass + " form-horizontal", 
			'role' : "form",
			'action' : 'javascript:void(0);',
			'onsubmit' : 'mainController.currentView.updateExperiment();'
		});
		
		$form.append($formColumn);
		
		//
		// Title
		//
		var $formTitle = null;
		if(this._experimentFormModel.mode === FormMode.VIEW || this._experimentFormModel.mode === FormMode.EDIT) {
			$formTitle = $("<h2>").append("Experiment " + this._experimentFormModel.experiment.identifier);
		} else {
			$formTitle = $("<h2>").append("Create " + this._experimentFormModel.experiment.experimentTypeCode);
		}
		$formColumn.append($formTitle);
		
		//
		// Create Sub Experiment
		//
		if(this._experimentFormModel.mode === FormMode.VIEW) {
			var $createSubExpBtn = $("<a>", { "class" : "btn btn-default"}).append("Create Sub Experiment");
			$createSubExpBtn.click(function() {
				var $dropdown = FormUtil.getSampleTypeDropdown("sampleTypeDropdown", true);
				Util.blockUI("Select the type for the sub Experiment: <br><br>" + $dropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='sampleTypeDropdownCancel'>Cancel</a>");
				
				$("#sampleTypeDropdown").on("change", function(event) {
					var sampleTypeCode = $("#sampleTypeDropdown")[0].value;
					var argsMap = {
							"sampleTypeCode" : sampleTypeCode,
							"experimentIdentifier" : _this._experimentFormModel.experiment.identifier
					}
					var argsMapStr = JSON.stringify(argsMap);
					
					Util.unblockUI();
					mainController.changeView("showCreateSubExperimentPage", argsMapStr);
				});
				
				$("#sampleTypeDropdownCancel").on("click", function(event) { 
					Util.unblockUI();
				});
			});
			$formTitle.append(" ");
			$formTitle.append($createSubExpBtn);
			
			var $editBtn = $("<a>", { "class" : "btn btn-default"}).append("<span class='glyphicon glyphicon-edit'></span> Enable Editing");
			$editBtn.click(function() {
				mainController.changeView("showEditExperimentPageFromIdentifier", _this._experimentFormModel.experiment.identifier);
			});
			$formTitle.append(" ");
			$formTitle.append($editBtn);
		}
		
		//
		// Metadata Identification
		//
		var $identificationInfo = $('<div>').append($('<legend>').text("Identification Info"));
		$formColumn.append($identificationInfo);
		
		var identifierParts = this._experimentFormModel.experiment.identifier.split("/");
		
		$formColumn.append(FormUtil.getFieldForLabelWithText("Type", this._experimentFormModel.experiment.experimentTypeCode));
		$formColumn.append(FormUtil.getFieldForLabelWithText("Project", "/" + identifierParts[1] + "/" + identifierParts[2]));
		var $projectField = FormUtil._getInputField("text", null, "project", null, true);
		$projectField.val("/" + identifierParts[1] + "/" + identifierParts[2]);
		$projectField.hide();
		$formColumn.append($projectField);
		
		if(this._experimentFormModel.mode === FormMode.VIEW || this._experimentFormModel.mode === FormMode.EDIT) {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Code", this._experimentFormModel.experiment.code));
			
			var $codeField = FormUtil._getInputField("text", null, "code", null, true);
			$codeField.val(identifierParts[3]);
			$codeField.hide();
			$formColumn.append($codeField);
			
			$formColumn.append(FormUtil.getFieldForLabelWithText("Registrator", this._experimentFormModel.experiment.registrationDetails.userId));
			$formColumn.append(FormUtil.getFieldForLabelWithText("Registration Date", (new Date(this._experimentFormModel.experiment.registrationDetails.registrationDate)).toLocaleString()));
		} else if(this._experimentFormModel.mode === FormMode.CREATE){
			var $codeField = FormUtil._getInputField("text", null, "code", null, true);
			$codeField.keyup(function() {
				$(this).val($(this).val().toUpperCase());
				_this._experimentFormModel.experiment.code = $(this).val();
				
				//Full Identifier
				var currentIdentifier = _this._experimentFormModel.experiment.identifier.split("/");
				var experimentIdentifier = "/" + currentIdentifier[1] + "/" + currentIdentifier[2] + "/" + _this._experimentFormModel.experiment.code;
				_this._experimentFormModel.experiment.identifier = experimentIdentifier;
			})
			var $codeFieldRow = FormUtil.getFieldForComponentWithLabel($codeField, "Code");
			$formColumn.append($codeFieldRow);
			
			mainController.serverFacade.getProjectFromIdentifier($projectField.val(), function(project) {
				delete project["@id"];
				delete project["@type"];
				mainController.serverFacade.listExperiments([project], function(data) {
					var autoGeneratedCode = identifierParts[1] + "_EXP_" + (data.result.length + 1);
					$codeField.val(autoGeneratedCode);
					_this._experimentFormModel.experiment.code = autoGeneratedCode;
					
					//Full Identifier
					var currentIdentifier = _this._experimentFormModel.experiment.identifier.split("/");
					var experimentIdentifier = "/" + currentIdentifier[1] + "/" + currentIdentifier[2] + "/" + _this._experimentFormModel.experiment.code;
					_this._experimentFormModel.experiment.identifier = experimentIdentifier;
				});
			});
			
			if(profile.hideCodes) {
				$codeFieldRow.hide();
			}
		}
		
		//
		// Metadata Fields
		//
		var experimentType = mainController.profile.getExperimentTypeForExperimentTypeCode(this._experimentFormModel.experiment.experimentTypeCode);
		
		for(var i = 0; i < experimentType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = experimentType.propertyTypeGroups[i];
			
			var $fieldset = $('<div>');
			var $legend = $('<legend>'); 
			$fieldset.append($legend);
			
			if((propertyTypeGroup.name !== null) && (propertyTypeGroup.name !== "")) {
				$legend.text(propertyTypeGroup.name);
			} else if((i === 0) || ((i !== 0) && (experimentType.propertyTypeGroups[i-1].name !== null) && (experimentType.propertyTypeGroups[i-1].name !== ""))) {
				$legend.text("Metadata");
			} else {
				$legend.remove();
			}
			
			for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
				var propertyType = propertyTypeGroup.propertyTypes[j];
				
				var $controlGroup =  null;
				
				var value = this._experimentFormModel.experiment.properties[propertyType.code];
				var isSystemProperty = false;
				if(!value && propertyType.code.charAt(0) === '$') {
					value = this._experimentFormModel.experiment.properties[propertyType.code.substr(1)];
					isSystemProperty = true;
				}
				
				if(this._experimentFormModel.mode === FormMode.VIEW) { //Show values without input boxes if the form is in view mode
					$controlGroup = FormUtil.getFieldForLabelWithText(propertyType.label, value);
				} else {
					var $component = FormUtil.getFieldForPropertyType(propertyType);
					//Update values if is into edit mode
					if(this._experimentFormModel.mode === FormMode.EDIT) {
						if(propertyType.dataType === "BOOLEAN") {
							$($component.children()[0]).prop('checked', value === "true");
						} else if(propertyType.dataType === "TIMESTAMP") {
							$($($component.children()[0]).children()[0]).val(value);
						} else {
							$component.val(value);
						}
					}
					
					var changeEvent = function(propertyType, isSystemProperty) {
						return function() {
							var propertyTypeCode = null;
							if(isSystemProperty) {
								propertyTypeCode = propertyType.code.substr(1);
							} else {
								propertyTypeCode = propertyType.code;
							}
							_this._experimentFormModel.isFormDirty = true;
							var field = $(this);
							if(propertyType.dataType === "BOOLEAN") {
								_this._experimentFormModel.experiment.properties[propertyTypeCode] = field.children()[0].checked;
							} else if (propertyType.dataType === "TIMESTAMP") {
								var timeValue = $($(field.children()[0]).children()[0]).val();
								_this._experimentFormModel.experiment.properties[propertyTypeCode] = timeValue;
							} else {
								_this._experimentFormModel.experiment.properties[propertyTypeCode] = Util.getEmptyIfNull(field.val());
							}
						}
					}
					
					//Avoid modifications in properties managed by scripts
					if(propertyType.managed || propertyType.dinamic) {
						$component.prop('disabled', true);
					}
					
					$component.change(changeEvent(propertyType, isSystemProperty));
					$controlGroup = FormUtil.getFieldForComponentWithLabel($component, propertyType.label);
				}
				
				$fieldset.append($controlGroup);
			}
			
			$formColumn.append($fieldset);
		}
		
		if(this._experimentFormModel.mode === FormMode.VIEW || this._experimentFormModel.mode === FormMode.EDIT) {
			var $subExperiments = $('<div>').append($('<legend>').text("Sub Experiments"));
			var $subExperimentsList = $('<div>', { "id" : "samplesDataSource" });
			$subExperiments.append($subExperimentsList);
			this._experimentFormController._mainController.serverFacade.listSamplesForExperiments([this._experimentFormModel.experiment], function(data) {
				if(data.result && data.result.length > 0) {
					//Table Structure
					var $table = $("<table>", { class : "table" });
					var $thead = $("<thead>");
					var $tbody = $("<tbody>");
					$table.append($thead).append($tbody);
					$thead.append($("<tr>").append("<th>Code</th>").append("<th>Name</th>").append("<th>Type</th>").append("<th>Metadata</th>"));
					for(var i = 0; i < data.result.length; i++) {
						var subExperiment = data.result[i];
						var link = $("<a>", { "style" : "cursor:pointer;" }).append(subExperiment.code);
						var clickFunction = function(permId) {
							return function() {
								_this._experimentFormController._mainController.changeView("showViewSamplePageFromPermId", permId);
							}
						}
						link.click(clickFunction(subExperiment.permId));
						$tbody.append($("<tr>").append($("<td>").append(link)).append("<td>" + Util.getEmptyIfNull(subExperiment.properties[profile.propertyReplacingCode]) + "</td>").append("<td>" + subExperiment.sampleTypeCode + "</td>").append("<td>" + Util.getMapAsString(subExperiment.properties, 200) + "</td>"));
					}
					$subExperimentsList.append($table);
				} else {
					$subExperimentsList.append("This experiment don't have sub experiments.");
				}
			});
			$formColumn.append($subExperiments);
		}
		if(this._experimentFormModel.mode === FormMode.EDIT || this._experimentFormModel.mode === FormMode.CREATE) {
			var label = "";
			
			if(this._experimentFormModel.mode === FormMode.EDIT) {
				label = "Update Experiment " + this._experimentFormModel.experiment.code;
			} else if(this._experimentFormModel.mode === FormMode.CREATE) {
				label = "Create Experiment";
			}
			
			var $updateBtn = $("<input>", { "type": "submit", "class" : "btn btn-primary", 'value' : label });
			$formColumn.append($updateBtn);
		}
		
		$container.append($form);
		
		Util.unblockUI();
	}
}