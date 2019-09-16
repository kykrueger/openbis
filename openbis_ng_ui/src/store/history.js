import { createHashHistory } from 'history'
import * as actions from './actions/actions.js'
import routes from '../common/consts/routes.js'

let history = createHashHistory({
  hashType: 'noslash'
})

history.configure = store => {
  history.listen(location => {
    let route = routes.parse(location.pathname)
    store.dispatch(actions.routeChange(route.path))
  })

  let currentRoute = store.getState().route

  store.subscribe(() => {
    let newRoute = store.getState().route

    if (newRoute && newRoute !== currentRoute) {
      currentRoute = newRoute
      if (currentRoute && currentRoute !== history.location.pathname) {
        history.push(currentRoute)
      }
    }
  })
}

export default history
