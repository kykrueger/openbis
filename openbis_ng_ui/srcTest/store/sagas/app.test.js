import openbis from '../../../src/services/openbis.js'
import * as actions from '../../../src/store/actions/actions.js'
import * as selectors from '../../../src/store/selectors/selectors.js'
import * as pages from '../../../src/store/consts/pages.js'
import { createStore } from '../../../src/store/store.js'
import * as fixture from './fixture.js'

jest.mock('../../../src/services/openbis.js')

let store = null

beforeEach(() => {
  jest.resetAllMocks()
  store = createStore()
})

describe('app', () => {
  test('login successful', () => {
    openbis.login.mockReturnValue(fixture.TEST_SESSION_TOKEN)

    store.dispatch(actions.login(fixture.TEST_USER, fixture.TEST_PASSWORD))

    const state = store.getState()
    expect(selectors.getSession(state)).toEqual(fixture.TEST_SESSION_TOKEN)
    expect(selectors.getCurrentPage(state)).toEqual(pages.TYPES)
  })

  test('login failed', () => {
    openbis.login.mockReturnValue(null)

    store.dispatch(actions.login(fixture.TEST_USER, fixture.TEST_PASSWORD))

    const state = store.getState()
    expect(selectors.getSession(state)).toBeNull()
    expect(selectors.getError(state)).toEqual({ message: 'Incorrect used or password' })
  })

  test('logout', () => {
    openbis.login.mockReturnValue(fixture.TEST_SESSION_TOKEN)

    store.dispatch(actions.login(fixture.TEST_USER, fixture.TEST_PASSWORD))
    store.dispatch(actions.logout())

    const state = store.getState()
    expect(selectors.getSession(state)).toBeNull()
    expect(selectors.getError(state)).toBeNull()
  })
})
