const OBJECT_NEW = 'OBJECT_NEW'
const OBJECT_CREATE = 'OBJECT_CREATE'
const OBJECT_OPEN = 'OBJECT_OPEN'
const OBJECT_UPDATE = 'OBJECT_UPDATE'
const OBJECT_DELETE = 'OBJECT_DELETE'
const OBJECT_CHANGE = 'OBJECT_CHANGE'
const OBJECT_CLOSE = 'OBJECT_CLOSE'
const ADD_OPEN_TAB = 'ADD_OPEN_TAB'
const REMOVE_OPEN_TAB = 'REMOVE_OPEN_TAB'
const REPLACE_OPEN_TAB = 'REPLACE_OPEN_TAB'
const SET_CURRENT_ROUTE = 'SET_CURRENT_ROUTE'

const objectNew = (page, type) => ({
  type: OBJECT_NEW,
  payload: {
    page,
    type
  }
})

const objectCreate = (page, oldType, oldId, newType, newId) => ({
  type: OBJECT_CREATE,
  payload: {
    page,
    oldType,
    oldId,
    newType,
    newId
  }
})

const objectOpen = (page, type, id) => ({
  type: OBJECT_OPEN,
  payload: {
    page,
    type,
    id
  }
})

const objectUpdate = (page, type, id) => ({
  type: OBJECT_UPDATE,
  payload: {
    page,
    type,
    id
  }
})

const objectDelete = (page, type, id) => ({
  type: OBJECT_DELETE,
  payload: {
    page,
    type,
    id
  }
})

const objectChange = (page, type, id, changed) => ({
  type: OBJECT_CHANGE,
  payload: {
    page,
    type,
    id,
    changed
  }
})

const objectClose = (page, type, id) => ({
  type: OBJECT_CLOSE,
  payload: {
    page,
    type,
    id
  }
})

const addOpenTab = (page, tab) => ({
  type: ADD_OPEN_TAB,
  payload: {
    page,
    tab
  }
})

const removeOpenTab = (page, id) => ({
  type: REMOVE_OPEN_TAB,
  payload: {
    page,
    id
  }
})

const replaceOpenTab = (page, id, tab) => ({
  type: REPLACE_OPEN_TAB,
  payload: {
    page,
    id,
    tab
  }
})

const setCurrentRoute = (page, currentRoute) => ({
  type: SET_CURRENT_ROUTE,
  payload: {
    page,
    currentRoute
  }
})

export default {
  OBJECT_NEW,
  OBJECT_CREATE,
  OBJECT_OPEN,
  OBJECT_UPDATE,
  OBJECT_DELETE,
  OBJECT_CHANGE,
  OBJECT_CLOSE,
  ADD_OPEN_TAB,
  REMOVE_OPEN_TAB,
  REPLACE_OPEN_TAB,
  SET_CURRENT_ROUTE,
  objectNew,
  objectCreate,
  objectOpen,
  objectUpdate,
  objectDelete,
  objectChange,
  objectClose,
  addOpenTab,
  removeOpenTab,
  replaceOpenTab,
  setCurrentRoute
}
