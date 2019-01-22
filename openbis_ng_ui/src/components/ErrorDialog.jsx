import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import PropTypes from 'prop-types'

import Button from '@material-ui/core/Button'
import Dialog from '@material-ui/core/Dialog'
import DialogActions from '@material-ui/core/DialogActions'
import DialogContent from '@material-ui/core/DialogContent'
import DialogContentText from '@material-ui/core/DialogContentText'
import DialogTitle from '@material-ui/core/DialogTitle'

import profile from '../profile.js'


const dialogStyles = {
  paper: {
    backgroundColor: '#ffd2d2',
  },
}

const StyledDialog = withStyles(dialogStyles)(Dialog)

class ErrorDialog extends React.Component {

  getErrorMailtoHref() {
    let report =
      'agent: ' + navigator.userAgent + '%0D%0A' +
      'domain: ' + location.hostname + '%0D%0A' +
      'timestamp: ' + new Date() + '%0D%0A' +
      'href: ' + location.href.replace(new RegExp('&', 'g'), ' - ') + '%0D%0A' +
      'error: ' + JSON.stringify(this.props.exception['data'])

    let href =
      'mailto:' + profile.devEmail +
      '?subject=openBIS Error Report [' + location.hostname + ']' +
      '&body=' + report
    return href
  }

  render() {
    return (
      <StyledDialog
        open={true}
        onClose={this.props.closeError}
        scroll="paper"
        aria-labelledby="error-dialog-title"
        fullWidth={true}
        maxWidth="md"
      >
        <DialogTitle id="error-dialog-title">Error</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {this.props.exception.message}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={this.props.onClose} color="primary">
            Dismiss
          </Button>
          <Button color="primary" href={this.getErrorMailtoHref()}>
            Send error report
          </Button>
        </DialogActions>
      </StyledDialog>
    )
  }
}

ErrorDialog.propTypes = {
  exception: PropTypes.any.isRequired,
}

export default ErrorDialog
