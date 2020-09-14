import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import TypeFormControler from '@src/js/components/types/form/TypeFormController.js'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import TypeFormFacade from '@src/js/components/types/form/TypeFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'

jest.mock('@src/js/components/types/form/TypeFormFacade')

const LOCAL_PROPERTY_TYPE = new openbis.PropertyType()
LOCAL_PROPERTY_TYPE.setCode('TEST_TYPE.TEST_PROPERTY_TYPE')
LOCAL_PROPERTY_TYPE.setLabel('TEST_LABEL')
LOCAL_PROPERTY_TYPE.setDescription('TEST_DESCRIPTION')
LOCAL_PROPERTY_TYPE.setDataType(openbis.DataType.INTEGER)

const LOCAL_PROPERTY_ASSIGNMENT = new openbis.PropertyAssignment()
LOCAL_PROPERTY_ASSIGNMENT.setPropertyType(LOCAL_PROPERTY_TYPE)

const SAMPLE_TYPE_WITH_LOCAL_PROPERTY = new openbis.SampleType()
SAMPLE_TYPE_WITH_LOCAL_PROPERTY.setCode('TEST_TYPE')
SAMPLE_TYPE_WITH_LOCAL_PROPERTY.setGeneratedCodePrefix('TEST_PREFIX')
SAMPLE_TYPE_WITH_LOCAL_PROPERTY.setPropertyAssignments([
  LOCAL_PROPERTY_ASSIGNMENT
])

const GLOBAL_PROPERTY_TYPE = new openbis.PropertyType()
GLOBAL_PROPERTY_TYPE.setCode('TEST_PROPERTY_TYPE')
GLOBAL_PROPERTY_TYPE.setLabel('TEST_LABEL')
GLOBAL_PROPERTY_TYPE.setDescription('TEST_DESCRIPTION')
GLOBAL_PROPERTY_TYPE.setDataType(openbis.DataType.INTEGER)

const GLOBAL_PROPERTY_ASSIGNMENT = new openbis.PropertyAssignment()
GLOBAL_PROPERTY_ASSIGNMENT.setPropertyType(GLOBAL_PROPERTY_TYPE)

const SAMPLE_TYPE_WITH_GLOBAL_PROPERTY = new openbis.SampleType()
SAMPLE_TYPE_WITH_GLOBAL_PROPERTY.setCode('TEST_TYPE')
SAMPLE_TYPE_WITH_GLOBAL_PROPERTY.setGeneratedCodePrefix('TEST_PREFIX')
SAMPLE_TYPE_WITH_GLOBAL_PROPERTY.setPropertyAssignments([
  GLOBAL_PROPERTY_ASSIGNMENT
])

let context = null
let facade = null
let controller = null

beforeEach(() => {
  jest.resetAllMocks()
  context = new ComponentContext()
  context.setProps({
    object: {
      id: 'TEST_OBJECT_ID',
      type: objectTypes.OBJECT_TYPE
    }
  })
  facade = new TypeFormFacade()
  controller = new TypeFormControler(facade)
  controller.init(context)
})

afterEach(() => {
  expect(facade.loadType).toHaveBeenCalledWith(context.getProps().object)
  expect(facade.loadUsages).toHaveBeenCalledWith(context.getProps().object)
})

describe('TypeFormController.handleSave', () => {
  test('validation', async () => {
    const SAMPLE_TYPE = new openbis.SampleType()

    facade.loadType.mockReturnValue(Promise.resolve(SAMPLE_TYPE))
    facade.loadUsages.mockReturnValue(Promise.resolve({}))

    await controller.load()
    controller.handleAddSection()
    controller.handleAddProperty()

    expect(context.getState()).toMatchObject({
      selection: {
        type: TypeFormSelectionType.PROPERTY,
        params: {
          id: 'property-0'
        }
      },
      type: {
        errors: 0
      },
      properties: [
        {
          id: 'property-0',
          code: { value: null },
          dataType: { value: null },
          label: { value: null },
          description: { value: null },
          errors: 0
        }
      ]
    })

    await controller.handleSave()

    expect(context.getState()).toMatchObject({
      selection: {
        type: TypeFormSelectionType.TYPE,
        params: {
          part: 'code'
        }
      },
      type: {
        code: {
          error: 'Code cannot be empty'
        },
        generatedCodePrefix: {
          error: 'Generated code prefix cannot be empty'
        },
        errors: 2
      },
      properties: [
        {
          id: 'property-0',
          code: { value: null, error: 'Code cannot be empty' },
          dataType: { value: null, error: 'Data Type cannot be empty' },
          label: { value: null, error: 'Label cannot be empty' },
          description: { value: null, error: 'Description cannot be empty' },
          errors: 4
        }
      ]
    })
  })

  test('add local property', async () => {
    await testAddProperty('local')
  })

  test('add global property', async () => {
    await testAddProperty('global')
  })

  test('update local property assignment', async () => {
    await testUpdatePropertyAssignment(
      SAMPLE_TYPE_WITH_LOCAL_PROPERTY,
      LOCAL_PROPERTY_TYPE
    )
  })

  test('update global property assignment', async () => {
    await testUpdatePropertyAssignment(
      SAMPLE_TYPE_WITH_GLOBAL_PROPERTY,
      GLOBAL_PROPERTY_TYPE
    )
  })

  test('update local property type if possible', async () => {
    await testUpdatePropertyTypeIfPossible(
      SAMPLE_TYPE_WITH_LOCAL_PROPERTY,
      LOCAL_PROPERTY_TYPE,
      LOCAL_PROPERTY_ASSIGNMENT
    )
  })

  test('update global property type if possible', async () => {
    await testUpdatePropertyTypeIfPossible(
      SAMPLE_TYPE_WITH_GLOBAL_PROPERTY,
      GLOBAL_PROPERTY_TYPE,
      GLOBAL_PROPERTY_ASSIGNMENT
    )
  })

  test('delete local property', async () => {
    await testDeleteProperty(
      SAMPLE_TYPE_WITH_LOCAL_PROPERTY,
      LOCAL_PROPERTY_TYPE
    )
  })

  test('delete global property', async () => {
    await testDeleteProperty(
      SAMPLE_TYPE_WITH_GLOBAL_PROPERTY,
      GLOBAL_PROPERTY_TYPE
    )
  })

  test('delete local property last assignment', async () => {
    await testDeletePropertyLastAssignment(
      SAMPLE_TYPE_WITH_LOCAL_PROPERTY,
      LOCAL_PROPERTY_TYPE
    )
  })

  test('delete global property last assignment', async () => {
    await testDeletePropertyLastAssignment(
      SAMPLE_TYPE_WITH_GLOBAL_PROPERTY,
      GLOBAL_PROPERTY_TYPE
    )
  })

  async function testAddProperty(scope) {
    const SAMPLE_TYPE = new openbis.SampleType()
    SAMPLE_TYPE.setCode('TEST_TYPE')
    SAMPLE_TYPE.setGeneratedCodePrefix('TEST_PREFIX')

    facade.loadType.mockReturnValue(Promise.resolve(SAMPLE_TYPE))
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.loadGlobalPropertyTypes.mockReturnValue(Promise.resolve([]))
    facade.executeOperations.mockReturnValue(Promise.resolve({}))

    await controller.load()

    controller.handleAddSection()
    controller.handleAddProperty()

    controller.handleChange(TypeFormSelectionType.PROPERTY, {
      id: 'property-0',
      field: 'scope',
      value: scope
    })
    controller.handleChange(TypeFormSelectionType.PROPERTY, {
      id: 'property-0',
      field: 'code',
      value: 'NEW_CODE'
    })
    controller.handleChange(TypeFormSelectionType.PROPERTY, {
      id: 'property-0',
      field: 'dataType',
      value: 'VARCHAR'
    })
    controller.handleChange(TypeFormSelectionType.PROPERTY, {
      id: 'property-0',
      field: 'label',
      value: 'NEW_LABEL'
    })
    controller.handleChange(TypeFormSelectionType.PROPERTY, {
      id: 'property-0',
      field: 'description',
      value: 'NEW_DESCRIPTION'
    })

    await controller.handleSave()

    const propertyTypeCode =
      scope === 'local' ? 'TEST_TYPE.NEW_CODE' : 'NEW_CODE'

    expectExecuteOperations([
      createPropertyTypeOperation({
        code: propertyTypeCode,
        dataType: openbis.DataType.VARCHAR,
        label: 'NEW_LABEL'
      }),
      setPropertyAssignmentOperation(
        SAMPLE_TYPE.getCode(),
        propertyTypeCode,
        false
      )
    ])
  }

  async function testUpdatePropertyAssignment(type, propertyType) {
    facade.loadType.mockReturnValue(Promise.resolve(type))
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.executeOperations.mockReturnValue(Promise.resolve({}))

    await controller.load()

    controller.handleChange(TypeFormSelectionType.PROPERTY, {
      id: 'property-0',
      field: 'mandatory',
      value: true
    })

    await controller.handleSave()

    expectExecuteOperations([
      setPropertyAssignmentOperation(
        type.getCode(),
        propertyType.getCode(),
        true
      )
    ])
  }

  async function testUpdatePropertyTypeIfPossible(
    type,
    propertyType,
    propertyAssignment
  ) {
    facade.loadType.mockReturnValue(Promise.resolve(type))
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.executeOperations.mockReturnValue(Promise.resolve({}))

    await controller.load()

    controller.handleChange(TypeFormSelectionType.PROPERTY, {
      id: 'property-0',
      field: 'label',
      value: 'Updated label'
    })

    await controller.handleSave()

    expectExecuteOperations([
      updatePropertyTypeOperation(propertyType.getCode(), 'Updated label'),
      setPropertyAssignmentOperation(
        type.getCode(),
        propertyType.getCode(),
        propertyAssignment.isMandatory()
      )
    ])
  }

  async function testDeleteProperty(type, propertyType) {
    facade.loadType.mockReturnValue(Promise.resolve(type))
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.executeOperations.mockReturnValue(Promise.resolve({}))

    await controller.load()

    controller.handleSelectionChange(TypeFormSelectionType.PROPERTY, {
      id: 'property-0'
    })
    controller.handleRemove()

    await controller.handleSave()

    expectExecuteOperations([
      deletePropertyAssignmentOperation(
        type.getCode(),
        propertyType.getCode(),
        false
      ),
      setPropertyAssignmentOperation(type.getCode())
    ])
  }

  async function testDeletePropertyLastAssignment(type, propertyType) {
    facade.loadType.mockReturnValue(Promise.resolve(type))
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.loadAssignments.mockReturnValue(
      Promise.resolve({
        [propertyType.getCode()]: 1
      })
    )
    facade.executeOperations.mockReturnValue(Promise.resolve({}))

    await controller.load()

    controller.handleSelectionChange(TypeFormSelectionType.PROPERTY, {
      id: 'property-0'
    })
    controller.handleRemove()

    await controller.handleSave()

    expectExecuteOperations([
      deletePropertyAssignmentOperation(
        type.getCode(),
        propertyType.getCode(),
        false
      ),
      deletePropertyTypeOperation(propertyType.getCode()),
      setPropertyAssignmentOperation(type.getCode())
    ])
  }

  function createPropertyTypeOperation({
    code: propertyTypeCode,
    dataType: propertyDataType,
    vocabulary: propertyTypeVocabulary,
    label: propertyTypeLabel
  }) {
    const creation = new openbis.PropertyTypeCreation()
    creation.setCode(propertyTypeCode)
    creation.setDataType(propertyDataType)
    if (propertyTypeVocabulary) {
      creation.setVocabularyId(
        new openbis.VocabularyPermId(propertyTypeVocabulary)
      )
    }
    creation.setLabel(propertyTypeLabel)
    return new openbis.CreatePropertyTypesOperation([creation])
  }

  function updatePropertyTypeOperation(propertyTypeCode, propertyTypeLabel) {
    const update = new openbis.PropertyTypeUpdate()
    update.setTypeId(new openbis.PropertyTypePermId(propertyTypeCode))
    update.setLabel(propertyTypeLabel)
    return new openbis.UpdatePropertyTypesOperation([update])
  }

  function deletePropertyTypeOperation(propertyTypeCode) {
    const id = new openbis.PropertyTypePermId(propertyTypeCode)
    const options = new openbis.PropertyTypeDeletionOptions()
    options.setReason('deleted via ng_ui')
    return new openbis.DeletePropertyTypesOperation([id], options)
  }

  function setPropertyAssignmentOperation(
    typeCode,
    propertyCode,
    propertyMandatory
  ) {
    const assignments = []
    if (propertyCode) {
      let assignment = new openbis.PropertyAssignmentCreation()
      assignment.setPropertyTypeId(new openbis.PropertyTypePermId(propertyCode))
      assignment.setMandatory(propertyMandatory)
      assignments.push(assignment)
    }

    const update = new openbis.SampleTypeUpdate()
    update.setTypeId(
      new openbis.EntityTypePermId(typeCode, openbis.EntityKind.SAMPLE)
    )
    update.getPropertyAssignments().set(assignments)

    return new openbis.UpdateSampleTypesOperation([update])
  }

  function deletePropertyAssignmentOperation(typeCode, propertyCode, force) {
    const assignmentId = new openbis.PropertyAssignmentPermId(
      new openbis.EntityTypePermId(typeCode, openbis.EntityKind.SAMPLE),
      new openbis.PropertyTypePermId(propertyCode)
    )

    const update = new openbis.SampleTypeUpdate()
    update.setTypeId(
      new openbis.EntityTypePermId(typeCode, openbis.EntityKind.SAMPLE)
    )
    update.getPropertyAssignments().remove([assignmentId])
    update.getPropertyAssignments().setForceRemovingAssignments(force)

    return new openbis.UpdateSampleTypesOperation([update])
  }

  function expectExecuteOperations(expectedOperations) {
    expect(facade.executeOperations).toHaveBeenCalledTimes(1)
    const actualOperations = facade.executeOperations.mock.calls[0][0]
    expect(actualOperations.length).toEqual(expectedOperations.length)
    actualOperations.forEach((actualOperation, index) => {
      expect(actualOperation).toMatchObject(expectedOperations[index])
    })
  }
})
