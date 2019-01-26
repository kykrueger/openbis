import {put, takeEvery, call, select} from 'redux-saga/effects'
import Openbis from '../services/openbis'
import actions from './actions'

// TODO split sagas when it gets too big

let openbis = new Openbis()

// used only for testing - need to have a new mock for each test
export function newOpenbis() {
  openbis = new Openbis()
  return openbis
}

export function* watchActions() {
  yield takeEvery('INIT', init)
  yield takeEvery('LOGIN', login)
  yield takeEvery('LOGIN-DONE', loginDone)
  yield takeEvery('LOGOUT', logout)
  yield takeEvery('SAVE-ENTITY', saveEntity)
  yield takeEvery('SET-SPACES', selectSpace)
  yield takeEvery('EXPAND-NODE', expandNode)
  yield takeEvery('SET-MODE', setMode)
}

function* handleException(f) {
  try {
    yield f()
  } catch (exception) {
    yield put(actions.error(exception))
  }
}

function* init() {
  // TODO Check for session token and yield loginDone if valid.
  //      This can properly be done when we have the session token in a cookie.
}

function* login(action) {
  yield handleException(function* () {
    yield openbis.login(action.username, action.password)
    yield put(actions.loginDone())
  })
}

function* loginDone() {
  yield handleException(function* () {
    const result = yield call(openbis.getSpaces)
    yield put(actions.setSpaces(result.getObjects()))
  })
}

function* logout() {
  yield handleException(function* () {
    yield call(openbis.logout)
    yield put(actions.logoutDone())
  })
}

function* selectSpace(action) {
  yield put(actions.selectEntity(action.spaces[0].permId.permId))
}

function* saveEntity(action) {
  yield handleException(function* () {
    yield openbis.updateSpace(action.entity.permId, action.entity.description)
    const result = yield call(openbis.getSpaces)
    const spaces = result.getObjects()
    const space = spaces.filter(space => space.permId.permId === action.entity.permId.permId)[0]
    yield put(actions.saveEntityDone(space))
  })
}

function* expandNode(action) {
  yield handleException(function* () {
    const node = action.node
    if (node.loaded === false) {
      if (node.type === 'as.dto.space.Space') {
        const result = yield openbis.searchProjects(node.id)
        const projects = result.getObjects()
        yield put(actions.setProjects(projects, node.id))
      }
    }
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
        yield put(actions.setModeDone(action.mode, {
          users: users.getObjects(),
          groups: groups.getObjects()
        }))
      } else {
        yield put(actions.setModeDone(action.mode))
      }
      break
    }
    case 'TYPES': {
      if (!state.types.browser.loaded) {
        let objectTypes = yield call(openbis.getObjectTypes)
        let collectionTypes = yield call(openbis.getCollectionTypes)
        let dataSetTypes = yield call(openbis.getDataSetTypes)
        let materialTypes = yield call(openbis.getMaterialTypes)
        yield put(actions.setModeDone(action.mode, {
          objectTypes: objectTypes.getObjects(),
          collectionTypes: collectionTypes.getObjects(),
          dataSetTypes: dataSetTypes.getObjects(),
          materialTypes: materialTypes.getObjects(),
        }))
      } else {
        yield put(actions.setModeDone(action.mode))
      }
      break
    }
    default: {
      yield put(actions.setModeDone(action.mode))
      break
    }
    }
  })
}
