import React from 'react'
import {connect} from 'react-redux'
import * as actions from '../store/actions/actions.js'
import * as selectors from '../store/selectors/selectors.js'

import Loading from './loading/Loading.jsx'
import Error from './error/Error.jsx'
import Login from './login/Login.jsx'
import Menu from './menu/Menu.jsx'
import Browser from './browser/Browser.jsx'

function mapStateToProps(state){
  return {
    initialized: selectors.getInitialized(state),
    session: selectors.getSession(state),
    error: selectors.getError(state)
  }
}

function mapDispatchToProps(dispatch){
  return {
    init: () => { dispatch(actions.init()) },
    errorClosed: () => { dispatch(actions.errorChanged(null)) }
  }
}

class App extends React.Component {

  componentDidMount(){
    this.props.init()
  }

  render() {
    return (
      <Loading loading={!this.props.initialized}>
        <Error error={this.props.error} errorClosed={this.props.errorClosed}>
          {this.renderPage()}
        </Error>
      </Loading>
    )
  }

  renderPage(){
    if(this.props.session){
      return (
        <div>
          <Menu/>
          <Browser/>
        </div>
      )
    }else{
      return <Login/>
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(App)
