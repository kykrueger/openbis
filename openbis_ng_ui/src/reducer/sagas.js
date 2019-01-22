import {put, takeEvery, call} from 'redux-saga/effects'
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
    console.log('logout')
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
    yield put(actions.savedEntity(space))
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
    switch (action.mode) {
    case 'USERS': {
      let users = yield call(openbis.getUsers)
      let groups = yield call(openbis.getGroups)
      yield put(actions.setModeDone(action.mode, {
        users: users.getObjects(),
        groups: groups.getObjects()
      }))
      break
    }
    case 'TYPES': {
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
      break
    }
    default: {
      yield put(actions.setModeDone(action.mode, {}))
      break
    }
    }
  } catch (exception) {
    yield put(actions.error(exception))
  }
}

function* log(action) {
  console.log('action', action);
}

export function* watchActions() {
  yield takeEvery('*', log)
  yield takeEvery('INIT', init)
  yield takeEvery('LOGIN', login)
  yield takeEvery('LOGIN-DONE', loginDone)
  yield takeEvery('LOGOUT', logout)
  yield takeEvery('SAVE-ENTITY', saveEntity)
  yield takeEvery('SET-SPACES', selectSpace)
  yield takeEvery('EXPAND-NODE', expandNode)
  yield takeEvery('SET-MODE', setMode)
}
