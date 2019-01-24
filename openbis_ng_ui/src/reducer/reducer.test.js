import actions from './actions'
import initialState from './initialstate.js'
import reducer from './reducer.js'


describe('reducer', () => {
  it('should add exception', () => {
    const exception = { message: 'test error' }
    const action = actions.error(exception)
    const expectedExceptions = [exception]
    const state = reducer(initialState, action)
    expect(state.exceptions).toEqual(expectedExceptions)
  })
})

describe('reducer', () => {
  it('should add and select entity', () => {
    const entityPermId = 0
    const action = actions.selectEntity(entityPermId)
    const state = reducer(initialState, action)
    expect(state.openEntities).toEqual({
      entities: [entityPermId],
      selectedEntity: entityPermId,
    })
  })
})

describe('reducer', () => {
  it('should close an entity and select the next open one', () => {
    const beforeState = Object.assign({}, initialState, {
      openEntities: {
        entities: [0, 1, 2],
        selectedEntity: 1
      }
    })
    const action = actions.closeEntity(1)
    const state = reducer(beforeState, action)
    expect(state.openEntities).toEqual({
      entities: [0, 2],
      selectedEntity: 2,
    })
  })
})
