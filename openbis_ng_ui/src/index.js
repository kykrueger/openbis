/* eslint-disable */
import "regenerator-runtime/runtime"
import React from 'react'
import ReactDOM from 'react-dom'
import {createStore, applyMiddleware, compose} from 'redux'
import {Provider} from 'react-redux'
import createSagaMiddleware from 'redux-saga'
import reducer from './reducer/reducer.js'
import {watchActions} from './reducer/sagas'

const sagaMiddleware = createSagaMiddleware()
const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;
export const store = createStore(reducer, composeEnhancers(applyMiddleware(sagaMiddleware)))

sagaMiddleware.run(watchActions)

const render = () => {
    const App = require('./components/App.jsx').default
    const WithLogin = require('./components/WithLogin.jsx').default
    const WithLoader = require('./components/WithLoader.jsx').default
    const WithError = require('./components/WithError.jsx').default
    ReactDOM.render(
        <Provider store={store}>
            <WithLoader>
                <WithError>
                    <WithLogin>
                        <App/>
                    </WithLogin>
                </WithError>
            </WithLoader>
        </Provider>,
        document.getElementById("app")
    )
}

if (module.hot) {
    module.hot.accept('./components/App.jsx', () => setTimeout(render))
    module.hot.accept('./reducer/reducer.js', () => {
        const nextRootReducer = require('./reducer/reducer.js').default
        store.replaceReducer(nextRootReducer)
    });
}
render()