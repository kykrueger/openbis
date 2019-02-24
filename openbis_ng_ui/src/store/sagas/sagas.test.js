import Openbis from '../services/openbis.js'
import * as actions from '../actions/login.js'
import { newOpenbis } from '../sagas/sagas.js'
import { store } from '../store.js'

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
  openbis.getObjectTypes.mockReturnValue({ getObjects: getTypes })
  openbis.getCollectionTypes.mockReturnValue({ getObjects: getTypes })
  openbis.getDataSetTypes.mockReturnValue({ getObjects: getTypes })
  openbis.getMaterialTypes.mockReturnValue({ getObjects: getTypes })
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
    expect(openbis.login).toHaveBeenCalledWith(username, password)
    expect(state.sessionActive).toEqual(true)
    expect(openbis.getObjectTypes).toHaveBeenCalled()
    expect(openbis.getCollectionTypes).toHaveBeenCalled()
    expect(openbis.getDataSetTypes).toHaveBeenCalled()
    expect(openbis.getMaterialTypes).toHaveBeenCalled()
    expect(state.exceptions).toEqual([])
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

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ mocked data ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

function getTypes() {
  return [
    { permId: { permId: '0' }, description: 'desc0' },
    { permId: { permId: '1' }, description: 'desc1' },
    { permId: { permId: '2' }, description: 'desc2' },
  ]
}
