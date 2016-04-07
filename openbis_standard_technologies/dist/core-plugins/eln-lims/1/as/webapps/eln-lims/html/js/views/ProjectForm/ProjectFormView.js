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

function ProjectFormView(projectFormController, projectFormModel) {
	this._projectFormController = projectFormController;
	this._projectFormModel = projectFormModel;
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		
		var $form = $("<div>", { "class" : "row"});
		
		var $formColumn = $("<form>", {
			"class" : FormUtil.formColumClass + " form-horizontal", 
			'role' : "form",
			'action' : 'javascript:void(0);',
			'onsubmit' : 'mainController.currentView.updateProject();'
		});
			
		$form.append($formColumn);
		
		//
		// Title
		//
		var title = null;
		var entityPath = null;
		if(this._projectFormModel.mode === FormMode.CREATE) {
			title = "Create Project";
			entityPath = "";
		} else if (this._projectFormModel.mode === FormMode.EDIT) {
			title = "Update Project: " + this._projectFormModel.project.code;
			entityPath = "/" + this._projectFormModel.project.spaceCode + "/" + this._projectFormModel.project.code;
		} else {
			title = "Project: " + this._projectFormModel.project.code;
			entityPath = "/" + this._projectFormModel.project.spaceCode + "/" + this._projectFormModel.project.code;
		}
		
		var $formTitle = $("<div>");
			$formTitle
				.append($("<h2>").append(title))
				.append($("<h4>", { "style" : "font-weight:normal;" } ).append(entityPath));
		$formColumn.append($formTitle);
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		if(this._projectFormModel.mode !== FormMode.CREATE) {
			//Create Experiment
			var $createExpBtn = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
				
				var showSelectExperimentType = function() {
					var $dropdown = FormUtil.getExperimentTypeDropdown("experimentTypeDropdown", true);
					Util.blockUI("Select the type for the Experiment: <br><br>" + $dropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='experimentTypeDropdownCancel'>Cancel</a>");
					
					$("#experimentTypeDropdown").on("change", function(event) {
						var experimentTypeCode = $("#experimentTypeDropdown")[0].value;
						_this._projectFormController.createNewExperiment(experimentTypeCode);
					});
					
					$("#experimentTypeDropdownCancel").on("click", function(event) { 
						Util.unblockUI();
					});
				}
				
				if(profile.isInventorySpace(_this._projectFormModel.project.spaceCode)) {
					var experimentType = profile.getExperimentTypeForExperimentTypeCode(_this._projectFormModel.project.spaceCode);
					if(experimentType) {
						_this._projectFormController.createNewExperiment(_this._projectFormModel.project.spaceCode);
					} else {
						showSelectExperimentType();
					}
				} else {
					showSelectExperimentType();
				}
			});
			toolbarModel.push({ component : $createExpBtn, tooltip: "Create Experiment" });
			
			//Edit
			var $editBtn = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
				_this._projectFormController.enableEditing();
			});
			toolbarModel.push({ component : $editBtn, tooltip: "Edit" });
			
			//Delete
			var $deleteBtn = FormUtil.getDeleteButton(function(reason) {
				_this._projectFormController.deleteProject(reason);
			}, true);
			toolbarModel.push({ component : $deleteBtn, tooltip: "Delete" });
		}
		$formColumn.append(FormUtil.getToolbar(toolbarModel));
		
		//
		// Metadata Fields
		//
		$formColumn.append($("<legend>").append("General"));
		
		var description = Util.getEmptyIfNull(this._projectFormModel.project.description);
		if(this._projectFormModel.mode !== FormMode.VIEW) {
			var $textBox = FormUtil._getTextBox(null, "Description", false);
			var textBoxEvent = function(event) {
				_this._projectFormModel.project.description = $(this).val();
				_this._projectFormModel.isFormDirty = true;
			};
			$textBox.val(description);
			$textBox = FormUtil.activateRichTextProperties($textBox, textBoxEvent);
			$formColumn.append(FormUtil.getFieldForComponentWithLabel($textBox, "Description"));
		} else {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Description", description));
		}
		
		// Experiment And Samples Table
		if(this._projectFormModel.mode !== FormMode.CREATE) {
			var $experimentsContainer = $("<div>");
			$formColumn.append($experimentsContainer);
			
			var experimentTableController = new ExperimentTableController(this._projectFormController, "Experiments", this._projectFormModel.project, true);
			experimentTableController.init($experimentsContainer);
			
			var $samplesContainer = $("<div>");
			$formColumn.append($samplesContainer);
			
			var sampleTableController = new SampleTableController(this._projectFormController, "Samples", null, this._projectFormModel.project.permId, true);
			sampleTableController.init($samplesContainer);
		}
		
		//
		// Identification info
		//
		$formColumn.append($("<legend>").append("Identification Info"));
		
		$formColumn.append(FormUtil.getFieldForLabelWithText("Space", this._projectFormModel.project.spaceCode));
		
		if(this._projectFormModel.mode === FormMode.CREATE) {
			var $textField = FormUtil._getInputField('text', null, "Project Code", null, true);
			$textField.keyup(function(event){
				var textField = $(this);
				var caretPosition = this.selectionStart;
				textField.val(textField.val().toUpperCase());
				this.selectionStart = caretPosition;
				this.selectionEnd = caretPosition;
				_this._projectFormModel.project.code = textField.val();
				_this._projectFormModel.isFormDirty = true;
			});
			$formColumn.append(FormUtil.getFieldForComponentWithLabel($textField, "Code"));
		} else {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Code", this._projectFormModel.project.code));
		}
		
		if(this._projectFormModel.mode !== FormMode.CREATE) {
			var registrationDetails = this._projectFormModel.project.registrationDetails;
			
			var $registrator = FormUtil.getFieldForLabelWithText("Registrator", registrationDetails.userId);
			$formColumn.append($registrator);
			
			var $registationDate = FormUtil.getFieldForLabelWithText("Registration Date", Util.getFormatedDate(new Date(registrationDetails.registrationDate)));
			$formColumn.append($registationDate);
			
			var $modifier = FormUtil.getFieldForLabelWithText("Modifier", registrationDetails.modifierUserId);
			$formColumn.append($modifier);
			
			var $modificationDate = FormUtil.getFieldForLabelWithText("Modification Date", Util.getFormatedDate(new Date(registrationDetails.modificationDate)));
			$formColumn.append($modificationDate);
		}
		
		//Create/Update Button
		if(this._projectFormModel.mode !== FormMode.VIEW) {
			var btnText = null;
			if(this._projectFormModel.mode === FormMode.CREATE) {
				btnText = "Create Project";
			} else if(this._projectFormModel.mode === FormMode.EDIT) {
				btnText = "Update Project";
			}
			
			var $updateBtn = $("<input>", { "type": "submit", "class" : "btn btn-primary", 'value' : btnText });
			$formColumn.append($updateBtn);
		}
		
		$container.append($form);
	}
}