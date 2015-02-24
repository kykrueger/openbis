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
	this.controlColumnClass = 'col-md-6';
	this.controlColumnClassBig = 'col-md-10';
	//
	// Annotations
	//
	
	this.getAnnotationsFromSample = function(sample) {
		var stateObj = {};
		var stateFieldValue = sample.properties["ANNOTATIONS_STATE"];

		if(!stateFieldValue) {
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
	
	this.getSampleTypeDropdown = function(id, isRequired) {
		var sampleTypes = this.profile.getAllSampleTypes();
		
		var $component = $("<select>", {"id" : id, class : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < sampleTypes.length; i++) {
			var sampleType = sampleTypes[i];
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
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < experimentTypes.length; i++) {
			var experimentType = experimentTypes[i];
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
	
	this.getDeleteButton = function(deleteFunction, includeReason) {
		var $deleteBtn = $("<a>", { 'class' : 'btn btn-default ' });
		$deleteBtn.append($("<span>", { 'class' : 'glyphicon glyphicon-trash', 'style' : 'width:16px; height:16px;'}));
		$deleteBtn.click(function() {
			var modalView = new DeleteEntityController(deleteFunction, includeReason);
			modalView.init();
		});
		return $deleteBtn;
	}
	
	this.getPINButton = function(permId) {
		var inspectedClass = "";
		if(mainController.inspector.containsByPermId(permId) !== -1) {
			inspectedClass = "inspectorClicked";
		}
		
		var $pinBtn = $("<a>", { 'id' : 'pinButton_' + permId, 'class' : 'btn btn-default ' + inspectedClass });
		$pinBtn.append($("<img>", { 'src' : './img/pin-icon.png', 'style' : 'width:16px; height:16px;'}));
		$pinBtn.click(function() {
			var isInspected = mainController.inspector.toggleInspectPermId(permId);
			if(isInspected) {
				$('#pinButton_' + permId).addClass('inspectorClicked');
			} else {
				$('#pinButton_' + permId).removeClass('inspectorClicked');
			}
		});
		return $pinBtn;
	}
	
	this.getButtonWithImage = function(src, clickEvent) {
		var $pinBtn = $("<a>", { 'class' : 'btn btn-default' });
		$pinBtn.append($("<img>", { 'src' : src, 'style' : 'width:16px; height:16px;'}));
		$pinBtn.click(clickEvent);
		return $pinBtn;
	}
	
	this.getButtonWithText = function(text, clickEvent) {
		var $pinBtn = $("<a>", { 'class' : 'btn btn-default' });
		$pinBtn.append(text);
		$pinBtn.click(clickEvent);
		return $pinBtn;
	}
	
	this.getHierarchyButton = function(permId) {
		var $hierarchyButton = $("<a>", { 'class' : 'btn btn-default'} )
									.append($('<img>', { 'src' : './img/hierarchy-icon.png', 'style' : 'width:16px; height:17px;' }));
		$hierarchyButton.click(function() {
			mainController.changeView('showSampleHierarchyPage', permId);
		});
		return $hierarchyButton;
	}
	
	//
	// Get Field with container to obtain a correct layout
	//
	this.getFieldForComponentWithLabel = function($component, label, postComponent) {
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
		
		var $controlLabel = $('<label>', { class : 'control-label ' + this.labelColumnClass }).html(labelText);
		var $controls = $('<div>', { class : 'controls ' + this.controlColumnClass });
			
		$controlGroup.append($controlLabel);
		$controlGroup.append($controls);
		if(postComponent) {
			$controlGroup.append(postComponent);
		}
		$fieldset.append($controlGroup);
		
		$controls.append($component);
		
		
		return $fieldset;
	}
	
	this.getFieldForLabelWithText = function(label, text, id, postComponent, cssForText) {
		var $fieldset = $('<div>');
		
		var $controlGroup = $('<div>', {class : 'form-group'});
		var $controlLabel = $('<label>', {class : 'control-label ' + this.labelColumnClass}).text(label + ":");
		var $controls = $('<div>', {class : 'controls ' + this.controlColumnClass });
		
		$controlGroup.append($controlLabel);
		$controlGroup.append($controls);
		if(postComponent) {
			$controlGroup.append(postComponent);
		}
		$fieldset.append($controlGroup);
		
		var $component = $("<p>", {'class' : 'form-control-static', 'style' : 'border:none; box-shadow:none; background:transparent;'});
		if(cssForText) {
			$component.css(cssForText);
		}
		$component.text(text);
		if(id) {
			$component.attr('id', id);
		}
		$controls.append($component);
		
		return $fieldset;
	}

	//
	// Get Field from property
	//
	this.getFieldForPropertyType = function(propertyType) {
		var $component = null;
		if (propertyType.dataType === "BOOLEAN") {
			$component = this._getBooleanField(propertyType.code, propertyType.description);
		} else if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
			var vocabulary = null;
			if(isNaN(propertyType.vocabulary)) {
				vocabulary = this.profile.getVocabularyById(propertyType.vocabulary.id);
				if(vocabulary === null) { //This should not happen, but can save the day.
					vocabulary = propertyType.vocabulary;
					vocabulary.terms = propertyType.terms;
				}
			} else {
				vocabulary = this.profile.getVocabularyById(propertyType.vocabulary);
			}
			
			$component = this._getDropDownFieldForVocabulary(propertyType.code, vocabulary.terms, propertyType.description, propertyType.mandatory);
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
	// Form Fields
	//
	this._getBooleanField = function(id, alt) {
		return $('<div>', {'class' : 'checkbox'}).append($('<input>', {'type' : 'checkbox', 'id' : id, 'alt' : alt, 'placeholder' : alt }));
	}
	
	this._getDropDownFieldForVocabulary = function(code, terms, alt, isRequired) {
		var $component = $("<select>", {'placeholder' : alt, 'class' : 'form-control'});
		$component.attr('id', code);
		
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < terms.length; i++) {
			$component.append($("<option>").attr('value',terms[i].code).text(terms[i].label));
		}
		
		return $component;
	}
	
	this._getInputField = function(type, id, alt, step, isRequired) {
		var $component = $('<input>', {'type' : type, 'id' : id, 'alt' : alt, 'placeholder' : alt, 'class' : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		if (isRequired) {
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
		$subComponent.datetimepicker({ language: 'en' });
		$component.append($subComponent);
		
		return $component;
	}
}