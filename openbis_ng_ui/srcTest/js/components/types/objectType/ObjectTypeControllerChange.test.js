import ObjectTypeControler from '@src/js/components/types/objectType/ObjectTypeController.js'
import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import ObjectTypeFacade from '@src/js/components/types/objectType/ObjectTypeFacade'
import objectTypes from '@src/js/common/consts/objectType.js'
import fixture from '@srcTest/js/common/fixture.js'

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

describe('ObjectTypeController.handleChange', () => {
  test('type', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))

    await controller.load()

    expect(context.getState()).toMatchObject({
      type: { description: null }
    })

    controller.handleChange('type', {
      field: 'description',
      value: 'TEST_DESCRIPTION'
    })

    expect(context.getState()).toMatchObject({
      type: { description: 'TEST_DESCRIPTION' }
    })
  })

  test('section', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))

    await controller.load()

    expect(context.getState()).toMatchObject({
      sections: [
        {
          id: 'section-0',
          name: 'TEST_SECTION_1',
          properties: ['property-0']
        },
        {
          id: 'section-1',
          name: 'TEST_SECTION_2',
          properties: ['property-1', 'property-2']
        }
      ]
    })

    controller.handleChange('section', {
      id: 'section-1',
      field: 'name',
      value: 'TEST_NAME'
    })

    expect(context.getState()).toMatchObject({
      sections: [
        {
          id: 'section-0',
          name: 'TEST_SECTION_1',
          properties: ['property-0']
        },
        {
          id: 'section-1',
          name: 'TEST_NAME',
          properties: ['property-1', 'property-2']
        }
      ]
    })
  })

  test('property', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))

    await controller.load()

    expect(context.getState()).toMatchObject({
      properties: [
        { id: 'property-0', code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
        {
          id: 'property-1',
          code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode(),
          description: null
        },
        { id: 'property-2', code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      ]
    })

    controller.handleChange('property', {
      id: 'property-1',
      field: 'description',
      value: 'TEST_DESCRIPTION'
    })

    expect(context.getState()).toMatchObject({
      properties: [
        { id: 'property-0', code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
        {
          id: 'property-1',
          code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode(),
          description: 'TEST_DESCRIPTION'
        },
        { id: 'property-2', code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      ]
    })
  })
})
