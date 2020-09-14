import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import TypeFormControler from '@src/js/components/types/form/TypeFormController.js'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import TypeFormFacade from '@src/js/components/types/form/TypeFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'
import fixture from '@srcTest/js/common/fixture.js'

jest.mock('@src/js/components/types/form/TypeFormFacade')

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

describe('TypeFormController.handleChange', () => {
  test('type', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))

    await controller.load()

    expect(context.getState()).toMatchObject({
      type: {
        description: { value: 'TEST_DESCRIPTION' }
      }
    })

    controller.handleChange(TypeFormSelectionType.TYPE, {
      field: 'description',
      value: 'NEW_DESCRIPTION'
    })

    expect(context.getState()).toMatchObject({
      type: {
        description: { value: 'NEW_DESCRIPTION' }
      }
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

    controller.handleChange(TypeFormSelectionType.SECTION, {
      id: 'section-1',
      field: 'name',
      value: 'TEST_NAME'
    })

    expect(context.getState()).toMatchObject({
      sections: [
        {
          id: 'section-0',
          name: { value: 'TEST_SECTION_1' },
          properties: ['property-0']
        },
        {
          id: 'section-1',
          name: { value: 'TEST_NAME' },
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
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
        },
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
          description: { value: null }
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ]
    })

    controller.handleChange(TypeFormSelectionType.PROPERTY, {
      id: 'property-1',
      field: 'description',
      value: 'TEST_DESCRIPTION'
    })

    expect(context.getState()).toMatchObject({
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
        },
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
          description: { value: 'TEST_DESCRIPTION' }
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ]
    })
  })
})
