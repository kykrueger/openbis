import ObjectTypeHandlerSelectionChange from '../../../../../src/js/components/types/objectType/ObjectTypeHandlerSelectionChange.js'
import ComponentState from '../../../common/ComponentState.js'

describe('ObjectTypeHandlerSelectionChangeTest', () => {
  test('section', () => {
    const componentState = new ComponentState({
      selection: null
    })

    execute(componentState, 'section', {
      id: 'section-0'
    })

    componentState.assertState({
      selection: {
        type: 'section',
        params: {
          id: 'section-0'
        }
      }
    })
  })

  test('property', () => {
    const componentState = new ComponentState({
      selection: null
    })

    execute(componentState, 'property', {
      id: 'property-0'
    })

    componentState.assertState({
      selection: {
        type: 'property',
        params: {
          id: 'property-0'
        }
      }
    })
  })
})

const execute = (componentState, type, params) => {
  new ObjectTypeHandlerSelectionChange(
    componentState.getState(),
    componentState.getSetState()
  ).execute(type, params)
}
