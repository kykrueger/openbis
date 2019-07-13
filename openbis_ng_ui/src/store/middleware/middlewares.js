import stateChangeCheck from './stateChangeCheck.js'
import { routerMiddleware } from 'connected-react-router'
import history from '../history.js'

export default [stateChangeCheck, routerMiddleware(history)]
