import * as pages from '../../common/consts/pages.js'
import routes from '../../common/consts/routes.js'

export const getLoading = (state) => {
  return state.ui.loading
}

export const getSearch = (state) => {
  return state.ui.search
}

export const getCurrentPage = (state) => {
  let pathname = state.router.location.pathname
  let route = routes.parse(pathname)

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
