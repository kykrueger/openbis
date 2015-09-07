/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/fetchoptions/FetchOptions", "dto/fetchoptions/person/PersonFetchOptions", "dto/fetchoptions/EmptyFetchOptions", "dto/fetchoptions/attachment/AttachmentSortOptions" ],
		function(require, stjs, FetchOptions) {
			var AttachmentFetchOptions = function() {
			};
			stjs.extend(AttachmentFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
				prototype['@type'] = 'dto.fetchoptions.attachment.AttachmentFetchOptions';
				constructor.serialVersionUID = 1;
				prototype.registrator = null;
				prototype.previousVersion = null;
				prototype.content = null;
				prototype.sort = null;
				prototype.withRegistrator = function() {
					if (this.registrator == null) {
						var PersonFetchOptions = require("dto/fetchoptions/person/PersonFetchOptions");
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
				prototype.withPreviousVersion = function() {
					if (this.previousVersion == null) {
						this.previousVersion = new AttachmentFetchOptions();
					}
					return this.previousVersion;
				};
				prototype.withPreviousVersionUsing = function(fetchOptions) {
					return this.previousVersion = fetchOptions;
				};
				prototype.hasPreviousVersion = function() {
					return this.previousVersion != null;
				};
				prototype.withContent = function() {
					if (this.content == null) {
						var EmptyFetchOptions = require("dto/fetchoptions/EmptyFetchOptions");
						this.content = new EmptyFetchOptions();
					}
					return this.content;
				};
				prototype.withContentUsing = function(fetchOptions) {
					return this.content = fetchOptions;
				};
				prototype.hasContent = function() {
					return this.content != null;
				};
				prototype.sortBy = function() {
					if (this.sort == null) {
						var AttachmentSortOptions = require("dto/fetchoptions/attachment/AttachmentSortOptions");
						this.sort = new AttachmentSortOptions();
					}
				};
				prototype.getSortBy = function() {
					return this.sort;
				};
			}, {
				registrator : "PersonFetchOptions",
				previousVersion : "AttachmentFetchOptions",
				content : "EmptyFetchOptions",
				sort : "AttachmentSortOptions"
			});
			return AttachmentFetchOptions;
		})