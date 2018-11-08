import { put, takeEvery, call } from 'redux-saga/effects'
import openbis from '../services/openbis'

// TODO split sagas

function* init() {
  const spaces = yield call(openbis.getSpaces)
  yield put({ type: 'SET-SPACES', spaces: spaces })
}

export function* watchInit() {
  yield takeEvery('INIT', init)
}

function* selectSpace(action) {
  yield put({ type: 'SELECT-ENTITY', entityPermId: action.spaces[0].permId.permId })
}

export function* watchSetSpaces() {
  yield takeEvery('SET-SPACES', selectSpace)
}

function* saveEntity(action) {
  yield openbis.updateSpace(action.entity.permId, action.entity.description)
  const spaces = yield call(openbis.getSpaces)
  const space = spaces.filter(space => space.permId.permId === action.entity.permId.permId)[0]
  yield put({ type: 'SAVED-ENTITY', entity: space })
}

export function* watchSaveEntity() {
  yield takeEvery('SAVE-ENTITY', saveEntity)
}

function* expandNode(action) {
  const node = action.node
  if (node.loaded === false) {
    if (node.type === 'as.dto.space.Space') {
      const projects = yield openbis.searchProjects(node.id)
      yield put({ type: 'SET-PROJECTS', projects: projects, spacePermId: node.id })
    }
  }
}

export function* watchExpandNode() {
  yield takeEvery('EXPAND-NODE', expandNode)
}
