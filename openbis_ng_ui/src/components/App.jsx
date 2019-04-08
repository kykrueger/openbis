import React from 'react'
import _ from 'lodash'
import {connect} from 'react-redux'
import {withStyles} from '@material-ui/core/styles'
import logger from '../common/logger.js'
import * as actions from '../store/actions/actions.js'
import * as selectors from '../store/selectors/selectors.js'

import Loading from './loading/Loading.jsx'
import Error from './error/Error.jsx'
import Login from './login/Login.jsx'
import Menu from './menu/Menu.jsx'
import Browser from './browser/Browser.jsx'
import Content from './content/Content.jsx'

const styles = () => ({
  contentContainer: {
    display: 'flex'
  }
})

function mapStateToProps(state){
  return {
    loading: selectors.getLoading(state),
    session: selectors.getSession(state),
    error: selectors.getError(state)
  }
}

function mapDispatchToProps(dispatch){
  return {
    init: () => { dispatch(actions.init()) },
    errorClosed: () => { dispatch(actions.errorChange(null)) }
  }
}

class App extends React.Component {

  componentDidMount(){
    this.props.init()
  }

  render() {
    logger.log(logger.DEBUG, 'App.render')

    return (
      <Loading loading={this.props.loading}>
        <Error error={this.props.error} errorClosed={this.props.errorClosed}>
          {this.renderPage()}
        </Error>
      </Loading>
    )
  }

  renderPage(){
    const classes = this.props.classes

    if(this.props.session){
      return (
        <div>
          <div className={classes.topContainer}>
            <Menu/>
          </div>
          <div className={classes.contentContainer}>
            <Browser/>
            <Content/>
          </div>
        </div>
      )
    }else{
      return <Login/>
    }
  }
}

export default _.flow(
  connect(mapStateToProps, mapDispatchToProps),
  withStyles(styles)
)(App)
