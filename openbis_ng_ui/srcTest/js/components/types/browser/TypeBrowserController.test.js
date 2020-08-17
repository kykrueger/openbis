import TypeBrowserController from '@src/js/components/types/browser/TypeBrowserController.js'
import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import openbis from '@srcTest/js/services/openbis.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import actions from '@src/js/store/actions/actions.js'
import fixture from '@srcTest/js/common/fixture.js'

let context = null
let controller = null

beforeEach(() => {
  jest.resetAllMocks()

  context = new ComponentContext()
  controller = new TypeBrowserController()
  controller.init(context)

  openbis.mockSearchSampleTypes([
    fixture.TEST_SAMPLE_TYPE_DTO,
    fixture.ANOTHER_SAMPLE_TYPE_DTO
  ])

  openbis.mockSearchExperimentTypes([fixture.TEST_EXPERIMENT_TYPE_DTO])
  openbis.mockSearchDataSetTypes([fixture.TEST_DATA_SET_TYPE_DTO])

  openbis.mockSearchMaterialTypes([
    fixture.TEST_MATERIAL_TYPE_DTO,
    fixture.ANOTHER_MATERIAL_TYPE_DTO
  ])

  openbis.mockSearchVocabularies([
    fixture.TEST_VOCABULARY_DTO,
    fixture.ANOTHER_VOCABULARY_DTO
  ])
})

describe('browser', () => {
  test('load', async () => {
    await controller.load()

    expect(controller.getNodes()).toMatchObject([
      {
        text: 'Object Types',
        expanded: false,
        selected: false
      },
      {
        text: 'Collection Types',
        expanded: false,
        selected: false
      },
      {
        text: 'Data Set Types',
        expanded: false,
        selected: false
      },
      {
        text: 'Material Types',
        expanded: false,
        selected: false
      },
      {
        text: 'Vocabulary Types',
        expanded: false,
        selected: false
      }
    ])

    context.expectNoActions()
  })

  test('filter', async () => {
    await controller.load()

    controller.filterChange('ANOTHER')

    expect(controller.getNodes()).toMatchObject([
      {
        text: 'Object Types',
        expanded: true,
        selected: false,
        children: [
          {
            text: fixture.ANOTHER_SAMPLE_TYPE_DTO.code,
            expanded: false,
            selected: false
          }
        ]
      },
      {
        text: 'Material Types',
        expanded: true,
        selected: false,
        children: [
          {
            text: fixture.ANOTHER_MATERIAL_TYPE_DTO.code,
            expanded: false,
            selected: false
          }
        ]
      },
      {
        text: 'Vocabulary Types',
        expanded: true,
        selected: false,
        children: [
          {
            text: fixture.ANOTHER_VOCABULARY_DTO.code,
            expanded: false,
            selected: false
          }
        ]
      }
    ])

    context.expectNoActions()
  })

  test('add node', async () => {
    await controller.load()

    controller.nodeSelect('objectTypes')
    controller.nodeAdd()

    expectNewTypeAction(objectType.NEW_OBJECT_TYPE)
  })

  test('remove node', async () => {
    openbis.mockSearchPropertyTypes([fixture.TEST_PROPERTY_TYPE_1_DTO])
    openbis.deleteSampleTypes.mockReturnValue(Promise.resolve())

    await controller.load()

    expect(controller.isRemoveNodeDialogOpen()).toBe(false)
    expect(openbis.deleteSampleTypes).toHaveBeenCalledTimes(0)

    controller.nodeSelect('objectTypes/' + fixture.TEST_SAMPLE_TYPE_DTO.code)
    controller.nodeRemove()

    expect(controller.isRemoveNodeDialogOpen()).toBe(true)
    expect(openbis.deleteSampleTypes).toHaveBeenCalledTimes(0)

    await controller.nodeRemoveConfirm()

    expect(controller.isRemoveNodeDialogOpen()).toBe(false)

    const createDeleteTypeOperation = typeCode => {
      const id = new openbis.EntityTypePermId(typeCode)
      const options = new openbis.SampleTypeDeletionOptions()
      options.setReason('deleted via ng_ui')
      return new openbis.DeleteSampleTypesOperation([id], options)
    }

    const createDeletePropertyTypeOperation = propertyTypeCode => {
      const id = new openbis.PropertyTypePermId(propertyTypeCode)
      const options = new openbis.PropertyTypeDeletionOptions()
      options.setReason('deleted via ng_ui')
      return new openbis.DeletePropertyTypesOperation([id], options)
    }

    const options = new openbis.SynchronousOperationExecutionOptions()
    options.setExecuteInOrder(true)

    expect(openbis.executeOperations).toHaveBeenCalledWith(
      [
        createDeleteTypeOperation(fixture.TEST_SAMPLE_TYPE_DTO.code),
        createDeletePropertyTypeOperation(fixture.TEST_PROPERTY_TYPE_1_DTO.code)
      ],
      options
    )

    expectDeleteTypeAction(
      objectType.OBJECT_TYPE,
      fixture.TEST_SAMPLE_TYPE_DTO.code
    )
  })
})

function expectNewTypeAction(type) {
  context.expectAction(actions.objectNew(pages.TYPES, type))
}

function expectDeleteTypeAction(type, id) {
  context.expectAction(actions.objectDelete(pages.TYPES, type, id))
}
