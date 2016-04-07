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

function StorageListController(sample, isDisabled) {
	this._storageListModel = new StorageListModel(sample, isDisabled);
	this._storageListView = new StorageListView(this, this._storageListModel);
	
	this.init = function($container) {
		this._storageListView.repaint($container);
	}
	
	this._saveState = function(storagePropGroup){
		delete this._storageListModel.savedState;
		var savedState = {};
		for(key in storagePropGroup) {
			var propertyKey = storagePropGroup[key];
			if(key != "groupDisplayName") {
				savedState[propertyKey] = this._storageListModel.sample.properties[propertyKey];
			}
		}
		this._storageListModel.savedState = savedState;
	}
	
	this._restoreState = function() {
		for(key in this._storageListModel.savedState) {
			this._storageListModel.sample.properties[key] = this._storageListModel.savedState[key];
		}
	}
}