function Grid(columns, getDataList, showAllColumns, columnsToShow, onColumnsChange) {
	this.init(columns, getDataList, showAllColumns, columnsToShow, onColumnsChange);
}

$.extend(Grid.prototype, {
	init : function(columns, getDataList, showAllColumns, columnsToShow, onColumnsChange) {
		this.columns = columns;
		this.getDataList = getDataList;
		this.showAllColumns = showAllColumns;
		this.columnsToShow = columnsToShow;
		this.onColumnsChange = onColumnsChange;
	},

	render : function() {
		var thisGrid = this;

		thisGrid.panel = $("<div>").addClass("fuelux");

		$.get("./lib/grid/js/Grid.html", function(template) {
			thisGrid.panel.html(template);
			thisGrid.renderColumnDropdown();
			thisGrid.renderDropDownOptions();
			thisGrid.panel.repeater({
				defaultView : "list",
				dataSource : function(options, callback) {
					if (options.view == "list") {
						thisGrid.list(options, callback);
					}
				},
				list_selectable : false,
				list_noItemsHTML : 'No items found',
				list_rowRendered : function(helpers, callback) {
					$(helpers.item).click(function() {
						var rowIndex = helpers.item[0].rowIndex;
						thisGrid.notifyRowClickListeners({
							"index" : rowIndex,
							"data" : thisGrid.result.datas[rowIndex-1],
							"item" : thisGrid.result.items[rowIndex-1]
						});
					});

					callback();
				}
			});
		});

		return thisGrid.panel;
	},

	renderColumnDropdown : function() {
		var thisGrid = this;

		var columnList = thisGrid.panel.find(".columnDropdown").find("ul");
		columnList.click(function(e) {
			e.stopPropagation();
		});
		

		
		var defaultNumColumns = 5; //Including last always
		
		thisGrid.columns.forEach(function(column, columnIndex) {
			checkbox = $("<input>")
				.attr("type", "checkbox")
				.attr("value", column.property)
				.attr("style", "margin-left: 5px;");
			
			if(column.showByDefault) {
				checkbox.attr("checked", "checked");
			} else if(thisGrid.columnsToShow && Object.keys(thisGrid.columnsToShow).length !== 0) {
				if((thisGrid.columnsToShow[column.property] === true)) { //If settings are present
					checkbox.attr("checked", "checked");
				}
			} else if(thisGrid.showAllColumns || columnIndex < (defaultNumColumns - 1) || (columnIndex+1 === thisGrid.columns.length)) { //Defaults
				checkbox.attr("checked", "checked");
			}
			
			checkbox.change(function() {
				thisGrid.panel.repeater('render');
			});
			var label = $("<label>", { style : 'white-space: nowrap;' }).attr("role", "menuitem").append(checkbox).append("&nbsp;").append(column.label);
			var item = $("<li>").attr("role", "presentation").append(label);
			columnList.append(item);
			
		});
	},

	getAllColumns : function() {
		return this.columns;
	},

	renderDropDownOptions: function() {
		var thisGrid = this;
		var columnList = thisGrid.panel.find(".optionsDropdown").find("ul");
		
		// Export shown rows with shown columns
		var labelSRSC = $("<label>", { style : 'white-space: nowrap; cursor:pointer;' })
						.attr("role", "menuitem")
						.append("Export visible columns with visible rows");

		var itemSRSC = $("<li>")
						.attr("role", "presentation")
						.attr("style", "margin-left: 5px; margin-right: 5px;")
						.append(labelSRSC);
		
		itemSRSC.click(function() {
			thisGrid.exportTSV(false, false);
		});
		
		columnList.append(itemSRSC);
		
		// Export shown rows with all columns
		var labelSRAC = $("<label>", { style : 'white-space: nowrap; cursor:pointer;' })
						.attr("role", "menuitem")
						.append("Export all columns with visible rows");

		var itemSRAC = $("<li>")
						.attr("role", "presentation")
						.attr("style", "margin-left: 5px; margin-right: 5px;")
						.append(labelSRAC);
		
		itemSRAC.click(function() {
			thisGrid.exportTSV(false, true);
		});
		
		columnList.append(itemSRAC);
		
		// Export all rows with visible columns
		var labelARSC = $("<label>", { style : 'white-space: nowrap; cursor:pointer;' })
						.attr("role", "menuitem")
						.append("Export visible columns with all rows");

		var itemARSC = $("<li>")
						.attr("role", "presentation")
						.attr("style", "margin-left: 5px; margin-right: 5px;")
						.append(labelARSC);
		
		itemARSC.click(function() {
			thisGrid.exportTSV(true, false);
		});
		
		columnList.append(itemARSC);
		
		// Export all rows with all columns
		var labelARAC = $("<label>", { style : 'white-space: nowrap; cursor:pointer;' })
						.attr("role", "menuitem")
						.append("Export all columns with all rows");

		var itemARAC = $("<li>")
						.attr("role", "presentation")
						.attr("style", "margin-left: 5px; margin-right: 5px;")
						.append(labelARAC);
		
		itemARAC.click(function() {
			thisGrid.exportTSV(true, true);
		});
		
		columnList.append(itemARAC);
		
	},
	
	exportTSV : function(isAllRowsOrVisible, isAllColumnsOrVisible) {
		var thisGrid = this;
		
		var exportColumnsFromData = function(namePrefix, data, headings) {
			var arrayOfRowArrays = [];
			arrayOfRowArrays.push(headings);
			for(var dIdx = 0; dIdx < data.length; dIdx++) {
				var rowAsArray = [];
				for(var hIdx = 0; hIdx < headings.length; hIdx++) {
					var headerKey = headings[hIdx];
					var rowValue = data[dIdx][headerKey];
					if(!rowValue) {
						rowValue = "";
					} else {
						rowValue = String(rowValue).replace(/\r?\n|\r|\t/g, " "); //Remove carriage returns and tabs
					}
					rowAsArray.push(rowValue);
				}
				arrayOfRowArrays.push(rowAsArray);
			}
			var tsv = $.tsv.formatRows(arrayOfRowArrays);
			var indexOfFirstLine = tsv.indexOf('\n');
			var tsvWithoutNumbers = tsv.substring(indexOfFirstLine + 1);
			
			//
			var csvContentEncoded = null;
			var out = null;
			var charType = null;
			try { //USE UTF-16 if available
				csvContentEncoded = (new TextEncoder("utf-16le")).encode([tsvWithoutNumbers]);
				var bom = new Uint8Array([0xFF, 0xFE]);
				out = new Uint8Array( bom.byteLength + csvContentEncoded.byteLength );
				out.set( bom , 0 );
				out.set( csvContentEncoded, bom.byteLength );
				charType = 'text/tsv;charset=UTF-16LE;';
			} catch(error) { //USE UTF-8
				csvContentEncoded = tsvWithoutNumbers;
				out = new Uint8Array(csvContentEncoded.length);
				for(var ii = 0,jj = csvContentEncoded.length; ii < jj; ++ii){
					out[ii] = csvContentEncoded.charCodeAt(ii);
				}
				charType = 'text/tsv;charset=UTF-8;';
			}
			//
			
			var blob = new Blob([out], {type: charType});
			saveAs(blob,'exportedTable' + namePrefix + '.tsv');
		}
		
		var headings = [];
		var data = [];
		var prefix = "";
		if(isAllColumnsOrVisible) {
			thisGrid.columns.forEach(function(head) {
				if(head.isExportable === true || head.isExportable === undefined) {
					headings.push(head.property);
				}
			});
			prefix += "AllColumns";
		} else {
			thisGrid.getVisibleColumns().forEach(function(head) {
				if(head.isExportable === true || head.isExportable === undefined) {
					headings.push(head.property);
				}
			});
			prefix += "VisibleColumns";
		}
		
		if(isAllRowsOrVisible) {
			var allData = thisGrid.getDataList(function(allData) {
				data = allData;
				prefix += "AllRows";
				exportColumnsFromData(prefix, data, headings);
			});
		} else {
			data = thisGrid.result.datas;
			prefix += "VisibleRows";
			exportColumnsFromData(prefix, data, headings);
		}
	},
	
	getVisibleColumns : function() {
		var thisGrid = this;
		var columns = [];

		var columnsModel = {};
		
		thisGrid.panel.find(".columnDropdown").find("input:checked").each(function(index, element) {
			
			thisGrid.getAllColumns().forEach(function(column) {
				var checkbox = $(element);
				if (column.property == checkbox.val()) {
					columns.push(column);
					columnsModel[column.property] = true;
				}
			});
		});

		if(thisGrid.onColumnsChange) {
			thisGrid.onColumnsChange(columnsModel);
		}

		return columns;
	},

	filterData : function(dataList, filter) {
		var thisGrid = this;

		if (filter) {
			filterKeywords = filter.toLowerCase().split(/[ ,]+/); //Split by regular space or comma
			dataList = dataList.filter(function(data) {
				var isValid = new Array(filterKeywords.length);
				
				for(cIdx = 0; cIdx < thisGrid.columns.length; cIdx++) {
					var column = thisGrid.columns[cIdx];
					for(var fIdx = 0; fIdx < filterKeywords.length; fIdx++) {
						var filterKeyword = filterKeywords[fIdx];
						if (column.filter) {
							isValid[fIdx] = isValid[fIdx] || column.filter(data, filterKeyword);
						} else {
							var value = "" + data[column.property];
							isValid[fIdx] = isValid[fIdx] || (value !== null && value.toLowerCase().indexOf(filterKeyword) !== -1);
						}
					}
				}
				
				var isFinallyValid = true;
				for(var fIdx = 0; fIdx < filterKeywords.length; fIdx++) {
					isFinallyValid = isFinallyValid && isValid[fIdx];
				}
				
				return isFinallyValid;
			});
		}

		return dataList;
	},

	sortData : function(dataList, sortProperty, sortDirection) {
		var thisGrid = this;

		if (sortProperty && sortDirection) {
			var sortColumn = null;
			thisGrid.columns.forEach(function(column) {
				if (column.property == sortProperty) {
					sortColumn = column;
				}
			});
			if (sortColumn) {
				var sortFunction = null;
				var sortDirection = sortDirection == "asc" ? 1 : -1;

				if (sortColumn.sort) {
					sortFunction = sortColumn.sort;
				} else {
					sortFunction = function(data1, data2) {
						var value1 = data1[sortColumn.property];
						var value2 = data2[sortColumn.property];
						return naturalSort(value1, value2);
					};
				}

				dataList.sort(function(data1, data2) {
					return sortDirection * sortFunction(data1, data2);
				});
			}
		}

		return dataList;
	},

	renderData : function(dataList) {
		var thisGrid = this;
		var items = [];
		var maxLineLength = 200;
		
		dataList.forEach(function(data) {
			var item = {};
			thisGrid.getVisibleColumns().forEach(function(column) {
				//1. Render
				var value = null;
				if (column.render) {
					value = column.render(data);
				} else {
					value = data[column.property];
				}
				
				//2. Sanitize
				var value = FormUtil.sanitizeRichHTMLText(value);
				
				//3. Shorten
				var finalValue = null;
				if(value && value.length > maxLineLength) {
					finalValue = value.substring(0, maxLineLength) + "...";
				} else {
					finalValue = value;
				}
				
				//4. Tooltip
				if(value !== finalValue) {
					finalValue = $("<div>").html(finalValue);
					finalValue.tooltipster({
		                content: $("<span>").html(value)
		            });
				}
				
				item[column.property] = finalValue;
			});
			items.push(item);
		});

		return items;
	},

	list : function(options, callback) {
		var thisGrid = this;

		thisGrid.getDataList(function(dataList) {

			dataList = thisGrid.filterData(dataList, options.search);
			dataList = thisGrid.sortData(dataList, options.sortProperty, options.sortDirection);

			var result = {};
			result.count = dataList.length;
			result.datas = [];
			result.items = [];
			result.columns = thisGrid.getVisibleColumns();
			result.page = options.pageIndex;
			result.pages = Math.ceil(result.count / options.pageSize);
			result.start = options.pageIndex * options.pageSize;
			result.end = result.start + options.pageSize;
			result.end = (result.end <= result.count) ? result.end : result.count;
			
			dataList = dataList.slice(result.start, result.end);
			
			if(dataList.length === 0) { //Special case, empty table
				result.start = 0;
			} else { //Normal Case
				result.start = result.start + 1;
			}
			
			itemList = thisGrid.renderData(dataList);
			itemList.forEach(function(item, index) {
				result.datas.push(dataList[index]);
				result.items.push(item);
			});

			thisGrid.result = result;
			callback(result);
			
			//HACK: Fixes header problems due to css incompatibilities
			var tableHeaders = $(thisGrid.panel).find('thead').find('th');
			for(var hIdx = 0; hIdx < tableHeaders.length; hIdx++) {
				var $th = $(tableHeaders[hIdx]);
				// HACK - Fixes chevron problems
				var notSorted = $th.find('.sorted').length === 0;
				if(notSorted) {
					$($th.find('span')).attr('class', 'glyphicon rlc');
				}
				// HACK - Fixes double headers
				if($th[0].childNodes.length === 3) {
					var $div = $($th[0].childNodes[2]).detach();
					$th.empty();
					$th.append($div);
				}
			}
			
			var optionsDropdowns = $(".dropdown.table-options-dropdown");
			for(var i = 0; i < optionsDropdowns.length; i++) {
				var $dropdownTD = $(optionsDropdowns[i]).parent();
				$dropdownTD.css({ "overflow" : "visible" });
			}
		});
	},

	addRowClickListener : function(listener) {
		if (!this.rowClickListeners) {
			this.rowClickListeners = [];
		}
		this.rowClickListeners.push(listener);
	},

	notifyRowClickListeners : function(event) {
		if (this.rowClickListeners) {
			this.rowClickListeners.forEach(function(listener) {
				listener(event);
			});
		}
	}

});