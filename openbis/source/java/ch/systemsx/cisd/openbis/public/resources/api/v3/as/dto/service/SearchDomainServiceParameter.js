define([ "stjs" ], function(stjs) {
	var SearchDomainServiceParameter = function() {
	};
	stjs.extend(SearchDomainServiceParameter, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.SearchDomainServiceParameter';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.label = null;
		prototype.description = null;
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		}
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		}
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		}
	}, {}
	);
	return SearchDomainServiceParameter;
})
