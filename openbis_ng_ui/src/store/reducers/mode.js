import * as pageActions from '../actions/page.js'

export default function mode(mode = 'TYPES', action) {
  if (!action.mode) {
    return mode
  }

  switch (action.type) {
  case pageActions.SET_MODE_DONE : {
    return action.mode
  }
  default: {
    return mode
  }
  }
}
