export const getInitialized = (state) => {
  return state.ui.initialized
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
