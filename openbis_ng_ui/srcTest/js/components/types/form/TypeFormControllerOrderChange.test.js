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

describe('TypeFormController.handleOrderChange', () => {
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

    controller.handleOrderChange(TypeFormSelectionType.SECTION, {
      fromIndex: 0,
      toIndex: 1
    })

    expect(context.getState()).toMatchObject({
      sections: [
        {
          id: 'section-1',
          name: { value: 'TEST_SECTION_2' },
          properties: ['property-1', 'property-2']
        },
        {
          id: 'section-0',
          name: { value: 'TEST_SECTION_1' },
          properties: ['property-0']
        }
      ]
    })
  })

  test('property within section', async () => {
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

    controller.handleOrderChange(TypeFormSelectionType.PROPERTY, {
      fromSectionId: 'section-1',
      toSectionId: 'section-1',
      fromIndex: 0,
      toIndex: 1
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
          name: { value: 'TEST_SECTION_2' },
          properties: ['property-2', 'property-1']
        }
      ]
    })
  })

  test('property between sections', async () => {
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

    controller.handleOrderChange(TypeFormSelectionType.PROPERTY, {
      fromSectionId: 'section-1',
      toSectionId: 'section-0',
      fromIndex: 1,
      toIndex: 0
    })

    expect(context.getState()).toMatchObject({
      sections: [
        {
          id: 'section-0',
          name: { value: 'TEST_SECTION_1' },
          properties: ['property-2', 'property-0']
        },
        {
          id: 'section-1',
          name: { value: 'TEST_SECTION_2' },
          properties: ['property-1']
        }
      ]
    })
  })
})
