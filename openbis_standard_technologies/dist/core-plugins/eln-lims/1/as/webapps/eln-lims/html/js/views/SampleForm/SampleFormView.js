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
	
	this.repaint = function(views, loadFromTemplate) {
		var $container = views.content;
		//
		// Form setup
		//
		var _this = this;

		var $form = $("<span>");
		
		var $formColumn = $("<form>", {
			'role' : "form",
			'action' : 'javascript:void(0);'
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
		var spaceCode = this._sampleFormModel.sample.spaceCode;
		var projectCode;
		var experimentCode;
		if(this._sampleFormModel.sample.experimentIdentifierOrNull) {	
			var experimentIdentifier = this._sampleFormModel.sample.experimentIdentifierOrNull;
			projectCode = IdentifierUtil.getProjectCodeFromExperimentIdentifier(experimentIdentifier);
			experimentCode = IdentifierUtil.getCodeFromIdentifier(experimentIdentifier);
		}
		var containerSampleIdentifier;
		var containerSampleCode;
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			containerSampleIdentifier = IdentifierUtil.getContainerSampleIdentifierFromContainedSampleIdentifier(this._sampleFormModel.sample.identifier);
			if(containerSampleIdentifier) {
				containerSampleCode = IdentifierUtil.getCodeFromIdentifier(containerSampleIdentifier);
			}
		}
		var sampleCode = this._sampleFormModel.sample.code;
		var samplePermId = this._sampleFormModel.sample.permId;
		var entityPath = FormUtil.getFormPath(spaceCode, projectCode, experimentCode, containerSampleCode, containerSampleIdentifier, sampleCode, samplePermId);
		
		var $formTitle = $("<div>");
		var nameLabel = this._sampleFormModel.sample.properties[profile.propertyReplacingCode];
		if(nameLabel) {
			//nameLabel = html.sanitize(nameLabel);
			nameLabel = DOMPurify.sanitize(nameLabel);
		} else if(this._sampleFormModel.sample.sampleTypeCode === "STORAGE_POSITION") {
			var properties = this._sampleFormModel.sample.properties;
			var storagePropertyGroup = profile.getStoragePropertyGroup();
			var boxProperty = properties[storagePropertyGroup.boxProperty];
			if(!boxProperty) {
				boxProperty = "NoBox";
			}
			var positionProperty = properties[storagePropertyGroup.positionProperty];
			if(!positionProperty) {
				positionProperty = "NoPos";
			}
			nameLabel = boxProperty + " - " + positionProperty;
		} else {
			nameLabel = this._sampleFormModel.sample.code;
		}
		
		var title = null;
		
		switch(this._sampleFormModel.mode) {
	    	case FormMode.CREATE:
	    		title = "New " + Util.getDisplayNameFromCode(this._sampleFormModel.sample.sampleTypeCode);
	    		break;
	    	case FormMode.EDIT:
	    		title = "Update " + Util.getDisplayNameFromCode(this._sampleFormModel.sample.sampleTypeCode) + ": " + nameLabel;
	    		break;
	    	case FormMode.VIEW:
	    		title = "" + Util.getDisplayNameFromCode(this._sampleFormModel.sample.sampleTypeCode) + ": " + nameLabel;
	    		break;
		}
		
		$formTitle
			.append($("<h2 id='sampleFormTitle'>").append(title));
			//.append($("<h4>", { "style" : "font-weight:normal;" } ).append(entityPath));
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		var rightToolbarModel = [];
        var dropdownOptionsModel = [];
		var toolbarConfig = profile.getSampleTypeToolbarConfiguration(_this._sampleFormModel.sample.sampleTypeCode);

		if(this._sampleFormModel.mode === FormMode.VIEW) {
			// New
			if(_this._allowedToCreateChild() && toolbarConfig.CREATE) {
				var sampleTypes = profile.getAllSampleTypes(true);
				FormUtil.addCreationDropdown(toolbarModel, sampleTypes, ["ENTRY", "EXPERIMENTAL_STEP"], function(typeCode) {
					return function() {
						Util.blockUI();
						setTimeout(function() {
							FormUtil.createNewSampleOfTypeWithParent(typeCode,
									_this._sampleFormModel.sample.experimentIdentifierOrNull, 
									_this._sampleFormModel.sample.identifier);
						}, 100);
					}
				});
			}
			
			if (_this._allowedToEdit()) {
				//Edit
				if(this._sampleFormModel.mode === FormMode.VIEW) {
					var $editButton = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
						var args = {
								permIdOrIdentifier : _this._sampleFormModel.sample.permId,
								paginationInfo : _this._sampleFormModel.paginationInfo
						}
						
						mainController.changeView('showEditSamplePageFromPermId', args);
					}, "Edit", null, "edit-btn");
					if(toolbarConfig.EDIT) {
						toolbarModel.push({ component : $editButton, tooltip: null });
					}
				}
			}
			if (_this._allowedToMove()) {
			    //Move
			    if(toolbarConfig.MOVE) {
                    dropdownOptionsModel.push({
                        label : "Move",
                        action : function() {
                                var moveEntityController = new MoveEntityController("SAMPLE", _this._sampleFormModel.sample.permId);
                                moveEntityController.init();
                                }
                    });
                }
			}
			if (_this._allowedToDelete()) {
				//Delete
				var warningText = null;
				if(this._sampleFormModel.sample.children.length > 0 || this._sampleFormModel.datasets.length > 0) {
					warningText = ""
						var childrenThatAreNotPositions = 0;
					for(var idx = 0; idx < this._sampleFormModel.sample.children.length; idx++) {
						var child = this._sampleFormModel.sample.children[idx];
						if(child.sampleTypeCode !== "STORAGE_POSITION") {
							childrenThatAreNotPositions++;
						}
					}
					
					if(this._sampleFormModel.sample.children.length > 0) {
						warningText += "The sample has " + childrenThatAreNotPositions + " children samples, these relationships will be broken but the children will remain:\n";
						var numChildrenToShow = childrenThatAreNotPositions;
						if(numChildrenToShow > 10) {
							numChildrenToShow = 10;
						}
						for(var cIdx = 0 ; cIdx < numChildrenToShow; cIdx++) {
							var child = this._sampleFormModel.sample.children[cIdx];
							if(child.sampleTypeCode !== "STORAGE_POSITION") {
								warningText += "\n\t" + child.code;
							}
						}
						if(numChildrenToShow > 10) {
							warningText += "\n\t...";
						}
					}
					if(this._sampleFormModel.datasets.length > 0) {
						warningText += "\n\nThe " + ELNDictionary.sample + " has " + this._sampleFormModel.datasets.length + " datasets, these will be deleted with the " + ELNDictionary.sample + ":\n";
						var numDatasetsToShow = this._sampleFormModel.datasets.length;
						if(numDatasetsToShow > 10) {
							numDatasetsToShow = 10;
						}
						for(var cIdx = 0 ; cIdx < numDatasetsToShow; cIdx++) {
							warningText += "\n\t" + this._sampleFormModel.datasets[cIdx].code;
						}
						if(numDatasetsToShow > 10) {
							warningText += "\n\t...";
						}
					}
				}

				if(toolbarConfig.DELETE) {
			        dropdownOptionsModel.push({
                        label : "Delete",
                        action : function() {
                            var modalView = new DeleteEntityController(function(reason) {
                                _this._sampleFormController.deleteSample(reason);
                            }, true, warningText);
                            modalView.init();
                        }
                    });
				}
				
			}
			if (_this._allowedToCopy()) {
				//Copy
				if(toolbarConfig.COPY) {
				    dropdownOptionsModel.push({
                        label : "Copy",
                        action : _this._getCopyButtonEvent()
                    });
				}
			}

			//Print
			if(toolbarConfig.PRINT) {
			    dropdownOptionsModel.push({
                    label : "Print",
                    action : function() {
                        PrintUtil.printEntity(_this._sampleFormModel.sample);
                    }
                });
			}

			//Barcode
			if(profile.mainMenu.showBarcodes) {
                if(toolbarConfig.BARCODE) {
                    dropdownOptionsModel.push({
                                        label : "Barcode Print",
                                        action : function() {
                                            BarcodeUtil.showBarcode(_this._sampleFormModel.sample);
                                        }
                    });

                    if(profile.isPropertyPressent(sampleType, "$BARCODE")) {
                        dropdownOptionsModel.push({
                            label : "Barcode Update",
                            action : function() {
                                BarcodeUtil.readBarcode(_this._sampleFormModel.sample);
                            }
                        });
                    }
                }
            }

			//Hierarchy Graph
			if(toolbarConfig.HIERARCHY_GRAPH) {
				dropdownOptionsModel.push({
                    label : "Hierarchy Graph",
                    action : function() {
                        mainController.changeView('showSampleHierarchyPage', _this._sampleFormModel.sample.permId);
                    }
                });
			}
			
			//Hierarchy Table
			if(toolbarConfig.HIERARCHY_TABLE) {
				dropdownOptionsModel.push({
                    label : "Hierarchy Table",
                    action : function() {
                        mainController.changeView('showSampleHierarchyTablePage', _this._sampleFormModel.sample.permId);
                    }
                });
			}
			
			if(_this._allowedToRegisterDataSet()) {
				//Create Dataset
				var $uploadBtn = FormUtil.getButtonWithIcon("glyphicon-upload", function () {
					mainController.changeView('showCreateDataSetPageFromPermId',_this._sampleFormModel.sample.permId);
				}, "Upload");
				if(toolbarConfig.UPLOAD_DATASET) {
					toolbarModel.push({ component : $uploadBtn, tooltip: null });
				}
			
				//Get dropbox folder name
				if(toolbarConfig.UPLOAD_DATASET_HELPER) {
					dropdownOptionsModel.push({
                        label : "Dataset upload helper tool for eln-lims dropbox",
                        action : function() {
                            var nameElements = [
                                "O",
                                _this._sampleFormModel.sample.spaceCode,
                                IdentifierUtil.getProjectCodeFromSampleIdentifier(_this._sampleFormModel.sample.identifier),
                                _this._sampleFormModel.sample.code,
                            ];
					        FormUtil.showDropboxFolderNameDialog(nameElements);
                        }
                    });
				}
			}
			
			//Export
            if(toolbarConfig.EXPORT_METADATA) {
                dropdownOptionsModel.push({
                    label : "Export Metadata",
                    action : FormUtil.getExportAction([{ type: "SAMPLE", permId : _this._sampleFormModel.sample.permId, expand : true }], true)
                });
            }

			if(toolbarConfig.EXPORT_ALL) {
				dropdownOptionsModel.push({
                    label : "Export Metadata & Data",
                    action : FormUtil.getExportAction([{ type: "SAMPLE", permId : _this._sampleFormModel.sample.permId, expand : true }], false)
                });
			}
			
			//Jupyter Button
			if(profile.jupyterIntegrationServerEndpoint) {
				dropdownOptionsModel.push({
                    label : "New Jupyter notebook",
                    action : function () {
                        var jupyterNotebook = new JupyterNotebookController(_this._sampleFormModel.sample);
                        jupyterNotebook.init();
                    }
                });
			}

            //Freeze
            if(_this._sampleFormModel.v3_sample && _this._sampleFormModel.v3_sample.frozen !== undefined) { //Freezing available on the API
                var isEntityFrozen = _this._sampleFormModel.v3_sample.frozen;
                if(toolbarConfig.FREEZE) {
                    if(isEntityFrozen) {
                        var $freezeButton = FormUtil.getFreezeButton("SAMPLE", this._sampleFormModel.v3_sample.permId.permId, isEntityFrozen);
                        rightToolbarModel.push({ component : $freezeButton, tooltip: null });
                    } else {
                        dropdownOptionsModel.push({
                            label : "Freeze Entity (Disable further modifications)",
                            action : function () {
                                FormUtil.showFreezeForm("SAMPLE", _this._sampleFormModel.v3_sample.permId.permId);
                            }
                        });
                    }
                }
            }
		} else { //Create and Edit
			var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", function() {
				_this._sampleFormController.createUpdateCopySample();
			}, "Save", null, "save-btn");
			$saveBtn.removeClass("btn-default");
			$saveBtn.addClass("btn-primary");
			toolbarModel.push({ component : $saveBtn });

            // Templates
            if(toolbarConfig.TEMPLATES && this._sampleFormModel.mode === FormMode.CREATE) {
                var $templateBtn = FormUtil.getButtonWithIcon("glyphicon-list-alt", function() {
                    var criteria = {
                        entityKind : "SAMPLE",
                    	logicalOperator : "AND",
                        rules : {
                            "1" : { type : "Experiment",  name : "ATTR.CODE", value : "TEMPLATES_COLLECTION" },
                    	    "2" : { type : "Project",     name : "ATTR.CODE", value : "TEMPLATES" },
                    	    "2" : { type : "Attribute",   name : "SAMPLE_TYPE", value : _this._sampleFormModel.sample.sampleTypeCode },
                        }
                    }

                    mainController.serverFacade.searchForSamplesAdvanced(criteria, {
                    			only : true,
                    			withProperties : true
                    }, function(results) {
                        var settingsForDropdown = [];
                        for(var rIdx = 0; rIdx < results.totalCount; rIdx++) {
                            var name = results.objects[rIdx].properties[profile.propertyReplacingCode];
                            if(!name) {
                                name = results.objects[rIdx].code;
                            }
                            settingsForDropdown.push({  label: name,
                                                    value: results.objects[rIdx].identifier.identifier});
                        }

                        var $dropdown = FormUtil.getDropdown(settingsForDropdown, "Select template");
                        $dropdown.attr("id", "templatesDropdown");
                        Util.showDropdownAndBlockUI("templatesDropdown", $dropdown);

                        $("#templatesDropdown").on("change", function(event) {
                            var sampleIdentifier = $("#templatesDropdown")[0].value;
                            var sample = null;
                            for(var rIdx = 0; rIdx < results.totalCount; rIdx++) {
                                if(results.objects[rIdx].identifier.identifier === sampleIdentifier) {
                                    sample = results.objects[rIdx];
                                }
                            }
                            _this._sampleFormModel.sample.properties = sample.properties;

                            if(_this._sampleFormModel.views.header) {
                                _this._sampleFormModel.views.header.empty();
                            }
                            if(_this._sampleFormModel.views.content) {
                                 _this._sampleFormModel.views.content.empty();
                            }
                            if(_this._sampleFormModel.views.auxContent) {
                                 _this._sampleFormModel.views.auxContent.empty();
                            }
                            _this._sampleFormController.init(_this._sampleFormModel.views, true);
                            Util.unblockUI();
                        });

                        $("#templatesDropdownCancel").on("click", function(event) {
                            Util.unblockUI();
                        });
                    });
                }, "Templates");
                toolbarModel.push({ component : $templateBtn, tooltip: "Templates" });
            }
		}

		if(this._sampleFormModel.mode !== FormMode.CREATE && this._sampleFormModel.paginationInfo) {
			var moveToIndex = function(index) {
				var pagOptionsToSend = $.extend(true, {}, _this._sampleFormModel.paginationInfo.pagOptions);
				pagOptionsToSend.pageIndex = index;
				pagOptionsToSend.pageSize = 1;
				_this._sampleFormModel.paginationInfo.pagFunction(function(result) {
					if(result && result.objects && result.objects[0] && result.objects[0].permId) {
						_this._sampleFormModel.paginationInfo.currentIndex = index;
						var arg = {
								permIdOrIdentifier : result.objects[0].permId,
								paginationInfo : _this._sampleFormModel.paginationInfo
						}
						mainController.changeView('showViewSamplePageFromPermId', arg);
					} else {
						window.alert("The item to go to is no longer available.");
					}
				}, pagOptionsToSend);
			}
			
			if(this._sampleFormModel.paginationInfo.currentIndex > 0) {
				var $backBtn = FormUtil.getButtonWithIcon("glyphicon-arrow-left", function () {
					moveToIndex(_this._sampleFormModel.paginationInfo.currentIndex-1);
				}, "Previous");
				rightToolbarModel.push({ component : $backBtn, tooltip: null });
			}
			
			if(this._sampleFormModel.paginationInfo.currentIndex+1 < this._sampleFormModel.paginationInfo.totalCount) {
				var $nextBtn = FormUtil.getButtonWithIcon("glyphicon-arrow-right", function () {
					moveToIndex(_this._sampleFormModel.paginationInfo.currentIndex+1);
				}, "Next");
				rightToolbarModel.push({ component : $nextBtn, tooltip: null });
			}
		}
		
		var $header = views.header;
		
		$header.append($formTitle);
		var sampleTypeDefinitionsExtension = profile.sampleTypeDefinitionsExtension[_this._sampleFormModel.sample.sampleTypeCode];
		if(sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension.extraToolbar) {
		    toolbarModel = toolbarModel.concat(sampleTypeDefinitionsExtension.extraToolbar(_this._sampleFormModel.mode, _this._sampleFormModel.sample));
		}

		var hideShowOptionsModel = [];

		// Preview
        var $previewImageContainer = new $('<div>', { id : "previewImageContainer" });
        $previewImageContainer.append($("<legend>").append("Preview"));
        $previewImageContainer.hide();
        $formColumn.append($previewImageContainer);

		//
		// Identification Info on Create
		//
		if(this._sampleFormModel.mode === FormMode.CREATE) {
			$formColumn.append(this._createIdentificationInfoSection(hideShowOptionsModel, sampleType, entityPath));
		}
		
		// Plugin Hook
		var $sampleFormTop = new $('<div>');
		$formColumn.append($sampleFormTop);
		profile.sampleFormTop($sampleFormTop, _this._sampleFormModel);
		
		//
		// Form Defined Properties from General Section
		//
		if(sampleTypeCode !== "ENTRY") {
            for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
                var propertyTypeGroup = sampleType.propertyTypeGroups[i];
                if(propertyTypeGroup.name === "General" || propertyTypeGroup.name === "General info") {
                    this._paintPropertiesForSection($formColumn, propertyTypeGroup, i, loadFromTemplate);
                }
            }
		}

		//
		//
		//
		var documentEditorEditableToolbar = null;

		if(sampleTypeCode === "ENTRY") {
		    var isReadOnly = this._sampleFormModel.mode === FormMode.VIEW;
		    var documentPropertyType = profile.getPropertyType("$DOCUMENT");
		    FormUtil.fixStringPropertiesForForm(documentPropertyType, this._sampleFormModel.sample);
            var documentChangeEvent = function(jsEvent, newValue) {
                var newCleanValue = Util.getEmptyIfNull(newValue);
                _this._sampleFormModel.isFormDirty = true;
                _this._sampleFormModel.sample.properties["$DOCUMENT"] = Util.getEmptyIfNull(newValue);
			    var titleStart = newCleanValue.indexOf("<h2>");
			    var titleEnd = newCleanValue.indexOf("</h2>");
			    if(titleStart !== -1 && titleEnd !== -1) {
			        _this._sampleFormModel.sample.properties["$NAME"] = newCleanValue.substring(titleStart+4, titleEnd);
			    } else {
			        _this._sampleFormModel.sample.properties["$NAME"] = null;
			    }
			}
            // https://ckeditor.com/docs/ckeditor5/latest/framework/guides/deep-dive/ui/document-editor.html
            var documentEditor = $("<div>", { class : "document-editor" });
            if(!isReadOnly) {
                documentEditorEditableToolbar = $("<div>", { class : "document-editor__toolbar" });
            }

            var height = LayoutManager.secondColumnContent.outerHeight();

            var documentEditorEditableContainer = $("<div>", { class : "document-editor__editable-container", style : "min-height: " + height + "px; overflow: hidden;" });

            var documentEditorEditable = $("<div>", { class : "document-editor__editable", id : "$DOCUMENT" });

		    var value = Util.getEmptyIfNull(this._sampleFormModel.sample.properties[documentPropertyType.code]);
		    if(this._sampleFormModel.mode === FormMode.CREATE) {
                value = "<h2>New Title</h2><br><p>new content</p>";
		    }
            var documentEditorEditableFinal = FormUtil.activateRichTextProperties(documentEditorEditable, documentChangeEvent, documentPropertyType, value, isReadOnly, documentEditorEditableToolbar);

            documentEditorEditableFinal.addClass("document-editor__editable");
            documentEditorEditableFinal.attr("id", "$DOCUMENT");
            //  documentEditorEditableFinal.css("height", "100%");
            //  Bugfix for Webkit Chrome/Safari
            documentEditorEditableFinal.css("min-height", height + "px");

            documentEditor.append(documentEditorEditableContainer.append(documentEditorEditableFinal));

            $formColumn.append(documentEditor);
		}

		//
		// LINKS TO PARENTS
		//
		if (this._sampleFormModel.mode !== FormMode.VIEW || (this._sampleFormModel.mode === FormMode.VIEW && this._sampleFormModel.sample.parents.length > 0)) {
			$formColumn.append(this._createParentsSection(hideShowOptionsModel, sampleTypeDefinitionsExtension, sampleTypeCode));
		}

		//
		// LINKS TO CHILDREN
		//
		if (this._sampleFormModel.mode !== FormMode.VIEW || (this._sampleFormModel.mode === FormMode.VIEW && this._sampleFormModel.sample.children.length > 0)) {
			$formColumn.append(this._createChildrenSection(hideShowOptionsModel, sampleTypeDefinitionsExtension, sampleTypeCode));
		}

		//
		// Form Defined Properties from non General Section
		//
		if(sampleTypeCode !== "ENTRY") {
            for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
                var propertyTypeGroup = sampleType.propertyTypeGroups[i];
                if(propertyTypeGroup.name !== "General" && propertyTypeGroup.name !== "General info") {
                    this._paintPropertiesForSection($formColumn, propertyTypeGroup, i, loadFromTemplate);
                }
            }
		}
		
		//
		// Plate View
		//
		if(this._sampleFormModel.sample.sampleTypeCode === "PLATE" && this._sampleFormModel.mode !== FormMode.CREATE) {
			var plateContainer = $("<div>", { 'id' : 'sample-form-plate-view' });
			$formColumn.append($("<legend>").append("Plate"));
			var plateController = new PlateController(this._sampleFormModel.sample, this._sampleFormModel.mode !== FormMode.EDIT);
			plateController.init(plateContainer);
			$formColumn.append(plateContainer);
			this._sampleFormController._plateController = plateController;
		}
		
		//
		// Storage
		//
		var isStorageAvailable = profile.isSampleTypeWithStorage(this._sampleFormModel.sample.sampleTypeCode);
		if(isStorageAvailable && profile.storagesConfiguration["isEnabled"]) {
			var $fieldsetOwner = $("<div>");
			var $legend = $("<legend>").append("Storage");
			var storageListContainer = $("<div>", { 'id' : 'sample-form-storage-list' });
			$fieldsetOwner.append($legend);
			$fieldsetOwner.append(storageListContainer);
			$formColumn.append($fieldsetOwner);
			
			$legend.prepend(FormUtil.getShowHideButton(storageListContainer, "SAMPLE-" + this._sampleFormModel.sample.sampleTypeCode + "-storage"));
			
			var storageListController = new StorageListController(this._sampleFormModel.sample, this._sampleFormModel.mode === FormMode.VIEW);	
			storageListController.init(storageListContainer);
		}
		
		//
		// Extra Content
		//
		$formColumn.append($("<div>", { 'id' : 'sample-form-content-extra' }));
		
		// Plugin Hook
		var $sampleFormBottom = new $('<div>');
		$formColumn.append($sampleFormBottom);
		profile.sampleFormBottom($sampleFormBottom, _this._sampleFormModel);
		
		//
		// Identification Info on View/Edit
		//
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			$formColumn.append(this._createIdentificationInfoSection(hideShowOptionsModel, sampleType, entityPath));
		}
		
		//
		// PREVIEW IMAGE
		//
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			var $previewImage = $("<img>", { 'data-preview-loaded' : 'false',
											 'class' : 'zoomableImage',
											 'id' : 'preview-image',
											 'src' : './img/image_loading.gif',
											 'style' : 'max-width:100%; display:none;'
											});
			$previewImage.click(function() {
				Util.showImage($("#preview-image").attr("src"));
			});
			
		    $previewImageContainer.append($previewImage);
		}

		//
        // DATASETS
        //
		if(this._sampleFormModel.mode !== FormMode.CREATE &&
		   this._sampleFormModel.datasets.length > 0) {

            //Preview image
            this._reloadPreviewImage();

            // Dataset Viewer
            var $dataSetViewerContainer = new $('<div>', { id : "dataSetViewerContainer" });
	        $dataSetViewerContainer.append($("<legend>").append("DataSets"));
            mainController.sideMenu.addSubSideMenu($dataSetViewerContainer);
            this._sampleFormModel.dataSetViewer = new DataSetViewerController("dataSetViewerContainer", profile, this._sampleFormModel.sample, mainController.serverFacade, profile.getDefaultDataStoreURL(), this._sampleFormModel.datasets, false, true);
            this._sampleFormModel.dataSetViewer.init();
		}

		//
		// INIT
		//
		FormUtil.addOptionsToToolbar(toolbarModel, dropdownOptionsModel, hideShowOptionsModel,
				"SAMPLE-VIEW-" + _this._sampleFormModel.sample.sampleTypeCode);
		$header.append(FormUtil.getToolbar(toolbarModel));
		$header.append(FormUtil.getToolbar(rightToolbarModel).css("float", "right"));
        if(documentEditorEditableToolbar) {
            documentEditorEditableToolbar.css("margin-top", "10px");
            $header.append($("<br>")).append(documentEditorEditableToolbar);
        }
		$container.append($form);
		
		//
		// Extra content
		//
		//Extra components
		try {
			profile.sampleFormContentExtra(this._sampleFormModel.sample.sampleTypeCode, this._sampleFormModel.sample, "sample-form-content-extra");
		} catch(err) {
			Util.manageError(err);
		}
		
		this._sampleFormModel.isFormLoaded = true;
	}
	
	this._paintPropertiesForSection = function($formColumn, propertyTypeGroup, i, loadFromTemplate) {
		var _this = this;
		var sampleTypeCode = this._sampleFormModel.sample.sampleTypeCode;
		var sampleType = mainController.profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
		
		var $fieldsetOwner = $('<div>');
		var $fieldset = $('<div>');
		var $legend = $('<legend>');
		
		$fieldsetOwner.append($legend).append($fieldset);
		
		if((propertyTypeGroup.name !== null) && (propertyTypeGroup.name !== "")) {
			$legend.text(propertyTypeGroup.name);
		} else if((i === 0) || ((i !== 0) && (sampleType.propertyTypeGroups[i-1].name !== null) && (sampleType.propertyTypeGroups[i-1].name !== ""))) {
			$legend.text("Metadata");
		} else {
			$legend.remove();
		}
		
		var propertyGroupPropertiesOnForm = 0;
		for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
			var propertyType = propertyTypeGroup.propertyTypes[j];
			FormUtil.fixStringPropertiesForForm(propertyType, this._sampleFormModel.sample);
			if(!propertyType.showInEditViews && this._sampleFormModel.mode === FormMode.EDIT && propertyType.code !== "$XMLCOMMENTS") { //Skip
				continue;
			} else if(propertyType.dinamic && this._sampleFormModel.mode === FormMode.CREATE) { //Skip
				continue;
			}
			
			if(propertyType.code === "$ANNOTATIONS_STATE" || propertyType.code === "$FREEFORM_TABLE_STATE" || propertyType.code === "$ORDER.ORDER_STATE" ) {
				continue;
			} else if(propertyType.code === "$XMLCOMMENTS") {
				var $commentsContainer = $("<div>");
				$fieldset.append($commentsContainer);
				var isAvailable = this._sampleFormController._addCommentsWidget($commentsContainer);
				if(!isAvailable) {
					continue;
				}
			} else {
				if(propertyType.code === "$SHOW_IN_PROJECT_OVERVIEW") {
					if(!(profile.inventorySpaces.length > 0 && $.inArray(this._sampleFormModel.sample.spaceCode, profile.inventorySpaces) === -1)) {
						continue;
					}
				}
				var $controlGroup =  null;
				var value = this._sampleFormModel.sample.properties[propertyType.code];

				if(!value && propertyType.code.charAt(0) === '$') {
					value = this._sampleFormModel.sample.properties[propertyType.code.substr(1)];
					this._sampleFormModel.sample.properties[propertyType.code] = value;
					delete this._sampleFormModel.sample.properties[propertyType.code.substr(1)];
				}
				
				if(this._sampleFormModel.mode === FormMode.VIEW) { //Show values without input boxes if the form is in view mode
					if(Util.getEmptyIfNull(value) !== "") { //Don't show empty fields, whole empty sections will show the title
						var customWidget = profile.customWidgetSettings[propertyType.code];
						if (customWidget === 'Spreadsheet') {
						    var $jexcelContainer = $("<div>");
                            JExcelEditorManager.createField($jexcelContainer, this._sampleFormModel.mode, propertyType.code, this._sampleFormModel.sample);
						    $controlGroup = FormUtil.getFieldForComponentWithLabel($jexcelContainer, propertyType.label);
						} else if (customWidget === 'Word Processor') {
						    var $component = FormUtil.getFieldForPropertyType(propertyType, value);
						    $component = FormUtil.activateRichTextProperties($component, undefined, propertyType, value, true);
						    $controlGroup = FormUtil.getFieldForComponentWithLabel($component, propertyType.label);
						} else {
						    $controlGroup = FormUtil.createPropertyField(propertyType, value);
						}
					} else {
						continue;
					}
				} else {
					var $component = FormUtil.getFieldForPropertyType(propertyType, value);
					
					//Update values if is into edit mode
					if(this._sampleFormModel.mode === FormMode.EDIT || loadFromTemplate) {
						if(propertyType.dataType === "BOOLEAN") {
							$($($component.children()[0]).children()[0]).prop('checked', value === "true");
						} else if(propertyType.dataType === "TIMESTAMP") {
						} else {
							$component.val(value);
						}
					} else {
						$component.val(""); //HACK-FIX: Not all browsers show the placeholder in Bootstrap 3 if you don't set an empty value.
					}
						
					var changeEvent = function(propertyType) {
						return function(jsEvent, newValue) {
							var propertyTypeCode = null;
							propertyTypeCode = propertyType.code;
							_this._sampleFormModel.isFormDirty = true;
							var field = $(this);
							if(propertyType.dataType === "BOOLEAN") {
								_this._sampleFormModel.sample.properties[propertyTypeCode] = $(field.children()[0]).children()[0].checked;
							} else if (propertyType.dataType === "TIMESTAMP") {
								var timeValue = $($(field.children()[0]).children()[0]).val();
								_this._sampleFormModel.sample.properties[propertyTypeCode] = timeValue;
							} else {
								if(newValue !== undefined && newValue !== null) {
									_this._sampleFormModel.sample.properties[propertyTypeCode] = Util.getEmptyIfNull(newValue);
								} else {
									_this._sampleFormModel.sample.properties[propertyTypeCode] = Util.getEmptyIfNull(field.val());
								}
							}
						}
					}
					
					//Avoid modifications in properties managed by scripts
					if(propertyType.managed || propertyType.dinamic) {
						$component.prop('disabled', true);
					}

					var customWidget = profile.customWidgetSettings[propertyType.code];
					if(customWidget) {
					    switch(customWidget) {
					        case 'Word Processor':
					            if(propertyType.dataType === "MULTILINE_VARCHAR") {
					                $component = FormUtil.activateRichTextProperties($component, changeEvent(propertyType), propertyType, value, false);
					            } else {
					                alert("Word Processor only works with MULTILINE_VARCHAR data type, " + propertyType.code + " is " + propertyType.dataType + ".");
					            }
					            break;
					        case 'Spreadsheet':
					            if(propertyType.dataType === "XML") {
                                    var $jexcelContainer = $("<div>");
                                    JExcelEditorManager.createField($jexcelContainer, this._sampleFormModel.mode, propertyType.code, this._sampleFormModel.sample);
                                    $component = $jexcelContainer;
					            } else {
					                alert("Spreadsheet only works with XML data type, " + propertyType.code + " is " + propertyType.dataType + ".");
					            }
					            break;
					    }
					} else if(propertyType.dataType === "TIMESTAMP") {
						$component.on("dp.change", changeEvent(propertyType));
					} else {
						$component.change(changeEvent(propertyType));
					}
					
					$controlGroup = FormUtil.getFieldForComponentWithLabel($component, propertyType.label);
				}
				$fieldset.append($controlGroup);
			}
			
			if(propertyType.code !== "$ANNOTATIONS_STATE") {
				propertyGroupPropertiesOnForm++;
			}	
		}
			
		if(propertyGroupPropertiesOnForm === 0) {
			$legend.remove();
		}
		
		$legend.prepend(FormUtil.getShowHideButton($fieldset, "SAMPLE-" + sampleTypeCode + "-" + propertyTypeGroup.name));
		$formColumn.append($fieldsetOwner);
			
		return false;
	}
	
	this._createIdentificationInfoSection = function(hideShowOptionsModel, sampleType, entityPath) {
		hideShowOptionsModel.push({
			label : "Identification Info",
			section : "#sample-identification-info"
		});
		
		var _this = this;
		var $identificationInfo = $("<div>", { id : "sample-identification-info" });
		var $legend = $("<legend>").append("Identification Info");
		var $fieldset = $("<div>");
		
		$identificationInfo.append($legend);
		$identificationInfo.append($fieldset);

		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			$fieldset.append(FormUtil.getFieldForComponentWithLabel(entityPath, "Path"));
		}
		$fieldset.append(FormUtil.getFieldForLabelWithText("Type", this._sampleFormModel.sample.sampleTypeCode));
		if(this._sampleFormModel.sample.experimentIdentifierOrNull) {
			$fieldset.append(FormUtil.getFieldForLabelWithText(ELNDictionary.getExperimentKindName(this._sampleFormModel.sample.experimentIdentifierOrNull), this._sampleFormModel.sample.experimentIdentifierOrNull));
		}
		
		//
		// Identification Info - Code
		//
		var $codeField = null;
		if(this._sampleFormModel.mode === FormMode.CREATE) {
			var $textField = FormUtil._getInputField('text', 'codeId', "Code", null, true);
			$textField.keyup(function(event){
				var textField = $(this);
				var caretPosition = this.selectionStart;
				textField.val(textField.val().toUpperCase());
				this.selectionStart = caretPosition;
				this.selectionEnd = caretPosition;
				_this._sampleFormModel.sample.code = textField.val();
				_this._sampleFormModel.isFormDirty = true;
			});
			$codeField = FormUtil.getFieldForComponentWithLabel($textField, "Code");
			
			mainController.serverFacade.generateCode(sampleType, function(autoGeneratedCode) {
				$textField.val(autoGeneratedCode);
				_this._sampleFormModel.sample.code = autoGeneratedCode;
				_this._sampleFormModel.isFormDirty = true;
			});
			
		} else {
			$codeField = FormUtil.getFieldForLabelWithText("Code", this._sampleFormModel.sample.code);
		}
		
		$fieldset.append($codeField);
		
		//
		// Identification Info - Registration and modification times
		//
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			var registrationDetails = this._sampleFormModel.sample.registrationDetails;
			
			var $registrator = FormUtil.getFieldForLabelWithText("Registrator", registrationDetails.userId);
			$fieldset.append($registrator);
			
			var $registationDate = FormUtil.getFieldForLabelWithText("Registration Date", Util.getFormatedDate(new Date(registrationDetails.registrationDate)))
			$fieldset.append($registationDate);
			
			var $modifier = FormUtil.getFieldForLabelWithText("Modifier", registrationDetails.modifierUserId);
			$fieldset.append($modifier);
			
			var $modificationDate = FormUtil.getFieldForLabelWithText("Modification Date", Util.getFormatedDate(new Date(registrationDetails.modificationDate)));
			$fieldset.append($modificationDate);
		}
		
		$identificationInfo.hide();
		return $identificationInfo;
	}
	
	this._createParentsSection = function(hideShowOptionsModel, sampleTypeDefinitionsExtension, sampleTypeCode) {
		hideShowOptionsModel.push({
			forceToShow : this._sampleFormModel.mode === FormMode.CREATE && sampleTypeDefinitionsExtension["FORCE_TO_SHOW_PARENTS_SECTION"],
			label : "Parents",
			section : "#sample-parents"
		});
		
		var requiredParents = [];
		if (sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_PARENTS_HINT"]) {
			requiredParents = sampleTypeDefinitionsExtension["SAMPLE_PARENTS_HINT"];
		}
		
		var sampleParentsWidgetId = "sample-parents";
		var $sampleParentsWidget = $("<div>", { "id" : sampleParentsWidgetId });
		
		var isDisabled = this._sampleFormModel.mode === FormMode.VIEW;
		
		var currentParentsLinks = this._sampleFormModel.sample ? this._sampleFormModel.sample.parents : null;
		var parentsTitle = "Parents";
		if (sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_PARENTS_TITLE"]) {
			parentsTitle = sampleTypeDefinitionsExtension["SAMPLE_PARENTS_TITLE"];
		}
		var parentsAnyTypeDisabled = sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_PARENTS_ANY_TYPE_DISABLED"];
		this._sampleFormModel.sampleLinksParents = new LinksController(parentsTitle,
																			requiredParents,
																			isDisabled,
																			currentParentsLinks,
																			this._sampleFormModel.mode === FormMode.CREATE || this._sampleFormModel.mode === FormMode.EDIT,
																			parentsAnyTypeDisabled,
																			sampleTypeCode);
		if (!sampleTypeDefinitionsExtension || !sampleTypeDefinitionsExtension["SAMPLE_PARENTS_DISABLED"]) {
			this._sampleFormModel.sampleLinksParents.init($sampleParentsWidget);
		}
		$sampleParentsWidget.hide();
		return $sampleParentsWidget;
	}
	
	this._createChildrenSection = function(hideShowOptionsModel, sampleTypeDefinitionsExtension, sampleTypeCode) {
		hideShowOptionsModel.push({
			label : "Children",
			section : "#sample-children"
		});

		var _this = this;
		var requiredChildren = [];
		if(sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_HINT"]) {
			requiredChildren = sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_HINT"];
		}
			
		var sampleChildrenWidgetId = "sample-children";
		var $sampleChildrenWidget = $("<div>", { "id" : sampleChildrenWidgetId });
		
		var currentChildrenLinks = (this._sampleFormModel.sample)?this._sampleFormModel.sample.children:null;
		
		var childrenTitle = "Children";
		if(sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_TITLE"]) {
			childrenTitle = sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_TITLE"];
		}
		
		var currentChildrenLinksNoStorage = [];
		if(currentChildrenLinks != null) {
			for(var cIdx = 0; cIdx < currentChildrenLinks.length; cIdx++) {
				if(currentChildrenLinks[cIdx].sampleTypeCode !== "STORAGE_POSITION") {
					currentChildrenLinksNoStorage.push(currentChildrenLinks[cIdx]);
				}
			}
		}
		var isDisabled = this._sampleFormModel.mode === FormMode.VIEW;
		var childrenAnyTypeDisabled = sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_ANY_TYPE_DISABLED"];
		this._sampleFormModel.sampleLinksChildren = new LinksController(childrenTitle,
															requiredChildren,
															isDisabled,
															currentChildrenLinksNoStorage,
															this._sampleFormModel.mode === FormMode.CREATE,
															childrenAnyTypeDisabled,
															sampleTypeCode);
		if(!sampleTypeDefinitionsExtension || !sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_DISABLED"]) {
			this._sampleFormModel.sampleLinksChildren.init($sampleChildrenWidget);
		}

		var childrenDisabled = sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_DISABLED"];
		if ((this._sampleFormModel.mode !== FormMode.VIEW) && this._sampleFormModel.isELNSample && !childrenDisabled) {
			var $generateChildrenBtn = $("<a>", { 'class' : 'btn btn-default', 'style' : 'margin-top:15px;', 'id' : 'generate_children'}).text("Generate Children");
			$generateChildrenBtn.click(function(event) {
				_this._generateChildren();
			});
			
			var $generateChildrenBox = $("<div>")
											.append($("<div>", { 'class' : 'form-group' }).append($generateChildrenBtn))
											.append($("<div>", { 'id' : 'newChildrenOnBenchDropDown' }))
			$sampleChildrenWidget.append($generateChildrenBox);
		}
		$sampleChildrenWidget.hide();
		return $sampleChildrenWidget;
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
			Util.blockUINoMessage();
			
			var copyFunction = function(defaultCode) {
				var component = "<div>"
					component += "<legend>Duplicate Entity</legend>";
					component += "<div>";
					component += "<div class='form-group'>";
					component += "<label class='control-label'>Options </label>";
					component += "<div class='controls'>";
					component += "<span class='checkbox'><label><input type='checkbox' id='copyCommentsLogOnCopy'>Copy Comments Log</label></span>";
				    component += "<span class='checkbox'><label><input type='checkbox' id='linkParentsOnCopy'>Link Parents</label></span>";
					component += "<span>Copy Children</span>";
					component += "<span class='checkbox'><label><input type='radio' name='copyChildrenOnCopy' value='None' checked>Don't Copy</label></span>";
					component += "<span class='checkbox'><label><input type='radio' name='copyChildrenOnCopy' value='ToParentCollection'>Into parents collection</label></span>";
					//component += "<span class='checkbox'><label><input type='radio' name='copyChildrenOnCopy' value='ToOriginalCollection'>Into original collection</label></span>";
					component += "</div>";
					component += "</div>";
					component += "</div>";
					component += "<div class='form-group'>";
					component += "<label class='control-label'>Code&nbsp;(*):</label>";
					component += "<div>";
					component += "<input type='text' class='form-control' placeholder='Code' id='newSampleCodeForCopy' pattern='[a-zA-Z0-9_\\-\\.]+' required>";
					component += "</div>";
					component += "<div>";
					component += " (Allowed characters are: letters, numbers, '-', '_', '.')";
					component += "</div>";
					component += "</div>";
				
				Util.blockUI(component + "<a class='btn btn-default' id='copyAccept'>Accept</a> <a class='btn btn-default' id='copyCancel'>Cancel</a>", FormUtil.getDialogCss());
				
				$("#newSampleCodeForCopy").on("keyup", function(event) {
					$(this).val($(this).val().toUpperCase());
				});
				
				$("#newSampleCodeForCopy").val(defaultCode);
				
				$("#copyAccept").on("click", function(event) {
					var newSampleCodeForCopy = $("#newSampleCodeForCopy");
					var linkParentsOnCopy = $("#linkParentsOnCopy")[0].checked;
					var copyChildrenOnCopy = $("input[name=copyChildrenOnCopy]:checked").val();
					var copyCommentsLogOnCopy = $("#copyCommentsLogOnCopy")[0].checked;
					var isValid = newSampleCodeForCopy[0].checkValidity();
					if(isValid) {
						var newSampleCodeForCopyValue = newSampleCodeForCopy.val();
						_this._sampleFormController.createUpdateCopySample(newSampleCodeForCopyValue, linkParentsOnCopy, copyChildrenOnCopy, copyCommentsLogOnCopy);
					} else {
						Util.showError("Invalid code.", function() {}, true);
					}
				});
				
				$("#copyCancel").on("click", function(event) { 
					Util.unblockUI();
				});
			};
			
			var spaceCode = _this._sampleFormModel.sample.spaceCode;
			if(profile.isInventorySpace(spaceCode)) {
				var sampleType = profile.getSampleTypeForSampleTypeCode(_this._sampleFormModel.sample.sampleTypeCode);
				mainController.serverFacade.generateCode(sampleType, copyFunction);
			} else {
				_this._sampleFormController.getNextCopyCode(copyFunction);
			}
			
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
					var listFilesForDataSetCallback = 
						function(dataFiles) {
							var found = false;
							if(!dataFiles.result) {
								//DSS Is not running probably
							} else {
								for(var pathIdx = 0; pathIdx < dataFiles.result.length; pathIdx++) {
									if(!dataFiles.result[pathIdx].isDirectory) {
										var elementId = 'preview-image';
										var downloadUrl = profile.getDefaultDataStoreURL() + '/' + data.result[0].code + "/" + dataFiles.result[pathIdx].pathInDataSet + "?sessionID=" + mainController.serverFacade.getSession();
										
										var img = $("#" + elementId);
										img.attr('src', downloadUrl);
										img.attr('data-preview-loaded', 'true');
										img.show();
										$("#previewImageContainer").show();
										break;
									}
								}
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
		FormUtil.getDefaultBenchDropDown('childrenStorageSelector', true, function($childrenStorageDropdown) {
			if($childrenStorageDropdown && !$("#childrenStorageSelector").length) {
				var $childrenStorageDropdownWithLabel = FormUtil.getFieldForComponentWithLabel($childrenStorageDropdown, 'Storage');
				$("#newChildrenOnBenchDropDown").append($childrenStorageDropdownWithLabel);
			}
		});
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
				Util.showUserError("The number of children replicas should be an integer number bigger than 0 and lower than 1000.", function() {}, true);
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
			var generatedChildrenSpace = _this._sampleFormModel.sample.spaceCode;
			var generatedChildrenProject = IdentifierUtil.getProjectCodeFromSampleIdentifier(_this._sampleFormModel.sample.identifier);
			var numberOfReplicas = parseInt($("#childrenReplicas").val());
			if(isNaN(numberOfReplicas) || numberOfReplicas < 0 || numberOfReplicas > 1000) {
				Util.showUserError("The number of children replicas should be an integer number bigger than 0 and lower than 1000.", function() {}, true);
				return;
			}
			var generatedChildrenCodes = getGeneratedChildrenCodes();
			var generatedChildrenType = $("#childrenTypeSelector").val();
			if(generatedChildrenType === "") {
				Util.showUserError("Please select the children type.", function() {}, true);
			} else {
				for(var i = 0; i < generatedChildrenCodes.length; i++) {
					var virtualSample = new Object();
					virtualSample.newSample = true;
					virtualSample.permId = Util.guid();
					virtualSample.code = generatedChildrenCodes[i];
					virtualSample.identifier = IdentifierUtil.getSampleIdentifier(generatedChildrenSpace, generatedChildrenProject, virtualSample.code);
					virtualSample.sampleTypeCode = generatedChildrenType;
					_this._sampleFormModel.sampleLinksChildren.addVirtualSample(virtualSample);
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
		$childrenGenerator.append($("<form>")
									.append($("<h1>").append("Children Generator"))
									.append($parentsComponent)
									.append($childrenComponent)
									.append($previewComponent)
									.append($("<br>")).append($generateButton)
								);
		
		// Show Widget
		Util.blockUI($childrenGenerator, {'text-align' : 'left', 'top' : '10%', 'width' : '80%', 'left' : '10%', 'right' : '10%', 'height' : '80%', 'overflow' : 'auto'});
	}
	
	this._allowedToCreateChild = function() {
		var sample = this._sampleFormModel.v3_sample;
		return sample.frozenForChildren == false && (!sample.experiment || sample.experiment.frozenForSamples == false)
				&& this._sampleFormModel.sampleRights.rights.indexOf("CREATE") >= 0;
	}
	
	this._allowedToEdit = function() {
		var sample = this._sampleFormModel.v3_sample;
		var updateAllowed = this._allowedToUpdate(this._sampleFormModel.rights);
		return updateAllowed && sample.frozen == false;
	}
	
	this._allowedToUpdate = function(rights) {
		return rights && rights.rights.indexOf("UPDATE") >= 0;
	}

	this._allowedToMove = function() {
		var sample = this._sampleFormModel.v3_sample;
		var experiment = sample.experiment;
		if (experiment && experiment.frozenForSamples) {
			return false;
		}
		return this._allowedToUpdate(this._sampleFormModel.rights);
	}
	
	this._allowedToDelete = function() {
		var sample = this._sampleFormModel.v3_sample;
		return sample.frozen == false && (!sample.experiment || sample.experiment.frozenForSamples == false);
	}
	
	this._allowedToCopy = function() {
		var sample = this._sampleFormModel.v3_sample;
		return !sample.experiment || sample.experiment.frozenForSamples == false;
	}
	
	this._allowedToRegisterDataSet = function() {
		var sample = this._sampleFormModel.v3_sample;
		return sample.frozenForDataSets == false && (!sample.experiment || sample.experiment.frozenForDataSets == false)
				&& this._sampleFormModel.sampleRights.rights.indexOf("CREATE") >= 0;
	}
}