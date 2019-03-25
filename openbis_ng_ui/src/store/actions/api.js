export const API_REQUEST = 'API_REQUEST'
export const API_SUCCESS = 'API_SUCCESS'
export const API_ERROR = 'API_ERROR'

export const apiRequest = ({method, params, meta}) => ({
  type: API_REQUEST,
  payload: {
    method,
    params
  },
  meta
})

export const apiSuccess = ({result, meta}) => ({
  type: API_SUCCESS,
  payload: {
    result
  },
  meta
})

export const apiError = ({error, meta}) => ({
  type: API_ERROR,
  payload: {
    error
  },
  meta
})
