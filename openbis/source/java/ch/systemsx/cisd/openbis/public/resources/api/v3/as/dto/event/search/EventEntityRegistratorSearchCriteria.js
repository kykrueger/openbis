/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var EventEntityRegistratorSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "entity_registerer", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(EventEntityRegistratorSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.event.search.EventEntityRegistratorSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return EventEntityRegistratorSearchCriteria;
})