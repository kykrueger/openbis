export const getLoading = (state) => {
  return state.ui.loading
}

export const getSearch = (state) => {
  return state.ui.search
}

export const getCurrentPage = (state) => {
  return state.ui.currentPage
}

export const getPage = (state) => {
  return state.ui.pages[getCurrentPage(state)]
}

export const getError = (state) => {
  return state.ui.error
}

export const getSession = (state) => {
  return state.session
}
