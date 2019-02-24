import * as loginActions from '../actions/login.js'

export default function sessionActive(sessionActive = false, action) {
  switch (action.type) {
  case loginActions.LOGIN_DONE: {
    return true
  }
  case loginActions.LOGOUT_DONE: {
    return false
  }
  default: {
    return sessionActive
  }
  }
}
