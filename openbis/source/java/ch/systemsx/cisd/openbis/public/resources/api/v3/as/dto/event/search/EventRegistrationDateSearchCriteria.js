/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/DateFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, DateFieldSearchCriteria, SearchFieldType) {
	var EventRegistrationDateSearchCriteria = function() {
		DateFieldSearchCriteria.call(this, "eventRegistrationDate", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(EventRegistrationDateSearchCriteria, DateFieldSearchCriteria, [ DateFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.event.search.EventRegistrationDateSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return EventRegistrationDateSearchCriteria;
})