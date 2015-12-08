/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/update/FieldUpdateValue", "dto/common/update/IdListUpdateValue", "dto/attachment/update/AttachmentListUpdateValue" ], function(stjs, FieldUpdateValue, IdListUpdateValue,
		AttachmentListUpdateValue) {
	var SampleUpdate = function() {
		this.properties = {};
		this.experimentId = new FieldUpdateValue();
		this.projectId = new FieldUpdateValue();
		this.spaceId = new FieldUpdateValue();
		this.tagIds = new IdListUpdateValue();
		this.containerId = new FieldUpdateValue();
		this.componentIds = new IdListUpdateValue();
		this.parentIds = new IdListUpdateValue();
		this.childIds = new IdListUpdateValue();
		this.attachments = new AttachmentListUpdateValue();
	};
	stjs.extend(SampleUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.sample.update.SampleUpdate';
		constructor.serialVersionUID = 1;
		prototype.sampleId = null;

		prototype.getSampleId = function() {
			return this.sampleId;
		};
		prototype.setSampleId = function(sampleId) {
			this.sampleId = sampleId;
		};
		prototype.getProjectId = function() {
			return this.projectId;
		};
		prototype.setProjectId = function(projectId) {
			this.projectId.setValue(projectId);
		};
		prototype.getExperimentId = function() {
			return this.experimentId;
		};
		prototype.setExperimentId = function(experimentId) {
			this.experimentId.setValue(experimentId);
		};
		prototype.getSpaceId = function() {
			return this.spaceId;
		};
		prototype.setSpaceId = function(spaceId) {
			this.spaceId.setValue(spaceId);
		};
		prototype.getContainerId = function() {
			return this.containerId;
		};
		prototype.setContainerId = function(containerId) {
			this.containerId.setValue(containerId);
		};
		prototype.setProperty = function(key, value) {
			this.properties[key] = value;
		};
		prototype.getProperties = function() {
			return this.properties;
		};
		prototype.getTagIds = function() {
			return this.tagIds;
		};
		prototype.setTagActions = function(actions) {
			this.tagIds.setActions(actions);
		};
		prototype.getComponentIds = function() {
			return this.componentIds;
		};
		prototype.setComponentActions = function(actions) {
			this.componentIds.setActions(actions);
		};
		prototype.getParentIds = function() {
			return this.parentIds;
		};
		prototype.setParentActions = function(actions) {
			this.parentIds.setActions(actions);
		};
		prototype.getChildIds = function() {
			return this.childIds;
		};
		prototype.setChildActions = function(actions) {
			this.childIds.setActions(actions);
		};
		prototype.getAttachments = function() {
			return this.attachments;
		};
		prototype.setAttachmentsActions = function(actions) {
			this.attachments.setActions(actions);
		};
	}, {
		sampleId : "ISampleId",
		experimentId : {
			name : "FieldUpdateValue",
			arguments : [ "IExperimentId" ]
		},
		projectId : {
			name : "FieldUpdateValue",
			arguments : [ "IProjectId" ]
		},
		spaceId : {
			name : "FieldUpdateValue",
			arguments : [ "ISpaceId" ]
		},
		tagIds : {
			name : "IdListUpdateValue",
			arguments : [ "ITagId" ]
		},
		properties : {
			name : "Map",
			arguments : [ null, null ]
		},
		containerId : {
			name : "FieldUpdateValue",
			arguments : [ "ISampleId" ]
		},
		componentIds : {
			name : "IdListUpdateValue",
			arguments : [ "ISampleId" ]
		},
		parentIds : {
			name : "IdListUpdateValue",
			arguments : [ "ISampleId" ]
		},
		childIds : {
			name : "IdListUpdateValue",
			arguments : [ "ISampleId" ]
		},
		attachments : "AttachmentListUpdateValue"
	});
	return SampleUpdate;
})