import { createHashHistory } from 'history'
import * as actions from './actions/actions.js'
import routes from '../common/consts/routes.js'

let history = createHashHistory({
    hashType: 'noslash'
})  

history.configure = (store) => {
    history.listen((location) => {
        let route = routes.parse(location.pathname)
        store.dispatch(actions.routeChange(route.path))
    })
}

export default history