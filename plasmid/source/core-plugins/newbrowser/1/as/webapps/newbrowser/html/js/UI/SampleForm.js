SampleFormMode = {
    CREATE : 0,
    EDIT : 1,
    VIEW : 2
}

function SampleForm(containerId, profile, sampleTypeCode, isELNExperiment, mode, sample) {
	this.containerId = containerId;
	this.profile = profile;
	this.sampleTypeCode = sampleTypeCode;
	this.isELNExperiment = isELNExperiment;
	this.projects = [];
	this.spaces = [];
	this.sampleTypesLinksTables = {};
	this.mode = mode;
	this.sample = sample;
	
	this.init = function() {
			Util.blockUI();
			var localReference = this;
			openbisServer.listSpacesWithProjectsAndRoleAssignments(null, function(data) {
				//Init Basic Form elements
				localReference.listSpacesWithProjectsAndRoleAssignmentsCallback(data);
				localReference.repaint();
				//Check Mode
				if(localReference.mode === SampleFormMode.CREATE) {
					//Do Nothing
				} else if(localReference.mode === SampleFormMode.EDIT || localReference.mode === SampleFormMode.VIEW) {
					
					var sample = localReference.sample;
					//Populate Project/Space and Code
					if(localReference.isELNExperiment) {
						var project = sample.experimentIdentifierOrNull.split("/")[0] + "/" + sample.experimentIdentifierOrNull.split("/")[1];
						$("#sampleSpaceProject").val(project);
					} else {
						$("#sampleSpaceProject").val(sample.spaceCode);
					}
					$("#sampleSpaceProject").prop('disabled', true);
					
					$("#sampleCode").val(sample.code);
					$("#sampleCode").prop('disabled', true);
					
					//Populate fields
					var sampleType = profile.getTypeForTypeCode(localReference.sampleTypeCode);
					for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
						var propertyTypeGroup = sampleType.propertyTypeGroups[i];
						for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
							var propertyType = propertyTypeGroup.propertyTypes[j];
							if(propertyType.dataType === "BOOLEAN") {
								$("#"+propertyType.code).prop('checked', sample.properties[propertyType.code] === "true");
							} else {
								$("#"+propertyType.code).val(sample.properties[propertyType.code]);
							}
						}
					}
						
					//Disable fields if needed
					if (localReference.mode === SampleFormMode.VIEW) {
						var sampleType = profile.getTypeForTypeCode(localReference.sampleTypeCode);
						for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
							var propertyTypeGroup = sampleType.propertyTypeGroups[i];
							for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
								var propertyType = propertyTypeGroup.propertyTypes[j];
								$("#"+propertyType.code).prop('disabled', true);
							}
						}	
					}
					
					//Populate Links
					for (var i = 0; i < sample.parents.length; i++) {
						var parent = sample.parents[i];
						var parentGroup = localReference.profile.getGroupTypeCodeForTypeCode(parent.sampleTypeCode);
						var linkTableId = "sampleParents_" + parentGroup;
						localReference.sampleTypesLinksTables[linkTableId].addSample(parent);
					}
				}
				//Allow user input
				Util.unblockUI();
			});
	}
	
	this.listSpacesWithProjectsAndRoleAssignmentsCallback = function(data) {
		for(var i = 0; i < data.result.length; i++) {
			this.spaces.push(data.result[i].code);
			if(data.result[i].projects) {
				for(var j = 0; j < data.result[i].projects.length; j++) {
					this.projects.push("/" + data.result[i].projects[j].spaceCode + "/" + data.result[i].projects[j].code);
				}
			}
		}
	}
	
	this.getTextBox = function(id, alt, isRequired) {
		var component = "<textarea id='" + id + "' alt='" + alt + "' style='height: 80px; width: 450px;'";
		
		if (isRequired) {
			component += "required></textarea> (Required)";
		} else {
			component += "></textarea>";
		}
		
		return component;
	}
	
	this.getBooleanField = function(id, alt) {
		return "<input type='checkbox' id='" + id + "' alt='" + alt + "' >";
	}
	
	this.getInputField = function(type, id, alt, isRequired) {
		var component = "<input type='" + type + "' id='" + id + "' alt='" + alt + "' ";
		
		if (isRequired) {
			component += "required> (Required)";
		} else {
			component += ">";
		}
		
		return component;
	}
	
	this.getNumberInputField = function(step, id, alt, isRequired) {
		var component = "<input type='number' id='" + id + "' alt='" + alt + "' step='" + step + "'";
		
		if (isRequired) {
			component += "required> (Required)";
		} else {
			component += ">";
		}
		
		return component;
	}
	
	this.getDropDownField = function(code, terms, isRequired) {
		var component = "<select id='" + code + "' ";
		
		if (isRequired) {
			component += "required>";
		} else {
			component += ">";
		}
		
		component += "<option value='' selected></option>";
		for(var i = 0; i < terms.length; i++) {
			component += "<option value='" + terms[i].code + "'>" + terms[i].label + "</option>";
		}
		component += "</select> ";
		
		if (isRequired) {
			component += " (Required)";
		}
		
		return component;
	}
	
	this.getDatePickerField = function(id, alt, isRequired) {	
		var component  = "<div class='well' style='width: 250px;'>";
			component += "<div id='datetimepicker_" + id + "' class='input-append date'>";
			
			component += "<input id='" + id + "' data-format='yyyy-MM-dd HH:mm:ss' type='text' ";
			
			if (isRequired) {
				component += "required></input>";
			} else {
				component += "></input>";
			}
			
			component += "<span class='add-on'>";
			component += "<i data-time-icon='icon-time' data-date-icon='icon-calendar'></i>";
			component += "</span>";
			
			component += "</div>";
			component += "</div>";
			
			component += "<script type='text/javascript'> $(function() { $('#datetimepicker_" + id + "').datetimepicker({ language: 'en' });  }); </script>";
			
			return component;
	}
	
	this.getLinksToParentsComponent = function() {
		var component = "<fieldset>";
		
		if (this.mode !== SampleFormMode.VIEW) {
			component += "<legend>Components:</legend>";
			//Print one drop down for each group
			component += "<center>";
		
			for(typeGroupCode in this.profile.typeGroups) {
				var sampleGroupTypeDisplayName = this.profile.typeGroups[typeGroupCode]["DISPLAY_NAME"];
			
				component += "<select onchange='sampleForm.showSamplesWithoutPage(event)'>";
				component += "<option value=''> -- "+sampleGroupTypeDisplayName+" --</a></option>";
				for(var i = 0; i < this.profile.typeGroups[typeGroupCode]["LIST"].length; i++) {
					var sampleType = this.profile.getTypeForTypeCode(this.profile.typeGroups[typeGroupCode]["LIST"][i]);
					component += "<option value='"+sampleType.code+"'>"+sampleType.description+"</a></option>";
				}
				component += "</select> ";
			}
			
			component += "</center>";
			
			component += "<div id='sampleSearchContainer'></div>";
		}
		
		component += "<legend>Selected Components:</legend>";
		
		this.sampleTypesLinksTables = {};
		
		for(typeGroupCode in this.profile.typeGroups) {
			var id = "sampleParents_" + typeGroupCode;
			var sampleGroupTypeDisplayName = this.profile.typeGroups[typeGroupCode]["DISPLAY_NAME"];
			
			component += "<div class='control-group'>";
			component += "<label class='control-label'>" + sampleGroupTypeDisplayName + ":</label>";
			component += "<div class='controls'>";
			component += "<div id='"+id+"'></div>";
			component += "</div>";
			component += "</div>";
			
			var disableLinksTables = this.mode === SampleFormMode.VIEW;
			this.sampleTypesLinksTables[id] = new SampleLinksTable(id, this.profile, disableLinksTables);
		}
		
		component += "</fieldset>";
		
		return component;
	}
	
	this.repaint = function() {
		$("#"+this.containerId).empty();
		
		var sampleType = profile.getTypeForTypeCode(this.sampleTypeCode);
		var sampleTypeDisplayName = sampleType.description;
		
		var component = "";
		
			component += "<div class='row-fluid'>";
			component += "<div class='span12'>";
			
			var message = null;
			if (this.mode === SampleFormMode.CREATE) {
				message = "Create";
			} else if (this.mode === SampleFormMode.EDIT) {
				message = "Update";
			} else if (this.mode === SampleFormMode.VIEW) {
				message = "View";
			}
			
			component += "<h2>" + message + " " + sampleTypeDisplayName + "</h2>";
			
			component += "<form class='form-horizontal' action='javascript:void(0);' onsubmit='sampleForm.createSample();'>";
			
			//
			// SELECT PROJECT/SPACE AND CODE
			//
			component += "<fieldset>";
			component += "<legend>Identification Info:</legend>";
			component += "<div class='control-group'>";
			if(this.isELNExperiment) {
				component += "<label class='control-label' for='inputSpace'>Project:</label>";
			} else {
				component += "<label class='control-label' for='inputSpace'>Space:</label>";
			}
			
			component += "<div class='controls'>";
			if(this.isELNExperiment) {
				component += "<select id='sampleSpaceProject' required>";
				component += "<option disabled=\"disabled\" selected></option>";
				for(var i = 0; i < this.projects.length; i++) {
					component += "<option value='"+this.projects[i]+"'>"+this.projects[i]+"</option>";
				}
				component += "</select> (Required)";
			} else {
				component += "<select id='sampleSpaceProject' required>";
				component += "<option disabled=\"disabled\" selected></option>";
				for(var i = 0; i < this.spaces.length; i++) {
					component += "<option value='"+this.spaces[i]+"'>"+this.spaces[i]+"</option>";
				}
				component += "</select> (Required)";
			}
			component += "</div>";
			component += "</div>";
			
			component += "<div class='control-group'>";
			component += "<label class='control-label' for='inputCode'>Code:</label>";
			component += "<div class='controls'>";
			component += "<input type='text' placeholder='Code' id='sampleCode' required> (Required)";
			component += "</div>";
			component += "</div>";
			component += "</fieldset>";
			
			//
			// LINKS TO PARENTS
			//
			component += this.getLinksToParentsComponent();
			
			//
			// SAMPLE TYPE FIELDS
			//
			for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
				var propertyTypeGroup = sampleType.propertyTypeGroups[i];
				component += "<fieldset>";
				
				if(propertyTypeGroup.name) {
					component += "<legend>" + propertyTypeGroup.name + ":</legend>";
				} else {
					component += "<legend></legend>";
				}
				
				for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
					var propertyType = propertyTypeGroup.propertyTypes[j];
					
					component += "<div class='control-group'>";
					component += "<label class='control-label' for='inputCode'>" + propertyType.label + ":</label>";
					component += "<div class='controls'>";
					
					if (propertyType.dataType === "BOOLEAN") {
						component += this.getBooleanField(propertyType.code, propertyType.description);
					} else if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
						var vocabulary = null;
						if(isNaN(propertyType.vocabulary)) {
							vocabulary = this.profile.getVocabularyById(propertyType.vocabulary.id);
						} else {
							vocabulary = this.profile.getVocabularyById(propertyType.vocabulary);
						}
						component += this.getDropDownField(propertyType.code, vocabulary.terms, propertyType.mandatory);
					} else if (propertyType.dataType === "HYPERLINK") {
						component += this.getInputField("url", propertyType.code, propertyType.description, propertyType.mandatory);
					} else if (propertyType.dataType === "INTEGER") {
						component += this.getNumberInputField("1", propertyType.code, propertyType.description, propertyType.mandatory);
					} else if (propertyType.dataType === "MATERIAL") {
						component += this.getInputField("text", propertyType.code, propertyType.description, propertyType.mandatory);
					} else if (propertyType.dataType === "MULTILINE_VARCHAR") {
						component += this.getTextBox(propertyType.code, propertyType.description, propertyType.mandatory);
					} else if (propertyType.dataType === "REAL") {
						component += this.getNumberInputField("any", propertyType.code, propertyType.description, propertyType.mandatory);
					} else if (propertyType.dataType === "TIMESTAMP") {
						component += this.getDatePickerField(propertyType.code, propertyType.description, propertyType.mandatory);
					} else if (propertyType.dataType === "VARCHAR") {
						component += this.getInputField("text", propertyType.code, propertyType.description, propertyType.mandatory);
					} else if (propertyType.dataType === "XML") {
						component += this.getTextBox(propertyType.code, propertyType.description, propertyType.mandatory);
					}
					
					component += "</div>";
					component += "</div>";
					
				}
				component += "</fieldset>";
			}
			
			//
			// FORM SUBMIT
			//
			
			if(!(this.mode === SampleFormMode.VIEW)) {
				component += "<fieldset>";
				component += "<div class='control-group'>";
				component += "<div class='controls'>";
				component += "<input type='submit' class='btn btn-primary' value='" + message + " " + sampleTypeDisplayName + "'>";
				component += "</div>";
				component += "</div>";
				component += "</fieldset>";
			}
			
			component += "</form>";
			
			component += "</div>";
			component += "</div>";
			
			
		//Add form to layout
		$("#"+this.containerId).append(component);
	}
	
	this.showSamplesWithoutPage = function(event) {
		var sampleTypeCode = event.target.value;
		var sampleType = this.profile.getTypeForTypeCode(sampleTypeCode);
		
		if(sampleType !== null) {
			sampleTable = new SampleTable("sampleSearchContainer", this.profile, sampleTypeCode, false, false, true, false, true);
			sampleTable.init();
		}
	}
	
	this.addLinkedSample = function(group, sample) {
		this.sampleTypesLinksTables["sampleParents_"+group].addSample(sample);
	}
	
	this.createSample = function() {
		Util.blockUI();
		
		//Identification Info
		var sampleCode = $("#sampleCode")[0].value;
		
		var sampleSpace = null;
		var sampleProject = null;
		if(this.isELNExperiment) {
			sampleSpace = $("#sampleSpaceProject")[0].value.split("/")[1];
			sampleProject = $("#sampleSpaceProject")[0].value.split("/")[2];
		} else {
			sampleSpace = $("#sampleSpaceProject").val();
		}
		
		//Other properties
		var properties = {};
		
		var sampleType = profile.getTypeForTypeCode(this.sampleTypeCode);
		for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = sampleType.propertyTypeGroups[i];
			for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
				var propertyType = propertyTypeGroup.propertyTypes[j];
				var value = null;
				
				if (propertyType.dataType === "BOOLEAN") {
					value = $("#"+propertyType.code+":checked").val() === "on";
				} else {
					value = Util.getEmptyIfNull($("#"+propertyType.code).val());
				}
				
				properties[propertyType.code] = value;
			}
		}
		
		//Parent Links
		var sampleParentsFinal = new Array();
		
		for(sampleGroupCode in this.profile.typeGroups) {
			var samplesIdentifiers = this.sampleTypesLinksTables["sampleParents_"+sampleGroupCode].getSamplesIdentifiers();
			sampleParentsFinal = sampleParentsFinal.concat(samplesIdentifiers);
		}
		
		var method = "";
		
		if(this.mode === SampleFormMode.CREATE) {
			method = "insertSample";
		} else if(this.mode === SampleFormMode.EDIT) {
			method = "updateSample";
		}
		
		var parameters = {
				//API Method
				"method" : method,
				//Identification Info
				"sampleSpace" : sampleSpace,
				"sampleProject" : sampleProject,
				"sampleCode" : sampleCode,
				"sampleType" : this.sampleTypeCode,
				//Other Properties
				"sampleProperties" : properties,
				//Parent links
				"sampleParents": sampleParentsFinal,
				//ELN Creation Related parameters
				"sampleExperimentCreate": isELNExperiment && (this.mode === SampleFormMode.CREATE),
				"sampleExperimentCode": sampleCode,
				"sampleExperimentType": this.sampleTypeCode,
				"sampleExperimentProject": sampleProject
		};
		
		var localReference = this;
		
		openbisServer.createReportFromAggregationService("DSS1", "newbrowserapi", parameters, function(response) {
			localReference.createSampleCallback(response);
		});
		
		return false;
	}

	this.createSampleCallback = function(response) {
		if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") {
			var sampleType = profile.getTypeForTypeCode(this.sampleTypeCode);
			var sampleTypeDisplayName = sampleType.description;
			
			var message = "";
			if(this.mode === SampleFormMode.CREATE) {
				message = "Created.";
			} else if(this.mode === SampleFormMode.EDIT) {
				message = "Updated.";
			}
			
			Util.showSuccess(sampleTypeDisplayName + " " + message);
		} else if (response.result.columns[1].title === "Error") {
			Util.showError(response.result.rows[0][1].value);
		} else {
			Util.showError("Unknown Error.");
		}
		Util.unblockUI();
	}
}