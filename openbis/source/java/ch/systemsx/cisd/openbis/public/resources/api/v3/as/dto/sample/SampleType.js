/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs" ], function(stjs) {
	var SampleType = function() {
	};
	stjs.extend(SampleType, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.SampleType';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.code = null;
		prototype.description = null;
		prototype.listable = null;
		prototype.subcodeUnique = null;
		prototype.autoGeneratedCode = null;
		prototype.showParentMetadata = null;
		prototype.generatedCodePrefix = null;
		prototype.modificationDate = null;
		prototype.propertyAssignments = null;
		prototype.getPropertyAssignments = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasPropertyAssignments()) {
				return this.propertyAssignments;
			} else {
				throw new exceptions.NotFetchedException("Property assignments have not been fetched.");
			}
		};
		prototype.setPropertyAssignments = function(propertyAssignments) {
			this.propertyAssignments = propertyAssignments;
		};
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
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.isListable = function() {
			return this.listable;
		};
		prototype.setListable = function(listable) {
			this.listable = listable;
		};
		prototype.isSubcodeUnique = function() {
			return this.subcodeUnique;
		};
		prototype.setSubcodeUnique = function(subcodeUnique) {
			this.subcodeUnique = subcodeUnique;
		};
		prototype.isAutoGeneratedCode = function() {
			return this.autoGeneratedCode;
		};
		prototype.setAutoGeneratedCode = function(autoGeneratedCode) {
			this.autoGeneratedCode = autoGeneratedCode;
		};
		prototype.isShowParentMetadata = function() {
			return this.showParentMetadata;
		};
		prototype.setShowParentMetadata = function(showParentMetadata) {
			this.showParentMetadata = showParentMetadata;
		};
		prototype.getGeneratedCodePrefix = function() {
			return this.generatedCodePrefix;
		};
		prototype.setGeneratedCodePrefix = function(generatedCodePrefix) {
			this.generatedCodePrefix = generatedCodePrefix;
		};
		prototype.getModificationDate = function() {
			return this.modificationDate;
		};
		prototype.setModificationDate = function(modificationDate) {
			this.modificationDate = modificationDate;
		};
	}, {
		fetchOptions : "SampleTypeFetchOptions",
		permId : "EntityTypePermId",
		modificationDate : "Date"
	});
	return SampleType;
})