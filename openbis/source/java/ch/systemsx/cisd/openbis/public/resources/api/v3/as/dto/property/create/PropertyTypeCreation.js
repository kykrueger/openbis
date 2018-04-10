define([ "stjs" ], function(stjs) {
	var PropertyTypeCreation = function() {
	};
	stjs.extend(PropertyTypeCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.create.PropertyTypeCreation';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.label = null;
		prototype.description = null;
		prototype.managedInternally = null;
		prototype.internalNameSpace = null;
		prototype.dataType = null;
		prototype.vocabularyId = null;
		prototype.materialTypeId = null;
		prototype.schema = null;
		prototype.transformation = null;

		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.isManagedInternally = function() {
			return this.managedInternally;
		};
		prototype.setManagedInternally = function(managedInternally) {
			this.managedInternally = managedInternally;
		};
		prototype.isInternalNameSpace = function() {
			return this.internalNameSpace;
		};
		prototype.setInternalNameSpace = function(internalNameSpace) {
			this.internalNameSpace = internalNameSpace;
		};
		prototype.getDataType = function() {
			return this.dataType;
		};
		prototype.setDataType = function(dataType) {
			this.dataType = dataType;
		};
		prototype.getVocabularyId = function() {
			return this.vocabularyId;
		};
		prototype.setVocabularyId = function(vocabularyId) {
			this.vocabularyId = vocabularyId;
		};
		prototype.getMaterialTypeId = function() {
			return this.materialTypeId;
		};
		prototype.setMaterialTypeId = function(materialTypeId) {
			this.materialTypeId = materialTypeId;
		};
		prototype.getSchema = function() {
			return this.schema;
		};
		prototype.setSchema = function(schema) {
			this.schema = schema;
		};
		prototype.getTransformation = function() {
			return this.transformation;
		};
		prototype.setTransformation = function(transformation) {
			this.transformation = transformation;
		};
	}, {
		dataType : "DataType",
		vocabularyId : "IVocabularyId",
		materialTypeId : "IEntityTypeId"
	});
	return PropertyTypeCreation;
})
