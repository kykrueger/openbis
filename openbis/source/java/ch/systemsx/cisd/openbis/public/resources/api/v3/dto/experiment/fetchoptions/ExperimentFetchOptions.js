/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/common/fetchoptions/FetchOptions", 'dto/experiment/fetchoptions/ExperimentTypeFetchOptions', 'dto/project/fetchoptions/ProjectFetchOptions',
		'dto/property/fetchoptions/PropertyFetchOptions', 'dto/tag/fetchoptions/TagFetchOptions', 'dto/person/fetchoptions/PersonFetchOptions', 'dto/attachment/fetchoptions/AttachmentFetchOptions',
		'dto/dataset/fetchoptions/DataSetFetchOptions', 'dto/sample/fetchoptions/SampleFetchOptions', 'dto/history/fetchoptions/HistoryEntryFetchOptions',
		'dto/material/fetchoptions/MaterialFetchOptions', 'dto/experiment/fetchoptions/ExperimentSortOptions' ], function(require, stjs, FetchOptions) {
	var ExperimentFetchOptions = function() {
	};
	stjs.extend(ExperimentFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.experiment.fetchoptions.ExperimentFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.type = null;
		prototype.project = null;
		prototype.dataSets = null;
		prototype.samples = null;
		prototype.history = null;
		prototype.properties = null;
		prototype.materialProperties = null;
		prototype.tags = null;
		prototype.registrator = null;
		prototype.modifier = null;
		prototype.attachments = null;
		prototype.sort = null;
		prototype.withType = function() {
			if (this.type == null) {
				var ExperimentTypeFetchOptions = require("dto/experiment/fetchoptions/ExperimentTypeFetchOptions");
				this.type = new ExperimentTypeFetchOptions();
			}
			return this.type;
		};
		prototype.withTypeUsing = function(fetchOptions) {
			return this.type = fetchOptions;
		};
		prototype.hasType = function() {
			return this.type != null;
		};
		prototype.withProject = function() {
			if (this.project == null) {
				var ProjectFetchOptions = require("dto/project/fetchoptions/ProjectFetchOptions");
				this.project = new ProjectFetchOptions();
			}
			return this.project;
		};
		prototype.withProjectUsing = function(fetchOptions) {
			return this.project = fetchOptions;
		};
		prototype.hasProject = function() {
			return this.project != null;
		};
		prototype.withDataSets = function() {
			if (this.dataSets == null) {
				var DataSetFetchOptions = require("dto/dataset/fetchoptions/DataSetFetchOptions");
				this.dataSets = new DataSetFetchOptions();
			}
			return this.dataSets;
		};
		prototype.withDataSetsUsing = function(fetchOptions) {
			return this.dataSets = fetchOptions;
		};
		prototype.hasDataSets = function() {
			return this.dataSets != null;
		};
		prototype.withSamples = function() {
			if (this.samples == null) {
				var SampleFetchOptions = require("dto/sample/fetchoptions/SampleFetchOptions");
				this.samples = new SampleFetchOptions();
			}
			return this.samples;
		};
		prototype.withSamplesUsing = function(fetchOptions) {
			return this.samples = fetchOptions;
		};
		prototype.hasSamples = function() {
			return this.samples != null;
		};
		prototype.withHistory = function() {
			if (this.history == null) {
				var HistoryEntryFetchOptions = require("dto/history/fetchoptions/HistoryEntryFetchOptions");
				this.history = new HistoryEntryFetchOptions();
			}
			return this.history;
		};
		prototype.withHistoryUsing = function(fetchOptions) {
			return this.history = fetchOptions;
		};
		prototype.hasHistory = function() {
			return this.history != null;
		};
		prototype.withProperties = function() {
			if (this.properties == null) {
				var PropertyFetchOptions = require("dto/property/fetchoptions/PropertyFetchOptions");
				this.properties = new PropertyFetchOptions();
			}
			return this.properties;
		};
		prototype.withPropertiesUsing = function(fetchOptions) {
			return this.properties = fetchOptions;
		};
		prototype.hasProperties = function() {
			return this.properties != null;
		};
		prototype.withMaterialProperties = function() {
			if (this.materialProperties == null) {
				var MaterialFetchOptions = require("dto/material/fetchoptions/MaterialFetchOptions");
				this.materialProperties = new MaterialFetchOptions();
			}
			return this.materialProperties;
		};
		prototype.withMaterialPropertiesUsing = function(fetchOptions) {
			return this.materialProperties = fetchOptions;
		};
		prototype.hasMaterialProperties = function() {
			return this.materialProperties != null;
		};
		prototype.withTags = function() {
			if (this.tags == null) {
				var TagFetchOptions = require("dto/tag/fetchoptions/TagFetchOptions");
				this.tags = new TagFetchOptions();
			}
			return this.tags;
		};
		prototype.withTagsUsing = function(fetchOptions) {
			return this.tags = fetchOptions;
		};
		prototype.hasTags = function() {
			return this.tags != null;
		};
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				var PersonFetchOptions = require("dto/person/fetchoptions/PersonFetchOptions");
				this.registrator = new PersonFetchOptions();
			}
			return this.registrator;
		};
		prototype.withRegistratorUsing = function(fetchOptions) {
			return this.registrator = fetchOptions;
		};
		prototype.hasRegistrator = function() {
			return this.registrator != null;
		};
		prototype.withModifier = function() {
			if (this.modifier == null) {
				var PersonFetchOptions = require("dto/person/fetchoptions/PersonFetchOptions");
				this.modifier = new PersonFetchOptions();
			}
			return this.modifier;
		};
		prototype.withModifierUsing = function(fetchOptions) {
			return this.modifier = fetchOptions;
		};
		prototype.hasModifier = function() {
			return this.modifier != null;
		};
		prototype.withAttachments = function() {
			if (this.attachments == null) {
				var AttachmentFetchOptions = require("dto/attachment/fetchoptions/AttachmentFetchOptions");
				this.attachments = new AttachmentFetchOptions();
			}
			return this.attachments;
		};
		prototype.withAttachmentsUsing = function(fetchOptions) {
			return this.attachments = fetchOptions;
		};
		prototype.hasAttachments = function() {
			return this.attachments != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var ExperimentSortOptions = require("dto/experiment/fetchoptions/ExperimentSortOptions");
				this.sort = new ExperimentSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		type : "ExperimentTypeFetchOptions",
		project : "ProjectFetchOptions",
		dataSets : "DataSetFetchOptions",
		samples : "SampleFetchOptions",
		history : "HistoryEntryFetchOptions",
		properties : "PropertyFetchOptions",
		materialProperties : "MaterialFetchOptions",
		tags : "TagFetchOptions",
		registrator : "PersonFetchOptions",
		modifier : "PersonFetchOptions",
		attachments : "AttachmentFetchOptions",
		sort : "ExperimentSortOptions"
	});
	return ExperimentFetchOptions;
})