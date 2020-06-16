import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Button from '@src/js/components/common/form/Button.jsx'
import logger from '@src/js/common/logger.js'

import Dialog from './Dialog.jsx'

const styles = theme => ({
  button: {
    marginLeft: theme.spacing(1)
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
          label='Confirm'
          type='risky'
          styles={{ root: classes.button }}
          onClick={onConfirm}
        />
        <Button
          label='Cancel'
          type='final'
          styles={{ root: classes.button }}
          onClick={onCancel}
        />
      </div>
    )
  }
}

export default withStyles(styles)(ConfirmationDialog)
