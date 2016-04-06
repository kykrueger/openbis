/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "as/dto/common/fetchoptions/FetchOptions" ], function(stjs, FetchOptions) {
	var ExperimentTypeFetchOptions = function() {
	};
	stjs.extend(ExperimentTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.fetchoptions.ExperimentTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.propertyAssignments = null;
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
		prototype.sortBy = function() {
			if (this.sort == null) {
				var ExperimentTypeSortOptions = require("as/dto/experiment/fetchoptions/ExperimentTypeSortOptions");
				this.sort = new ExperimentTypeSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "ExperimentTypeSortOptions"
	});
	return ExperimentTypeFetchOptions;
})