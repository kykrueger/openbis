var FormMode = {
    CREATE : 0,
    EDIT : 1,
    VIEW : 2
}

var FormUtil = new function() {
	this.profile = null;
	
	//
	// Form css classes
	//
	this.shortformColumClass = 'col-md-9'
	this.formColumClass = 'col-md-12'
	this.labelColumnClass = 'col-md-2';
	this.shortControlColumnClass = 'col-md-5';
	this.controlColumnClass = 'col-md-9';
	this.controlColumnClassBig = 'col-md-9';
	//
	// Annotations
	//
	
	this.getXMLFromAnnotations = function(stateObj) {
		var rootNode = document.createElementNS("http://www.w3.org/1999/xhtml", "root"); //The namespace should be ignored by both ELN and openBIS parsers
		
		for(var permId in stateObj) {
			var sampleNode	= document.createElementNS("http://www.w3.org/1999/xhtml", "Sample"); //Should not add the namespace since is the same as the root
			sampleNode.setAttributeNS(null, "permId", permId); //Should not add the namespace
			
			for(var propertyTypeCode in stateObj[permId]) {
				var propertyTypeValue = stateObj[permId][propertyTypeCode];
				sampleNode.setAttributeNS(null, propertyTypeCode, propertyTypeValue); //Should not add the namespace
			}
			
			rootNode.appendChild(sampleNode);
		}
		
		var serializer = new XMLSerializer();
		var xmlDoc = serializer.serializeToString(rootNode);
		return xmlDoc;
	}
	
	this.getAnnotationsFromSample = function(sample) {
		var field = sample.properties["ANNOTATIONS_STATE"];
		var stateFieldValue = Util.getEmptyIfNull(field);
		if(stateFieldValue === "") {
			stateFieldValue = undefined;
			sample.properties["ANNOTATIONS_STATE"] = undefined;
		}
		return this.getAnnotationsFromField(stateFieldValue);
	}
	
	this.getAnnotationsFromField = function(field) {
		var stateObj = {};
		var stateFieldValue = Util.getEmptyIfNull(field);

		if(stateFieldValue === "") {
			return stateObj;
		}
		var xmlDoc = new DOMParser().parseFromString(stateFieldValue, 'text/xml');
		var samples = xmlDoc.getElementsByTagName("Sample");
		for(var i = 0; i < samples.length; i++) {
			var sample = samples[i];
			var permId = sample.attributes["permId"].value;
			for(var j = 0; j < sample.attributes.length; j++) {
				var attribute = sample.attributes[j];
				if(attribute.name !== "permId") {
					if(!stateObj[permId]) {
						stateObj[permId] = {};
					}
					stateObj[permId][attribute.name] = attribute.value;
				}
			}
		}
		return stateObj;
	}
	//
	// Standard Form Fields
	//
	
	this.getDropDownToogleWithSelectedFeedback = function(prefixElement, labelWithEvents, isSelectedFeedback, clickCallback) {
		var $dropDownToogle = $('<span>', { class : 'dropdown' });
		if(prefixElement) {
			$dropDownToogle.append(prefixElement);
		}
		$dropDownToogle.append($('<button>', { 'href' : '#', 'data-toggle' : 'dropdown', 'class' : 'dropdown-toggle btn btn-default'}).append($('<b>', { 'class' : 'caret' })));
		
		var $dropDownToogleOptions = $('<ul>', { class : 'dropdown-menu', 'role' : 'menu' });
		$dropDownToogle.append($dropDownToogleOptions);
		
		for(var i = 0; i < labelWithEvents.length; i++) {
			
			var selectedFeedback = $('<span>', { 'id' : 'dropdown-' + labelWithEvents[i].id });
			
			if(isSelectedFeedback && i === 0) {
				selectedFeedback.append("<span class='glyphicon glyphicon-ok'></span>");
			}
			
			var $a = $('<a>', { class : '', 'title' : labelWithEvents[i].title }).append(selectedFeedback).append('&nbsp;').append(labelWithEvents[i].title);
			
			var clickFunction = function(labelWithEvents, selectedIndex, isSelectedFeedback) {
				return function() {
					if(isSelectedFeedback) {
						for(var j = 0; j < labelWithEvents.length; j++) {
							$("#" + 'dropdown-' + labelWithEvents[j].id).empty();
							if(j === selectedIndex) {
								$("#" + 'dropdown-' + labelWithEvents[j].id).append("<span class='glyphicon glyphicon-ok'></span>");
							}
						}
					}
					
					labelWithEvents[selectedIndex].href();
					
					if(clickCallback) {
						clickCallback();
					}
				};
			}
			
			$a.click(clickFunction(labelWithEvents, i, isSelectedFeedback));
			$dropDownToogleOptions.append($('<li>', { 'role' : 'presentation' }).append($a));
		}	
		return $dropDownToogle;
	}
	
	this.getDefaultBenchDropDown = function(id, isRequired) {
		var $storageDropDown = this.getDefaultStoragesDropDown(id, isRequired);
		if(!$storageDropDown) {
			return null;
		}
		for(var i = $storageDropDown.children().length -1; i >= 0 ; i--){
			var isEmpty = $storageDropDown.children()[i].value === "";
			var isBench = $storageDropDown.children()[i].value.indexOf("BENCH") > -1;
			if(!isEmpty && !isBench){
				$storageDropDown.children()[i].remove();
		    }
		}
		return $storageDropDown;
	}
	
	this.getDefaultStorageBoxSizesDropDown = function(id, isRequired) {
		if(!this.profile.storagesConfiguration["isEnabled"]) {
			return null;
		}
		var storageBoxesVocabularyProp = this.profile.getPropertyType(this.profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["BOX_SIZE_PROPERTY"]);
		if(!storageBoxesVocabularyProp) {
			return null;
		}
		var $storageBoxesDropDown = this.getFieldForPropertyType(storageBoxesVocabularyProp);
		$storageBoxesDropDown.attr('id', id);
		if (isRequired) {
			$storageBoxesDropDown.attr('required', '');
		}
		return $storageBoxesDropDown;
	}
	
	this.getDefaultStoragesDropDown = function(id, isRequired) {
		if(!this.profile.storagesConfiguration["isEnabled"]) {
			return null;
		}
		var storageVocabularyProp = this.profile.getPropertyType(this.profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["NAME_PROPERTY"]);
		if(!storageVocabularyProp) {
			return null;
		}
		var $storageDropDown = this.getFieldForPropertyType(storageVocabularyProp);
		$storageDropDown.attr('id', id);
		if (isRequired) {
			$storageDropDown.attr('required', '');
		}
		return $storageDropDown;
	}
	
	this.getStoragePropertyGroupsDropdown = function(id, isRequired) {
		var propertyGroups = this.profile.getStoragePropertyGroups();
		
		var $component = $("<select>", {"id" : id, class : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		
		for(var i = 0; i < propertyGroups.length; i++) {
			var propertyGroup = propertyGroups[i];
			$component.append($("<option>").attr('value',propertyGroup.groupDisplayName).text(propertyGroup.groupDisplayName));
		}
		
		return $component;
	}
	
	this.getBoxPositionsDropdown = function(id, isRequired, code) {
		var numRows = null;
		var numCols = null;
		
		if(code) {
			var rowsAndCols = code.split("X");
			numRows = parseInt(rowsAndCols[0]);
			numCols = parseInt(rowsAndCols[1]);
		} else {
			numRows = 0;
			numCols = 0;
		}
		
		var $component = $("<select>", {"id" : id, class : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		
		for(var i = 1; i <= numRows; i++) {
			var rowLetter = Util.getLetterForNumber(i);
			for(var j = 1; j <= numCols; j++) {
				$component.append($("<option>").attr('value',rowLetter+j).text(rowLetter+j));
			}
			
		}
		return $component;
	}
	
	this.getSampleTypeDropdown = function(id, isRequired) {
		var sampleTypes = this.profile.getAllSampleTypes();
		
		var $component = $("<select>", {"id" : id, class : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').attr('disabled', '').text("Select a sample type"));
		for(var i = 0; i < sampleTypes.length; i++) {
			var sampleType = sampleTypes[i];
			if(profile.isSampleTypeHidden(sampleType.code)) {
				continue;
			}
			var label = Util.getEmptyIfNull(sampleType.description);
			if(label === "") {
				label = sampleType.code;
			}
			
			$component.append($("<option>").attr('value',sampleType.code).text(label));
		}
		
		return $component;
	}
	
	this.getExperimentTypeDropdown = function(id, isRequired) {
		var experimentTypes = this.profile.allExperimentTypes;
		
		var $component = $("<select>", {"id" : id, class : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text('Select an experiment type'));
		for(var i = 0; i < experimentTypes.length; i++) {
			var experimentType = experimentTypes[i];
			if(profile.isExperimentTypeHidden(experimentType.code)) {
				continue;
			}
			
			var label = experimentType.code;
			var description = Util.getEmptyIfNull(experimentType.description);
			if(description !== "") {
				label += " (" + description + ")";
			}
			
			$component.append($("<option>").attr('value',experimentType.code).text(label));
		}
		
		return $component;
	}
	
	this.getSpaceDropdown = function(id, isRequired) {
		var spaces = this.profile.allSpaces;
		
		var $component = $("<select>", {"id" : id, class : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < spaces.length; i++) {
			$component.append($("<option>").attr('value', spaces[i]).text(spaces[i]));
		}
		
		return $component;
	}
	
	this.getDropdown = function(mapVals, placeHolder) {

		var $component = $("<select>", {class : 'form-control'});
		if(placeHolder) {
			$component.append($("<option>").attr('value', '').attr('selected', '').attr('disabled', '').text(placeHolder));
		}
		for(var mIdx = 0; mIdx < mapVals.length; mIdx++) {
			var $option = $("<option>").attr('value', mapVals[mIdx].value).text(mapVals[mIdx].label);
			if(mapVals[mIdx].disabled) {
				$option.attr('disabled', '');
			}
			if(mapVals[mIdx].selected) {
				$option.attr('selected', '');
			}
			$component.append($option);
		}
		
		return $component;
	}
	
	
	this.getDataSetsDropDown = function(code, dataSetTypes) {
		var $component = $("<select>", { class : 'form-control ' });
		$component.attr('id', code);
		
		$component.attr('required', '');
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		
		for(var i = 0; i < dataSetTypes.length; i++) {
			var displayName = dataSetTypes[i].code;
			if(dataSetTypes[i].description) {
				var length = dataSetTypes[i].description.length > 40;
				if(dataSetTypes[i].description.length > 40) {
					displayName = dataSetTypes[i].description.substring(1,36) + " ...";
				} else {
					displayName = dataSetTypes[i].description;
				}
			}
			$component.append($("<option>").attr('value',dataSetTypes[i].code).text(displayName));
		}
		
		return $component;
	}
	
	this.getOptionsRadioButtons = function(name, isFirstSelected, values, changeAction) {
		var $component = $("<div>");
		for(var vIdx = 0; vIdx < values.length; vIdx++) {
			if(vIdx !== 0) {
				$component.append(" ");
			}
			var $radio = $("<input>", { type: "radio", name: name, value: values[vIdx]});
			
			if(isFirstSelected && (vIdx === 0)) {
				$radio.attr("checked", "");
			}
			$radio.change(changeAction);
			$component.append($radio);
			$component.append(" " + values[vIdx]);
		}
		return $component;
	}
	
	this.getProjectAndExperimentsDropdown = function(withProjects, withExperiments, isRequired, callbackForComponent) {
		mainController.serverFacade.listSpacesWithProjectsAndRoleAssignments(null, function(dataWithSpacesAndProjects) {
			var spaces = dataWithSpacesAndProjects.result;
            var projectsToUse = [];
            for (var i = 0; i < spaces.length; i++) {
            	var space = spaces[i];
            	for (var j = 0; j < space.projects.length; j++) {
                    var project = space.projects[j];
                    delete project["@id"];
                    delete project["@type"];
                    projectsToUse.push(project);
                }
            }
            
            mainController.serverFacade.listExperiments(projectsToUse, function(experiments) {
            	if(experiments.result) {
            		
            		for (var k = 0; k < experiments.result.length; k++) {
                		var experiment = experiments.result[k];
                		
                		for(var pIdx = 0; pIdx < projectsToUse.length; pIdx++) {
                    		var project = projectsToUse[pIdx];
                    		var projectIdentifier = "/" + project.spaceCode + "/" + project.code;
                    		if(experiment.identifier.startsWith(projectIdentifier)) {
                    			if(!project.experiments) {
                    				project.experiments = [];
                    			}
                    			project.experiments.push(experiment);
                    		}
                    	}
                	}
                	//
            		//
            		var $component = $("<select>", { class : 'form-control'});
            		if(isRequired) {
            			$component.attr('required', '');
            		}
            		var placeHolder = "";
            		if(withProjects && withExperiments) {
            			placeHolder = "Select a project or experiment";
            		} else if(withProjects) {
            			placeHolder = "Select a project";
            		} else if(withExperiments) {
            			placeHolder = "Select an experiment";
            		}
            		$component.append($("<option>").attr('value', '').attr('selected', '').attr('disabled', '').text(placeHolder));
            		for(var pIdx = 0; pIdx < projectsToUse.length; pIdx++) {
            			var project = projectsToUse[pIdx];
            			var projectIdentifier = "/" + project.spaceCode + "/" + project.code;
            			if(withProjects) {
            				$component.append($("<option>").attr('value', projectIdentifier).text(projectIdentifier));
            			}
            			if(project.experiments) {
            				for(var eIdx = 0; eIdx < project.experiments.length; eIdx++) {
                    			var experiment = project.experiments[eIdx];
                    			if(withExperiments) {
                    				$component.append($("<option>").attr('value',experiment.identifier).text(experiment.identifier));
                    			}
                			}
            			}
            		}
            		//
            		//
            		callbackForComponent($component);
            	}
            });
		});
	}
	
	this.getDeleteButton = function(deleteFunction, includeReason, warningText) {
		var $deleteBtn = $("<a>", { 'class' : 'btn btn-default ' });
		$deleteBtn.append($("<span>", { 'class' : 'glyphicon glyphicon-trash', 'style' : 'width:16px; height:16px;'}));
		$deleteBtn.click(function() {
			var modalView = new DeleteEntityController(deleteFunction, includeReason, warningText);
			modalView.init();
		});
		return $deleteBtn;
	}
	
	this.getButtonWithImage = function(src, clickEvent) {
		var $pinBtn = $("<a>", { 'class' : 'btn btn-default' });
		$pinBtn.append($("<img>", { 'src' : src, 'style' : 'width:16px; height:16px;'}));
		$pinBtn.click(clickEvent);
		return $pinBtn;
	}
	
	this.getButtonWithText = function(text, clickEvent, btnClass) {
		var auxBtnClass = "btn-default";
		if(btnClass) {
			auxBtnClass = btnClass;
		}
		var $pinBtn = $("<a>", { 'class' : 'btn ' + auxBtnClass });
		$pinBtn.append(text);
		$pinBtn.click(clickEvent);
		return $pinBtn;
	}
	
	this.getButtonWithIcon = function(iconClass, clickEvent) {
		var $pinBtn = $("<a>", { 'class' : 'btn btn-default' }).append($("<span>", { 'class' : 'glyphicon ' + iconClass }));
		$pinBtn.click(clickEvent);
		return $pinBtn;
	}
	
	this.getHierarchyButton = function(permId) {
		var $hierarchyButton = $("<a>", { 'class' : 'btn btn-default'} )
									.append($('<img>', { 'src' : './img/hierarchy-icon.png', 'style' : 'width:16px; height:17px;' }))
									.append(' G');
		$hierarchyButton.click(function() {
			mainController.changeView('showSampleHierarchyPage', permId);
		});
		return $hierarchyButton;
	}
	
	//
	// Get Field with container to obtain a correct layout
	//
	this.getFieldForComponentWithLabel = function($component, label, postComponent, isInline) {
		var $fieldset = $('<div>');
		
		var $controlGroup = $('<div>', {class : 'form-group'});
		var requiredText = '';
		if($component.attr('required')) {
			requiredText = "&nbsp;(*)"
		}
		
		var labelText = "";
		if(label) {
			labelText = label + requiredText + ":";
		}
		var labelColumnClass = ""
		if(!isInline) {
			labelColumnClass = this.labelColumnClass;
		}
		var $controlLabel = $('<label>', { class : 'control-label ' + labelColumnClass }).html(labelText);
		
		var controlColumnClass = ""
		if(!isInline) {
			controlColumnClass = this.controlColumnClass;
		}
		var $controls = $('<div>', { class : 'controls ' + controlColumnClass });
			
		$controlGroup.append($controlLabel);
		
		if(isInline) {
			$controlGroup.append($component);
		} else {
			$controls.append($component);
			$controlGroup.append($controls);
		}
		
		if(postComponent) {
			$controlGroup.append(postComponent);
		}
		$fieldset.append($controlGroup);
		
		if(isInline) {
			return $controlGroup;
		} else {
			return $fieldset;
		}
	}
	
	this.getFieldForLabelWithText = function(label, text, id, postComponent, cssForText) {
		var $fieldset = $('<div>');
		
		var $controlGroup = $('<div>', {class : 'form-group'});
		
		var $controlLabel = $('<label>', {class : 'control-label ' + this.labelColumnClass});
		if(label) {
			$controlLabel.text(label + ":");
		}
		
		var $controls = $('<div>', {class : 'controls ' + this.controlColumnClass });
		
		$controlGroup.append($controlLabel);
		$controlGroup.append($controls);
		if(postComponent) {
			$controlGroup.append(postComponent);
		}
		$fieldset.append($controlGroup);
		
		var $component = $("<p>", {'class' : 'form-control-static', 'style' : 'border:none; box-shadow:none; background:transparent; word-wrap: break-word; white-space: pre-wrap;'});
		if(cssForText) {
			$component.css(cssForText);
		}
		
		if(text) {
			text = text.replace(/(?:\r\n|\r|\n)/g, '\n'); //Normalise carriage returns
		}
		
		$component.html(text);
		if(id) {
			$component.attr('id', id);
		}
		$controls.append($component);
		
		return $fieldset;
	}

	//
	// Get Field from property
	//
	this.getVocabularyLabelForTermCode = function(propertyType, termCode) {
		var vocabulary = propertyType.vocabulary;
		if(vocabulary) {
			for(var tIdx = 0; tIdx < vocabulary.terms.length; tIdx++) {
				if(vocabulary.terms[tIdx].code === termCode) {
					return vocabulary.terms[tIdx].label;
				}
			}
		}
		return termCode;
	}
	
	this.getFieldForPropertyType = function(propertyType) {
		var $component = null;
		if (propertyType.dataType === "BOOLEAN") {
			$component = this._getBooleanField(propertyType.code, propertyType.description);
		} else if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
			$component = this._getDropDownFieldForVocabulary(propertyType.code, propertyType.vocabulary.terms, propertyType.description, propertyType.mandatory);
		} else if (propertyType.dataType === "HYPERLINK") {
			$component = this._getInputField("url", propertyType.code, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "INTEGER") {
			$component = this._getInputField("number", propertyType.code, propertyType.description, '1', propertyType.mandatory);
		} else if (propertyType.dataType === "MATERIAL") {
			$component = this._getInputField("text", propertyType.code, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "MULTILINE_VARCHAR") {
			$component = this._getTextBox(propertyType.code, propertyType.description, propertyType.mandatory);
		} else if (propertyType.dataType === "REAL") {
			$component = this._getInputField("number", propertyType.code, propertyType.description, 'any', propertyType.mandatory);
		} else if (propertyType.dataType === "TIMESTAMP") {
			$component = this._getDatePickerField(propertyType.code, propertyType.description, propertyType.mandatory);
		} else if (propertyType.dataType === "VARCHAR") {
			$component = this._getInputField("text", propertyType.code, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "XML") {
			$component = this._getTextBox(propertyType.code, propertyType.description, propertyType.mandatory);
		}
		
		return $component;
	}
	
	//
	// Read/Write Fields
	//
	this.setFieldValue = function(propertyType, $field, value) {
		if(propertyType.dataType === "BOOLEAN") {
			$($($field.children()[0]).children()[0]).prop('checked', value === "true");
		} else if(propertyType.dataType === "TIMESTAMP") {
			$($($field.children()[0]).children()[0]).val(value);
		} else {
			$field.val(value);
		}
	}
	
	this.getFieldValue = function(propertyType, $field) {
		var propertyTypeValue;
		if (propertyType.dataType === "BOOLEAN") {
			propertyTypeValue = $field.children().is(":checked");
		} else {
			propertyTypeValue = $field.val();
		}
		return propertyTypeValue;
	}
	
	//
	// Form Fields
	//
	this._getBooleanField = function(id, alt) {
		return $('<div>', {'class' : 'checkbox'}).append($('<label>').append($('<input>', {'type' : 'checkbox', 'id' : id, 'alt' : alt, 'placeholder' : alt })));
	}
	
	this.getDropDownForTerms = function(id, terms, alt, isRequired) {
		return this._getDropDownFieldForVocabulary(id, terms, alt, isRequired);
	}
	
	this._getDropDownFieldForVocabulary = function(code, terms, alt, isRequired) {
		var $component = $("<select>", {'placeholder' : alt, 'class' : 'form-control'});
		$component.attr('id', code);
		
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(alt));
		for(var i = 0; i < terms.length; i++) {
			$component.append($("<option>").attr('value',terms[i].code).text(terms[i].label));
		}
		
		return $component;
	}
	
	this.getTextInputField = function(id, alt, isRequired) {
		return this._getInputField('text', id, alt, null, isRequired);
	}
	
	this._getInputField = function(type, id, alt, step, isRequired) {
		var $component = $('<input>', {'type' : type, 'id' : id, 'alt' : alt, 'placeholder' : alt, 'class' : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		if (step) {
			$component.attr('step', step);
		}
		return $component;
	}
	
	this._getTextBox = function(id, alt, isRequired) {
		var $component = $('<textarea>', {'id' : id, 'alt' : alt, 'style' : 'height: 80px; width: 450px;', 'placeholder' : alt, 'class' : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		return $component;
	}
	
	this._getDatePickerField = function(id, alt, isRequired) {
		var $component = $('<div>', {'class' : 'form-group', 'style' : 'margin-left: 0px;', 'placeholder' : alt });
		var $subComponent = $('<div>', {'class' : 'input-group date', 'id' : 'datetimepicker_' + id });
		var $input = $('<input>', {'class' : 'form-control', 'type' : 'text', 'id' : id, 'data-format' : 'yyyy-MM-dd HH:mm:ss'});
		if (isRequired) {
			$input.attr('required', '');
		}
		var $spanAddOn = $('<span>', {'class' : 'input-group-addon'})
							.append($('<span>', {'class' : 'glyphicon glyphicon-calendar' }));
		
		$subComponent.append($input);
		$subComponent.append($spanAddOn);
		$subComponent.datetimepicker({ format : 'YYYY-MM-DD HH:mm:ss', useCurrent : false });
		$component.append($subComponent);
		
		return $component;
	}
	
	
	//
	// Rich Text Editor Support - (Summernote)
	//
	this.activateRichTextProperties = function($component, componentOnChange) {
		$("body").append($component);
		
		$component.summernote({
			toolbar: [
		['Font Style', ['fontname', 'fontsize', 'color', 'bold', 'italic', 'underline', 'strikethrough', 'superscript', 'subscript', 'clear']],
		['Paragraph style ', ['style', 'ol', 'ul', 'paragraph', 'height']],
		['Insert', ['link', 'table', 'hr', 'specialCharacter']],
		['Misc', ['fullscreen', 'undo', 'redo', 'help']],],
		disableDragAndDrop: true});
		
		$component.on("summernote.change", componentOnChange);
		
		$('.note-editable.panel-body').css({ "min-height" : "200px" });
		
		var $editor = $component.next();
		$component.detach();
		$editor.detach();
		
		return $editor;
	}
	
	this.fixStringPropertiesForForm = function(propertyType, entity) {
		var originalValue = entity.properties[propertyType.code];
		if (propertyType.dataType !== "XML") {
			entity.properties[propertyType.code] = this.sanitizeRichHTMLText(originalValue);
		}
	}
	
	this.sanitizeRichHTMLText = function(originalValue) {
		if(typeof originalValue === "string") {
			//Take envelope out if pressent
			var bodyStart = originalValue.indexOf("<body>");
			var bodyEnd = originalValue.indexOf("</body>");
			if(bodyStart !== -1 && bodyEnd !== -1) {
				originalValue = originalValue.substring(bodyStart + 6, bodyEnd);
			}
			//Clean the contents
			originalValue = html.sanitize(originalValue);
		}
		return originalValue;
	}
	
	this.getToolbar = function(toolbarModel) {
		var $toolbarContainer = $("<div>", { class : 'toolBox', style : "width: 100%;" });
		
		for(var tbIdx = 0; tbIdx < toolbarModel.length; tbIdx++) {
			var $toolbarComponent = toolbarModel[tbIdx].component;
			var toolbarComponentTooltip = toolbarModel[tbIdx].tooltip;
			if(toolbarComponentTooltip) {
				$toolbarComponent.attr("title", toolbarComponentTooltip);
				$toolbarComponent.tooltipster();
			}
			$toolbarContainer.append($toolbarComponent);
			$toolbarContainer.append("&nbsp;");
		}
		
		return $toolbarContainer;
	}
	
	this.getFormLink = function(displayName, entityKind, permIdOrIdentifier) {
		var view = null;
		switch(entityKind) {
			case "Sample":
				view = "showViewSamplePageFromPermId";
				break;
			case "Experiment":
				view = "showExperimentPageFromIdentifier";
				break;
			case "DataSet":
				view = "showViewDataSetPageFromPermId";
				break;
		}
		
		var href = Util.getURLFor(mainController.sideMenu.getCurrentNodeId(), view, permIdOrIdentifier);
		var click = function() {
			mainController.changeView(view, permIdOrIdentifier);
		}
		var link = $("<a>", { "href" : href, "class" : "browser-compatible-javascript-link" }).append(displayName);
		link.click(click);
		return link;
	}
}