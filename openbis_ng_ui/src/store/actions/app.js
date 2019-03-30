export const INIT = 'INIT'
export const LOGIN = 'LOGIN'
export const LOGOUT = 'LOGOUT'
export const CURRENT_PAGE_CHANGED = 'CURRENT_PAGE_CHANGED'
export const ERROR_CHANGED = 'ERROR_CHANGED'

export const SET_LOADING = 'SET_LOADING'
export const SET_SESSION = 'SET_SESSION'
export const SET_CURRENT_PAGE = 'SET_PAGE'
export const SET_ERROR = 'SET_ERROR'

export const init = () => ({
  type: INIT
})

export const login = (username, password) => ({
  type: LOGIN,
  payload: {
    username,
    password
  }
})

export const logout = () => ({
  type: LOGOUT
})

export const currentPageChanged = (currentPage) => ({
  type: CURRENT_PAGE_CHANGED,
  payload: {
    currentPage
  }
})

export const errorChanged = (error) => ({
  type: ERROR_CHANGED,
  payload: {
    error
  }
})

export const setLoading = (loading) => ({
  type: SET_LOADING,
  payload: {
    loading
  }
})

export const setSession = (session) => ({
  type: SET_SESSION,
  payload: {
    session
  }
})

export const setCurrentPage = (currentPage) => ({
  type: SET_CURRENT_PAGE,
  payload: {
    currentPage
  }
})

export const setError = (error) => ({
  type: SET_ERROR,
  payload: {
    error
  }
})
