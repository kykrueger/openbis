/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions", "as/dto/common/Relationship" ], function(stjs, exceptions, Relationship) {
	var Sample = function() {
	};
	stjs.extend(Sample, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.Sample';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.identifier = null;
		prototype.code = null;
		prototype.frozen = null;
		prototype.frozenForComponents = null;
		prototype.frozenForChildren = null;
		prototype.frozenForParents = null;
		prototype.frozenForDataSets = null;
		prototype.registrationDate = null;
		prototype.modificationDate = null;
		prototype.type = null;
		prototype.project = null;		
		prototype.space = null;
		prototype.experiment = null;
		prototype.properties = null;
		prototype.materialProperties = null;
		prototype.sampleProperties = null;
		prototype.parents = null;
		prototype.parentsRelationships = null;
		prototype.children = null;
		prototype.childrenRelationships = null;
		prototype.container = null;
		prototype.components = null;
		prototype.dataSets = null;
		prototype.history = null;
		prototype.tags = null;
		prototype.registrator = null;
		prototype.modifier = null;
		prototype.attachments = null;
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
		prototype.getIdentifier = function() {
			return this.identifier;
		};
		prototype.setIdentifier = function(identifier) {
			this.identifier = identifier;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.isFrozen = function() {
			return this.frozen;
		}
		prototype.setFrozen = function(frozen) {
			this.frozen = frozen;
		}
		prototype.isFrozenForComponents = function() {
			return this.frozenForComponents;
		}
		prototype.setFrozenForComponents = function(frozenForComponents) {
			this.frozenForComponents = frozenForComponents;
		}
		prototype.isFrozenForChildren = function() {
			return this.frozenForChildren;
		}
		prototype.setFrozenForChildren = function(frozenForChildren) {
			this.frozenForChildren = frozenForChildren;
		}
		prototype.isFrozenForParents = function() {
			return this.frozenForParents;
		}
		prototype.setFrozenForParents = function(frozenForParents) {
			this.frozenForParents = frozenForParents;
		}
		prototype.isFrozenForDataSets = function() {
			return this.frozenForDataSets;
		}
		prototype.setFrozenForDataSets = function(frozenForDataSets) {
			this.frozenForDataSets = frozenForDataSets;
		}
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
		prototype.getModificationDate = function() {
			return this.modificationDate;
		};
		prototype.setModificationDate = function(modificationDate) {
			this.modificationDate = modificationDate;
		};
		prototype.getType = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasType()) {
				return this.type;
			} else {
				throw new exceptions.NotFetchedException("Sample type has not been fetched.");
			}
		};
		prototype.setType = function(type) {
			this.type = type;
		};
		prototype.getProject = function() {
			if (this.getFetchOptions().hasProject()) {
				return this.project;
			} else {
				throw new exceptions.NotFetchedException("Project has not been fetched.");
			}
		};
		prototype.setProject = function(project) {
			this.project = project;
		};		
		prototype.getSpace = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSpace()) {
				return this.space;
			} else {
				throw new exceptions.NotFetchedException("Space has not been fetched.");
			}
		};
		prototype.setSpace = function(space) {
			this.space = space;
		};
		prototype.getExperiment = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasExperiment()) {
				return this.experiment;
			} else {
				throw new exceptions.NotFetchedException("Experiment has not been fetched.");
			}
		};
		prototype.setExperiment = function(experiment) {
			this.experiment = experiment;
		};
		prototype.getProperty = function(propertyName) {
			var properties = this.getProperties();
			return properties ? properties[propertyName] : null;
		};
		prototype.setProperty = function(propertyName, propertyValue) {
			if (this.properties == null) {
				this.properties = {};
			}
			this.properties[propertyName] = propertyValue;
		};
		prototype.getProperties = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasProperties()) {
				return this.properties;
			} else {
				throw new exceptions.NotFetchedException("Properties has not been fetched.");
			}
		};
		prototype.setProperties = function(properties) {
			this.properties = properties;
		};
		prototype.getMaterialProperty = function(propertyName) {
			var properties = this.getMaterialProperties();
			return properties ? properties[propertyName] : null;
		};
		prototype.setMaterialProperty = function(propertyName, propertyValue) {
			if (this.materialProperties == null) {
				this.materialProperties = {};
			}
			this.materialProperties[propertyName] = propertyValue;
		};
		prototype.getMaterialProperties = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasMaterialProperties()) {
				return this.materialProperties;
			} else {
				throw new exceptions.NotFetchedException("Material properties have not been fetched.");
			}
		};
		prototype.setMaterialProperties = function(materialProperties) {
			this.materialProperties = materialProperties;
		};
		prototype.getSampleProperties = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSampleProperties()) {
				return this.sampleProperties;
			} else {
				throw new exceptions.NotFetchedException("Sample properties have not been fetched.");
			}
		};
		prototype.setSampleProperties = function(sampleProperties) {
			this.sampleProperties = sampleProperties;
		};
		prototype.getParents = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasParents()) {
				return this.parents;
			} else {
				throw new exceptions.NotFetchedException("Parents has not been fetched.");
			}
		};
		prototype.setParents = function(parents) {
			this.parents = parents;
		};
		prototype.getParentsRelationships = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasParents()) {
				return this.parentsRelationships;
			} else {
				throw new exceptions.NotFetchedException("Parents has not been fetched.");
			}
		};
		prototype.getParentRelationship = function(sampleId) {
			var relationships = this.getParentsRelationships();
			if (relationships == null) {
				return new Relationship();
			}
			return relationships[sampleId];
		};
		prototype.setParentsRelationships = function(parentsRelationships) {
			this.parentsRelationships = parentsRelationships;
		};
		prototype.getChildren = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasChildren()) {
				return this.children;
			} else {
				throw new exceptions.NotFetchedException("Children has not been fetched.");
			}
		};
		prototype.setChildren = function(children) {
			this.children = children;
		};
		prototype.getChildrenRelationships = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasChildren()) {
				return this.childrenRelationships;
			} else {
				throw new exceptions.NotFetchedException("Children has not been fetched.");
			}
		};
		prototype.getChildRelationship = function(sampleId) {
			var relationships = this.getChildrenRelationships();
			if (relationships == null) {
				return new Relationship();
			}
			return relationships[sampleId];
		};
		prototype.setChildrenRelationships = function(childrenRelationships) {
			this.childrenRelationships = childrenRelationships;
		};
		prototype.getContainer = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasContainer()) {
				return this.container;
			} else {
				throw new exceptions.NotFetchedException("Container sample has not been fetched.");
			}
		};
		prototype.setContainer = function(container) {
			this.container = container;
		};
		prototype.getComponents = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasComponents()) {
				return this.components;
			} else {
				throw new exceptions.NotFetchedException("Component samples has not been fetched.");
			}
		};
		prototype.setComponents = function(components) {
			this.components = components;
		};
		prototype.getDataSets = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasDataSets()) {
				return this.dataSets;
			} else {
				throw new exceptions.NotFetchedException("Data sets have not been fetched.");
			}
		};
		prototype.setDataSets = function(dataSets) {
			this.dataSets = dataSets;
		};
		prototype.getHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasHistory()) {
				return this.history;
			} else {
				throw new exceptions.NotFetchedException("History has not been fetched.");
			}
		};
		prototype.setHistory = function(history) {
			this.history = history;
		};
		prototype.getTags = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasTags()) {
				return this.tags;
			} else {
				throw new exceptions.NotFetchedException("Tags has not been fetched.");
			}
		};
		prototype.setTags = function(tags) {
			this.tags = tags;
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
		prototype.getModifier = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasModifier()) {
				return this.modifier;
			} else {
				throw new exceptions.NotFetchedException("Modifier has not been fetched.");
			}
		};
		prototype.setModifier = function(modifier) {
			this.modifier = modifier;
		};
		prototype.getAttachments = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasAttachments()) {
				return this.attachments;
			} else {
				throw new exceptions.NotFetchedException("Attachments has not been fetched.");
			}
		};
		prototype.setAttachments = function(attachments) {
			this.attachments = attachments;
		};
		prototype.toString = function() {
			return "Sample " + this.permId;
		};
	}, {
		fetchOptions : "SampleFetchOptions",
		permId : "SamplePermId",
		identifier : "SampleIdentifier",
		registrationDate : "Date",
		modificationDate : "Date",
		type : "SampleType",
		project : "Project",		
		space : "Space",
		experiment : "Experiment",
		properties : {
			name : "Map",
			arguments : [ null, null ]
		},
		materialProperties : {
			name : "Map",
			arguments : [ null, "Material" ]
		},
		sampleProperties : {
			name : "Map",
			arguments : [ null, "Sample" ]
		},
		parents : {
			name : "List",
			arguments : [ "Sample" ]
		},
		parentsRelationships : {
			name : "Map",
			arguments : [ null, "Relationship" ]
		},
		children : {
			name : "List",
			arguments : [ "Sample" ]
		},
		childrenRelationships : {
			name : "Map",
			arguments : [ null, "Relationship" ]
		},
		container : "Sample",
		components : {
			name : "List",
			arguments : [ "Sample" ]
		},
		dataSets : {
			name : "List",
			arguments : [ "DataSet" ]
		},
		history : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		tags : {
			name : "Set",
			arguments : [ "Tag" ]
		},
		registrator : "Person",
		modifier : "Person",
		attachments : {
			name : "List",
			arguments : [ "Attachment" ]
		}
	});
	return Sample;
})