import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Button from '@material-ui/core/Button'
import Dialog from '../../common/dialog/Dialog.jsx'
import logger from '../../../common/logger.js'
import profile from '../../../profile.js'

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

    const content = error && error.message ? error.message : error

    return (
      <Dialog
        open={!!error}
        onClose={onClose}
        title={'Error'}
        content={content || ''}
        actions={this.renderButtons()}
      />
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
    let report =
      'agent: ' +
      navigator.userAgent +
      '%0D%0A' +
      'domain: ' +
      location.hostname +
      '%0D%0A' +
      'timestamp: ' +
      new Date() +
      '%0D%0A' +
      'href: ' +
      location.href.replace(new RegExp('&', 'g'), ' - ') +
      '%0D%0A' +
      'error: ' +
      JSON.stringify(this.props.error)

    let href =
      'mailto:' +
      profile.devEmail +
      '?subject=openBIS Error Report [' +
      location.hostname +
      ']' +
      '&body=' +
      report
    return href
  }
}

export default withStyles(styles)(ErrorDialog)
