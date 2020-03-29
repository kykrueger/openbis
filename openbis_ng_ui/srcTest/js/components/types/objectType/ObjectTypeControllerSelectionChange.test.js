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
  facade = new ObjectTypeFacade()
  controller = new ObjectTypeControler(facade)
  controller.init(context)
})

describe('ObjectTypeController.handleSelectionChange', () => {
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
