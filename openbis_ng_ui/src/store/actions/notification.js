export const ERROR = 'ERROR'
export const CLOSE_ERROR = 'CLOSE-ERROR'

export const error = (exception) => ({
  type: ERROR,
  exception: exception
})

export const closeError = () => ({
  type: CLOSE_ERROR
})
