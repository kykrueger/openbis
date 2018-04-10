/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/material/fetchoptions/MaterialTypeSortOptions",
         "as/dto/property/fetchoptions/PropertyAssignmentFetchOptions", "as/dto/plugin/fetchoptions/PluginFetchOptions" ], function(stjs, FetchOptions) {
	var MaterialTypeFetchOptions = function() {
	};
	stjs.extend(MaterialTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.fetchoptions.MaterialTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.propertyAssignments = null;
		prototype.validationPlugin = null;
		prototype.withPropertyAssignments = function() {
			if (this.propertyAssignments == null) {
				var PropertyAssignmentFetchOptions = require("as/dto/property/fetchoptions/PropertyAssignmentFetchOptions");
				this.propertyAssignments = new PropertyAssignmentFetchOptions();
			}
			return this.propertyAssignments;
		};
		prototype.withPropertyAssignmentsUsing = function(fetchOptions) {
			return this.propertyAssignments = fetchOptions;
		};
		prototype.hasPropertyAssignments = function() {
			return this.propertyAssignments != null;
		};
		prototype.withValidationPlugin = function() {
			if (this.validationPlugin == null) {
				var PluginFetchOptions = require("as/dto/plugin/fetchoptions/PluginFetchOptions");
				this.validationPlugin = new PluginFetchOptions();
			}
			return this.validationPlugin;
		};
		prototype.withValidationPluginUsing = function(fetchOptions) {
			return this.validationPlugin = fetchOptions;
		};
		prototype.hasValidationPlugin = function() {
			return this.validationPlugin != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var MaterialTypeSortOptions = require("as/dto/material/fetchoptions/MaterialTypeSortOptions");
				this.sort = new MaterialTypeSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		propertyAssignments : "PropertyAssignmentFetchOptions",
		validationPlugin : "PluginFetchOptions",
		sort : "MaterialTypeSortOptions"
	});
	return MaterialTypeFetchOptions;
})