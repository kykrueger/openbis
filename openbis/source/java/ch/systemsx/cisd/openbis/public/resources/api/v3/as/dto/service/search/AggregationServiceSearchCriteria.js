define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria"], 
	function(require, stjs, AbstractObjectSearchCriteria) {
	var AggregationServiceSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(AggregationServiceSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.AggregationServiceSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withOrOperator = function() {
			return this.withOperator(SearchOperator.OR);
		};
		prototype.withAndOperator = function() {
			return this.withOperator(SearchOperator.AND);
		};
	}, {
	});
	return AggregationServiceSearchCriteria;
})
