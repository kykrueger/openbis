define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var SearchDomainService = function() {
	};
	stjs.extend(SearchDomainService, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.SearchDomainService';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.name = null;
		prototype.label = null;
		prototype.possibleSearchOptionsKey = null;
		prototype.parameters = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getName = function() {
			return this.name;
		};
		prototype.setName = function(name) {
			this.name = name;
		};
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		};
		prototype.getPossibleSearchOptionsKey = function() {
			return this.possibleSearchOptionsKey;
		};
		prototype.setPossibleSearchOptionsKey = function(possibleSearchOptionsKey) {
			this.possibleSearchOptionsKey = possibleSearchOptionsKey;
		};
		prototype.getParameters = function() {
			return this.parameters;
		};
		prototype.setParameters = function(parameters) {
			this.parameters = parameters;
		};
		prototype.toString = function() {
			return "SearchDomainService: " + this.code;
		};
	}, {
		fetchOptions : "SearchDomainServiceFetchOptions"
	});
	return SearchDomainService;
})