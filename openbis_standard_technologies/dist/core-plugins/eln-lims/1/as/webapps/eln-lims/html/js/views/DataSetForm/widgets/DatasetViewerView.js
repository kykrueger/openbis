/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function DataSetViewerView(dataSetViewerController, dataSetViewerModel) {
	this._dataSetViewerController = dataSetViewerController;
	this._dataSetViewerModel = dataSetViewerModel;
	
	this.repaintDatasets = function() {
		var _this = this;
		
		// Container
		var $mainContainer = $("#"+this._dataSetViewerModel.containerId);
		$mainContainer.empty();
		
		// Title / Upload Button
		var $containerTitle = $("<div>", {"id" : this._dataSetViewerModel.containerIdTitle });
		var $uploadButton = "";
		if(this._dataSetViewerModel.enableUpload) {
			$uploadButton = $("<a>", { class: "btn btn-default" }).append($("<span>", { class: "glyphicon glyphicon-upload" })).append(" Upload New Dataset");
			$uploadButton.click(function() { 
				mainController.changeView('showCreateDataSetPageFromPermId',_this._dataSetViewerModel.sample.permId); //TO-DO Fix Global Access
			});
		}
		
		$containerTitle.append($("<div>").append($uploadButton));
		
		// Title / Container Content
		var $containerContent = $("<div>", {"id" : this._dataSetViewerModel.containerIdContent });
		$containerContent.append($("<legend>").append("Datasets:"));
		$mainContainer.append($containerTitle).append($containerContent);
		
		var $container = $("#"+this._dataSetViewerModel.containerIdContent);
		$container.empty();
		
		var $filesContainer = $("<div>");
		$container.append($filesContainer);
		this.repaintFilesAsTree($filesContainer);
	}
	
	this.repaintFilesAsTree = function($container) {
		$container.empty();
		var _this = this;
		var $tree = $("<div>", { "id" : "tree" });
		$container.append($tree);
		
		var treeModel = [];
		for(var datasetCode in this._dataSetViewerModel.sampleDataSets) {
			var dataset = this._dataSetViewerModel.sampleDataSets[datasetCode];
			treeModel.push({ title : dataset.dataSetTypeCode + " : " + datasetCode, key : "/", folder : true, lazy : true, datasetCode : datasetCode });
		}
		
		var glyph_opts = {
        	    map: {
        	      doc: "glyphicon glyphicon-file",
        	      docOpen: "glyphicon glyphicon-file",
        	      checkbox: "glyphicon glyphicon-unchecked",
        	      checkboxSelected: "glyphicon glyphicon-check",
        	      checkboxUnknown: "glyphicon glyphicon-share",
        	      dragHelper: "glyphicon glyphicon-play",
        	      dropMarker: "glyphicon glyphicon-arrow-right",
        	      error: "glyphicon glyphicon-warning-sign",
        	      expanderClosed: "glyphicon glyphicon-plus-sign",
        	      expanderLazy: "glyphicon glyphicon-plus-sign",  // glyphicon-expand
        	      expanderOpen: "glyphicon glyphicon-minus-sign",  // glyphicon-collapse-down
        	      folder: "glyphicon glyphicon-folder-close",
        	      folderOpen: "glyphicon glyphicon-folder-open",
        	      loading: "glyphicon glyphicon-refresh"
        	    }
        };
		
        var onActivate = function(event, data) {
        	if(data.node.key === "/") {
        		mainController.changeView('showViewDataSetPageFromPermId', data.node.data.datasetCode);
        	}
    	};
    	
    	var onClick = function(event, data) {
    		
    	};
    	
    	var onLazyLoad = function(event, data) {
    		var dfd = new $.Deferred();
    	    data.result = dfd.promise();
    	    
    		var pathToLoad = data.node.key;
    		var parentDatasetCode = data.node.data.datasetCode;
    		
    		var repaintEvent = function(code, files) {
    			if(!files.result) {
    				Util.showError("Files can't be found, most probably the DSS is down, contact your admin.");
    			} else {
    				var results = [];
        			for(var fIdx = 0; fIdx < files.result.length; fIdx++) {
        				var file = files.result[fIdx];
        				
        				var titleValue = null;
        				if(file.isDirectory) {
        					titleValue = file.pathInListing;
        					var directLink = _this._dataSetViewerModel.getDirectDirectoryLink(code, file);
        					if(directLink) {
        						titleValue = directLink + " " + titleValue;
        					}
        				} else {
        					var $fileLink = _this._dataSetViewerModel.getDownloadLink(code, file, true);
        					titleValue = $fileLink[0].outerHTML;
        					var previewLink = _this._dataSetViewerModel.getPreviewLink(code, file);
        					if(previewLink) {
        						titleValue = previewLink + " " + titleValue;
        					}
        				}
        				results.push({ title : titleValue, key : file.pathInDataSet, folder : file.isDirectory, lazy : file.isDirectory, datasetCode : parentDatasetCode });
        			}
        			
        			dfd.resolve(results);
    			}
			};
			
			_this.updateDirectoryView(parentDatasetCode, pathToLoad, true, repaintEvent);
    	};
    	
    	$tree.fancytree({
        	extensions: ["dnd", "edit", "glyph"], //, "wide"
        	glyph: glyph_opts,
        	source: treeModel,
        	activate: onActivate,
        	click: onClick,
        	lazyLoad : onLazyLoad
        });
        
	}
	
	this.updateDirectoryView = function(code, path, notAddPath, repaintEvent) {
		var _this = this;
		mainController.serverFacade.listFilesForDataSet(code, path, false, function(files) {
			repaintEvent(code, files);
		});
	}
}