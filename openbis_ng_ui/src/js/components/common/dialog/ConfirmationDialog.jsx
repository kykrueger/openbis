import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Button from '@material-ui/core/Button'
import Dialog from '../../common/dialog/Dialog.jsx'
import * as util from '../../../common/util.js'
import logger from '../../../common/logger.js'

const styles = theme => ({
  button: {
    marginLeft: theme.spacing(2)
  },
  confirm: {
    backgroundColor: theme.palette.error.main,
    color: theme.palette.error.contrastText,
    '&:hover': {
      backgroundColor: theme.palette.error.dark
    },
    '&:disabled': {
      backgroundColor: theme.palette.error.light
    }
  }
})

class ConfirmationDialog extends React.Component {
  constructor(props) {
    super(props)
    this.handleClose = this.handleClose.bind(this)
  }

  handleClose() {
    const { onCancel } = this.props
    if (onCancel) {
      onCancel()
    }
  }

  render() {
    logger.log(logger.DEBUG, 'ConfirmationDialog.render')

    const { open, title, content } = this.props

    return (
      <Dialog
        open={open}
        onClose={this.handleClose}
        title={title || 'Confirmation'}
        content={content || ''}
        actions={this.renderButtons()}
      />
    )
  }

  renderButtons() {
    const { onConfirm, onCancel, classes } = this.props
    return (
      <div>
        <Button
          variant='contained'
          color='secondary'
          classes={{
            root: util.classNames(classes.button, classes.confirm)
          }}
          onClick={onConfirm}
        >
          Confirm
        </Button>
        <Button
          variant='contained'
          color='primary'
          classes={{ root: classes.button }}
          onClick={onCancel}
        >
          Cancel
        </Button>
      </div>
    )
  }
}

export default withStyles(styles)(ConfirmationDialog)
