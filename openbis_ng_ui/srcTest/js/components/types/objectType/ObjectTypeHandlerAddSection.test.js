import ObjectTypeHandlerAddSection from '@src/js/components/types/objectType/ObjectTypeHandlerAddSection.js'
import ComponentState from '@srcTest/js/common/ComponentState.js'

describe('ObjectTypeHandlerAddSectionTest', () => {
  test('add with nothing selected', () => {
    const componentState = ComponentState.fromState({
      sections: [],
      sectionsCounter: 10
    })

    execute(componentState)

    componentState.assertState({
      selection: {
        type: 'section',
        params: {
          id: 'section-10'
        }
      },
      sections: [
        {
          id: 'section-10',
          name: null,
          properties: []
        }
      ],
      sectionsCounter: 11
    })
  })

  test('add with a section selected', () => {
    const componentState = ComponentState.fromState({
      selection: {
        type: 'section',
        params: { id: 'section-0' }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-9']
        },
        {
          id: 'section-1',
          properties: []
        }
      ],
      properties: [{ id: 'property-9', section: 'section-0' }],
      sectionsCounter: 2
    })

    execute(componentState)

    componentState.assertState({
      selection: {
        type: 'section',
        params: { id: 'section-2' }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-9']
        },
        {
          id: 'section-2',
          name: null,
          properties: []
        },
        {
          id: 'section-1',
          properties: []
        }
      ],
      properties: [{ id: 'property-9', section: 'section-0' }],
      sectionsCounter: 3
    })
  })

  test('add with a property selected', () => {
    const componentState = ComponentState.fromState({
      selection: {
        type: 'property',
        params: { id: 'property-0' }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0', 'property-1']
        },
        {
          id: 'section-1',
          properties: ['property-2']
        }
      ],
      properties: [
        { id: 'property-0', section: 'section-0' },
        { id: 'property-1', section: 'section-0' },
        { id: 'property-2', section: 'section-1' }
      ],
      sectionsCounter: 2
    })

    execute(componentState)

    componentState.assertState({
      selection: {
        type: 'section',
        params: { id: 'section-2' }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0', 'property-1']
        },
        {
          id: 'section-2',
          name: null,
          properties: []
        },
        {
          id: 'section-1',
          properties: ['property-2']
        }
      ],
      properties: [
        { id: 'property-0', section: 'section-0' },
        { id: 'property-1', section: 'section-0' },
        { id: 'property-2', section: 'section-1' }
      ],
      sectionsCounter: 3
    })
  })
})

const execute = componentState => {
  new ObjectTypeHandlerAddSection(
    componentState.getGetState(),
    componentState.getSetState()
  ).execute()
}
