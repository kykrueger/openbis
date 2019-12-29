import ObjectTypeHandlerAddSection from '../../../../../src/js/components/types/objectType/ObjectTypeHandlerAddSection.js'
import componentState from '../../../common/componentState.js'

describe('ObjectTypeHandlerAddSectionTest', () => {
  test('add with nothing selected', () => {
    let state = {
      sections: [],
      sectionsCounter: 10
    }

    execute(state)

    expect(state).toEqual({
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
    let state = {
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
    }

    execute(state)

    expect(state).toEqual({
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
    let state = {
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
    }

    execute(state)

    expect(state).toEqual({
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

const execute = state => {
  new ObjectTypeHandlerAddSection(
    state,
    componentState.createSetState(state)
  ).execute()
}
