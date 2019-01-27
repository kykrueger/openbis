import actions from './actions'
import initialState from './initialstate.js'
import reducer from './reducer.js'


describe('reducer', () => {
  it('should add exception', () => {
    const exception = {message: 'test error'}
    const action = actions.error(exception)
    const expectedExceptions = [exception]
    const state = reducer(initialState, action)
    expect(state.exceptions).toEqual(expectedExceptions)
  })
})

describe('reducer', () => {
  it('should add and select entity', () => {
    const entity = {
      permId: 0,
      type: 'A'
    }
    const action = actions.selectEntity(entity.permId, entity.type)
    const state = reducer(initialState, action)
    expect(state.database.openEntities).toEqual({
      entities: [entity],
      selectedEntity: entity
    })
  })
})

describe('reducer', () => {
  it('should close an entity and select the next open one', () => {
    const beforeState = Object.assign({}, initialState, {
      database: {
        openEntities: {
          entities: [{permId: 0, type: 'A'}, {permId: 1, type: 'A'}, {permId: 1, type: 'B'}, {permId: 2, type: 'B'}],
          selectedEntity: {permId: 1, type: 'B'}
        }
      }
    })
    const action = actions.closeEntity(1, 'B')
    const state = reducer(beforeState, action)
    expect(state.database.openEntities).toEqual({
      entities: [{permId: 0, type: 'A'}, {permId: 1, type: 'A'}, {permId: 2, type: 'B'}],
      selectedEntity: {permId: 2, type: 'B'},
    })
  })
})
