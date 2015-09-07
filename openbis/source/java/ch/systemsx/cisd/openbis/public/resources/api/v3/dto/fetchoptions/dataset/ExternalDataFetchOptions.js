/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/fetchoptions/FetchOptions", "dto/fetchoptions/vocabulary/VocabularyTermFetchOptions", "dto/fetchoptions/dataset/FileFormatTypeFetchOptions",
		"dto/fetchoptions/dataset/LocatorTypeFetchOptions", "dto/fetchoptions/dataset/ExternalDataSortOptions" ], function(require, stjs, FetchOptions) {
	var ExternalDataFetchOptions = function() {
	};
	stjs.extend(ExternalDataFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.dataset.ExternalDataFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.storageFormat = null;
		prototype.fileFormatType = null;
		prototype.locatorType = null;
		prototype.sort = null;
		prototype.withStorageFormat = function() {
			if (this.storageFormat == null) {
				var VocabularyTermFetchOptions = require("dto/fetchoptions/vocabulary/VocabularyTermFetchOptions");
				this.storageFormat = new VocabularyTermFetchOptions();
			}
			return this.storageFormat;
		};
		prototype.withStorageFormatUsing = function(fetchOptions) {
			return this.storageFormat = fetchOptions;
		};
		prototype.hasStorageFormat = function() {
			return this.storageFormat != null;
		};
		prototype.withFileFormatType = function() {
			if (this.fileFormatType == null) {
				var FileFormatTypeFetchOptions = require("dto/fetchoptions/dataset/FileFormatTypeFetchOptions");
				this.fileFormatType = new FileFormatTypeFetchOptions();
			}
			return this.fileFormatType;
		};
		prototype.withFileFormatTypeUsing = function(fetchOptions) {
			return this.fileFormatType = fetchOptions;
		};
		prototype.hasFileFormatType = function() {
			return this.fileFormatType != null;
		};
		prototype.withLocatorType = function() {
			if (this.locatorType == null) {
				var LocatorTypeFetchOptions = require("dto/fetchoptions/dataset/LocatorTypeFetchOptions");
				this.locatorType = new LocatorTypeFetchOptions();
			}
			return this.locatorType;
		};
		prototype.withLocatorTypeUsing = function(fetchOptions) {
			return this.locatorType = fetchOptions;
		};
		prototype.hasLocatorType = function() {
			return this.locatorType != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var ExternalDataSortOptions = require("dto/fetchoptions/dataset/ExternalDataSortOptions");
				this.sort = new ExternalDataSortOptions();
			}
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		storageFormat : "VocabularyTermFetchOptions",
		fileFormatType : "FileFormatTypeFetchOptions",
		locatorType : "LocatorTypeFetchOptions",
		sort : "ExternalDataSortOptions"
	});
	return ExternalDataFetchOptions;
})