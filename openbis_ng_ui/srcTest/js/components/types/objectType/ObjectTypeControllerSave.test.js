import ObjectTypeControler from '@src/js/components/types/objectType/ObjectTypeController.js'
import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import ObjectTypeFacade from '@src/js/components/types/objectType/ObjectTypeFacade'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'

jest.mock('@src/js/components/types/objectType/ObjectTypeFacade')

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
  facade = new ObjectTypeFacade()
  controller = new ObjectTypeControler(facade)
  controller.init(context)
})

afterEach(() => {
  expect(facade.loadType).toHaveBeenCalledWith(context.getProps().object.id)
  expect(facade.loadUsages).toHaveBeenCalledWith(context.getProps().object.id)
})

describe('ObjectTypeController.handleSave', () => {
  test('validation', async () => {
    const TEST_PROPERTY_TYPE_DTO = new openbis.PropertyType()
    TEST_PROPERTY_TYPE_DTO.setCode('TEST_PROPERTY_TYPE')
    TEST_PROPERTY_TYPE_DTO.setLabel('TEST_LABEL')
    TEST_PROPERTY_TYPE_DTO.setDescription('TEST_DESCRIPTION')
    TEST_PROPERTY_TYPE_DTO.setDataType(openbis.DataType.INTEGER)

    const TEST_PROPERTY_ASSIGNMENT = new openbis.PropertyAssignment()
    TEST_PROPERTY_ASSIGNMENT.setPropertyType(TEST_PROPERTY_TYPE_DTO)

    const TEST_SAMPLE_TYPE_DTO = new openbis.SampleType()
    TEST_SAMPLE_TYPE_DTO.setCode('TEST_TYPE')
    TEST_SAMPLE_TYPE_DTO.setPropertyAssignments([TEST_PROPERTY_ASSIGNMENT])

    facade.loadType.mockReturnValue(Promise.resolve(TEST_SAMPLE_TYPE_DTO))
    facade.loadUsages.mockReturnValue(Promise.resolve({}))

    await controller.load()
    controller.handleSelectionChange('section', { id: 'section-0' })
    controller.handleAddProperty()

    expect(context.getState()).toMatchObject({
      selection: {
        type: 'property',
        params: {
          id: 'property-1'
        }
      },
      type: {
        errors: {}
      },
      properties: [
        {
          id: 'property-0',
          code: TEST_PROPERTY_TYPE_DTO.getCode(),
          dataType: TEST_PROPERTY_TYPE_DTO.getDataType(),
          label: TEST_PROPERTY_TYPE_DTO.getLabel(),
          description: TEST_PROPERTY_TYPE_DTO.getDescription(),
          errors: {}
        },
        {
          id: 'property-1',
          code: null,
          dataType: openbis.DataType.VARCHAR,
          label: null,
          description: null,
          errors: {}
        }
      ]
    })

    await controller.handleSave()

    expect(context.getState()).toMatchObject({
      selection: {
        type: 'type',
        params: {
          part: 'generatedCodePrefix'
        }
      },
      type: {
        errors: {
          generatedCodePrefix: 'Generated code prefix cannot be empty'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: TEST_PROPERTY_TYPE_DTO.getCode(),
          dataType: TEST_PROPERTY_TYPE_DTO.getDataType(),
          label: TEST_PROPERTY_TYPE_DTO.getLabel(),
          description: TEST_PROPERTY_TYPE_DTO.getDescription(),
          errors: {}
        },
        {
          id: 'property-1',
          code: null,
          dataType: openbis.DataType.VARCHAR,
          label: null,
          description: null,
          errors: {
            code: 'Code cannot be empty',
            description: 'Description cannot be empty',
            label: 'Label cannot be empty'
          }
        }
      ]
    })
  })

  test('add property', async () => {
    const TEST_PROPERTY_TYPE_DTO = new openbis.PropertyType()
    TEST_PROPERTY_TYPE_DTO.setCode('TEST_PROPERTY_TYPE')
    TEST_PROPERTY_TYPE_DTO.setLabel('TEST_LABEL')
    TEST_PROPERTY_TYPE_DTO.setDescription('TEST_DESCRIPTION')
    TEST_PROPERTY_TYPE_DTO.setDataType(openbis.DataType.INTEGER)

    const TEST_PROPERTY_ASSIGNMENT = new openbis.PropertyAssignment()
    TEST_PROPERTY_ASSIGNMENT.setPropertyType(TEST_PROPERTY_TYPE_DTO)

    const TEST_SAMPLE_TYPE_DTO = new openbis.SampleType()
    TEST_SAMPLE_TYPE_DTO.setCode('TEST_TYPE')
    TEST_SAMPLE_TYPE_DTO.setGeneratedCodePrefix('TEST_PREFIX')
    TEST_SAMPLE_TYPE_DTO.setPropertyAssignments([TEST_PROPERTY_ASSIGNMENT])

    facade.loadType.mockReturnValue(Promise.resolve(TEST_SAMPLE_TYPE_DTO))
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.executeOperations.mockReturnValue(Promise.resolve({}))

    await controller.load()

    expect(context.getState()).toMatchObject({
      selection: null,
      properties: [
        { id: 'property-0', code: TEST_PROPERTY_TYPE_DTO.getCode() }
      ],
      sections: [
        {
          id: 'section-0',
          properties: ['property-0']
        }
      ]
    })

    controller.handleSelectionChange('section', { id: 'section-0' })
    controller.handleAddProperty()
    controller.handleChange('property', {
      id: 'property-1',
      field: 'code',
      value: 'NEW_CODE'
    })
    controller.handleChange('property', {
      id: 'property-1',
      field: 'label',
      value: 'NEW_LABEL'
    })
    controller.handleChange('property', {
      id: 'property-1',
      field: 'description',
      value: 'NEW_DESCRIPTION'
    })

    expect(context.getState()).toMatchObject({
      selection: {
        type: 'property',
        params: {
          id: 'property-1'
        }
      },
      properties: [
        { id: 'property-0', code: TEST_PROPERTY_TYPE_DTO.getCode() },
        {
          id: 'property-1',
          code: 'NEW_CODE',
          label: 'NEW_LABEL',
          description: 'NEW_DESCRIPTION'
        }
      ],
      sections: [
        {
          id: 'section-0',
          properties: ['property-0', 'property-1']
        }
      ]
    })

    await controller.handleSave()

    expect(facade.executeOperations).toHaveBeenCalledTimes(1)
    const operations = facade.executeOperations.mock.calls[0][0]

    expect(operations).toMatchObject([
      createPropertyTypeOperation('TEST_TYPE.NEW_CODE'),
      updateSampleTypeOperation(TEST_SAMPLE_TYPE_DTO.getCode(), [
        propertyAssignment(TEST_PROPERTY_TYPE_DTO.getCode()),
        propertyAssignment('TEST_TYPE.NEW_CODE')
      ])
    ])
  })

  function createPropertyTypeOperation(propertyTypeCode) {
    const creation = new openbis.PropertyTypeCreation()
    creation.setCode(propertyTypeCode)
    return new openbis.CreatePropertyTypesOperation([creation])
  }

  function updateSampleTypeOperation(typeCode, propertyAssignments) {
    const update = new openbis.SampleTypeUpdate()
    update.setTypeId(
      new openbis.EntityTypePermId(typeCode, openbis.EntityKind.SAMPLE)
    )
    update.getPropertyAssignments().set(propertyAssignments.reverse())
    return new openbis.UpdateSampleTypesOperation([update])
  }

  function propertyAssignment(propertyCode) {
    let creation = new openbis.PropertyAssignmentCreation()
    creation.setPropertyTypeId(new openbis.PropertyTypePermId(propertyCode))
    return creation
  }
})
