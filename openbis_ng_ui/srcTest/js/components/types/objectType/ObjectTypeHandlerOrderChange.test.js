import ObjectTypeHandlerOrderChange from '@src/js/components/types/objectType/ObjectTypeHandlerOrderChange.js'
import ComponentState from '@srcTest/js/common/ComponentState.js'

describe('ObjectTypeHandlerOrderChangeTest', () => {
  test('section', () => {
    const componentState = new ComponentState({
      sections: [
        {
          id: 'section-0'
        },
        {
          id: 'section-1'
        },
        {
          id: 'section-2'
        }
      ]
    })

    execute(componentState, 'section', {
      fromIndex: 2,
      toIndex: 0
    })

    componentState.assertState({
      sections: [
        {
          id: 'section-2'
        },
        {
          id: 'section-0'
        },
        {
          id: 'section-1'
        }
      ]
    })
  })

  test('property', () => {
    const componentState = new ComponentState({
      sections: [
        {
          id: 'section-0',
          properties: ['property-0', 'property-1']
        },
        {
          id: 'section-1',
          properties: []
        },
        {
          id: 'section-2',
          properties: ['property-2', 'property-3', 'property-4']
        }
      ],
      properties: [
        { id: 'property-0', section: 'section-0' },
        { id: 'property-1', section: 'section-0' },
        { id: 'property-2', section: 'section-2' },
        { id: 'property-3', section: 'section-2' },
        { id: 'property-4', section: 'section-2' }
      ]
    })

    execute(componentState, 'property', {
      fromSectionId: 'section-2',
      toSectionId: 'section-2',
      fromIndex: 0,
      toIndex: 1
    })

    componentState.assertState({
      sections: [
        {
          id: 'section-0',
          properties: ['property-0', 'property-1']
        },
        {
          id: 'section-1',
          properties: []
        },
        {
          id: 'section-2',
          properties: ['property-3', 'property-2', 'property-4']
        }
      ],
      properties: [
        { id: 'property-0', section: 'section-0' },
        { id: 'property-1', section: 'section-0' },
        { id: 'property-2', section: 'section-2' },
        { id: 'property-3', section: 'section-2' },
        { id: 'property-4', section: 'section-2' }
      ]
    })

    execute(componentState, 'property', {
      fromSectionId: 'section-2',
      toSectionId: 'section-0',
      fromIndex: 1,
      toIndex: 1
    })

    componentState.assertState({
      sections: [
        {
          id: 'section-0',
          properties: ['property-0', 'property-2', 'property-1']
        },
        {
          id: 'section-1',
          properties: []
        },
        {
          id: 'section-2',
          properties: ['property-3', 'property-4']
        }
      ],
      properties: [
        { id: 'property-0', section: 'section-0' },
        { id: 'property-1', section: 'section-0' },
        { id: 'property-2', section: 'section-0' },
        { id: 'property-3', section: 'section-2' },
        { id: 'property-4', section: 'section-2' }
      ]
    })
  })
})

const execute = (componentState, type, params) => {
  new ObjectTypeHandlerOrderChange(
    componentState.getState(),
    componentState.getSetState()
  ).execute(type, params)
}
