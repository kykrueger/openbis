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
	
	var samplesByTypeCache = {};
	
	var $samplePicker = $("<div>");
	var $savedContainer = null;
	
	//
	// External API
	//
	this.addSample = function(sample) {
		var sampleTypeCode = sample.sampleTypeCode;
		var $dataGridContainer = sampleGridContainerByType[sampleTypeCode];
		var samplesOnGrid = linksModel.samplesByType[sampleTypeCode];
		
		//Create Model
		if(!samplesOnGrid) {
			samplesOnGrid = [];
			linksModel.samplesByType[sampleTypeCode] = samplesOnGrid;
		}
		
		//Check if the sample is already added
		for(var sIdx = 0; sIdx < samplesOnGrid.length; sIdx++) {
			if(samplesOnGrid[sIdx].permId === sample.permId) {
				Util.showError("Sample " + sample.code + " already present, it will not be added again.");
				return;
			}
		}
		
		//Create Layout
		if(!$dataGridContainer) { //Create if is not there yet
			//Layout
			var $sampleTableContainer = $("<div>");
			$sampleTableContainer.append($("<div>").append(sampleTypeCode + ":"));
			
			$dataGridContainer = $("<div>");
			$sampleTableContainer.append($dataGridContainer);
			
			sampleGridContainerByType[sampleTypeCode] = $dataGridContainer;
			
			$savedContainer.append($sampleTableContainer);
		}
		
		//Render Grid
		$dataGridContainer.empty();
		samplesOnGrid.push(sample);
		
		var dataGrid = SampleDataGridUtil.getSampleDataGrid(sampleTypeCode, samplesOnGrid, null, linksView.getCustomOperationsForGrid(), linksView.getCustomAnnotationColumns(sampleTypeCode), "ANNOTATIONS");
		dataGrid.init($dataGridContainer);
	}
	
	this.repaint = function($container) {
		$savedContainer = $container;
		$container.empty();
		$container.append($("<legend>").append(linksModel.title));
		$container.append($samplePicker);
		$container.append(linksView.getAddAnyBtn());
	}
	
	//
	// Internal API
	//
	
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
			label : "Annot.::" + propertyType.label,
			property : propertyAnnotationCode,
			isExportable: true,
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
					$field.change(function() {
						var $field = $(this);
						propertyTypeValue = FormUtil.getFieldValue(propertyType, $field);
						sample[propertyAnnotationCode] = propertyTypeValue;
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
				
				var $copyAndLink = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Copy and Link'}).append("Copy and Link"));
				$copyAndLink.click(function(e) {
					
				});
				$list.append($copyAndLink);
				
				if(linksModel.isDisabled) {
					return "";
				} else {
					return $dropDownMenu;
				}
			}
		}
	}
	
	linksView.showSamplePicker = function(sampleType) {
		$samplePicker.empty();
		
		//Close Button
		var $closeBtn = FormUtil.getButtonWithIcon("glyphicon-remove", function() {
			$samplePicker.empty();
		});
		var $closeBtnContainer = $("<div>").append($closeBtn).css({"text-align" : "right", "padding-right" : "2px"});
		$samplePicker.append($closeBtnContainer);
		
		//Grid Contaienr
		var $gridContainer = $("<div>");
		$samplePicker.append($gridContainer);
		
		//Show Table Logic
		var showTableFunction = function(samples) {
			samplesByTypeCache[sampleType.code] = samples;
			
			var rowClick = function(e) {
				linksController.addSample(e.data["$object"]);
				$samplePicker.empty();
			}
			
			var dataGrid = SampleDataGridUtil.getSampleDataGrid(sampleType.code, samples, rowClick);
			dataGrid.init($gridContainer);
		}
		
		//Check Cache and Show Table
		var sampleTypeCache = samplesByTypeCache[sampleType.code];
		if(sampleTypeCache) {
			showTableFunction(sampleTypeCache);
		} else {
			mainController.serverFacade.searchWithType(sampleType.code, null, false, showTableFunction);
		}
	}
			
	linksView.getAddAnyBtn = function() {
		var enabledFunction = function() {
			var $sampleTypesDropdown = FormUtil.getSampleTypeDropdown("sampleTypeSelector", true);
			Util.blockUI("Select type: <br><br>" + $sampleTypesDropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='sampleTypeSelectorCancel'>Cancel</a>");
			
			$("#sampleTypeSelector").on("change", function(event) {
				var sampleTypeCode = $(this).val();
				var sampleType = profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
				
				linksView.showSamplePicker(sampleType);
				Util.unblockUI();
			});
			
			$("#sampleTypeSelectorCancel").on("click", function(event) { 
				Util.unblockUI();
			});
		};
		
		var $addBtn = FormUtil.getButtonWithIcon("glyphicon-plus", (linksModel.isDisabled)?null:enabledFunction);
		if(linksModel.isDisabled) {
			$addBtn.attr("disabled", "");
		}
		
		return $addBtn;
	}
}