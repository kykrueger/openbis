import TypeFormControllerTest from '@srcTest/js/components/types/form/TypeFormControllerTest.js'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new TypeFormControllerTest()
  common.beforeEach()
})

describe(TypeFormControllerTest.SUITE, () => {
  test('load successful existing', testLoadSuccessfulExisting)
  test('load successful new', testLoadSuccessfulNew)
  test('load maintains section selection', testLoadMaintainsSectionSelection)
  test('load maintains property selection', testLoadMaintainsPropertySelection)
})

async function testLoadSuccessfulExisting() {
  common.init({
    id: 'TEST_OBJECT_ID',
    type: objectTypes.OBJECT_TYPE
  })

  common.facade.loadType.mockReturnValue(Promise.resolve(TEST_SAMPLE_TYPE_DTO))
  common.facade.loadUsages.mockReturnValue(
    Promise.resolve({
      type: 10,
      propertyLocal: {
        [TEST_PROPERTY_TYPE_1_DTO.getCode()]: 1,
        [TEST_PROPERTY_TYPE_2_DTO.getCode()]: 2
      },
      propertyGlobal: {
        [TEST_PROPERTY_TYPE_1_DTO.getCode()]: 10,
        [TEST_PROPERTY_TYPE_2_DTO.getCode()]: 20
      }
    })
  )

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
    loading: false,
    selection: null,
    type: {
      code: { value: 'TEST_TYPE' },
      description: { value: 'TEST_DESCRIPTION' },
      listable: { value: true },
      showContainer: { value: true },
      showParents: { value: true },
      showParentMetadata: { value: true },
      autoGeneratedCode: { value: true },
      generatedCodePrefix: { value: 'TEST_PREFIX' },
      subcodeUnique: { value: true },
      validationPlugin: { value: 'TEST_PLUGIN_2' },
      usages: 10
    },
    properties: [
      {
        id: 'property-0',
        code: { value: 'TEST_PROPERTY_TYPE_1' },
        label: { value: 'TEST_LABEL_1' },
        description: { value: 'TEST_DESCRIPTION_1' },
        dataType: { value: 'INTEGER' },
        plugin: { value: 'TEST_PLUGIN_1' },
        mandatory: { value: true },
        showInEditView: { value: true },
        showRawValueInForms: { value: true },
        usagesLocal: 1,
        usagesGlobal: 10,
        section: 'section-0'
      },
      {
        id: 'property-1',
        code: { value: 'TEST_PROPERTY_TYPE_2' },
        label: { value: 'TEST_LABEL_2' },
        description: { value: 'TEST_DESCRIPTION_2' },
        dataType: { value: 'CONTROLLEDVOCABULARY' },
        vocabulary: { value: 'TEST_VOCABULARY' },
        mandatory: { value: true },
        showInEditView: { value: false },
        showRawValueInForms: { value: true },
        usagesLocal: 2,
        usagesGlobal: 20,
        section: 'section-1'
      },
      {
        id: 'property-2',
        code: { value: 'TEST_PROPERTY_TYPE_3' },
        label: { value: 'TEST_LABEL_3' },
        description: { value: 'TEST_DESCRIPTION_3' },
        dataType: { value: 'MATERIAL' },
        materialType: { value: 'TEST_MATERIAL_TYPE' },
        mandatory: { value: false },
        showInEditView: { value: true },
        showRawValueInForms: { value: false },
        usagesLocal: 0,
        usagesGlobal: 0,
        section: 'section-1'
      }
    ],
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0']
      },
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-1', 'property-2']
      }
    ]
  })

  expect(common.facade.loadType).toHaveBeenCalledWith(
    common.context.getProps().object
  )
  expect(common.facade.loadUsages).toHaveBeenCalledWith(
    common.context.getProps().object
  )
}

async function testLoadSuccessfulNew() {
  common.init({
    id: 'TEST_OBJECT_ID',
    type: objectTypes.NEW_OBJECT_TYPE
  })

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
    loading: false,
    selection: null,
    type: {
      code: { value: null },
      description: { value: null },
      listable: { value: true },
      showContainer: { value: false },
      showParents: { value: true },
      showParentMetadata: { value: false },
      autoGeneratedCode: { value: true },
      generatedCodePrefix: { value: null },
      subcodeUnique: { value: false },
      validationPlugin: { value: null },
      usages: 0
    },
    properties: [],
    sections: []
  })
}

async function testLoadMaintainsSectionSelection() {
  common.init({
    id: 'TEST_OBJECT_ID',
    type: objectTypes.OBJECT_TYPE
  })

  common.facade.loadType.mockReturnValue(
    Promise.resolve(new openbis.SampleType())
  )
  common.facade.loadUsages.mockReturnValue({})

  await common.controller.load()

  common.controller.handleAddSection()
  common.controller.handleAddProperty()
  common.controller.handleAddProperty()
  common.controller.handleAddSection()
  common.controller.handleAddProperty()
  common.controller.handleOrderChange(TypeFormSelectionType.SECTION, {
    fromIndex: 0,
    toIndex: 1
  })
  common.controller.handleSelectionChange(TypeFormSelectionType.SECTION, {
    id: 'section-0'
  })

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: TypeFormSelectionType.SECTION,
      params: { id: 'section-0' }
    },
    sections: [
      {
        id: 'section-1',
        properties: ['property-2']
      },
      {
        id: 'section-0',
        properties: ['property-0', 'property-1']
      }
    ]
  })

  common.facade.loadType.mockReturnValue(Promise.resolve(TEST_SAMPLE_TYPE_DTO))
  common.facade.loadUsages.mockReturnValue({})

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: TypeFormSelectionType.SECTION,
      params: { id: 'section-1' }
    },
    sections: [
      {
        id: 'section-0',
        properties: ['property-0']
      },
      {
        id: 'section-1',
        properties: ['property-1', 'property-2']
      }
    ]
  })
}

async function testLoadMaintainsPropertySelection() {
  common.init({
    id: 'TEST_OBJECT_ID',
    type: objectTypes.OBJECT_TYPE
  })

  common.facade.loadType.mockReturnValue(
    Promise.resolve(new openbis.SampleType())
  )
  common.facade.loadUsages.mockReturnValue({})

  await common.controller.load()

  common.controller.handleAddSection()
  common.controller.handleAddProperty()
  common.controller.handleAddProperty()
  common.controller.handleAddSection()
  common.controller.handleAddProperty()
  common.controller.handleOrderChange(TypeFormSelectionType.SECTION, {
    fromIndex: 0,
    toIndex: 1
  })
  common.controller.handleSelectionChange(TypeFormSelectionType.PROPERTY, {
    id: 'property-1'
  })

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: TypeFormSelectionType.PROPERTY,
      params: { id: 'property-1' }
    },
    sections: [
      {
        id: 'section-1',
        properties: ['property-2']
      },
      {
        id: 'section-0',
        properties: ['property-0', 'property-1']
      }
    ]
  })

  common.facade.loadType.mockReturnValue(Promise.resolve(TEST_SAMPLE_TYPE_DTO))
  common.facade.loadUsages.mockReturnValue({})

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: TypeFormSelectionType.PROPERTY,
      params: { id: 'property-2' }
    },
    sections: [
      {
        id: 'section-0',
        properties: ['property-0']
      },
      {
        id: 'section-1',
        properties: ['property-1', 'property-2']
      }
    ]
  })
}

const TEST_PLUGIN_1 = new openbis.Plugin()
TEST_PLUGIN_1.setName('TEST_PLUGIN_1')

const TEST_PLUGIN_2 = new openbis.Plugin()
TEST_PLUGIN_2.setName('TEST_PLUGIN_2')

const TEST_VOCABULARY = new openbis.Vocabulary()
TEST_VOCABULARY.setCode('TEST_VOCABULARY')

const TEST_MATERIAL_TYPE = new openbis.MaterialType()
TEST_MATERIAL_TYPE.setCode('TEST_MATERIAL_TYPE')

const TEST_PROPERTY_TYPE_1_DTO = new openbis.PropertyType()
TEST_PROPERTY_TYPE_1_DTO.setCode('TEST_PROPERTY_TYPE_1')
TEST_PROPERTY_TYPE_1_DTO.setLabel('TEST_LABEL_1')
TEST_PROPERTY_TYPE_1_DTO.setDescription('TEST_DESCRIPTION_1')
TEST_PROPERTY_TYPE_1_DTO.setDataType(openbis.DataType.INTEGER)

const TEST_PROPERTY_TYPE_2_DTO = new openbis.PropertyType()
TEST_PROPERTY_TYPE_2_DTO.setCode('TEST_PROPERTY_TYPE_2')
TEST_PROPERTY_TYPE_2_DTO.setLabel('TEST_LABEL_2')
TEST_PROPERTY_TYPE_2_DTO.setDescription('TEST_DESCRIPTION_2')
TEST_PROPERTY_TYPE_2_DTO.setDataType(openbis.DataType.CONTROLLEDVOCABULARY)
TEST_PROPERTY_TYPE_2_DTO.setVocabulary(TEST_VOCABULARY)

const TEST_PROPERTY_TYPE_3_DTO = new openbis.PropertyType()
TEST_PROPERTY_TYPE_3_DTO.setCode('TEST_PROPERTY_TYPE_3')
TEST_PROPERTY_TYPE_3_DTO.setLabel('TEST_LABEL_3')
TEST_PROPERTY_TYPE_3_DTO.setDescription('TEST_DESCRIPTION_3')
TEST_PROPERTY_TYPE_3_DTO.setDataType(openbis.DataType.MATERIAL)
TEST_PROPERTY_TYPE_3_DTO.setMaterialType(TEST_MATERIAL_TYPE)

const TEST_PROPERTY_ASSIGNMENT_1 = new openbis.PropertyAssignment()
TEST_PROPERTY_ASSIGNMENT_1.setPropertyType(TEST_PROPERTY_TYPE_1_DTO)
TEST_PROPERTY_ASSIGNMENT_1.setSection('TEST_SECTION_1')
TEST_PROPERTY_ASSIGNMENT_1.setMandatory(true)
TEST_PROPERTY_ASSIGNMENT_1.setShowInEditView(true)
TEST_PROPERTY_ASSIGNMENT_1.setShowRawValueInForms(true)
TEST_PROPERTY_ASSIGNMENT_1.setPlugin(TEST_PLUGIN_1)

const TEST_PROPERTY_ASSIGNMENT_2 = new openbis.PropertyAssignment()
TEST_PROPERTY_ASSIGNMENT_2.setPropertyType(TEST_PROPERTY_TYPE_2_DTO)
TEST_PROPERTY_ASSIGNMENT_2.setSection('TEST_SECTION_2')
TEST_PROPERTY_ASSIGNMENT_2.setMandatory(true)
TEST_PROPERTY_ASSIGNMENT_2.setShowInEditView(false)
TEST_PROPERTY_ASSIGNMENT_2.setShowRawValueInForms(true)

const TEST_PROPERTY_ASSIGNMENT_3 = new openbis.PropertyAssignment()
TEST_PROPERTY_ASSIGNMENT_3.setPropertyType(TEST_PROPERTY_TYPE_3_DTO)
TEST_PROPERTY_ASSIGNMENT_3.setSection('TEST_SECTION_2')
TEST_PROPERTY_ASSIGNMENT_3.setMandatory(false)
TEST_PROPERTY_ASSIGNMENT_3.setShowInEditView(true)
TEST_PROPERTY_ASSIGNMENT_3.setShowRawValueInForms(false)

const TEST_SAMPLE_TYPE_DTO = new openbis.SampleType()
TEST_SAMPLE_TYPE_DTO.setCode('TEST_TYPE')
TEST_SAMPLE_TYPE_DTO.setDescription('TEST_DESCRIPTION')
TEST_SAMPLE_TYPE_DTO.setListable(true)
TEST_SAMPLE_TYPE_DTO.setSubcodeUnique(true)
TEST_SAMPLE_TYPE_DTO.setAutoGeneratedCode(true)
TEST_SAMPLE_TYPE_DTO.setShowContainer(true)
TEST_SAMPLE_TYPE_DTO.setShowParents(true)
TEST_SAMPLE_TYPE_DTO.setShowParentMetadata(true)
TEST_SAMPLE_TYPE_DTO.setGeneratedCodePrefix('TEST_PREFIX')
TEST_SAMPLE_TYPE_DTO.setValidationPlugin(TEST_PLUGIN_2)
TEST_SAMPLE_TYPE_DTO.setPropertyAssignments([
  TEST_PROPERTY_ASSIGNMENT_1,
  TEST_PROPERTY_ASSIGNMENT_2,
  TEST_PROPERTY_ASSIGNMENT_3
])
