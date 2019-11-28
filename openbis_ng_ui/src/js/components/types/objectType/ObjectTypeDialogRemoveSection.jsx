import _ from 'lodash'
import React from 'react'
import ConfirmationDialog from '../../common/dialog/ConfirmationDialog.jsx'
import logger from '../../../common/logger.js'

class ObjectTypeDialogRemoveSection extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'ObjectTypeDialogRemoveSection.render')

    const { open, onConfirm, onCancel } = this.props

    return (
      <ConfirmationDialog
        open={open}
        onConfirm={onConfirm}
        onCancel={onCancel}
        title={this.getTitle()}
        content='This section contains properties which are already used by some entities. Are you sure you want to remove it?'
      />
    )
  }

  getTitle() {
    const { open, selection, sections } = this.props

    if (open) {
      const section = _.find(sections, ['id', selection.params.id])
      if (section.name) {
        return `Remove "${section.name}" section`
      } else {
        return 'Remove section'
      }
    } else {
      return null
    }
  }
}

export default ObjectTypeDialogRemoveSection
