import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Button from '@src/js/components/common/form/Button.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import Dialog from '@src/js/components/common/dialog/Dialog.jsx'
import logger from '@src/js/common/logger.js'

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
        content={<Message type={this.getMessageType()}>{content}</Message>}
        actions={this.renderButtons()}
      />
    )
  }

  renderButtons() {
    const { onConfirm, onCancel, classes } = this.props
    return (
      <div>
        <Button
          name='confirm'
          label='Confirm'
          type={this.getButtonType()}
          styles={{ root: classes.button }}
          onClick={onConfirm}
        />
        <Button
          name='cancel'
          label='Cancel'
          styles={{ root: classes.button }}
          onClick={onCancel}
        />
      </div>
    )
  }

  getMessageType() {
    const type = this.getType()

    if (type === 'warning') {
      return 'warning'
    } else if (type === 'info') {
      return 'info'
    } else {
      throw new Error('Unsupported type: ' + type)
    }
  }

  getButtonType() {
    const type = this.getType()

    if (type === 'warning') {
      return 'risky'
    } else if (type === 'info') {
      return null
    } else {
      throw new Error('Unsupported type: ' + type)
    }
  }

  getType() {
    return this.props.type || 'warning'
  }
}

export default withStyles(styles)(ConfirmationDialog)
