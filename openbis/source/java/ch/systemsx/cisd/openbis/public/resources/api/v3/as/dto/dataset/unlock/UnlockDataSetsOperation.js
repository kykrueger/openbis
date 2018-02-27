define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var UnlockDataSetsOperation = function(dataSetIds, options) {
		this.dataSetIds = dataSetIds;
		this.options = options;
	};
	stjs.extend(UnlockDataSetsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.unlock.UnlockDataSetsOperation';
		prototype.dataSetIds = null;
		prototype.options = null;
		prototype.getDataSetIds = function() {
			return this.dataSetIds;
		};
		prototype.getOptions = function() {
			return this.options;
		};
		prototype.getMessage = function() {
			return "UnlockDataSetsOperation";
		};
	}, {
		dataSetIds : {
			name : "List",
			arguments : [ "IDataSetId" ]
		},
		options : "DataSetUnlockOptions"
	});
	return UnlockDataSetsOperation;
})
