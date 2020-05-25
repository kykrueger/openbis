import actions from '@src/js/store/actions/actions.js'

export default function session(state = null, action) {
  switch (action.type) {
    case actions.SET_SESSION: {
      return action.payload.session
    }
    default: {
      return state
    }
  }
}
