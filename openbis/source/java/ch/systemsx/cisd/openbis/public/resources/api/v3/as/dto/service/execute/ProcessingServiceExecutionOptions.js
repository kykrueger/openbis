define([ "stjs", "as/dto/service/execute/AbstractExecutionOptionsWithParameters"], function(stjs, AbstractExecutionOptionsWithParameters) {
	var ProcessingServiceExecutionOptions = function() {
		AbstractExecutionOptionsWithParameters.call(this);
		this.dataSetCodes = [];
	};
	stjs.extend(ProcessingServiceExecutionOptions, null, [ ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.ProcessingServiceExecutionOptions';
		constructor.serialVersionUID = 1;
		prototype.dataSetCodes = null;
		prototype.withDataSets = function(dataSetCodes) {
			this.dataSetCodes = dataSetCodes;
		};
		prototype.getDataSetCodes = function() {
			return this.dataSetCodes;
		};
	}, {});
	return ProcessingServiceExecutionOptions;
})
