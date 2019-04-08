export const OBJECT_OPEN = 'OBJECT_OPEN'
export const OBJECT_CLOSE = 'OBJECT_CLOSE'

export const ADD_OPEN_OBJECT = 'ADD_OPEN_OBJECT'
export const REMOVE_OPEN_OBJECT = 'REMOVE_OPEN_OBJECT'
export const SET_SELECTED_OBJECT = 'SET_SELECTED_OBJECT'

export const objectOpen = (page, type, id) => ({
  type: OBJECT_OPEN,
  payload: {
    page,
    type,
    id
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

export const setSelectedObject = (page, type, id) => ({
  type: SET_SELECTED_OBJECT,
  payload: {
    page,
    type,
    id
  }
})
