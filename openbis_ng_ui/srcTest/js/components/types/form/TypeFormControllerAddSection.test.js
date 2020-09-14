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

describe('TypeFormController.handleAddProperty', () => {
  test('add with nothing selected', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))

    await controller.load()

    expect(context.getState()).toMatchObject({
      selection: null,
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

    controller.handleAddSection()

    expect(context.getState()).toMatchObject({
      selection: {
        type: TypeFormSelectionType.SECTION,
        params: {
          id: 'section-2'
        }
      },
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
        },
        {
          id: 'section-2',
          name: { value: null },
          properties: []
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
    controller.handleSelectionChange(TypeFormSelectionType.PROPERTY, {
      id: 'property-0'
    })

    expect(context.getState()).toMatchObject({
      selection: {
        type: TypeFormSelectionType.PROPERTY,
        params: {
          id: 'property-0'
        }
      },
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

    controller.handleAddSection()

    expect(context.getState()).toMatchObject({
      selection: {
        type: TypeFormSelectionType.SECTION,
        params: {
          id: 'section-2'
        }
      },
      sections: [
        {
          id: 'section-0',
          name: { value: 'TEST_SECTION_1' },
          properties: ['property-0']
        },
        {
          id: 'section-2',
          name: { value: null },
          properties: []
        },
        {
          id: 'section-1',
          name: { value: 'TEST_SECTION_2' },
          properties: ['property-1', 'property-2']
        }
      ]
    })
  })

  test('add with a section selected', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))

    await controller.load()
    controller.handleSelectionChange(TypeFormSelectionType.SECTION, {
      id: 'section-0'
    })

    expect(context.getState()).toMatchObject({
      selection: {
        type: TypeFormSelectionType.SECTION,
        params: {
          id: 'section-0'
        }
      },
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

    controller.handleAddSection()

    expect(context.getState()).toMatchObject({
      selection: {
        type: TypeFormSelectionType.SECTION,
        params: {
          id: 'section-2'
        }
      },
      sections: [
        {
          id: 'section-0',
          name: { value: 'TEST_SECTION_1' },
          properties: ['property-0']
        },
        {
          id: 'section-2',
          name: { value: null },
          properties: []
        },
        {
          id: 'section-1',
          name: { value: 'TEST_SECTION_2' },
          properties: ['property-1', 'property-2']
        }
      ]
    })
  })
})
