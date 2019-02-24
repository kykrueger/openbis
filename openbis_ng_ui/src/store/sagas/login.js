import {put, takeEvery, call} from 'redux-saga/effects'
import {openbis} from '../../services/openbis.js'

import * as pageActions from '../actions/page.js'
import * as loginActions from '../actions/login.js'
import * as notificationActions from '../actions/notification.js'

export function* watchActions() {
  yield takeEvery(loginActions.LOGIN, login)
  yield takeEvery(loginActions.LOGIN_DONE, loginDone)
  yield takeEvery(loginActions.LOGOUT, logout)
}

// TODO handleException is duplicated in each saga file
function* handleException(f) {
  try {
    yield f()
  } catch (exception) {
    yield put(notificationActions.error(exception))
  }
}

function* login(action) {
  yield handleException(function* () {
    yield call(openbis.login, action.username, action.password)
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
