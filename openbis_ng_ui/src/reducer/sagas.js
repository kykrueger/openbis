import { put, takeEvery, call } from 'redux-saga/effects'
import openbis from '../services/openbis'
import actions from './actions'

// TODO split sagas

function* init() {
  try {
    const result = yield call(openbis.getSpaces)
    yield put(actions.setSpaces(result.getObjects()))  
  } catch(exception) {
    yield put(actions.error(exception))  
  }
}

export function* watchInit() {
  yield takeEvery('INIT', init)
}

function* selectSpace(action) {
  yield put(actions.selectEntity(action.spaces[0].permId.permId))
}

export function* watchSetSpaces() {
  yield takeEvery('SET-SPACES', selectSpace)
}

function* saveEntity(action) {
  try {
    yield openbis.updateSpace(action.entity.permId, action.entity.description)
    const result = yield call(openbis.getSpaces)
    const spaces = result.getObjects()
    const space = spaces.filter(space => space.permId.permId === action.entity.permId.permId)[0]
    yield put(actions.savedEntity(space))
  } catch(exception) {
    yield put(actions.error(exception))  
  }
}

export function* watchSaveEntity() {
  yield takeEvery('SAVE-ENTITY', saveEntity)
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
  } catch(exception) {
    yield put(actions.error(exception))  
  }
}

export function* watchExpandNode() {
  yield takeEvery('EXPAND-NODE', expandNode)
}
