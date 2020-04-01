const OBJECT_OPEN = 'OBJECT_OPEN'
const OBJECT_CHANGE = 'OBJECT_CHANGE'
const OBJECT_CLOSE = 'OBJECT_CLOSE'
const OBJECT_NEW = 'OBJECT_NEW'
const ADD_OPEN_OBJECT = 'ADD_OPEN_OBJECT'
const REMOVE_OPEN_OBJECT = 'REMOVE_OPEN_OBJECT'
const ADD_CHANGED_OBJECT = 'ADD_CHANGED_OBJECT'
const REMOVE_CHANGED_OBJECT = 'REMOVE_CHANGED_OBJECT'
const SET_CURRENT_ROUTE = 'SET_CURRENT_ROUTE'

const objectOpen = (page, type, id) => ({
  type: OBJECT_OPEN,
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

const objectNew = (page, type) => ({
  type: OBJECT_NEW,
  payload: {
    page,
    type
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
  OBJECT_OPEN,
  OBJECT_CHANGE,
  OBJECT_CLOSE,
  OBJECT_NEW,
  ADD_OPEN_OBJECT,
  REMOVE_OPEN_OBJECT,
  ADD_CHANGED_OBJECT,
  REMOVE_CHANGED_OBJECT,
  SET_CURRENT_ROUTE,
  objectOpen,
  objectChange,
  objectClose,
  objectNew,
  addOpenObject,
  removeOpenObject,
  addChangedObject,
  removeChangedObject,
  setCurrentRoute
}
