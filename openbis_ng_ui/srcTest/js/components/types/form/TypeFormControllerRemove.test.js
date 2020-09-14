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

describe('TypeFormController.handleRemove', () => {
  test('section not used', async () => {
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
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
        },
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
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

    controller.handleRemove()

    expect(context.getState()).toMatchObject({
      selection: null,
      properties: [
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
      sections: [
        {
          id: 'section-1',
          name: { value: 'TEST_SECTION_2' },
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
        propertyLocal: {
          [fixture.TEST_PROPERTY_TYPE_1_DTO.getCode()]: 1
        },
        propertyGlobal: {
          [fixture.TEST_PROPERTY_TYPE_1_DTO.getCode()]: 10
        }
      })
    )

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
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
          usagesLocal: 1,
          usagesGlobal: 10
        },
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
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

    controller.handleRemove()

    expect(context.getState()).toMatchObject({
      removeSectionDialogOpen: true,
      selection: {
        type: TypeFormSelectionType.SECTION,
        params: {
          id: 'section-0'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
          usagesLocal: 1,
          usagesGlobal: 10
        },
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
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

    controller.handleRemoveConfirm()

    expect(context.getState()).toMatchObject({
      removeSectionDialogOpen: false,
      selection: null,
      properties: [
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
      sections: [
        {
          id: 'section-1',
          name: { value: 'TEST_SECTION_2' },
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
        propertyLocal: {
          [fixture.TEST_PROPERTY_TYPE_1_DTO.getCode()]: 1
        },
        propertyGlobal: {
          [fixture.TEST_PROPERTY_TYPE_1_DTO.getCode()]: 10
        }
      })
    )

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
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
          usagesLocal: 1,
          usagesGlobal: 10
        },
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
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

    controller.handleRemove()

    expect(context.getState()).toMatchObject({
      removeSectionDialogOpen: true,
      selection: {
        type: TypeFormSelectionType.SECTION,
        params: {
          id: 'section-0'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
          usagesLocal: 1,
          usagesGlobal: 10
        },
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
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

    controller.handleRemoveCancel()

    expect(context.getState()).toMatchObject({
      removeSectionDialogOpen: false,
      selection: {
        type: TypeFormSelectionType.SECTION,
        params: {
          id: 'section-0'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() },
          usagesLocal: 1,
          usagesGlobal: 10
        },
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
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
  })

  test('property not used', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))

    await controller.load()
    controller.handleSelectionChange(TypeFormSelectionType.PROPERTY, {
      id: 'property-1'
    })

    expect(context.getState()).toMatchObject({
      selection: {
        type: TypeFormSelectionType.PROPERTY,
        params: {
          id: 'property-1'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
        },
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
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

    controller.handleRemove()

    expect(context.getState()).toMatchObject({
      selection: null,
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
      sections: [
        {
          id: 'section-0',
          name: { value: 'TEST_SECTION_1' },
          properties: ['property-0']
        },
        {
          id: 'section-1',
          name: { value: 'TEST_SECTION_2' },
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
        propertyLocal: {
          [fixture.TEST_PROPERTY_TYPE_2_DTO.getCode()]: 1
        },
        propertyGlobal: {
          [fixture.TEST_PROPERTY_TYPE_2_DTO.getCode()]: 10
        }
      })
    )

    await controller.load()
    controller.handleSelectionChange(TypeFormSelectionType.PROPERTY, {
      id: 'property-1'
    })

    expect(context.getState()).toMatchObject({
      selection: {
        type: TypeFormSelectionType.PROPERTY,
        params: {
          id: 'property-1'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
        },
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
          usagesLocal: 1,
          usagesGlobal: 10
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
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

    controller.handleRemove()

    expect(context.getState()).toMatchObject({
      removePropertyDialogOpen: true,
      selection: {
        type: TypeFormSelectionType.PROPERTY,
        params: {
          id: 'property-1'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
        },
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
          usagesLocal: 1,
          usagesGlobal: 10
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
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

    controller.handleRemoveConfirm()

    expect(context.getState()).toMatchObject({
      removePropertyDialogOpen: false,
      selection: null,
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
      sections: [
        {
          id: 'section-0',
          name: { value: 'TEST_SECTION_1' },
          properties: ['property-0']
        },
        {
          id: 'section-1',
          name: { value: 'TEST_SECTION_2' },
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
        propertyLocal: {
          [fixture.TEST_PROPERTY_TYPE_2_DTO.getCode()]: 1
        },
        propertyGlobal: {
          [fixture.TEST_PROPERTY_TYPE_2_DTO.getCode()]: 10
        }
      })
    )

    await controller.load()
    controller.handleSelectionChange(TypeFormSelectionType.PROPERTY, {
      id: 'property-1'
    })

    expect(context.getState()).toMatchObject({
      selection: {
        type: TypeFormSelectionType.PROPERTY,
        params: {
          id: 'property-1'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
        },
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
          usagesLocal: 1,
          usagesGlobal: 10
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
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

    controller.handleRemove()

    expect(context.getState()).toMatchObject({
      removePropertyDialogOpen: true,
      selection: {
        type: TypeFormSelectionType.PROPERTY,
        params: {
          id: 'property-1'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
        },
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
          usagesLocal: 1,
          usagesGlobal: 10
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
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

    controller.handleRemoveCancel()

    expect(context.getState()).toMatchObject({
      removePropertyDialogOpen: false,
      selection: {
        type: TypeFormSelectionType.PROPERTY,
        params: {
          id: 'property-1'
        }
      },
      properties: [
        {
          id: 'property-0',
          code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
        },
        {
          id: 'property-1',
          code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
          usagesLocal: 1,
          usagesGlobal: 10
        },
        {
          id: 'property-2',
          code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
        }
      ],
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
  })
})
