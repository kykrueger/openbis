import {put, takeEvery, call, select} from 'redux-saga/effects'
import openbis from '../services/openbis'
import actions from './actions'

// TODO split sagas

function* init() {
  // TODO we want to check if there is an active session here to avoid the login
}

function* loginDone() {
  try {
    const result = yield call(openbis.getSpaces)
    yield put(actions.setSpaces(result.getObjects()))
  } catch (exception) {
    yield put(actions.error(exception))
  }
}

function* logout() {
  try {
    const result = yield call(openbis.logout)
    yield put(actions.logoutDone())
  } catch (exception) {
    yield put(actions.error(exception))
  }
}

function* selectSpace(action) {
  yield put(actions.selectEntity(action.spaces[0].permId.permId))
}

function* saveEntity(action) {
  try {
    yield openbis.updateSpace(action.entity.permId, action.entity.description)
    const result = yield call(openbis.getSpaces)
    const spaces = result.getObjects()
    const space = spaces.filter(space => space.permId.permId === action.entity.permId.permId)[0]
    yield put(actions.saveEntityDone(space))
  } catch (exception) {
    yield put(actions.error(exception))
  }
}

function* expandNode(action) {
  try {
    const node = action.node
    if (node.loaded === false) {
      if (node.type === 'as.dto.space.Space') {
        const result = yield openbis.searchProjects(node.id)
        const projects = result.getObjects()
        yield put(actions.setProjects(projects, node.id))
      }
    }
  } catch (exception) {
    yield put(actions.error(exception))
  }
}

export function* login(action) {
  try {
    const result = yield openbis.login(action.username, action.password)
    yield put(actions.loginDone())
  } catch (exception) {
    yield put(actions.error(exception))
  }
}

function* setMode(action) {
  try {
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
  } catch (exception) {
    yield put(actions.error(exception))
  }
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
