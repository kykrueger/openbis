export const OBJECT_OPEN = 'OBJECT_OPEN'
export const OBJECT_CHANGE = 'OBJECT_CHANGE'
export const OBJECT_CLOSE = 'OBJECT_CLOSE'

export const ADD_OPEN_OBJECT = 'ADD_OPEN_OBJECT'
export const REMOVE_OPEN_OBJECT = 'REMOVE_OPEN_OBJECT'
export const ADD_CHANGED_OBJECT = 'ADD_CHANGED_OBJECT'
export const REMOVE_CHANGED_OBJECT = 'REMOVE_CHANGED_OBJECT'
export const SET_CURRENT_ROUTE = 'SET_CURRENT_ROUTE'

export const objectOpen = (page, type, id) => ({
  type: OBJECT_OPEN,
  payload: {
    page,
    type,
    id
  }
})

export const objectChange = (page, type, id, changed) => ({
  type: OBJECT_CHANGE,
  payload: {
    page,
    type,
    id,
    changed
  }
})

export const objectClose = (page, type, id) => ({
  type: OBJECT_CLOSE,
  payload: {
    page,
    type,
    id
  }
})

export const addOpenObject = (page, type, id) => ({
  type: ADD_OPEN_OBJECT,
  payload: {
    page,
    type,
    id
  }
})

export const removeOpenObject = (page, type, id) => ({
  type: REMOVE_OPEN_OBJECT,
  payload: {
    page,
    type,
    id
  }
})

export const addChangedObject = (page, type, id) => ({
  type: ADD_CHANGED_OBJECT,
  payload: {
    page,
    type,
    id
  }
})

export const removeChangedObject = (page, type, id) => ({
  type: REMOVE_CHANGED_OBJECT,
  payload: {
    page,
    type,
    id
  }
})

export const setCurrentRoute = (page, currentRoute) => ({
  type: SET_CURRENT_ROUTE,
  payload: {
    page,
    currentRoute
  }
})
