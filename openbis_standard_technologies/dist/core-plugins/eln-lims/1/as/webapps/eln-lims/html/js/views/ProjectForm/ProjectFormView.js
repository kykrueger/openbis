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
	
	this.repaint = function(views) {
		var $container = views.content;
		var _this = this;
		var projectIdentifier = IdentifierUtil.getProjectIdentifier(_this._projectFormModel.project.spaceCode, _this._projectFormModel.project.code);
		var $form = $("<div>");
		
		var $formColumn = $("<form>", {
			'role' : "form",
			'action' : 'javascript:void(0);'
		});
		
		$form.append($formColumn);

		//
		// Title
		//
		var title = null;
		var isInventoryProject = this._projectFormModel.project && profile.isInventorySpace(this._projectFormModel.project.spaceCode);
		var typeTitle = "Project: ";
		
		var spaceCode = this._projectFormModel.project.spaceCode;
		var projectCode = (this._projectFormModel.mode !== FormMode.CREATE)?this._projectFormModel.project.code:null;
		var entityPath = FormUtil.getFormPath(spaceCode, projectCode);
		
		if(this._projectFormModel.mode === FormMode.CREATE) {
			title = "Create " + typeTitle;
		} else if (this._projectFormModel.mode === FormMode.EDIT) {
			title = "Update " + typeTitle + this._projectFormModel.project.code;
		} else {
			title = typeTitle + this._projectFormModel.project.code;
		}
		
		var $formTitle = $("<div>");
			$formTitle
				.append($("<h2>").append(title))
				.append($("<h4>", { "style" : "font-weight:normal;" } ).append(entityPath));
		
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		var dropdownOptionsModel = [];
		if(this._projectFormModel.mode === FormMode.VIEW) {
			var experimentKindName = ELNDictionary.getExperimentKindName(projectIdentifier);
			if (_this._allowedToCreateExperiments()) {
				//Create Experiment
				var isDefaultExperimentPresent = mainController.profile.getExperimentTypeForExperimentTypeCode("DEFAULT_EXPERIMENT") != null;
				if(isDefaultExperimentPresent) {
					var newExperimentTypeDropdownId = "new-experiment-type-dropdown";
					var defaultValueKey = entityPath.text() + "-FORM-" + newExperimentTypeDropdownId;

					this._projectFormController.getDefaultSpaceValue(defaultValueKey, function (settingsValue) {
						var defaultValue;
						if (settingsValue) {
							defaultValue = settingsValue;
						} else if (profile.isInventorySpace(_this._projectFormModel.project.spaceCode)) {
							var experimentType = profile.getExperimentTypeForExperimentTypeCode(_this._projectFormModel.project.spaceCode);
							if (experimentType) {
								defaultValue = _this._projectFormModel.project.spaceCode;
							} else {
								defaultValue = "COLLECTION";
							}
						} else {
							defaultValue = "DEFAULT_EXPERIMENT";
						}

						$("option[value=" + defaultValue + "]").prop("selected", true);
					});

					var $experimentTypeDropdown = FormUtil.getInlineExperimentTypeDropdown(newExperimentTypeDropdownId, true);
					var $createExpBtn = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
						var experimentTypeCode = $("#" + newExperimentTypeDropdownId)[0].value;

						if (experimentTypeCode && experimentTypeCode !== "") {
							_this._projectFormController.createNewExperiment(experimentTypeCode);
						}
					});

					toolbarModel.push({ component: $createExpBtn, tooltip: "Create " + experimentKindName });
					toolbarModel.push({ component: $experimentTypeDropdown, tooltip: "Type of the " + experimentKindName + " to create" });

					$experimentTypeDropdown.change(function(event) {
						_this._projectFormController.setDefaultSpaceValue(defaultValueKey, $(event.target).val());
					});
				}
			}
            if (_this._allowedToMove()) {
                //Move
				dropdownOptionsModel.push({
                    label : "Move",
                    action : function() {
                        var moveEntityController = new MoveEntityController("PROJECT", _this._projectFormModel.project.permId);
                        moveEntityController.init();
                    }
                });
            }
			if(_this._allowedToEdit()) {
				//Edit
				var $editBtn = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
					_this._projectFormController.enableEditing();
				});
				toolbarModel.push({ component : $editBtn, tooltip: "Edit" });
			}
			if(_this._allowedToDelete()) {
				//Delete
				dropdownOptionsModel.push({
                    label : "Delete",
                    action : function() {
                        var modalView = new DeleteEntityController(function(reason) {
					        _this._projectFormController.deleteProject(reason);
                        }, true);
                        modalView.init();
                    }
                });
			}
			
			//Export
			dropdownOptionsModel.push({
                label : "Export Metadata",
                action : FormUtil.getExportAction([{ type: "PROJECT", permId : _this._projectFormModel.project.permId, expand : true }], true)
            });

            dropdownOptionsModel.push({
                label : "Export Metadata & Data",
                action : FormUtil.getExportAction([{ type: "PROJECT", permId : _this._projectFormModel.project.permId, expand : true }], false)
            });

			//Jupyter Button
			if(profile.jupyterIntegrationServerEndpoint) {
			    dropdownOptionsModel.push({
                    label : "New Jupyter notebook",
                    action : function () {
                        var jupyterNotebook = new JupyterNotebookController(_this._projectFormModel.project);
                        jupyterNotebook.init();
                    }
                });
			}

			// authorization
			if (this._projectFormModel.roles.indexOf("ADMIN") > -1 ) {
				dropdownOptionsModel.push({
                    label : "Manage access",
                    action : function () {
                        FormUtil.showAuthorizationDialog({
                            project: _this._projectFormModel.project,
                        });
                    }
                });
			}

            //Freeze
            if(_this._projectFormModel.v3_project && _this._projectFormModel.v3_project.frozen !== undefined) { //Freezing available on the API
                var isEntityFrozen = _this._projectFormModel.v3_project.frozen;
                if(isEntityFrozen) {
                    var $freezeButton = FormUtil.getFreezeButton("PROJECT", this._projectFormModel.v3_project.permId.permId, "Entity Frozen");
                    toolbarModel.push({ component : $freezeButton, tooltip: "Entity Frozen" });
                } else {
                    dropdownOptionsModel.push({
                        label : "Freeze Entity (Disable further modifications)",
                        action : function() {
                            FormUtil.showFreezeForm("PROJECT", _this._projectFormModel.v3_project.permId.permId);
                        }
                    });
                }

            }
		} else {
			var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", function() {
				_this._projectFormController.updateProject();
			}, "Save");
			$saveBtn.removeClass("btn-default");
			$saveBtn.addClass("btn-primary");
			toolbarModel.push({ component : $saveBtn, tooltip: "Save" });
		}
		
		var $header = views.header;
		$header.append($formTitle);

		var hideShowOptionsModel = [];

		$formColumn.append(this._createIdentificationInfo(hideShowOptionsModel));

		FormUtil.addOptionsToToolbar(toolbarModel, dropdownOptionsModel, hideShowOptionsModel);
		$header.append(FormUtil.getToolbar(toolbarModel));


		//
		// Metadata Fields
		//
		$formColumn.append($("<legend>").append("General"));
		
		var description = Util.getEmptyIfNull(this._projectFormModel.project.description);
		if(this._projectFormModel.mode !== FormMode.VIEW) {
			var $textBox = FormUtil._getTextBox(null, "Description", false);
			var textBoxEvent = function(jsEvent, newValue) {
				var valueToUse = null;
				if(newValue !== undefined && newValue !== null) {
					valueToUse = newValue;
				} else {
					valueToUse = $(this).val();
				}
				_this._projectFormModel.project.description = valueToUse;
				_this._projectFormModel.isFormDirty = true;
			};
			$textBox.val(description);
			$textBox = FormUtil.activateRichTextProperties($textBox, textBoxEvent, null, description, false);
			$formColumn.append(FormUtil.getFieldForComponentWithLabel($textBox, "Description"));
		} else {
            var $textBox = FormUtil._getTextBox(null, "Description", false);
			$textBox = FormUtil.activateRichTextProperties($textBox, undefined, null, description, true);
            $formColumn.append(FormUtil.getFieldForComponentWithLabel($textBox, "Description"));
		}
		
		// Experiment And Samples Table
		if(this._projectFormModel.mode !== FormMode.CREATE && !isInventoryProject) {
			var $experimentsContainer = $("<div>");
			$formColumn.append($("<legend>").append(ELNDictionary.getExperimentKindName(projectIdentifier, true)))
			$formColumn.append($experimentsContainer);
			
			var experimentTableController = new ExperimentTableController(this._projectFormController, null, jQuery.extend(true, {}, this._projectFormModel.project), true);
			experimentTableController.init($experimentsContainer);
			
			$formColumn.append($("<legend>").append("" + ELNDictionary.Samples + ""))
			var $samplesContainerHeader = $("<div>");
			$formColumn.append($samplesContainerHeader);
			var $samplesContainer = $("<div>");
			$formColumn.append($samplesContainer);
			
			var views = {
					header : $samplesContainerHeader,
					content : $samplesContainer
			}
			var sampleTableController = new SampleTableController(this._projectFormController, null, null, this._projectFormModel.project.permId, true);
			sampleTableController.init(views);
		}

		$container.append($form);
	};
	
	this._createIdentificationInfo = function(hideShowOptionsModel) {
		//TODO : Save on the user settings the default state after update by Kind, for example project-identification-info
		hideShowOptionsModel.push({
			label : "Identification Info",
			section : "#project-identification-info"
		});
		
		var $identificationInfo = $("<div>", { id : "project-identification-info" });

		$identificationInfo.append($("<legend>").append("Identification Info"));

		$identificationInfo.append(FormUtil.getFieldForLabelWithText("Space", this._projectFormModel.project.spaceCode));

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
			$identificationInfo.append(FormUtil.getFieldForComponentWithLabel($textField, "Code"));
		} else {
			$identificationInfo.append(FormUtil.getFieldForLabelWithText("Code", this._projectFormModel.project.code));
		}

		if(this._projectFormModel.mode !== FormMode.CREATE) {
			var registrationDetails = this._projectFormModel.project.registrationDetails;

			var $registrator = FormUtil.getFieldForLabelWithText("Registrator", registrationDetails.userId);
			$identificationInfo.append($registrator);

			var $registationDate = FormUtil.getFieldForLabelWithText("Registration Date", Util.getFormatedDate(new Date(registrationDetails.registrationDate)));
			$identificationInfo.append($registationDate);

			var $modifier = FormUtil.getFieldForLabelWithText("Modifier", registrationDetails.modifierUserId);
			$identificationInfo.append($modifier);

			var $modificationDate = FormUtil.getFieldForLabelWithText("Modification Date", Util.getFormatedDate(new Date(registrationDetails.modificationDate)));
			$identificationInfo.append($modificationDate);
		}
		return $identificationInfo;
	}
	
	this._allowedToCreateExperiments = function() {
		var project = this._projectFormModel.v3_project;
		return project.frozenForExperiments == false && this._projectFormModel.experimentRights.rights.indexOf("CREATE") >= 0;
	};
	
	this._allowedToEdit = function() {
		var project = this._projectFormModel.v3_project;
		return project.frozen == false && this._allowedToUpdate(this._projectFormModel.rights);
	};
	
	this._allowedToUpdate = function(rights) {
		return rights && rights.rights.indexOf("UPDATE") >= 0;
	}

	this._allowedToMove = function() {
		var project = this._projectFormModel.v3_project;
		if (project.frozen || project.space.frozenForProjects) {
			return false;
		}
		return this._allowedToUpdate(this._projectFormModel.rights);
	};
	
	this._allowedToDelete = function() {
		var project = this._projectFormModel.v3_project;
		return project.frozen == false && project.space.frozenForProjects == false;
	};
}
