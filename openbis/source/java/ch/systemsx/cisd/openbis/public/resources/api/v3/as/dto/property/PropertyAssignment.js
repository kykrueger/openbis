define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var PropertyAssignment = function() {
	};
	stjs.extend(PropertyAssignment, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.PropertyAssignment';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.section = null;
		prototype.ordinal = null;
		prototype.entityType = null;
		prototype.propertyType = null;
		prototype.mandatory = null;
		prototype.showInEditView = null;
		prototype.showRawValueInForms = null;
		prototype.registrator = null;
		prototype.registrationDate = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getSection = function() {
			return this.section;
		};
		prototype.setSection = function(section) {
			this.section = section;
		};
		prototype.getOrdinal = function() {
			return this.ordinal;
		};
		prototype.setOrdinal = function(ordinal) {
			this.ordinal = ordinal;
		};
		prototype.getEntityType = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasEntityType()) {
				return this.entityType;
			} else {
				throw new exceptions.NotFetchedException("Entity type has not been fetched.");
			}
		};
		prototype.setEntityType = function(entityType) {
			this.entityType = entityType;
		};
		prototype.getPropertyType = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasPropertyType()) {
				return this.propertyType;
			} else {
				throw new exceptions.NotFetchedException("Property type has not been fetched.");
			}
		};
		prototype.setPropertyType = function(propertyType) {
			this.propertyType = propertyType;
		};
		prototype.isMandatory = function() {
			return this.mandatory;
		};
		prototype.setMandatory = function(mandatory) {
			this.mandatory = mandatory;
		};
		prototype.isShowInEditView = function() {
			return this.showInEditView;
		};
		prototype.setShowInEditView = function(showInEditView) {
			this.showInEditView = showInEditView;
		};
		prototype.isShowRawValueInForms = function() {
			return this.showRawValueInForms;
		};
		prototype.setShowRawValueInForms = function(showRawValueInForms) {
			this.showRawValueInForms = showRawValueInForms;
		};
		prototype.getRegistrator = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasRegistrator()) {
				return this.registrator;
			} else {
				throw new exceptions.NotFetchedException("Registrator has not been fetched.");
			}
		};
		prototype.setRegistrator = function(registrator) {
			this.registrator = registrator;
		};
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
	}, {
		fetchOptions : "PropertyAssignmentFetchOptions",
		entityType : "IEntityType",
		propertyType : "PropertyType",
		registrator : "Person",
		registrationDate : "Date"
	});
	return PropertyAssignment;
})