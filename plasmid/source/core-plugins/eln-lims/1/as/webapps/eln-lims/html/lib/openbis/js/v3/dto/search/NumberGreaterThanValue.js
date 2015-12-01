/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/AbstractNumberValue" ], function(stjs, AbstractNumberValue) {
	var NumberGreaterThanValue = function(number) {
		AbstractNumberValue.call(this, number);
	};
	stjs.extend(NumberGreaterThanValue, AbstractNumberValue, [ AbstractNumberValue ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.NumberGreaterThanValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "greater than '" + this.getValue() + "'";
		};
	}, {});
	return NumberGreaterThanValue;
})