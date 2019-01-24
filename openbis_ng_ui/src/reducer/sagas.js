import { put, takeEvery, call } from 'redux-saga/effects'
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
}

function* handleException(f) {
  try {
    yield f()
  } catch(exception) {
    yield put(actions.error(exception))
  }
}

function* init() {
  // TODO Check for session token and yield loginDone if valid.
  //      This can properly be done when we have the session token in a cookie.
}

function* login(action) {
  yield handleException(function*() {
    yield openbis.login(action.username, action.password)
    yield put(actions.loginDone())
  })
}

function* loginDone() {
  yield handleException(function*() {
    const result = yield call(openbis.getSpaces)
    yield put(actions.setSpaces(result.getObjects()))
  })
}

function* logout() {
  yield handleException(function*() {
    yield call(openbis.logout)
    yield put(actions.logoutDone())
  })
}

function* selectSpace(action) {
  yield put(actions.selectEntity(action.spaces[0].permId.permId))
}

function* saveEntity(action) {
  yield handleException(function*() {
    yield openbis.updateSpace(action.entity.permId, action.entity.description)
    const result = yield call(openbis.getSpaces)
    const spaces = result.getObjects()
    const space = spaces.filter(space => space.permId.permId === action.entity.permId.permId)[0]
    yield put(actions.saveEntityDone(space))    
  })
}

function* expandNode(action) {
  yield handleException(function*() {
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
