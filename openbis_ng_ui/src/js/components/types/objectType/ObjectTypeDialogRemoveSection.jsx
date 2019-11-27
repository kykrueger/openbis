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
        title={'Remove "' + this.getSectionName() + '" section'}
        content='The section contains properties used in entities. Are you sure you want to remove it?'
      />
    )
  }

  getSectionName() {
    const { open, selection, sections } = this.props

    if (open) {
      const section = _.find(sections, ['id', selection.params.id])
      return section.name
    } else {
      return null
    }
  }
}

export default ObjectTypeDialogRemoveSection
