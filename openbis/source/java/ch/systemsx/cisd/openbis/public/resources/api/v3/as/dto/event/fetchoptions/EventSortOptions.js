define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var EventSortOptions = function() {
		SortOptions.call(this);
	};

	var fields = {
	    ID : "ID",
        REGISTRATION_DATE : "REGISTRATION_DATE"
    };

	stjs.extend(EventSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.event.fetchoptions.EventSortOptions';
		constructor.serialVersionUID = 1;

        prototype.id = function() {
            return this.getOrCreateSorting(fields.ID);
        };
        prototype.getId = function() {
            return this.getSorting(fields.ID);
        };
        prototype.registrationDate = function() {
            return this.getOrCreateSorting(fields.REGISTRATION_DATE);
        };
        prototype.getRegistrationDate = function() {
            return this.getSorting(fields.REGISTRATION_DATE);
        };
	}, {});
	return EventSortOptions;
})