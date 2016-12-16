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

function LinksView(linksController, linksModel) {
	var linksController = linksController;
	var linksModel = linksModel;
	var linksView = this;
	
	var sampleGridContainerByType = {};
	
	var $samplePicker = $("<div>");
	var $savedContainer = null;
	
	//
	// External API
	//
	linksView.initContainerForType = function(sampleTypeCode) {
		var $dataGridContainer = sampleGridContainerByType[sampleTypeCode];
		var samplesOnGrid = linksModel.samplesByType[sampleTypeCode];
		
		//Create Model
		if(!samplesOnGrid) {
			samplesOnGrid = [];
			linksModel.samplesByType[sampleTypeCode] = samplesOnGrid;
		}
		
		//Create Layout
		if(!$dataGridContainer) { //Create if is not there yet
			//Layout
			var $sampleTableContainer = $("<div>");
			var $samplePickerContainer = $("<div>");
			
			if(sampleTypeCode) {
				$sampleTableContainer.append($("<div>").append(sampleTypeCode + ":")
						.append("&nbsp;")
						.append(linksView.getAddBtn($samplePickerContainer, sampleTypeCode))
						.css("margin","5px"));
			}
			
			$sampleTableContainer.append($samplePickerContainer);
			$dataGridContainer = $("<div>");
			$sampleTableContainer.append($dataGridContainer);
			
			sampleGridContainerByType[sampleTypeCode] = $dataGridContainer;
			
			$savedContainer.append($sampleTableContainer);
		}
	}
	
	this.updateSample = function(sample, isAdd) {
		var sampleTypeCode = sample.sampleTypeCode;
		
		var containerCode = null;
		if(!linksModel.isDisabled) {
			containerCode = sampleTypeCode;
		}
		
		linksView.initContainerForType(containerCode);
		var $dataGridContainer = sampleGridContainerByType[containerCode];
		
		var samplesOnGrid = linksModel.samplesByType[containerCode];
		
		//Check if the sample is already added
		var foundAtIndex = -1;
		for(var sIdx = 0; sIdx < samplesOnGrid.length; sIdx++) {
			if(samplesOnGrid[sIdx].permId === sample.permId) {
				foundAtIndex = sIdx;
				if(isAdd) {
					Util.showError("Sample " + sample.code + " already present, it will not be added again.");
					return;
				} else {
					linksModel.samplesRemoved.push(sample.identifier);
					break;
				}
			}
		}
		
		//Render Grid
		$dataGridContainer.empty();
		
		if(isAdd) {
			samplesOnGrid.push(sample);
		} else {
			samplesOnGrid.splice(foundAtIndex, 1);
		}
		
		var customAnnotationColumnsByType = {};
		for(var sIdx = 0; sIdx < samplesOnGrid.length; sIdx++) {
			var sample = samplesOnGrid[sIdx];
			if(!customAnnotationColumnsByType[sample.sampleTypeCode]) {
				var customACols = linksView.getCustomAnnotationColumns(sample.sampleTypeCode);
				customAnnotationColumnsByType[sample.sampleTypeCode] = customACols;
			}
		}
		
		var allCustomAnnotations = [];
		for(type in customAnnotationColumnsByType) {
			var customACols = customAnnotationColumnsByType[type];
			for(var cIdx = 0; cIdx < customACols.length; cIdx++) {
				var customACol = customACols[cIdx];
				var isFound = false;
				for(aIdx = 0; aIdx < allCustomAnnotations.length; aIdx++) {
					if(allCustomAnnotations[aIdx].property == customACol.property) {
						isFound = true;
					}
				}
				if(!isFound) {
					allCustomAnnotations.push(customACol);
				}
			}
		}
		
		var postFix = null;
		if(containerCode) {
			postFix = "ANNOTATIONS";
		} else {
			containerCode = mainController.currentView._sampleFormModel.sample.sampleTypeCode;
			postFix = "ANNOTATIONS_ALL" + linksModel.title;
		}
		
		var dataGrid = SampleDataGridUtil.getSampleDataGrid(containerCode, samplesOnGrid, null, linksView.getCustomOperationsForGrid(), allCustomAnnotations, postFix, linksModel.isDisabled, false);
		dataGrid.init($dataGridContainer);
		linksModel.writeState(sample, null, null, false);
	}
	
	this.repaint = function($container) {
		var $fieldsetOwner = $("<div>");
		var $legend = $("<legend>");
		var $fieldset = $("<div>");
		$fieldsetOwner.append($legend).append($fieldset);
		
		$container.empty();
		$container.append($fieldsetOwner);
		$savedContainer = $fieldset;
		
		var addAnyBtn = null;
		if(linksModel.disableAddAnyType) {
			addAnyBtn = "";
		} else {
			addAnyBtn = linksView.getAddAnyBtn();
		}
		
		$legend.append(linksModel.title).append("&nbsp;").append(addAnyBtn); //.css("margin-top", "20px").css("margin-bottom", "20px");
		
		$legend.prepend(FormUtil.getShowHideButton($fieldset, "SAMPLE-" + linksModel.sampleTypeCode + "-" + linksModel.title));
		
		$fieldset.append($samplePicker);
	}
	
	//
	// Internal API
	//
	
	linksView.showCopyProtocolPopUp = function(callback) {
		Util.blockUINoMessage();
		var component = "<div class='form-horizontal'>"
			component += "<legend>Copy Protocol</legend>";
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
		
		Util.blockUI(component + "<br><br><br> <a class='btn btn-default' id='copyAccept'>Accept</a> <a class='btn btn-default' id='copyCancel'>Cancel</a>", css);
		
		$("#newSampleCodeForCopy").on("keyup", function(event) {
			$(this).val($(this).val().toUpperCase());
		});
		
		$("#copyAccept").on("click", function(event) {
			var code = $("#newSampleCodeForCopy").val();
			if(code) {
				callback(code);
			} else {
				Util.showError("Code missing.");
			}
		});
		
		$("#copyCancel").on("click", function(event) { 
			Util.unblockUI();
		});
	}
	
	linksView.getCustomAnnotationColumns = function(sampleTypeCode) {
		var annotationDefinitions = linksModel.sampleTypeHints;
		
		var extraColumns = [];
		if(annotationDefinitions) {
			for(var aIdx = 0; aIdx < annotationDefinitions.length; aIdx++) {
				var annotationDefinition = annotationDefinitions[aIdx];
				if(annotationDefinition.TYPE === sampleTypeCode) {
					var annotationProperties = annotationDefinition.ANNOTATION_PROPERTIES;
					for(var pIdx = 0; pIdx < annotationProperties.length; pIdx++) {
						var annotationProperty = annotationProperties[pIdx];
						var propertyType = profile.getPropertyType(annotationProperty.TYPE);
						extraColumns.push(linksView.getCustomField(propertyType));
					}
				}
			}
		}
		return extraColumns;
	}
	
	linksView.getCustomField = function(propertyType) {
		var propertyAnnotationCode = "$ANNOTATION::" + propertyType.code;
		return {
			label : propertyType.label,
			property : propertyAnnotationCode,
			isExportable: true,
			showByDefault: true,
			sortable : true,
			render : function(data) {
				var sample = data["$object"];
				var currentValue = linksModel.readState(sample.permId, propertyType.code);
				
				if(linksModel.isDisabled) {
					return currentValue;
				} else {
					var $field = FormUtil.getFieldForPropertyType(propertyType);
					if (propertyType.dataType === "MULTILINE_VARCHAR") {
						$field.css({
							"height" : "100%",
							"width" : "100%"
						});
					}
					if(currentValue) {
						FormUtil.setFieldValue(propertyType, $field, currentValue);
					}
					$field.attr("id", ""); //Fix for current summernote behaviour
					$field.change(function() {
						var $field = $(this);
						propertyTypeValue = FormUtil.getFieldValue(propertyType, $field);
						linksModel.writeState(sample, propertyType.code, propertyTypeValue, false);
					});
					return $field;
				}
				
			}
		};
	}
	
	linksView.getCustomOperationsForGrid = function() {
		return {
			label : "Operations",
			property : 'operations',
			isExportable: false,
			showByDefault: true,
			sortable : false,
			render : function(data) {
				//Dropdown Setup
				var $dropDownMenu = $("<span>", { class : 'dropdown table-options-dropdown' });
				var $caret = $("<a>", { 'href' : '#', 'data-toggle' : 'dropdown', class : 'dropdown-toggle btn btn-default'}).append("Operations ").append($("<b>", { class : 'caret' }));
				var $list = $("<ul>", { class : 'dropdown-menu', 'role' : 'menu', 'aria-labelledby' :'sampleTableDropdown' });
				$dropDownMenu.append($caret);
				$dropDownMenu.append($list);
				
				var clickFunction = function($dropDown) {
					return function(event) {
						event.stopPropagation();
						event.preventDefault();
						$caret.dropdown('toggle');
					};
				}
				$dropDownMenu.dropdown();
				$dropDownMenu.click(clickFunction($dropDownMenu));
				
				if(profile.isSampleTypeProtocol(data["$object"].sampleTypeCode)) {
					var $copyAndLink = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Use as template'}).append("Use as template"));
					$copyAndLink.click(function(e) {
						
						var copyAndLink = function(code) {
							var newSampleIdentifier = "/" + mainController.currentView._sampleFormModel.sample.spaceCode + "/" + code;
							Util.blockUI();
							mainController.serverFacade.customELNApi({
								"method" : "copyAndLinkAsParent",
								"newSampleIdentifier" : newSampleIdentifier,
								"sampleIdentifierToCopyAndLinkAsParent" : data["$object"].identifier,
								"experimentIdentifierToAssignToCopy" : mainController.currentView._sampleFormModel.sample.experimentIdentifierOrNull
							}, function(error, result) {
								if(error) {
									Util.showError(error);
								} else {
									var searchUntilFound = null;
								    searchUntilFound = function() {
										mainController.serverFacade.searchWithIdentifiers([newSampleIdentifier], function(results) {
											if(results.length > 0) {
												linksView.updateSample(data["$object"], false);
												linksView.updateSample(results[0], true);
												Util.unblockUI();
											} else {
												searchUntilFound();
											}
										});
									};
									
									searchUntilFound();
								}
							});
						};
						
						linksView.showCopyProtocolPopUp(copyAndLink);
					});
					$list.append($copyAndLink);
				}
				
				var $delete = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Remove'}).append("Remove"));
				
				var getDeleteFunc = function(sample) {
					return function(e) {
						linksView.updateSample(sample, false);
					};
				}
				
				$delete.click(getDeleteFunc(data["$object"]));
				$list.append($delete);
				
				if(linksModel.isDisabled) {
					return "";
				} else {
					return $dropDownMenu;
				}
			}
		}
	}
	
	linksView.showSamplePicker = function($container, sampleTypeCode) {
		$container.empty().show();
		$container.css({
			"margin" : "5px",
			"padding" : "5px",
			"background-color" : "#f6f6f6"
		});
		
		//Close Button
		var $closeBtn = FormUtil.getButtonWithIcon("glyphicon-remove", function() {
			$container.empty().hide();
		});
		var $closeBtnContainer = $("<div>").append($closeBtn).css({"text-align" : "right", "padding-right" : "2px"});
		$container.append($closeBtnContainer);
		
		//Title
		$container.append($("<div>").append("Select " + sampleTypeCode + ":"));
		
		//Grid Contaienr
		var $gridContainer = $("<div>");
		$container.append($gridContainer);
		
		//Show Table Logic
		var extraOptions = [];
		extraOptions.push({ name : "Add selected", action : function(selected) {
			for(var sIdx = 0; sIdx < selected.length; sIdx++) {
				linksController.addSample(selected[sIdx]);
			}
			$container.empty().hide();
		}});
		
		var advancedSampleSearchCriteria = {
				entityKind : "SAMPLE",
				logicalOperator : "AND",
				rules : { "1" : { type : "Attribute", name : "SAMPLE_TYPE", value : sampleTypeCode } }
		}
		
		var rowClick = function(e) {
			linksController.addSample(e.data["$object"]);
			$container.empty().hide();
		}
		
		var dataGrid = SampleDataGridUtil.getSampleDataGrid(sampleTypeCode, advancedSampleSearchCriteria, rowClick, null, null, null, true, true, true);
		dataGrid.init($gridContainer, extraOptions);
	}
			
	linksView.getAddBtn = function($container, sampleTypeCode) {
		var enabledFunction = function() {
			linksView.showSamplePicker($container, sampleTypeCode);
		};
		
		var $addBtn = FormUtil.getButtonWithIcon("glyphicon-plus", (linksModel.isDisabled)?null:enabledFunction);
		if(linksModel.isDisabled) {
			return "";
		} else {
			return $addBtn;
		}
	}
	
	linksView.getAddAnyBtn = function() {
		var enabledFunction = function() {
			var $sampleTypesDropdown = FormUtil.getSampleTypeDropdown("sampleTypeSelector", true);
			Util.blockUI($sampleTypesDropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='sampleTypeSelectorCancel'>Cancel</a>");
			
			$("#sampleTypeSelector").on("change", function(event) {
				var sampleTypeCode = $(this).val();
				linksView.showSamplePicker($samplePicker, sampleTypeCode);
				Util.unblockUI();
			});
			
			$("#sampleTypeSelectorCancel").on("click", function(event) { 
				Util.unblockUI();
			});
		};
		
		var $addBtn = FormUtil.getButtonWithIcon("glyphicon-plus", (linksModel.isDisabled)?null:enabledFunction);
		
		if(linksModel.isDisabled) {
			return "";
		} else {
			return $addBtn;
		}
	}
}