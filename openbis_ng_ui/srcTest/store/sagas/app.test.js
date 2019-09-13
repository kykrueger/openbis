import { facade } from '../../../src/services/openbis.js'
import * as actions from '../../../src/store/actions/actions.js'
import * as selectors from '../../../src/store/selectors/selectors.js'
import * as pages from '../../../src/common/consts/pages.js'
import { createStore } from '../../../src/store/store.js'
import * as fixture from '../../common/fixture.js'

jest.mock('../../../src/services/openbis.js')

let store = null

beforeEach(() => {
  jest.resetAllMocks()
  store = createStore()
})

describe('app', () => {
  test('login successful', () => {
    facade.login.mockReturnValue(fixture.TEST_SESSION_TOKEN)

    store.dispatch(actions.login(fixture.TEST_USER, fixture.TEST_PASSWORD))

    const state = store.getState()
    expect(selectors.getSession(state)).toEqual({
      sessionToken: fixture.TEST_SESSION_TOKEN,
      userName: fixture.TEST_USER
    })
    expect(selectors.getCurrentPage(state)).toEqual(pages.TYPES)
  })

  test('login failed', () => {
    facade.login.mockReturnValue(null)

    store.dispatch(actions.login(fixture.TEST_USER, fixture.TEST_PASSWORD))

    const state = store.getState()
    expect(selectors.getSession(state)).toBeNull()
    expect(selectors.getError(state)).toEqual({
      message: 'Incorrect used or password'
    })
  })

  test('logout', () => {
    facade.login.mockReturnValue(fixture.TEST_SESSION_TOKEN)

    store.dispatch(actions.login(fixture.TEST_USER, fixture.TEST_PASSWORD))
    store.dispatch(actions.logout())

    const state = store.getState()
    expect(selectors.getSession(state)).toBeNull()
    expect(selectors.getError(state)).toBeNull()
  })
})
