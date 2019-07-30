import history from '../history.js'
import * as pages from '../../common/consts/pages.js'
import routes from '../../common/consts/routes.js'

export const getLoading = (state) => {
  return state.ui.loading
}

export const getSearch = (state) => {
  return state.ui.search
}

export const getCurrentPage = (state) => {
  let route = routes.parse(history.location.pathname)

  if(route){
    return route.page
  }else{
    return pages.TYPES
  }
}

export const getError = (state) => {
  return state.ui.error
}

export const getSession = (state) => {
  return state.session
}
