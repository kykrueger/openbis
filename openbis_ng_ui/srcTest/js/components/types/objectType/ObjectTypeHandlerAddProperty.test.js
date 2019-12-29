import ObjectTypeHandlerAddProperty from '../../../../../src/js/components/types/objectType/ObjectTypeHandlerAddProperty.js'
import componentState from '../../../common/componentState.js'

describe('ObjectTypeHandlerAddPropertyTest', () => {
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
      propertiesCounter: 10
    }

    execute(state)

    expect(state).toEqual({
      selection: {
        type: 'property',
        params: { id: 'property-10' }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-9', 'property-10']
        },
        {
          id: 'section-1',
          properties: []
        }
      ],
      properties: [
        { id: 'property-9', section: 'section-0' },
        {
          code: null,
          dataType: 'VARCHAR',
          description: null,
          errors: {},
          id: 'property-10',
          label: null,
          mandatory: false,
          materialType: null,
          section: 'section-0',
          showInEditView: true,
          usages: 0,
          vocabulary: null
        }
      ],
      propertiesCounter: 11
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
      propertiesCounter: 3
    }

    execute(state)

    expect(state).toEqual({
      selection: {
        type: 'property',
        params: { id: 'property-3' }
      },
      sections: [
        {
          id: 'section-0',
          properties: ['property-0', 'property-3', 'property-1']
        },
        {
          id: 'section-1',
          properties: ['property-2']
        }
      ],
      properties: [
        { id: 'property-0', section: 'section-0' },
        { id: 'property-1', section: 'section-0' },
        { id: 'property-2', section: 'section-1' },
        {
          code: null,
          dataType: 'VARCHAR',
          description: null,
          errors: {},
          id: 'property-3',
          label: null,
          mandatory: false,
          materialType: null,
          section: 'section-0',
          showInEditView: true,
          usages: 0,
          vocabulary: null
        }
      ],
      propertiesCounter: 4
    })
  })
})

const execute = state => {
  new ObjectTypeHandlerAddProperty(
    state,
    componentState.createSetState(state)
  ).execute()
}
