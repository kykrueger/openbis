import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Button from '@material-ui/core/Button'
import profile from '@src/js/profile.js'
import logger from '@src/js/common/logger.js'

import Dialog from './Dialog.jsx'

const styles = theme => ({
  button: {
    marginLeft: theme.spacing(2)
  }
})

class ErrorDialog extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'ErrorDialog.render')

    const { error, onClose } = this.props

    return (
      <Dialog
        open={!!error}
        onClose={onClose}
        title={'Error'}
        content={this.renderContent()}
        actions={this.renderButtons()}
      />
    )
  }

  renderContent() {
    const message = this.getErrorMessage()
    const stack = this.getErrorStack()
    return (
      <div>
        <div>{message}</div>
        <pre>{stack}</pre>
      </div>
    )
  }

  renderButtons() {
    const { onClose, classes } = this.props
    return (
      <div>
        <Button
          variant='contained'
          color='secondary'
          classes={{ root: classes.button }}
          href={this.getErrorMailtoHref()}
        >
          Send error report
        </Button>
        <Button
          variant='contained'
          color='primary'
          classes={{ root: classes.button }}
          onClick={onClose}
        >
          Close
        </Button>
      </div>
    )
  }

  getErrorMailtoHref() {
    const message = this.getErrorMessage()
    const stack = this.getErrorStack()

    let report =
      'agent: ' +
      navigator.userAgent +
      '\n' +
      'domain: ' +
      location.hostname +
      '\n' +
      'timestamp: ' +
      new Date() +
      '\n' +
      'href: ' +
      location.href +
      '\n' +
      'error: ' +
      (message ? message : '') +
      '\n' +
      'stack: ' +
      (stack ? stack : '')

    let href =
      'mailto:' +
      profile.devEmail +
      '?subject=' +
      encodeURIComponent('openBIS Error Report [' + location.hostname + ']') +
      '&body=' +
      encodeURIComponent(report)

    return href
  }

  getErrorMessage() {
    const { error } = this.props

    if (error) {
      if (error.message) {
        return error.message
      } else {
        return error
      }
    } else {
      return null
    }
  }

  getErrorStack() {
    const { error } = this.props

    if (error && error.stack) {
      return error.stack
    } else {
      return null
    }
  }
}

export default withStyles(styles)(ErrorDialog)
