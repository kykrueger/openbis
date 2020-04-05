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

describe('ObjectTypeController.handleRemove', () => {
  test('section not used', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))

    await controller.load()
    controller.handleSelectionChange('section', { id: 'section-0' })

    expect(context.getState()).toMatchObject({
      selection: {
        type: 'section',
        params: {
          id: 'section-0'
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

    controller.handleRemove()

    expect(context.getState()).toMatchObject({
      selection: null,
      properties: [
        { id: 'property-1', code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
        { id: 'property-2', code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      ],
      sections: [
        {
          id: 'section-1',
          name: 'TEST_SECTION_2',
          properties: ['property-1', 'property-2']
        }
      ]
    })
  })

  test('section used and confirmed', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(
      Promise.resolve({
        property: {
          [fixture.TEST_PROPERTY_TYPE_1_DTO.getCode()]: 1
        }
      })
    )

    await controller.load()
    controller.handleSelectionChange('section', { id: 'section-0' })

    expect(context.getState()).toMatchObject({
      selection: {
        type: 'section',
        params: {
          id: 'section-0'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode(),
          usages: 1
        },
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

    controller.handleRemove()

    expect(context.getState()).toMatchObject({
      removeSectionDialogOpen: true,
      selection: {
        type: 'section',
        params: {
          id: 'section-0'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode(),
          usages: 1
        },
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

    controller.handleRemoveConfirm()

    expect(context.getState()).toMatchObject({
      removeSectionDialogOpen: false,
      selection: null,
      properties: [
        { id: 'property-1', code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
        { id: 'property-2', code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      ],
      sections: [
        {
          id: 'section-1',
          name: 'TEST_SECTION_2',
          properties: ['property-1', 'property-2']
        }
      ]
    })
  })

  test('section used and cancelled', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(
      Promise.resolve({
        property: {
          [fixture.TEST_PROPERTY_TYPE_1_DTO.getCode()]: 1
        }
      })
    )

    await controller.load()
    controller.handleSelectionChange('section', { id: 'section-0' })

    expect(context.getState()).toMatchObject({
      selection: {
        type: 'section',
        params: {
          id: 'section-0'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode(),
          usages: 1
        },
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

    controller.handleRemove()

    expect(context.getState()).toMatchObject({
      removeSectionDialogOpen: true,
      selection: {
        type: 'section',
        params: {
          id: 'section-0'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode(),
          usages: 1
        },
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

    controller.handleRemoveCancel()

    expect(context.getState()).toMatchObject({
      removeSectionDialogOpen: false,
      selection: {
        type: 'section',
        params: {
          id: 'section-0'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode(),
          usages: 1
        },
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
  })

  test('property not used', async () => {
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

    controller.handleRemove()

    expect(context.getState()).toMatchObject({
      selection: null,
      properties: [
        { id: 'property-0', code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
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
          properties: ['property-2']
        }
      ]
    })
  })

  test('property used and confirmed', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(
      Promise.resolve({
        property: {
          [fixture.TEST_PROPERTY_TYPE_2_DTO.getCode()]: 1
        }
      })
    )

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
        {
          id: 'property-1',
          code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode(),
          usages: 1
        },
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

    controller.handleRemove()

    expect(context.getState()).toMatchObject({
      removePropertyDialogOpen: true,
      selection: {
        type: 'property',
        params: {
          id: 'property-1'
        }
      },
      properties: [
        { id: 'property-0', code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
        {
          id: 'property-1',
          code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode(),
          usages: 1
        },
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

    controller.handleRemoveConfirm()

    expect(context.getState()).toMatchObject({
      removePropertyDialogOpen: false,
      selection: null,
      properties: [
        { id: 'property-0', code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
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
          properties: ['property-2']
        }
      ]
    })
  })

  test('property used and cancelled', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(
      Promise.resolve({
        property: {
          [fixture.TEST_PROPERTY_TYPE_2_DTO.getCode()]: 1
        }
      })
    )

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
        {
          id: 'property-1',
          code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode(),
          usages: 1
        },
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

    controller.handleRemove()

    expect(context.getState()).toMatchObject({
      removePropertyDialogOpen: true,
      selection: {
        type: 'property',
        params: {
          id: 'property-1'
        }
      },
      properties: [
        { id: 'property-0', code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
        {
          id: 'property-1',
          code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode(),
          usages: 1
        },
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

    controller.handleRemoveCancel()

    expect(context.getState()).toMatchObject({
      removePropertyDialogOpen: false,
      selection: {
        type: 'property',
        params: {
          id: 'property-1'
        }
      },
      properties: [
        { id: 'property-0', code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
        {
          id: 'property-1',
          code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode(),
          usages: 1
        },
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
  })
})
