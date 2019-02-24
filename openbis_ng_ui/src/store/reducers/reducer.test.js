import {selectEntity} from '../actions/page.js'
import {error} from '../actions/notification.js'
import initialState from '../initialstate.js'
import reducer from '../reducers/root.js'


describe('reducer', () => {
  it('should add exception', () => {
    const exception = {message: 'test error'}
    const action = error(exception)
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
    const action = selectEntity(entity.permId, entity.type)
    const state = reducer(initialState, action)
    expect(state.types.openEntities).toEqual({
      entities: [entity],
      selectedEntity: entity
    })
  })
})
