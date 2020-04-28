import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import TypeFormControler from '@src/js/components/types/form/TypeFormController.js'
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

describe('TypeFormController.handleAddProperty', () => {
  test('add with a section selected', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))

    await controller.load()
    controller.handleSelectionChange('section', { id: 'section-1' })

    expect(context.getState()).toMatchObject({
      selection: {
        type: 'section',
        params: {
          id: 'section-1'
        }
      },
      properties: [
        { id: 'property-0', code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
        { id: 'property-1', code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
        { id: 'property-2', code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      ],
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

    controller.handleAddProperty()

    expect(context.getState()).toMatchObject({
      selection: {
        type: 'property',
        params: {
          id: 'property-3'
        }
      },
      properties: [
        { id: 'property-0', code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
        { id: 'property-1', code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
        { id: 'property-2', code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() },
        { id: 'property-3', code: null }
      ],
      sections: [
        {
          id: 'section-0',
          name: 'TEST_SECTION_1',
          properties: ['property-0']
        },
        {
          id: 'section-1',
          name: 'TEST_SECTION_2',
          properties: ['property-1', 'property-2', 'property-3']
        }
      ]
    })
  })

  test('add with a property selected', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))

    await controller.load()
    controller.handleSelectionChange('property', { id: 'property-1' })

    expect(context.getState()).toMatchObject({
      selection: {
        type: 'property',
        params: {
          id: 'property-1'
        }
      },
      properties: [
        { id: 'property-0', code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
        { id: 'property-1', code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
        { id: 'property-2', code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      ],
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

    controller.handleAddProperty()

    expect(context.getState()).toMatchObject({
      selection: {
        type: 'property',
        params: {
          id: 'property-3'
        }
      },
      properties: [
        { id: 'property-0', code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
        { id: 'property-1', code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
        { id: 'property-2', code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() },
        { id: 'property-3', code: null }
      ],
      sections: [
        {
          id: 'section-0',
          name: 'TEST_SECTION_1',
          properties: ['property-0']
        },
        {
          id: 'section-1',
          name: 'TEST_SECTION_2',
          properties: ['property-1', 'property-3', 'property-2']
        }
      ]
    })
  })
})
