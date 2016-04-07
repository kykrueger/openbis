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

function GridView(gridModel) {
	this._gridModel = gridModel;
	this._gridTable = null;
	this._posClickedEventHandler = null;
	this._labelClickedEventHandler = null;
	this._posDropEventHandler = null;
	
	this.repaint = function($container) {
		$container.empty();
		if(this._gridModel.isValid()) {
			this._gridTable = this._getGridTable();
			$container.append(this._gridTable);
		} else {
			$container.append("Grid can't be displayed because of missing configuration.");
		}
		
	}
	
	this._getGridTable = function() {
		var _this = this;
		var gridTable = $("<table>", { "class" : "table table-bordered gridTable" });
		var $headerRow = $("<tr>");
		var $emptyCell = $("<th>");
		$headerRow.append($emptyCell);
		
		for(var j = 0; j < this._gridModel.numColumns; j++) {
			var $numberCell = $("<th>").append(j+1);
			$numberCell.css('width', Math.floor(80/(this._gridModel.numColumns+1)) +'%');
//			$numberCell.css('padding', '0px');
			$headerRow.append($numberCell);
		}
		
		gridTable.append($headerRow);
		
		for(var i = 0; i < this._gridModel.numRows; i++) {
			var $newRow = $("<tr>");
			var rowLabel = i+1;
			if(this._gridModel.useLettersOnRows) {
				rowLabel = Util.getLetterForNumber(rowLabel);
			}
			var $numberCell = $("<th>").append(rowLabel);
			$numberCell.css('width', Math.floor(80/(this._gridModel.numColumns+1)) +'%');
			$numberCell.css('padding', '0px');
			$newRow.append($numberCell);
			
			for(var j = 0; j < this._gridModel.numColumns; j++) {
				var $newColumn = $("<td>");
				if(this._gridModel.isDragable) {
					var dropEventFuncCopyPos = function(newX,newY) {
						return function(event) {
					    	event.preventDefault();
					    	var origX = event.originalEvent.dataTransfer.getData("origX");
					    	var origY = event.originalEvent.dataTransfer.getData("origY");
					        var tagId = event.originalEvent.dataTransfer.getData("tagId");
					        var objectAsString = event.originalEvent.dataTransfer.getData("object");
					        var extraDragDataString = event.originalEvent.dataTransfer.getData("extraDragData");
					        
					        if(event.target.nodeName === "TD") {
					        	if(_this._posDropEventHandler) {
					        		var object = JSON.parse(objectAsString);
					        		var extraDragDataObject = null;
					        		if(extraDragDataString) {
					        			extraDragDataObject = JSON.parse(extraDragDataString);
					        		}
					        		_this._posDropEventHandler(origX,origY, newX, newY, object, event.target, extraDragDataObject);
					        	}
					        	var $targetDrop = $(event.target);
						        var $elementToDrop = $("#" + tagId);
						        $targetDrop.append($elementToDrop);
					        }
					    };
					};
					var dropEventFunc = dropEventFuncCopyPos(i+1, j+1);
					
					$newColumn.on({
					    dragover: function(e) {
					        e.preventDefault();
					    },
					    drop: dropEventFunc
					});
				}
				
				var clickEvent = function(i, j) {
					return function(event) {
						event.stopPropagation();
						_this._posClicked(i + 1, j + 1);
					}
				}
				
				if(!this._gridModel.isDisabled) {
					$newColumn.click(clickEvent(i, j));
				}
				
				this._addLabels($newColumn, i + 1, j + 1);
				$newColumn.css('width', Math.floor(80/(this._gridModel.numColumns+1)) +'%');
				$newColumn.css('padding', '0px');
				$newRow.append($newColumn);
			}
			gridTable.append($newRow);
		}
		return gridTable;
	}
	
	this._addLabels = function($component, posX, posY) {
		var _this = this;
		var labels = this._gridModel.getLabels(posX, posY);
		var usedLabels = [];
		if(labels) {
			for(var i = 0; i < labels.length; i++) {
				if(!usedLabels[labels[i].displayName]) {
					var labelContainer = $("<div>", { class: "storageBox", id : Util.guid() }).append(labels[i].displayName);
					var dragCopyFunc = function(object, origX, origY) {
						return function(event) {
							event.originalEvent.dataTransfer.setData('origX', origX);
							event.originalEvent.dataTransfer.setData('origY', origY);
							event.originalEvent.dataTransfer.setData('tagId', this.id);
							event.originalEvent.dataTransfer.setData('object', JSON.stringify(object.data));
							if(_this._extraDragData) {
								event.originalEvent.dataTransfer.setData("extraDragData", JSON.stringify(_this._extraDragData));
							}
						};
					}
					
					//The original position where the label was is stored and not changed between drags
					//This is used by multiple position samples
					var dragFunc = dragCopyFunc(labels[i], posX, posY);
					
					if(this._gridModel.isDragable) {
						labelContainer.attr('draggable', 'true');
						labelContainer.on({
						    dragstart: dragFunc
						});
					}
					usedLabels[labels[i].displayName] = true
					var clickEvent = function(posX, posY, label, data) {
						return function(event) {
							event.stopPropagation();
							_this._labelClicked(posX, posY, label, data);
						}
					}
					
					if(!this._gridModel.isDisabled) {
						labelContainer.click(clickEvent(posX, posY, labels[i].displayName, labels[i].data));
					}
					
					$component.append(labelContainer);
				}
			}
		}
	}
	
	this._selectPosition = function(posX, posY, label) { //To give user feedback so he knows what he have selected
		if(this._gridTable && posX > 0 && posY > 0) {//API only available if the table is loaded and 0 positions are labels that can't be selected
			if(!this._gridModel.isSelectMultiple) {
				this._gridTable.find("td").removeClass("rackSelected");
			}
			
			var rows = this._gridTable.find("tr");
			var columns = $(rows[posX]).find("td");
			var cell = $(columns[posY-1]); //-1 because the th tag is skipped by the selector
			var cellClasses = cell.attr("class");
			
			if(this._gridModel.isSelectMultiple && cellClasses && cellClasses.indexOf("rackSelected") !== -1) {
				cell.removeClass("rackSelected");
				return false;
			} else {
				cell.addClass("rackSelected");
				return true;
			}
			
		}
	}
	
	//
	// Event Handlers
	//
	this._posClicked = function(posX, posY) {
		var isSelectedOrDeleted = this._selectPosition(posX, posY, null);
		if(this._posClickedEventHandler) {
			this._posClickedEventHandler(posX, posY, isSelectedOrDeleted);
		}
	}
	
	this._labelClicked = function(posX, posY, label, data) {
		if(this._labelClickedEventHandler) {
			this._labelClickedEventHandler(posX, posY, label, data);
		}
	}
	
	//
	// Setters
	//
	this.setPosSelectedEventHandler = function(posClickedEventHandler) {
		this._posClickedEventHandler = posClickedEventHandler;
	}
	
	this.setLabelSelectedEventHandler = function(labelClickedEventHandler) {
		this._labelClickedEventHandler = labelClickedEventHandler;
	}
	
	this.setPosDropEventHandler = function(posDropEventHandler) {
		this._posDropEventHandler = posDropEventHandler;
	}
	
	this.setExtraDragData = function(extraDragData) {
		this._extraDragData = extraDragData;
	}
}