import ObjectTypeHandlerRemove from '../../../../../src/js/components/types/objectType/ObjectTypeHandlerRemove.js'
import ComponentState from '../../../common/ComponentState.js'

describe('ObjectTypeHandlerRemoveTest', () => {
  test('section not used', () => {
    const componentState = new ComponentState({
      selection: {
        type: 'section',
        params: {
          id: 'section-0'
        }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0', 'property-1']
        },
        {
          id: 'section-1',
          properties: ['property-2', 'property-3', 'property-4']
        }
      ],
      properties: [
        { id: 'property-0', section: 'section-0' },
        { id: 'property-1', section: 'section-0' },
        { id: 'property-2', section: 'section-1' },
        { id: 'property-3', section: 'section-1' },
        { id: 'property-4', section: 'section-1' }
      ]
    })

    executeRemove(componentState)

    componentState.assertState({
      selection: null,
      sections: [
        {
          id: 'section-1',
          properties: ['property-2', 'property-3', 'property-4']
        }
      ],
      properties: [
        { id: 'property-2', section: 'section-1' },
        { id: 'property-3', section: 'section-1' },
        { id: 'property-4', section: 'section-1' }
      ]
    })
  })

  test('section used and confirmed', () => {
    const componentState = new ComponentState({
      selection: {
        type: 'section',
        params: {
          id: 'section-0'
        }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0']
        }
      ],
      properties: [{ id: 'property-0', section: 'section-0', usages: 1 }]
    })

    executeRemove(componentState)
    executeRemove(componentState)

    componentState.assertState({
      removeSectionDialogOpen: true,
      selection: {
        type: 'section',
        params: {
          id: 'section-0'
        }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0']
        }
      ],
      properties: [{ id: 'property-0', section: 'section-0', usages: 1 }]
    })

    executeRemove(componentState, true)

    componentState.assertState({
      removeSectionDialogOpen: false,
      selection: null,
      sections: [],
      properties: []
    })
  })

  test('section used and cancelled', () => {
    const componentState = new ComponentState({
      selection: {
        type: 'section',
        params: {
          id: 'section-0'
        }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0']
        }
      ],
      properties: [{ id: 'property-0', section: 'section-0', usages: 1 }]
    })

    executeRemove(componentState)
    executeRemove(componentState)

    componentState.assertState({
      removeSectionDialogOpen: true,
      selection: {
        type: 'section',
        params: {
          id: 'section-0'
        }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0']
        }
      ],
      properties: [{ id: 'property-0', section: 'section-0', usages: 1 }]
    })

    executeCancel(componentState)

    componentState.assertState({
      removeSectionDialogOpen: false,
      selection: {
        type: 'section',
        params: {
          id: 'section-0'
        }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0']
        }
      ],
      properties: [{ id: 'property-0', section: 'section-0', usages: 1 }]
    })
  })

  test('property not used', () => {
    const componentState = new ComponentState({
      selection: {
        type: 'property',
        params: {
          id: 'property-3'
        }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0', 'property-1']
        },
        {
          id: 'section-1',
          properties: ['property-2', 'property-3', 'property-4']
        }
      ],
      properties: [
        { id: 'property-0', section: 'section-0' },
        { id: 'property-1', section: 'section-0' },
        { id: 'property-2', section: 'section-1' },
        { id: 'property-3', section: 'section-1' },
        { id: 'property-4', section: 'section-1' }
      ]
    })

    executeRemove(componentState)

    componentState.assertState({
      selection: null,
      sections: [
        {
          id: 'section-0',
          properties: ['property-0', 'property-1']
        },
        {
          id: 'section-1',
          properties: ['property-2', 'property-4']
        }
      ],
      properties: [
        { id: 'property-0', section: 'section-0' },
        { id: 'property-1', section: 'section-0' },
        { id: 'property-2', section: 'section-1' },
        { id: 'property-4', section: 'section-1' }
      ]
    })
  })

  test('property used and confirmed', () => {
    const componentState = new ComponentState({
      selection: {
        type: 'property',
        params: {
          id: 'property-0'
        }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0']
        }
      ],
      properties: [{ id: 'property-0', section: 'section-0', usages: 1 }]
    })

    executeRemove(componentState)
    executeRemove(componentState)

    componentState.assertState({
      removePropertyDialogOpen: true,
      selection: {
        type: 'property',
        params: {
          id: 'property-0'
        }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0']
        }
      ],
      properties: [{ id: 'property-0', section: 'section-0', usages: 1 }]
    })

    executeRemove(componentState, true)

    componentState.assertState({
      removePropertyDialogOpen: false,
      selection: null,
      sections: [
        {
          id: 'section-0',
          properties: []
        }
      ],
      properties: []
    })
  })

  test('property used and cancelled', () => {
    const componentState = new ComponentState({
      selection: {
        type: 'property',
        params: {
          id: 'property-0'
        }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0']
        }
      ],
      properties: [{ id: 'property-0', section: 'section-0', usages: 1 }]
    })

    executeRemove(componentState)
    executeRemove(componentState)

    componentState.assertState({
      removePropertyDialogOpen: true,
      selection: {
        type: 'property',
        params: {
          id: 'property-0'
        }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0']
        }
      ],
      properties: [{ id: 'property-0', section: 'section-0', usages: 1 }]
    })

    executeCancel(componentState)

    componentState.assertState({
      removePropertyDialogOpen: false,
      selection: {
        type: 'property',
        params: {
          id: 'property-0'
        }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0']
        }
      ],
      properties: [{ id: 'property-0', section: 'section-0', usages: 1 }]
    })
  })
})

const executeRemove = (componentState, confirmed) => {
  new ObjectTypeHandlerRemove(
    componentState.getState(),
    componentState.getSetState()
  ).executeRemove(confirmed)
}

const executeCancel = componentState => {
  new ObjectTypeHandlerRemove(
    componentState.getState(),
    componentState.getSetState()
  ).executeCancel()
}
