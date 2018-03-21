define([ "stjs"], function(stjs) {
	var ReportingServiceExecutionOptions = function() {
		this.dataSetCodes = [];
	};
	stjs.extend(ReportingServiceExecutionOptions, null, [ ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.ReportingServiceExecutionOptions';
		constructor.serialVersionUID = 1;
		prototype.dataSetCodes = null;
		prototype.withDataSets = function(dataSetCodes) {
			this.dataSetCodes = dataSetCodes;
		};
		prototype.getDataSetCodes = function() {
			return this.dataSetCodes;
		};
	}, {});
	return ReportingServiceExecutionOptions;
})
