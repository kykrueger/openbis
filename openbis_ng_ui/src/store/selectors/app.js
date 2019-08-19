import routes from '../../common/consts/routes.js'

export const getLoading = (state) => {
  return state.ui.loading
}

export const getSearch = (state) => {
  return state.ui.search
}

export const getRoute = (state) => {
  return state.route
}

export const getCurrentPage = (state) => {
  let route = routes.parse(state.route)
  return route.page
}

export const getError = (state) => {
  return state.ui.error
}

export const getSession = (state) => {
  return state.session
}
