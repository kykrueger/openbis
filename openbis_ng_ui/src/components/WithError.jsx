import React from 'react'
import {connect} from 'react-redux'
import flow from 'lodash/flow'

import ErrorDialog from './ErrorDialog.jsx'
import actions from '../store/actions/actions.js'


function mapStateToProps(state) {
  return {
    exception: state.exceptions.length > 0 ? state.exceptions[0] : null
  }
}

function mapDispatchToProps(dispatch) {
  return {
    closeError: () => dispatch(actions.closeError()),
  }
}


class WithError extends React.Component {

  render() {

    return (
      <div>
        {
          this.props.exception &&
          <ErrorDialog exception={this.props.exception} onClose={this.props.closeError}/>
        }
        {this.props.children}
      </div>
    )
  }
}

export default flow(
  connect(mapStateToProps, mapDispatchToProps),
)(WithError)
