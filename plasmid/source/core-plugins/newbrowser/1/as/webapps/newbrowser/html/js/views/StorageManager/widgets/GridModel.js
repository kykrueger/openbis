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

function GridModel() {
	this.numRows = null;
	this.numColumns = null;
	this.labels = null;
	
	this.selectedRow = null;
	this.selectedColumn = null;
	this.selectedLabel = null;
	
	this.reset = function(numRows, numColumns, labels) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		this.labels = labels;
		
		this.selectedRow = null;
		this.selectedColumn = null;
		this.selectedLabel = null;
	}
	
	this.getLabels = function(posX, posY) {
		if(this.labels) {
			if(this.labels[posX]) {
				if(this.labels[posX][posY]) {
					var labelsToReturn = this.labels[posX][posY];
					
					var labelsToReturnArray = [];
					for(var labelToReturn in labelsToReturn) {
						labelsToReturnArray.push(labelToReturn);
					}
					
					var sortedLabelsToReturn = labelsToReturnArray.sort(naturalSort);
					return sortedLabelsToReturn;
				}
			}
		}
		
		return null;
	}
	

	this.getLabelDataByLabelName = function(name) {
		for(row in this.labels) {
			var rowData = this.labels[row];
			for (column in rowData) {
				var columnData = rowData[column];
				for(boxName in columnData) {
					if(boxName === name) {
						return columnData[boxName];
					}
				}
			}
		}
		return null;
	}
	
	this.isValid = function() {
		return this.numRows !== null && this.numColumns !== null;
	}
}