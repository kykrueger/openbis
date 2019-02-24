import * as loginActions from '../actions/login.js'
import * as notificationActions from '../actions/notification.js'

export default function loading(loading = false, action) {
  switch (action.type) {
  case notificationActions.ERROR: {
    return false
  }
  case loginActions.LOGIN: {
    return true
  }
  case loginActions.LOGIN_DONE: {
    return false
  }
  case loginActions.LOGOUT: {
    return true
  }
  case loginActions.LOGOUT_DONE: {
    return false
  }
  default: {
    return loading
  }
  }
}
