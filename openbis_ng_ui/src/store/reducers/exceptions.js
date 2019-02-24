import * as notificationActions from '../actions/notification.js'

export default function exceptions(exceptions = [], action) {
  switch (action.type) {
  case notificationActions.ERROR: {
    return [].concat(exceptions, [action.exception])
  }
  case notificationActions.CLOSE_ERROR: {
    return exceptions.slice(1)
  }
  default: {
    return exceptions
  }
  }
}
