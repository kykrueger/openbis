import React from 'react'
import ErrorDialog from './ErrorDialog.jsx'
import logger from '../../common/logger.js'

class Error extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'Error.render')

    return (
      <div>
        {
          this.props.error &&
          <ErrorDialog error={this.props.error} onClose={this.props.errorClosed}/>
        }
        {this.props.children}
      </div>
    )
  }
}

export default Error
