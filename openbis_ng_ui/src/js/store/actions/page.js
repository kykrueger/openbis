const OBJECT_NEW = 'OBJECT_NEW'
const OBJECT_CREATE = 'OBJECT_CREATE'
const OBJECT_OPEN = 'OBJECT_OPEN'
const OBJECT_SAVE = 'OBJECT_SAVE'
const OBJECT_CHANGE = 'OBJECT_CHANGE'
const OBJECT_CLOSE = 'OBJECT_CLOSE'
const ADD_OPEN_OBJECT = 'ADD_OPEN_OBJECT'
const REMOVE_OPEN_OBJECT = 'REMOVE_OPEN_OBJECT'
const REPLACE_OPEN_OBJECT = 'REPLACE_OPEN_OBJECT'
const ADD_CHANGED_OBJECT = 'ADD_CHANGED_OBJECT'
const REMOVE_CHANGED_OBJECT = 'REMOVE_CHANGED_OBJECT'
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

const objectSave = (page, type, id) => ({
  type: OBJECT_SAVE,
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

const addOpenObject = (page, type, id) => ({
  type: ADD_OPEN_OBJECT,
  payload: {
    page,
    type,
    id
  }
})

const removeOpenObject = (page, type, id) => ({
  type: REMOVE_OPEN_OBJECT,
  payload: {
    page,
    type,
    id
  }
})

const replaceOpenObject = (page, oldType, oldId, newType, newId) => ({
  type: REPLACE_OPEN_OBJECT,
  payload: {
    page,
    oldType,
    oldId,
    newType,
    newId
  }
})

const addChangedObject = (page, type, id) => ({
  type: ADD_CHANGED_OBJECT,
  payload: {
    page,
    type,
    id
  }
})

const removeChangedObject = (page, type, id) => ({
  type: REMOVE_CHANGED_OBJECT,
  payload: {
    page,
    type,
    id
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
  OBJECT_SAVE,
  OBJECT_CHANGE,
  OBJECT_CLOSE,
  ADD_OPEN_OBJECT,
  REMOVE_OPEN_OBJECT,
  REPLACE_OPEN_OBJECT,
  ADD_CHANGED_OBJECT,
  REMOVE_CHANGED_OBJECT,
  SET_CURRENT_ROUTE,
  objectNew,
  objectCreate,
  objectOpen,
  objectSave,
  objectChange,
  objectClose,
  addOpenObject,
  removeOpenObject,
  replaceOpenObject,
  addChangedObject,
  removeChangedObject,
  setCurrentRoute
}
