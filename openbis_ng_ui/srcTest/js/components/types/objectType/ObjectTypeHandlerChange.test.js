import ObjectTypeHandlerChange from '../../../../../src/js/components/types/objectType/ObjectTypeHandlerChange.js'
import ComponentState from '../../../common/ComponentState.js'

describe('ObjectTypeHandlerChangeTest', () => {
  test('type', () => {
    const componentState = new ComponentState({
      type: {
        field1: 'value1',
        field2: 'value2'
      }
    })

    execute(componentState, 'type', {
      field: 'field2',
      value: 'value2 changed'
    })

    componentState.assertState({
      type: {
        field1: 'value1',
        field2: 'value2 changed'
      }
    })
  })

  test('section', () => {
    const componentState = new ComponentState({
      sections: [
        {
          id: 'section-0',
          field1: 'value1',
          field2: 'value2'
        },
        {
          id: 'section-1',
          field1: 'value1'
        }
      ]
    })

    execute(componentState, 'section', {
      id: 'section-1',
      field: 'field1',
      value: 'value1 changed'
    })

    componentState.assertState({
      sections: [
        {
          id: 'section-0',
          field1: 'value1',
          field2: 'value2'
        },
        {
          id: 'section-1',
          field1: 'value1 changed'
        }
      ]
    })
  })

  test('property', () => {
    const componentState = new ComponentState({
      properties: [
        {
          id: 'property-0',
          field1: 'value1',
          field2: 'value2'
        },
        {
          id: 'property-1',
          field1: 'value1'
        }
      ]
    })

    execute(componentState, 'property', {
      id: 'property-1',
      field: 'field1',
      value: 'value1 changed'
    })

    componentState.assertState({
      properties: [
        {
          id: 'property-0',
          field1: 'value1',
          field2: 'value2'
        },
        {
          id: 'property-1',
          field1: 'value1 changed'
        }
      ]
    })
  })
})

const execute = (componentState, type, params) => {
  new ObjectTypeHandlerChange(
    componentState.getState(),
    componentState.getSetState()
  ).execute(type, params)
}
