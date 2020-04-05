import ObjectTypeControler from '@src/js/components/types/objectType/ObjectTypeController.js'
import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import ObjectTypeFacade from '@src/js/components/types/objectType/ObjectTypeFacade'
import fixture from '@srcTest/js/common/fixture.js'

jest.mock('@src/js/components/types/objectType/ObjectTypeFacade')

let context = null
let facade = null
let controller = null

beforeEach(() => {
  jest.resetAllMocks()
  context = new ComponentContext()
  context.setProps({
    objectId: 'TEST_OBJECT_ID'
  })
  facade = new ObjectTypeFacade()
  controller = new ObjectTypeControler(facade)
  controller.init(context)
})

afterEach(() => {
  expect(facade.loadType).toHaveBeenCalledWith(context.getProps().objectId)
  expect(facade.loadUsages).toHaveBeenCalledWith(context.getProps().objectId)
})

describe('ObjectTypeController.handleOrderChange', () => {
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

    controller.handleOrderChange('section', {
      fromIndex: 0,
      toIndex: 1
    })

    expect(context.getState()).toMatchObject({
      sections: [
        {
          id: 'section-1',
          name: 'TEST_SECTION_2',
          properties: ['property-1', 'property-2']
        },
        {
          id: 'section-0',
          name: 'TEST_SECTION_1',
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

    controller.handleOrderChange('property', {
      fromSectionId: 'section-1',
      toSectionId: 'section-1',
      fromIndex: 0,
      toIndex: 1
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
          name: 'TEST_SECTION_2',
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

    controller.handleOrderChange('property', {
      fromSectionId: 'section-1',
      toSectionId: 'section-0',
      fromIndex: 1,
      toIndex: 0
    })

    expect(context.getState()).toMatchObject({
      sections: [
        {
          id: 'section-0',
          name: 'TEST_SECTION_1',
          properties: ['property-2', 'property-0']
        },
        {
          id: 'section-1',
          name: 'TEST_SECTION_2',
          properties: ['property-1']
        }
      ]
    })
  })
})
