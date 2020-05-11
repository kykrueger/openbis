define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ISamplePropertiesHolder = function() {
	};
	stjs.extend(ISamplePropertiesHolder, null, [], function(constructor, prototype) {
		prototype.getSampleProperties = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return ISamplePropertiesHolder;
})
