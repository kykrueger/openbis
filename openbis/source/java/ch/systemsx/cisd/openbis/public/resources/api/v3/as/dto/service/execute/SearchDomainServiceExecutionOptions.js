define([ "stjs" ], function(stjs) {
	var SearchDomainServiceExecutionOptions = function() {
		this.parameters = {};
	};
	stjs.extend(SearchDomainServiceExecutionOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.SearchDomainServiceExecutionOptions';
		constructor.serialVersionUID = 1;
		prototype.preferredSearchDomain = null;
		prototype.searchString = null;
		prototype.parameters = null;
		prototype.withPreferredSearchDomain = function(preferredSearchDomain) {
			this.preferredSearchDomain = preferredSearchDomain;
			return this;
		}
		prototype.getPreferredSearchDomain = function() {
			return this.preferredSearchDomain;
		}
		prototype.withSearchString = function(searchString) {
			this.searchString = searchString;
			return this;
		}
		prototype.getSearchString = function() {
			return this.searchString;
		}
		prototype.withParameter = function(parameterName, value) {
			this.parameters[parameterName] = value;
			return this;
		}
		prototype.getParameters = function() {
			return this.parameters;
		}
	}, {
		parameters : {
			name : "Map",
			arguments : [ null, null ]
		}
	});
	return SearchDomainServiceExecutionOptions;
})
