import Openbis from '../services/openbis'
import actions from './actions'
import { newOpenbis } from './sagas.js'
import { store } from './middleware'

// These tests test sagas, reducer and the interaction with openbis.

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ setup ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// replace the exported Openbis class by a mock generator
jest.mock('../services/openbis')

// set in beforeEach
let openbis = null

// before each test, remove the openbis mock and create a new one
// to reset all captured calls
beforeEach(() => {
  Openbis.mockClear()
  openbis = newOpenbis()
  openbis.getSpaces.mockReturnValue({ getObjects: getSpaces })
})

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ tests ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

describe('sagas', () => {
  it('should login and load spaces', () => {
    // given
    const username = 'mark.watney'
    const password = 'secret'
    // when
    store.dispatch(actions.login(username, password))
    // then
    const state = store.getState()
    const spaces = getSpaces()
    expect(openbis.login).toHaveBeenCalledWith(username, password)
    expect(state.sessionActive).toEqual(true)
    expect(openbis.getSpaces).toHaveBeenCalled()
    expect(state.exceptions).toEqual([])
    expect(state.database.spaces).toEqual({
      '0': spaces[0],
      '1': spaces[1],
      '2': spaces[2],
    })
  })
})

describe('sagas', () => {
  it('should logout', () => {
    // given
    store.dispatch(actions.login('kiva.lagos', 'secret'))
    // when
    store.dispatch(actions.logout())
    // then
    const state = store.getState()
    expect(openbis.logout).toHaveBeenCalled()
    expect(state.sessionActive).toEqual(false)
  })
})

describe('sagas', () => {
  it('should save entity', () => {
    // given
    store.dispatch(actions.login('paul.atreides', 'secret'))
    // when
    store.dispatch(actions.saveEntity(getSpaces()[0]))
    // then
    const space = getSpaces()[0]
    expect(openbis.updateSpace).toHaveBeenCalledWith(space.permId, space.description)
  })
})

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ mocked data ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

function getSpaces() {
  return [
    { permId: { permId: '0' }, description: 'desc0' },
    { permId: { permId: '1' }, description: 'desc1' },
    { permId: { permId: '2' }, description: 'desc2' },
  ]
}
