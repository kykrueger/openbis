import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import TypeFormControler from '@src/js/components/types/form/TypeFormController.js'
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
        type: 'property',
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
          dataType: { value: openbis.DataType.VARCHAR },
          label: { value: null },
          description: { value: null },
          errors: 0
        }
      ]
    })

    await controller.handleSave()

    expect(context.getState()).toMatchObject({
      selection: {
        type: 'type',
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
          dataType: { value: openbis.DataType.VARCHAR },
          label: { value: null, error: 'Label cannot be empty' },
          description: { value: null, error: 'Description cannot be empty' },
          errors: 3
        }
      ]
    })
  })

  test('add property', async () => {
    const SAMPLE_TYPE = new openbis.SampleType()
    SAMPLE_TYPE.setCode('TEST_TYPE')
    SAMPLE_TYPE.setGeneratedCodePrefix('TEST_PREFIX')

    facade.loadType.mockReturnValue(Promise.resolve(SAMPLE_TYPE))
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.executeOperations.mockReturnValue(Promise.resolve({}))

    await controller.load()

    controller.handleAddSection()
    controller.handleAddProperty()
    controller.handleChange('property', {
      id: 'property-0',
      field: 'code',
      value: 'NEW_CODE'
    })
    controller.handleChange('property', {
      id: 'property-0',
      field: 'label',
      value: 'NEW_LABEL'
    })
    controller.handleChange('property', {
      id: 'property-0',
      field: 'description',
      value: 'NEW_DESCRIPTION'
    })

    await controller.handleSave()

    expectExecuteOperations([
      createPropertyTypeOperation(
        'TEST_TYPE.NEW_CODE',
        openbis.DataType.VARCHAR,
        'NEW_LABEL'
      ),
      setPropertyAssignmentOperation(
        SAMPLE_TYPE.getCode(),
        'TEST_TYPE.NEW_CODE',
        false
      )
    ])
  })

  test('update local property assignment', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(SAMPLE_TYPE_WITH_LOCAL_PROPERTY)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.executeOperations.mockReturnValue(Promise.resolve({}))

    await controller.load()

    controller.handleChange('property', {
      id: 'property-0',
      field: 'mandatory',
      value: true
    })

    await controller.handleSave()

    expectExecuteOperations([
      setPropertyAssignmentOperation(
        SAMPLE_TYPE_WITH_LOCAL_PROPERTY.getCode(),
        LOCAL_PROPERTY_TYPE.getCode(),
        true
      )
    ])
  })

  test('update local property type if possible', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(SAMPLE_TYPE_WITH_LOCAL_PROPERTY)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.executeOperations.mockReturnValue(Promise.resolve({}))

    await controller.load()

    controller.handleChange('property', {
      id: 'property-0',
      field: 'label',
      value: 'Updated label'
    })

    await controller.handleSave()

    expectExecuteOperations([
      updatePropertyTypeOperation(
        LOCAL_PROPERTY_TYPE.getCode(),
        'Updated label'
      ),
      setPropertyAssignmentOperation(
        SAMPLE_TYPE_WITH_LOCAL_PROPERTY.getCode(),
        LOCAL_PROPERTY_TYPE.getCode(),
        LOCAL_PROPERTY_ASSIGNMENT.isMandatory()
      )
    ])
  })

  test('update local property type if not possible', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(SAMPLE_TYPE_WITH_LOCAL_PROPERTY)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.executeOperations.mockReturnValue(Promise.resolve({}))

    await controller.load()

    controller.handleChange('property', {
      id: 'property-0',
      field: 'dataType',
      value: openbis.DataType.BOOLEAN
    })

    await controller.handleSave()

    expectExecuteOperations([
      deletePropertyAssignmentOperation(
        SAMPLE_TYPE_WITH_LOCAL_PROPERTY.getCode(),
        LOCAL_PROPERTY_TYPE.getCode(),
        false
      ),
      deletePropertyTypeOperation(LOCAL_PROPERTY_TYPE.getCode()),
      createPropertyTypeOperation(
        LOCAL_PROPERTY_TYPE.getCode(),
        openbis.DataType.BOOLEAN,
        LOCAL_PROPERTY_TYPE.getLabel()
      ),
      setPropertyAssignmentOperation(
        SAMPLE_TYPE_WITH_LOCAL_PROPERTY.getCode(),
        LOCAL_PROPERTY_TYPE.getCode(),
        LOCAL_PROPERTY_ASSIGNMENT.isMandatory()
      )
    ])
  })

  test('update global property assignment', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(SAMPLE_TYPE_WITH_GLOBAL_PROPERTY)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.executeOperations.mockReturnValue(Promise.resolve({}))

    await controller.load()

    controller.handleChange('property', {
      id: 'property-0',
      field: 'mandatory',
      value: true
    })

    await controller.handleSave()

    expectExecuteOperations([
      setPropertyAssignmentOperation(
        SAMPLE_TYPE_WITH_GLOBAL_PROPERTY.getCode(),
        GLOBAL_PROPERTY_TYPE.getCode(),
        true
      )
    ])
  })

  test('update global property type', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(SAMPLE_TYPE_WITH_GLOBAL_PROPERTY)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.executeOperations.mockReturnValue(Promise.resolve({}))

    await controller.load()

    controller.handleChange('property', {
      id: 'property-0',
      field: 'label',
      value: 'Updated label'
    })

    await controller.handleSave()

    const newPropertyTypeCode =
      SAMPLE_TYPE_WITH_GLOBAL_PROPERTY.getCode() +
      '.' +
      GLOBAL_PROPERTY_TYPE.getCode()

    expectExecuteOperations([
      deletePropertyAssignmentOperation(
        SAMPLE_TYPE_WITH_GLOBAL_PROPERTY.getCode(),
        GLOBAL_PROPERTY_TYPE.getCode(),
        false
      ),
      createPropertyTypeOperation(
        newPropertyTypeCode,
        GLOBAL_PROPERTY_TYPE.getDataType(),
        'Updated label'
      ),
      setPropertyAssignmentOperation(
        SAMPLE_TYPE_WITH_GLOBAL_PROPERTY.getCode(),
        newPropertyTypeCode,
        GLOBAL_PROPERTY_ASSIGNMENT.isMandatory()
      )
    ])
  })

  test('delete local property', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(SAMPLE_TYPE_WITH_LOCAL_PROPERTY)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.executeOperations.mockReturnValue(Promise.resolve({}))

    await controller.load()

    controller.handleSelectionChange('property', { id: 'property-0' })
    controller.handleRemove()

    await controller.handleSave()

    expectExecuteOperations([
      deletePropertyAssignmentOperation(
        SAMPLE_TYPE_WITH_LOCAL_PROPERTY.getCode(),
        LOCAL_PROPERTY_TYPE.getCode(),
        false
      ),
      deletePropertyTypeOperation(LOCAL_PROPERTY_TYPE.getCode()),
      setPropertyAssignmentOperation(SAMPLE_TYPE_WITH_LOCAL_PROPERTY.getCode())
    ])
  })

  test('delete global property', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(SAMPLE_TYPE_WITH_GLOBAL_PROPERTY)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.executeOperations.mockReturnValue(Promise.resolve({}))

    await controller.load()

    controller.handleSelectionChange('property', { id: 'property-0' })
    controller.handleRemove()

    await controller.handleSave()

    expectExecuteOperations([
      deletePropertyAssignmentOperation(
        SAMPLE_TYPE_WITH_GLOBAL_PROPERTY.getCode(),
        GLOBAL_PROPERTY_TYPE.getCode(),
        false
      ),
      setPropertyAssignmentOperation(SAMPLE_TYPE_WITH_GLOBAL_PROPERTY.getCode())
    ])
  })

  function createPropertyTypeOperation(
    propertyTypeCode,
    propertyDataType,
    propertyTypeLabel
  ) {
    const creation = new openbis.PropertyTypeCreation()
    creation.setCode(propertyTypeCode)
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
