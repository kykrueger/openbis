import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import PropTypes from 'prop-types'

import Button from '@material-ui/core/Button'
import Dialog from '@material-ui/core/Dialog'
import DialogActions from '@material-ui/core/DialogActions'
import DialogContent from '@material-ui/core/DialogContent'
import DialogContentText from '@material-ui/core/DialogContentText'
import DialogTitle from '@material-ui/core/DialogTitle'
import Slide from '@material-ui/core/Slide'

import logger from '../../../common/logger.js'
import profile from '../../../profile.js'


const dialogStyles = {
  paper: {
    backgroundColor: '#ffd2d2',
  },
}

const StyledDialog = withStyles(dialogStyles)(Dialog)

const ANIMATION_TIME_MS = 250

function Transition(props) {
  return <Slide
    direction="up"
    timeout={{ enter: ANIMATION_TIME_MS, exit: ANIMATION_TIME_MS }}
    {...props}
  />
}

class ErrorDialog extends React.Component {

  state = {
    open: true,
  }

  getErrorMailtoHref() {
    let report =
      'agent: ' + navigator.userAgent + '%0D%0A' +
      'domain: ' + location.hostname + '%0D%0A' +
      'timestamp: ' + new Date() + '%0D%0A' +
      'href: ' + location.href.replace(new RegExp('&', 'g'), ' - ') + '%0D%0A' +
      'error: ' + JSON.stringify(this.props.error['data'])

    let href =
      'mailto:' + profile.devEmail +
      '?subject=openBIS Error Report [' + location.hostname + ']' +
      '&body=' + report
    return href
  }

  close() {
    this.setState({ open: false })
    setTimeout(this.props.onClose, ANIMATION_TIME_MS)
  }

  render() {
    logger.log(logger.DEBUG, 'ErrorDialog.render')

    return (
      <StyledDialog
        open={ this.state.open }
        onClose={ this.props.closeError }
        scroll="paper"
        aria-labelledby="error-dialog-title"
        fullWidth={ true }
        maxWidth="md"
        TransitionComponent={Transition}
      >
        <DialogTitle id="error-dialog-title">Error</DialogTitle>
        <DialogContent>
          <DialogContentText>
            { this.props.error.message }
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={ this.close.bind(this) } color="primary">
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
  error: PropTypes.any.isRequired,
}

export default ErrorDialog
