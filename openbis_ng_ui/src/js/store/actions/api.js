const API_REQUEST = 'API_REQUEST'
const API_SUCCESS = 'API_SUCCESS'
const API_ERROR = 'API_ERROR'

const apiRequest = ({ method, params, meta }) => ({
  type: API_REQUEST,
  payload: {
    method,
    params
  },
  meta
})

const apiSuccess = ({ result, meta }) => ({
  type: API_SUCCESS,
  payload: {
    result
  },
  meta
})

const apiError = ({ error, meta }) => ({
  type: API_ERROR,
  payload: {
    error
  },
  meta
})

export default {
  API_REQUEST,
  API_SUCCESS,
  API_ERROR,
  apiRequest,
  apiSuccess,
  apiError
}
