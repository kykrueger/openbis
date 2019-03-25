import React from 'react'
import ErrorDialog from './ErrorDialog.jsx'

class Error extends React.Component {

  render() {

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
