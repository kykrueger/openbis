export const LOGIN = 'LOGIN'
export const LOGIN_DONE = 'LOGIN-DONE'
export const LOGOUT = 'LOGOUT'
export const LOGOUT_DONE = 'LOGOUT-DONE'

export const login = (username, password) => ({
  type: LOGIN,
  username: username,
  password: password
})

export const loginDone = () => ({
  type: LOGIN_DONE
})

export const logout = () => ({
  type: LOGOUT
})

export const logoutDone = () => ({
  type: LOGOUT_DONE
})
