define([ 'test/test-login', 'test/test-jsVSjava', 'test/test-create', 'test/test-update', 'test/test-search', 'test/test-map', 'test/test-delete', 'test/test-dto' ], function() {
	var testSuites = arguments;
	return function() {
		for (var i = 0; i < testSuites.length; i++) {
			testSuites[i]();
		}
	};
});