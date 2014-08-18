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

function SampleFormView(sampleFormController, sampleFormModel) {
	this._sampleFormController = sampleFormController;
	this._sampleFormModel = sampleFormModel;
	
	this.repaint = function($container) {
		//
		// Form setup
		//
		var _this = this;
		$container.empty();
		
		var $form = $("<div>", { "class" : "row"});
		var $formColumn = $("<form>", { 
			"class" : FormUtil.formColumClass + " form-horizontal", 
			'role' : "form"
		});
			
		$form.append($formColumn);
		
		//
		// Extra Metadata
		//
		var sampleTypeCode = this._sampleFormModel.sample.sampleTypeCode;
		var sampleType = mainController.profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
		var sampleTypeDefinitionsExtension = profile.sampleTypeDefinitionsExtension[this._sampleFormModel.sample.sampleTypeCode];
		
		//
		// TITLE
		//
		var $formTitle = null;
		
		var title = null;
		switch(this._sampleFormModel.mode) {
	    	case FormMode.CREATE:
	    		title = "Create " + this._sampleFormModel.sample.sampleTypeCode;
	    		break;
	    	case FormMode.EDIT:
	    		title = "Update " + this._sampleFormModel.sample.code;
	    		break;
	    	case FormMode.VIEW:
	    		title = "View " + this._sampleFormModel.sample.code;
	    		break;
		} 
		
		var $formTitle = $("<h2>").append(title + " ");
		
		//
		// TITLE BUTTONS
		//
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			//Pin
			$formTitle.append(FormUtil.getPINButton(this._sampleFormModel.sample.permId));
			//Hierarchy
			$formTitle.append(" ");
			$formTitle.append(FormUtil.getHierarchyButton(this._sampleFormModel.sample.permId));
			//Copy
			$formTitle.append(" ");
			var $copyButton = $("<a>", { 'class' : 'btn btn-default'} )
										.append($('<img>', { 'src' : './img/copy-icon.png', 'style' : 'width:16px; height:16px;' }));
			$copyButton.click(_this._getCopyButtonEvent());
			$formTitle.append($copyButton);
			//Edit
			if(this._sampleFormModel.mode === FormMode.VIEW) {
				$formTitle.append(" ");
				var $editButton = $("<a>", { 'class' : 'btn btn-default'} )
									.append($('<span>', { 'class' : 'glyphicon glyphicon-edit' }))
									.append(' Enable Editing');
				
				$editButton.click(function() {
					mainController.changeView('showEditSamplePageFromPermId', _this._sampleFormModel.sample.permId);
				});
				
				$formTitle.append($editButton);
			}
			
		}
		
		$formColumn.append($formTitle);
		
		//
		// PREVIEW IMAGE
		//
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			var $previewImage = $("<img>", { 'data-preview-loaded' : 'false',
											 'class' : 'zoomableImage',
											 'id' : 'preview-image',
											 'src' : './img/image_loading.gif',
											 'style' : 'height:300px; margin-right:20px; display:none;'
											});
			$previewImage.click(function() {
				Util.showImage($("#preview-image").attr("src"));
			});
			
			$formColumn.append($previewImage);
		}
		
		//
		// SELECT EXPERIMENT/SPACE
		//
		$formColumn.append($("<legend>").append("Identification Info"));
		if(this._sampleFormModel.isELNSubExperiment) {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Experiment", this._sampleFormModel.sample.experimentIdentifierOrNull));
		} else {
				var defaultSpace = profile.getSpaceForSampleType(sampleTypeCode);
				if(!defaultSpace) {
					var spacesDropDown = FormUtil.getSpaceDropdown();
					$formColumn.append(FormUtil.getFieldForComponentWithLabel(spacesDropDown, "Space"));
					spacesDropDown.change(function(event){
						var spacesDropDown = $(this);
						_this._sampleFormModel.sample.spaceCode = spacesDropDown.val();
						_this._sampleFormModel.isFormDirty = true;
					});
				} else {
					$formColumn.append(FormUtil.getFieldForLabelWithText("Space", defaultSpace));
				}
		}
		
		//
		// CODE
		//
		if(this._sampleFormModel.mode === FormMode.CREATE) {
			var $textField = FormUtil._getInputField('text', null, "Code", null, true);
			$textField.keyup(function(event){
				var textField = $(this);
				textField.val(textField.val().toUpperCase());
				_this._sampleFormModel.sample.code = textField.val();
				_this._sampleFormModel.isFormDirty = true;
			});
			$formColumn.append(FormUtil.getFieldForComponentWithLabel($textField, "Code"));
		} else {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Code", this._sampleFormModel.sample.code));
		}
		
		//
		// LINKS TO PARENTS
		//
		var requiredParents = [];
		if(sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_PARENTS_HINT"]) {
			requiredParents = sampleTypeDefinitionsExtension["SAMPLE_PARENTS_HINT"];
		}
		
		var sampleParentsWidgetId = "sampleParentsWidgetId";
		var $sampleParentsWidget = $("<div>", { "id" : sampleParentsWidgetId });
		$formColumn.append($sampleParentsWidget);
		var isDisabled = this._sampleFormModel.mode === FormMode.VIEW;
		
		var currentParentsLinks = (this._sampleFormModel.sample)?this._sampleFormModel.sample.parents:null;
		this._sampleFormModel.sampleLinksParents = new SampleLinksWidget(sampleParentsWidgetId,
																		profile,
																		mainController.serverFacade,
																		"Parents",
																		requiredParents,
																		isDisabled,
																		currentParentsLinks);
		
		//
		// LINKS TO CHILDREN
		//
		var requiredChildren = [];
		if(sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_HINT"]) {
			requiredChildren = sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_HINT"];
		}
		
		var sampleChildrenWidgetId = "sampleChildrenWidgetId";
		var $sampleChildrenWidget = $("<div>", { "id" : sampleChildrenWidgetId });
		$formColumn.append($sampleChildrenWidget);
		
		var currentChildrenLinks = (this._sampleFormModel.sample)?this._sampleFormModel.sample.children:null;
		this._sampleFormModel.sampleLinksChildren = new SampleLinksWidget(sampleChildrenWidgetId,
														profile,
														mainController.serverFacade,
														"Children",
														requiredChildren,
														isDisabled,
														currentChildrenLinks);
		
		//
		// GENERATE CHILDREN
		//
		if((this._sampleFormModel.mode !== FormMode.VIEW) && this._sampleFormModel.isELNSubExperiment) {
			var $generateChildrenBtn = $("<a>", { 'class' : 'btn btn-default', 'style' : 'margin-left:25px;', 'id' : 'generate_children'}).text("Generate Children");
			$generateChildrenBtn.click(function(event) {
				_this._generateChildren();
			});
			
			var $generateChildrenBox = $("<div>")
											.append($("<div>", { 'class' : 'form-group' }).append($generateChildrenBtn))
											.append($("<div>", { 'id' : 'newChildrenOnBenchDropDown' }))
			$formColumn.append($generateChildrenBox);
		}
		
		//
		// PROPERTIES
		//
		for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = sampleType.propertyTypeGroups[i];
			
			var $fieldset = $('<div>');
			var $legend = $('<legend>'); 
			$fieldset.append($legend);
			
			if((propertyTypeGroup.name !== null) && (propertyTypeGroup.name !== "")) {
				$legend.text(propertyTypeGroup.name);
			} else if((i === 0) || ((i !== 0) && (sampleType.propertyTypeGroups[i-1].name !== null) && (sampleType.propertyTypeGroups[i-1].name !== ""))) {
				$legend.text("Metadata");
			} else {
				$legend.remove();
			}
			
			var storagePropertyGroup = profile.getPropertyGroupFromStorage(propertyTypeGroup.name);
			if(storagePropertyGroup) {
				var storageContainer = $("<div>");
				$fieldset.append(storageContainer);
				$formColumn.append($fieldset);
				this._sampleFormController.addStorageController(storagePropertyGroup["STORAGE_GROUP_DISPLAY_NAME"]);
				var isDisabled =  this._sampleFormModel.mode === FormMode.VIEW;
				this._sampleFormController.getLastStorageController().bindSample(this._sampleFormModel.sample, isDisabled);
				this._sampleFormController.getLastStorageController().getView().repaint(storageContainer);
				continue;
			}
			
			for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
				var propertyType = propertyTypeGroup.propertyTypes[j];
				
				var $controlGroup =  null;
				
				var value = this._sampleFormModel.sample.properties[propertyType.code];
				var isSystemProperty = false;
				if(!value && propertyType.code.charAt(0) === '$') {
					value = this._sampleFormModel.sample.properties[propertyType.code.substr(1)];
					isSystemProperty = true;
				}
				
				if(this._sampleFormModel.mode === FormMode.VIEW) { //Show values without input boxes if the form is in view mode
					$controlGroup = FormUtil.getFieldForLabelWithText(propertyType.label, value, propertyType.code);
				} else {
					var $component = FormUtil.getFieldForPropertyType(propertyType);
					//Update values if is into edit mode
					if(this._sampleFormModel.mode === FormMode.EDIT) {
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
							_this._sampleFormModel.isFormDirty = true;
							var field = $(this);
							if(propertyType.dataType === "BOOLEAN") {
								_this._sampleFormModel.sample.properties[propertyTypeCode] = field.children()[0].checked;
							} else if (propertyType.dataType === "TIMESTAMP") {
								var timeValue = $($(field.children()[0]).children()[0]).val();
								_this._sampleFormModel.sample.properties[propertyTypeCode] = timeValue;
							} else {
								_this._sampleFormModel.sample.properties[propertyTypeCode] = Util.getEmptyIfNull(field.val());
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
		
		//
		// Extra Content
		//
		$formColumn.append($("<div>", { 'id' : 'sample-form-content-extra' }));
		
		//
		// FORM SUBMIT
		//
		if(this._sampleFormModel.mode !== FormMode.VIEW) {
			var $updateBtn = $("<a>", { "class" : "btn btn-primary"}).append(title);
			$updateBtn.click(function() {
				_this._sampleFormController.createUpdateCopySample();
			});
			
			$formColumn.append($updateBtn);
		}
		
		//
		// DATASETS
		//
		
		var $dataSetViewerContainer = $("<div>", { 'id' : 'dataSetViewerContainer', 'style' : 'margin-top:10px;'});
		$formColumn.append($dataSetViewerContainer);
		
		//
		// INIT
		//
		$container.append($form);
		
		//
		// Extra content
		//
		//Extra components
		profile.sampleFormContentExtra(this._sampleFormModel.sample.sampleTypeCode, this._sampleFormModel.sample, "sample-form-content-extra");
		
		//
		// TO-DO: Legacy code to be refactored
		//
		
		//Repaint parents and children after updating the property state to show the annotations
		this._sampleFormModel.sampleLinksParents.repaint();
		this._sampleFormModel.sampleLinksChildren.repaint();
		
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			//Preview image
			this._reloadPreviewImage();
			
			// Dataset Viewer
			this._sampleFormModel.dataSetViewer = new DataSetViewer("dataSetViewerContainer", profile, this._sampleFormModel.sample, mainController.serverFacade, profile.getDefaultDataStoreURL());
			this._sampleFormModel.dataSetViewer.init();
		}
		
		this._sampleFormModel.isFormLoaded = true;
	}
	
	//
	// TO-DO: Legacy code to be refactored
	//
	
	//
	// Copy Sample pop up
	//
	this._getCopyButtonEvent = function() {
		var _this = this;
		return function() {
			var component = "<div class='form-horizontal'>"
				component += "<legend>Duplicate Entity</legend>";
				component += "<div class='form-inline'>";
				component += "<div class='form-group " + FormUtil.shortformColumClass + "'>";
				component += "<label class='control-label " + FormUtil.labelColumnClass + "'>Options </label>";
				component += "<div class='" + FormUtil.controlColumnClass + "'>";
				component += "<span class='checkbox'><label><input type='checkbox' id='linkParentsOnCopy' checked> Link Parents </label></span>";
				component += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
				component += "<span class='checkbox'><label><input type='checkbox' id='copyChildrenOnCopy' checked> Copy Children </label></span>";
				component += "</div>";
				component += "</div>";
				component += "</div>";
				component += "<br /><br />";
				component += "<div class='form-group " + FormUtil.shortformColumClass + "'>";
				component += "<label class='control-label  " + FormUtil.labelColumnClass+ "'>Code&nbsp;(*):</label>";
				component += "<div class='" + FormUtil.shortControlColumnClass + "'>";
				component += "<input type='text' class='form-control' placeholder='Code' id='newSampleCodeForCopy' pattern='[a-zA-Z0-9_\\-\\.]+' required>";
				component += "</div>";
				component += "<div class='" + FormUtil.shortControlColumnClass + "'>";
				component += " (Allowed characters are: letters, numbers, '-', '_', '.')";
				component += "</div>";
				component += "</div>";
				
			var css = {
					'text-align' : 'left',
					'top' : '15%',
					'width' : '70%',
					'left' : '15%',
					'right' : '20%',
					'overflow' : 'auto'
			};
				
			var css = {
					'text-align' : 'left',
					'top' : '15%',
					'width' : '70%',
					'left' : '15%',
					'right' : '20%',
					'overflow' : 'auto'
			};
			
			Util.blockUI(component + "<br><br><br> <a class='btn btn-default' id='copyAccept'>Accept</a> <a class='btn btn-default' id='copyCancel'>Cancel</a>", css);
			
			$("#newSampleCodeForCopy").on("keyup", function(event) {
				$(this).val($(this).val().toUpperCase());
			});
			
			$("#copyAccept").on("click", function(event) {
				var newSampleCodeForCopy = $("#newSampleCodeForCopy");
				var linkParentsOnCopy = $("#linkParentsOnCopy")[0].checked;
				var copyChildrenOnCopy = $("#copyChildrenOnCopy")[0].checked;
				var isValid = newSampleCodeForCopy[0].checkValidity();
				if(isValid) {
					var newSampleCodeForCopyValue = newSampleCodeForCopy.val();
					_this._sampleFormController.createUpdateCopySample(newSampleCodeForCopyValue, linkParentsOnCopy, copyChildrenOnCopy);
					Util.unblockUI();
				} else {
					Util.showError("Invalid code.", function() {}, true);
				}
			});
			
			$("#copyCancel").on("click", function(event) { 
				Util.unblockUI();
			});
		}
	}
	
	//
	// Preview Image
	//
	this._reloadPreviewImage = function() {
		var _this = this;
		var previewCallback = 
			function(data) {
				if (data.result.length == 0) {
					_this._updateLoadingToNotAvailableImage();
				} else {
					var x = "123";
					var listFilesForDataSetCallback = 
						function(dataFiles) {
							if(!dataFiles.result) {
								//DSS Is not running probably
							} else {
								var elementId = 'preview-image';
								var downloadUrl = profile.getDefaultDataStoreURL() + '/' + data.result[0].code + "/" + dataFiles.result[1].pathInDataSet + "?sessionID=" + mainController.serverFacade.getSession();
								
								var img = $("#" + elementId);
								img.attr('src', downloadUrl);
								img.attr('data-preview-loaded', 'true');
								img.show();
							}
						};
					mainController.serverFacade.listFilesForDataSet(data.result[0].code, "/", true, listFilesForDataSetCallback);
				}
			};
		
		mainController.serverFacade.searchDataSetsWithTypeForSamples("ELN_PREVIEW", [this._sampleFormModel.sample.permId], previewCallback);
	}
	
	this._updateLoadingToNotAvailableImage = function() {
		var notLoadedImages = $("[data-preview-loaded='false']");
		notLoadedImages.attr('src', "./img/image_unavailable.png");
	}
	
	//
	// Children Generator
	//
	this._childrenAdded = function() {
		var $childrenStorageDropdown = FormUtil.getDefaultBenchDropDown('childrenStorageSelector', true);
		if($childrenStorageDropdown && !$("#childrenStorageSelector").length) {
			var $childrenStorageDropdownWithLabel = FormUtil.getFieldForComponentWithLabel($childrenStorageDropdown, 'Storage');
			$("#newChildrenOnBenchDropDown").append($childrenStorageDropdownWithLabel);
		}
	}
	
	this._generateChildren = function() {
		var _this = this;
		// Utility self contained methods
		var getGeneratedChildrenCodes = function() {
			//Get selected parents
			var $parentsFields = $("#parentsToGenerateChildren").find("input:checked");
			//Group parents by type - this structure helps the create children algorithm
			var selectedParentsByType = {};
			for(var i = 0; i < $parentsFields.length; i++) {
				var parentIdentifier = $parentsFields[i].id;
				var parent = parentsByIdentifier[parentIdentifier];
				var typeList = selectedParentsByType[parent.sampleTypeCode];
				if(!typeList) {
					typeList = [];
					selectedParentsByType[parent.sampleTypeCode] = typeList;
				}
				typeList.push(parent);
			}
			//Generate Children from parents
			var generatedChildren = [];
			var parentSampleCode = _this._sampleFormModel.sample.code;
			for(var sampleTypeCode in selectedParentsByType) {
				var parentsOfType = selectedParentsByType[sampleTypeCode];
				
				var newGeneratedChildren = [];
				
				for(var i = 0; i < parentsOfType.length; i++) {
					var parentOfType = parentsOfType[i];
					if(generatedChildren.length === 0) {
						newGeneratedChildren.push(parentSampleCode + "_" + parentOfType.code);
					} else {
						for(var k = 0; k < generatedChildren.length; k++) {
							newGeneratedChildren.push(generatedChildren[k] + "_" + parentOfType.code);
						}
					}
				}
				
				generatedChildren = newGeneratedChildren;
			}
			
			//Number of Replicas
			var numberOfReplicas = parseInt($("#childrenReplicas").val());
			if(isNaN(numberOfReplicas) || numberOfReplicas < 0 || numberOfReplicas > 1000) {
				Util.showError("The number of children replicas should be an integer number bigger than 0 and lower than 1000.", function() {}, true);
				return;
			}
			
			var generatedChildrenWithReplicas = [];
			for(var i = 0; i < generatedChildren.length; i++) {
				for(var j = 0; j < numberOfReplicas; j++) {
					generatedChildrenWithReplicas.push(generatedChildren[i] + "_" + (j + 1));
				}
			}
			
			return generatedChildrenWithReplicas;
		}
		
		var showPreview = function() {
			$("#previewChildrenGenerator").empty();
			
			var generatedChildren = getGeneratedChildrenCodes();
			//Show generated children
			if(generatedChildren) {
				for(var i = 0; i < generatedChildren.length; i++) {
					$("#previewChildrenGenerator").append(generatedChildren[i] + "<br />");
				}
			}
		}
		
		var _this = this;
		// Buttons
		var $generateButton = $("<a>", { "class" : "btn btn-default" }).append("Generate!");
		$generateButton.click(function(event) { 
			var generatedChildrenSpace = _this._sampleFormModel.sample.space;
			
			var numberOfReplicas = parseInt($("#childrenReplicas").val());
			if(isNaN(numberOfReplicas) || numberOfReplicas < 0 || numberOfReplicas > 1000) {
				Util.showError("The number of children replicas should be an integer number bigger than 0 and lower than 1000.", function() {}, true);
				return;
			}
			var generatedChildrenCodes = getGeneratedChildrenCodes();
			var generatedChildrenType = $("#childrenTypeSelector").val();
			if(generatedChildrenType === "") {
				Util.showError("Please select the children type.", function() {}, true);
			} else {
				for(var i = 0; i < generatedChildrenCodes.length; i++) {
					var virtualSample = new Object();
					virtualSample.newSample = true;
					virtualSample.code = generatedChildrenCodes[i];
					virtualSample.identifier = "/" + generatedChildrenSpace + "/" + virtualSample.code;
					virtualSample.sampleTypeCode = generatedChildrenType;
					_this._sampleFormModel.sampleLinksChildren.addSample(virtualSample);
				}
				
				_this._childrenAdded();
				Util.unblockUI();
			}
			
			
		});
		
		var $cancelButton = $("<a>", { "class" : "btn btn-default" }).append("<span class='glyphicon glyphicon-remove'></span>");
		$cancelButton.click(function(event) { 
			Util.unblockUI();
		});
		
		var $selectAllButton = $("<a>", { "class" : "btn btn-default" }).append("Enable/Disable All");
		$selectAllButton.attr("ison", "false");
		
		$selectAllButton.click(function(event) {
			var $button = $(this);
			var isOn = !($button.attr("ison") === "true");
			$button.attr("ison", isOn);
			
			var $parentsFields = $("#parentsToGenerateChildren").find("input");
			for(var i = 0; i < $parentsFields.length; i++) {
				var $parentField = $parentsFields[i];
				$parentField.checked = isOn;
			}
			
			showPreview();
		});
		
		// Parents
		var $parents = $("<div>");
		var parentsIdentifiers = _this._sampleFormModel.sampleLinksParents.getSamplesIdentifiers();
		var parentsByType = {}; //This is the main model
		var parentsByIdentifier = {}; // Used by the getGeneratedChildrenCodes function
		for(var i = 0; i < parentsIdentifiers.length; i++) {
			var parent = _this._sampleFormModel.sampleLinksParents.getSampleByIdentifier(parentsIdentifiers[i]);
			var typeList = parentsByType[parent.sampleTypeCode];
			if(!typeList) {
				typeList = [];
				parentsByType[parent.sampleTypeCode] = typeList;
			}
			typeList.push(parent);
			parentsByIdentifier[parent.identifier] = parent;
		}
		
		var $parentsTable = $("<table>", { "class" : "table table-bordered table-compact" });
		var $headerRow = $("<tr>");
		$parentsTable.append($headerRow);
		var maxDepth = 0;
		for (var key in parentsByType) {
			$headerRow.append($("<th>", {"class" : "text-center-important"}).text(key));
			maxDepth = Math.max(maxDepth, parentsByType[key].length);
		}

		for (var idx = 0; idx < maxDepth; idx++) {
			var $tableRow = $("<tr>");
			for (key in parentsByType) {
				if (idx < parentsByType[key].length) {
					var parent = parentsByType[key][idx];
					var parentProperty = {
							code : parent.identifier,
							description : parent.identifier,
							label : parent.code,
							dataType : "BOOLEAN"
					};
					
					var $checkBox = $('<input>', {'style' : 'margin-bottom:7px;', 'type' : 'checkbox', 'id' : parent.identifier, 'alt' : parent.identifier, 'placeholder' : parent.identifier });
					$checkBox.change(function() { 
						showPreview();
					});
					
					var $field = $('<div>');
					$field.append($checkBox);
					$field.append(" " + parent.code);
					
					$tableRow.append($("<td>").append($field));
 				} else {
 					$tableRow.append($("<td>").html("&nbsp;"));
 				}
			}
			$parentsTable.append($tableRow);
		}
		
		$parents.append($parentsTable);
		
		var $parentsComponent = $("<div>", { "id" : 'parentsToGenerateChildren'} );
		$parentsComponent.append($("<legend>").append("Parents ").append($selectAllButton))
		$parentsComponent.append($parents);
		
		// Children
		var $childrenComponent = $("<div>");
		$childrenComponent.append($("<legend>").text("Children"))
		
		var $childrenTypeDropdown = FormUtil.getSampleTypeDropdown('childrenTypeSelector', true);
		var $childrenTypeDropdownWithLabel = FormUtil.getFieldForComponentWithLabel($childrenTypeDropdown, 'Type');
		$childrenComponent.append($childrenTypeDropdownWithLabel);
		
		var $childrenReplicas = FormUtil._getInputField('number', 'childrenReplicas', 'Children Replicas', '1', true);
		$childrenReplicas.val("1");
		$childrenReplicas.keyup(function() { 
			showPreview();
		});
		
		var $childrenReplicasWithLabel = FormUtil.getFieldForComponentWithLabel($childrenReplicas, 'Children Replicas');
		$childrenComponent.append($childrenReplicasWithLabel);
		
		// Preview
		var $previewComponent = $("<div>");
		$previewComponent.append($("<legend>").append("Preview"));
		$previewComponent.append($("<div>", {"id" : "previewChildrenGenerator"}));
		
		// Mounting the widget with the components
		var $childrenGenerator = $("<div>");
		$childrenGenerator.append($("<div>", {"style" : "text-align:right;"}).append($cancelButton));
		$childrenGenerator.append($("<form>", { "class" : "form-horizontal" , "style" : "margin-left:20px; margin-right:20px;"})
									.append($("<h1>").append("Children Generator"))
									.append($parentsComponent)
									.append($childrenComponent)
									.append($previewComponent)
									.append($("<br>")).append($generateButton)
								);
		
		// Show Widget
		Util.blockUI($childrenGenerator, {'text-align' : 'left', 'top' : '10%', 'width' : '80%', 'left' : '10%', 'right' : '10%', 'height' : '80%', 'overflow' : 'auto'});
	}
}