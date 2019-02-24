import {put, takeEvery, call, select} from 'redux-saga/effects'
import Openbis from '../../services/openbis.js'

import * as pageActions from '../actions/page.js'
import * as loginActions from '../actions/login.js'
import * as browserActions from '../actions/browser.js'
import * as notificationActions from '../actions/notification.js'

// TODO split sagas when it gets too big

let openbis = new Openbis()

// used only for testing - need to have a new mock for each test
export function newOpenbis() {
  openbis = new Openbis()
  return openbis
}

export function* watchActions() {
  yield takeEvery(pageActions.INIT, init)
  yield takeEvery(loginActions.LOGIN, login)
  yield takeEvery(loginActions.LOGIN_DONE, loginDone)
  yield takeEvery(loginActions.LOGOUT, logout)
  yield takeEvery(pageActions.SET_MODE, setMode)
}

function* handleException(f) {
  try {
    yield f()
  } catch (exception) {
    yield put(notificationActions.error(exception))
  }
}

function* init() {
  // TODO Check for session token and yield loginDone if valid.
  //      This can properly be done when we have the session token in a cookie.
}

function* login(action) {
  yield handleException(function* () {
    yield openbis.login(action.username, action.password)
    yield put(loginActions.loginDone())
  })
}

function* loginDone() {
  yield handleException(function* () {
    yield put(pageActions.setMode('TYPES'))
  })
}

function* logout() {
  yield handleException(function* () {
    yield call(openbis.logout)
    yield put(loginActions.logoutDone())
  })
}

function* setMode(action) {
  yield handleException(function* () {
    let state = yield select()

    switch (action.mode) {
    case 'USERS': {
      if (!state.users.browser.loaded) {
        let users = yield call(openbis.getUsers)
        let groups = yield call(openbis.getGroups)
        yield put(pageActions.setModeDone(action.mode, {
          users: users.getObjects(),
          groups: groups.getObjects()
        }))
      } else {
        yield put(pageActions.setModeDone(action.mode))
      }
      break
    }
    case 'TYPES': {
      if (!state.types.browser.loaded) {
        let objectTypes = yield call(openbis.getObjectTypes)
        let collectionTypes = yield call(openbis.getCollectionTypes)
        let dataSetTypes = yield call(openbis.getDataSetTypes)
        let materialTypes = yield call(openbis.getMaterialTypes)
        yield put(pageActions.setModeDone(action.mode, {
          objectTypes: objectTypes.getObjects(),
          collectionTypes: collectionTypes.getObjects(),
          dataSetTypes: dataSetTypes.getObjects(),
          materialTypes: materialTypes.getObjects(),
        }))
      } else {
        yield put(pageActions.setModeDone(action.mode))
      }
      break
    }
    default: {
      yield put(pageActions.setModeDone(action.mode))
      break
    }
    }
  })
}
