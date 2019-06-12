import _ from 'lodash'
import {put, takeEvery, select} from './effects.js'
import * as selectors from '../selectors/selectors.js'
import * as actions from '../actions/actions.js'

export default function* pageSaga() {
  yield takeEvery(actions.OBJECT_OPEN, objectOpen)
  yield takeEvery(actions.OBJECT_CHANGE, objectChange)
  yield takeEvery(actions.OBJECT_CLOSE, objectClose)
}

function* objectOpen(action) {
  let { page, type, id } = action.payload
  yield put(actions.addOpenObject(page, type, id))
  yield put(actions.setSelectedObject(page, type, id))
}

function* objectChange(action) {
  let { page, type, id, changed } = action.payload

  if(changed){
    yield put(actions.addChangedObject(page, type, id))
  }else{
    yield put(actions.removeChangedObject(page, type, id))
  }
}

function* objectClose(action) {
  let { page, type, id } = action.payload

  let selectedObject = yield select(selectors.getSelectedObject, page)
  let openObjects = yield select(selectors.getOpenObjects, page)

  if(selectedObject && selectedObject.type === type && selectedObject.id === id){
    if(_.size(openObjects) === 1){
      selectedObject = null
    }else{
      let selectedIndex = _.findIndex(openObjects, selectedObject)
      if(selectedIndex === 0){
        selectedObject = openObjects[selectedIndex + 1]
      }else{
        selectedObject = openObjects[selectedIndex - 1]
      }
    }
  }

  yield put(actions.removeOpenObject(page, type, id))
  yield put(actions.removeChangedObject(page, type, id))
  yield put(actions.setSelectedObject(page, selectedObject ? selectedObject.type : null, selectedObject ? selectedObject.id : null))
}
