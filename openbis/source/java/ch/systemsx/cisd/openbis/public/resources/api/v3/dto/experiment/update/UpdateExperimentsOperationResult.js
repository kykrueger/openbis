/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var UpdateExperimentsOperationResult = function() {
	};
	stjs.extend(UpdateExperimentsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'dto.experiment.update.UpdateExperimentsOperationResult';
	}, {});
	return UpdateExperimentsOperationResult;
})