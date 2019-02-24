export const INIT = 'INIT'
export const SELECT_ENTITY = 'SELECT-ENTITY'
export const SET_MODE = 'SET-MODE'
export const SET_MODE_DONE = 'SET-MODE-DONE'


export const init = () => ({
  type: INIT,
})

export const selectEntity = (entityPermId, entityType) => ({
  type: SELECT_ENTITY,
  entityPermId: entityPermId,
  entityType: entityType
})

export const setMode = mode => ({
  type: SET_MODE,
  mode
})

export const setModeDone = (mode, data) => ({
  type: SET_MODE_DONE,
  mode,
  data
})
