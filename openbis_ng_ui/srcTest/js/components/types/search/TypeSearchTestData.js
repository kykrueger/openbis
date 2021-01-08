import openbis from '@srcTest/js/services/openbis.js'

const testObjectType = new openbis.SampleType()
testObjectType.setCode('TEST_OBJECT_TYPE')
testObjectType.setDescription('Test Description')

const anotherObjectType = new openbis.SampleType()
anotherObjectType.setCode('ANOTHER_OBJECT_TYPE')
anotherObjectType.setDescription('Another Description')

const testCollectionType = new openbis.ExperimentType()
testCollectionType.setCode('TEST_COLLECTION_TYPE')
testCollectionType.setDescription('Test Description')

const anotherCollectionType = new openbis.ExperimentType()
anotherCollectionType.setCode('ANOTHER_COLLECTION_TYPE')
anotherCollectionType.setDescription('Another Description')

const testDataSetType = new openbis.DataSetType()
testDataSetType.setCode('TEST_DATA_SET_TYPE')
testDataSetType.setDescription('Test Description')

const anotherDataSetType = new openbis.DataSetType()
anotherDataSetType.setCode('ANOTHER_DATA_SET_TYPE')
anotherDataSetType.setDescription('Another Description')

const testMaterialType = new openbis.MaterialType()
testMaterialType.setCode('TEST_MATERIAL_TYPE')
testMaterialType.setDescription('Test Description')

const anotherMaterialType = new openbis.MaterialType()
anotherMaterialType.setCode('ANOTHER_MATERIAL_TYPE')
anotherMaterialType.setDescription('Another Description')

const testVocabularyType = new openbis.Vocabulary()
testVocabularyType.setCode('TEST_VOCABULARY_TYPE')
testVocabularyType.setDescription('Test Description')

const anotherVocabularyType = new openbis.Vocabulary()
anotherVocabularyType.setCode('ANOTHER_VOCABULARY_TYPE')
anotherVocabularyType.setDescription('Another Description')

export default {
  testObjectType,
  anotherObjectType,
  testCollectionType,
  anotherCollectionType,
  testDataSetType,
  anotherDataSetType,
  testMaterialType,
  anotherMaterialType,
  testVocabularyType,
  anotherVocabularyType
}
