import {put, takeEvery, call, select} from 'redux-saga/effects'
import {openbis} from '../../services/openbis.js'

import * as pageActions from '../actions/page.js'
import * as notificationActions from '../actions/notification.js'

export function* watchActions() {
  yield takeEvery(pageActions.INIT, init)
  yield takeEvery(pageActions.SET_MODE, setMode)
}

// TODO handleException is duplicated in each saga file
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
