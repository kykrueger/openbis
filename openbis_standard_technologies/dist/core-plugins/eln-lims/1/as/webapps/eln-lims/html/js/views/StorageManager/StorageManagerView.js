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

function StorageManagerView(storageManagerController, storageManagerModel, storageFromView, storageToView) {
	this._storageManagerController = storageManagerController;
	this._storageManagerModel = storageManagerModel;
	
	this._storageFromView = storageFromView;
	this._storageToView = storageToView;
	this._changeLogContainer = $("<div>").append("None");
	
	this._moveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", null, "Save Changes");
	this._showHideStorageToBtn = FormUtil.getButtonWithIcon("glyphicon-eye-open", null, "Toggle Storage B");
	this._showHideMenuBtn = FormUtil.getButtonWithIcon("glyphicon-resize-full", function() {
			var iconSpan = $(this.children[0]);
			if(iconSpan.hasClass("glyphicon-resize-full")) {
				iconSpan.removeClass("glyphicon-resize-full");
				iconSpan.addClass("glyphicon-resize-small");
				LayoutManager.fullScreen();
			} else if(iconSpan.hasClass("glyphicon-resize-small")) {
				iconSpan.removeClass("glyphicon-resize-small");
				iconSpan.addClass("glyphicon-resize-full");
				LayoutManager.restoreStandardSize();
			}
	}, "Toggle Full Screen");
		
	this._moveBtn.removeClass("btn-default");
	this._moveBtn.addClass("btn-primary");
	
	this.repaint = function(views) {
		var $header = views.header;
		var $container = views.content;
		
		$header.append($("<h2>").append("Storage Manager"));
		$header.append(this._moveBtn).append(" ").append(this._showHideStorageToBtn).append(" ").append(this._showHideMenuBtn);
		
		var $containerColumn = $("<form>", {
			'role' : "form", 
			"action" : "javascript:void(0);", 
			"onsubmit" : ""
		});
		
		var $twoColumnsContainer = $("<div>", {"id" : "storageFromContainer", "class" : "row"});
		
		this._$storageFromContainer = $("<div>", {"id" : "storageFromContainer", "class" : "col-md-12"});
		this._storageFromView.repaint(this._$storageFromContainer);
		
		this._$storageToContainer = $("<div>", {"id" : "storageToContainer", "class" : "col-md-12"});
		this._$storageToContainer.hide();
		var _this = this;
		this._showHideStorageToBtn.click(function() {
			var iconSpan = $(_this._showHideStorageToBtn.children()[0]);
			if(iconSpan.hasClass("glyphicon-eye-open")) {
				iconSpan.removeClass("glyphicon-eye-open");
				iconSpan.addClass("glyphicon-eye-close");
			} else if(iconSpan.hasClass("glyphicon-eye-close")) {
				iconSpan.removeClass("glyphicon-eye-close");
				iconSpan.addClass("glyphicon-eye-open");
			}
			_this._$storageToContainer.toggle();
		});
		
		this._storageToView.repaint(this._$storageToContainer);
		
		$containerColumn.append(this._$storageFromContainer);
		$containerColumn.append(this._$storageToContainer);
		$containerColumn.append($("<div>", { class : "col-md-12" }).append($("<legend>").append("Changes")).append(this._changeLogContainer));
		$container.css("padding", "0px");
		$container.append($containerColumn);
	}
	
	this.getMoveButton = function() {
		return this._moveBtn;
	}
	
	this.updateChangeLogView = function() {
		this._changeLogContainer.empty();
		var changeLog = this._storageManagerModel.changeLog;
		for(var cIdx = 0; cIdx < changeLog.length; cIdx++) {
			var item = changeLog[cIdx];
			this._changeLogContainer.append("<strong>Type:</strong>");
			this._changeLogContainer.append(" ");
			this._changeLogContainer.append(item.type);
			this._changeLogContainer.append(" ");
			
			this._changeLogContainer.append("<strong>Identifier:</strong>");
			this._changeLogContainer.append(" ");
			this._changeLogContainer.append(item.data.identifier);
			this._changeLogContainer.append(" ");
			
			this._changeLogContainer.append("<strong>New Box:</strong>");
			this._changeLogContainer.append(" ");
			this._changeLogContainer.append(item.newProperties[item.storagePropertyGroup.boxProperty]);
			this._changeLogContainer.append(" ");
			
			this._changeLogContainer.append("<strong>New Position:</strong>");
			this._changeLogContainer.append(" ");
			this._changeLogContainer.append(item.newProperties[item.storagePropertyGroup.positionProperty]);
			this._changeLogContainer.append(" ");
			
			this._changeLogContainer.append("<strong>New Storage:</strong>");
			this._changeLogContainer.append(" ");
			this._changeLogContainer.append(item.newProperties[item.storagePropertyGroup.nameProperty]);
			this._changeLogContainer.append(" ");
			
			this._changeLogContainer.append("<strong>New Rack:</strong>");
			this._changeLogContainer.append(" ");
			this._changeLogContainer.append(item.newProperties[item.storagePropertyGroup.rowProperty]);
			this._changeLogContainer.append(",");
			this._changeLogContainer.append(item.newProperties[item.storagePropertyGroup.columnProperty]);
			this._changeLogContainer.append(" ");
			
			this._changeLogContainer.append($("<br>"));
		}
	}
}