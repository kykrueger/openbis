import React from 'react'
import ConfirmationDialog from '@src/js/components/common/dialog/ConfirmationDialog.jsx'
import logger from '@src/js/common/logger.js'

export default class UnsavedChangesDialog extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'UnsavedChangesDialog.render')

    const { open, onConfirm, onCancel } = this.props

    return (
      <ConfirmationDialog
        open={open}
        onConfirm={onConfirm}
        onCancel={onCancel}
        title={this.getMessage()}
        content={this.getMessage()}
      />
    )
  }

  getMessage() {
    return 'Are you sure you want to lose the unsaved changes?'
  }
}
