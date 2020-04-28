import actions from '@src/js/store/actions/actions.js'

export default function route(state = null, action) {
  switch (action.type) {
    case actions.SET_ROUTE: {
      return action.payload.route
    }
    default: {
      return state
    }
  }
}
