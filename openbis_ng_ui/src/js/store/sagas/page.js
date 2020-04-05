import _ from 'lodash'
import { put, takeEvery, select } from '@src/js/store/sagas/effects.js'
import selectors from '@src/js/store/selectors/selectors.js'
import actions from '@src/js/store/actions/actions.js'
import routes from '@src/js/common/consts/routes.js'

const getSelectedObject = selectors.createGetSelectedObject()

export default function* pageSaga() {
  yield takeEvery(actions.OBJECT_NEW, objectNew)
  yield takeEvery(actions.OBJECT_CREATE, objectCreate)
  yield takeEvery(actions.OBJECT_OPEN, objectOpen)
  yield takeEvery(actions.OBJECT_SAVE, objectSave)
  yield takeEvery(actions.OBJECT_CHANGE, objectChange)
  yield takeEvery(actions.OBJECT_CLOSE, objectClose)
  yield takeEvery(actions.ROUTE_CHANGE, routeChange)
}

function* objectNew(action) {
  const { page, type } = action.payload

  let id = 1

  const openObjects = yield select(selectors.getOpenObjects, page)
  openObjects.forEach(openObject => {
    if (openObject.type === type) {
      id++
    }
  })

  const route = routes.format({ page, type, id })
  yield put(actions.routeChange(route))
}

function* objectCreate(action) {
  const { page, oldType, oldId, newType, newId } = action.payload

  yield put(actions.replaceOpenObject(page, oldType, oldId, newType, newId))

  let route = routes.format({ page, type: newType, id: newId })
  yield put(actions.routeReplace(route))
}

function* objectOpen(action) {
  let { page, type, id } = action.payload
  let route = routes.format({ page, type, id })
  yield put(actions.routeChange(route))
}

function objectSave(action) {}

function* objectChange(action) {
  let { page, type, id, changed } = action.payload

  if (changed) {
    yield put(actions.addChangedObject(page, type, id))
  } else {
    yield put(actions.removeChangedObject(page, type, id))
  }
}

function* objectClose(action) {
  let { page, type, id } = action.payload

  let selectedObject = yield select(getSelectedObject, page)
  let openObjects = yield select(selectors.getOpenObjects, page)

  if (
    selectedObject &&
    selectedObject.type === type &&
    selectedObject.id === id
  ) {
    if (_.size(openObjects) === 1) {
      selectedObject = null
    } else {
      let selectedIndex = _.findIndex(openObjects, selectedObject)
      if (selectedIndex === 0) {
        selectedObject = openObjects[selectedIndex + 1]
      } else {
        selectedObject = openObjects[selectedIndex - 1]
      }
    }
  }

  yield put(actions.removeOpenObject(page, type, id))
  yield put(actions.removeChangedObject(page, type, id))

  if (selectedObject) {
    let route = routes.format({
      page,
      type: selectedObject.type,
      id: selectedObject.id
    })
    yield put(actions.routeChange(route))
  } else {
    let route = routes.format({ page })
    yield put(actions.routeChange(route))
  }
}

function* routeChange(action) {
  let route = routes.parse(action.payload.route)

  if (route.type && route.id) {
    let openObjects = yield select(selectors.getOpenObjects, route.page)
    let selectedObject = { type: route.type, id: route.id }

    if (_.findIndex(openObjects, selectedObject) === -1) {
      yield put(actions.addOpenObject(route.page, route.type, route.id))
    }
  }

  yield put(actions.setCurrentRoute(route.page, route.path))
}
