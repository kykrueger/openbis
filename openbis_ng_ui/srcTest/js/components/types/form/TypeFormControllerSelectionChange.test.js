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
  expect(facade.loadType).toHaveBeenCalledWith(context.getProps().object.id)
  expect(facade.loadUsages).toHaveBeenCalledWith(context.getProps().object.id)
})

describe('TypeFormController.handleSelectionChange', () => {
  test('section', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))

    await controller.load()

    expect(context.getState()).toMatchObject({
      selection: null
    })

    controller.handleSelectionChange('section', { id: 'section-0' })

    expect(context.getState()).toMatchObject({
      selection: {
        type: 'section',
        params: {
          id: 'section-0'
        }
      }
    })

    controller.handleSelectionChange()

    expect(context.getState()).toMatchObject({
      selection: null
    })
  })

  test('property', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))

    await controller.load()

    expect(context.getState()).toMatchObject({
      selection: null
    })

    controller.handleSelectionChange('property', { id: 'property-0' })

    expect(context.getState()).toMatchObject({
      selection: {
        type: 'property',
        params: {
          id: 'property-0'
        }
      }
    })

    controller.handleSelectionChange()

    expect(context.getState()).toMatchObject({
      selection: null
    })
  })
})
