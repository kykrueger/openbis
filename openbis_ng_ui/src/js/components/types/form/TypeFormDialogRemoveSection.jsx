import _ from 'lodash'
import React from 'react'
import ConfirmationDialog from '@src/js/components/common/dialog/ConfirmationDialog.jsx'
import logger from '@src/js/common/logger.js'

class TypeFormDialogRemoveSection extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'TypeFormDialogRemoveSection.render')

    const { open, onConfirm, onCancel } = this.props

    return (
      <ConfirmationDialog
        open={open}
        onConfirm={onConfirm}
        onCancel={onCancel}
        title={this.getTitle()}
        content='This section contains properties which are already used by some entities. Removing the section and the contained property definitions is going to remove the existing property values as well - data will be lost! Are you sure you want to proceed?'
      />
    )
  }

  getTitle() {
    const { open, selection, sections } = this.props

    if (open) {
      const section = _.find(sections, ['id', selection.params.id])
      if (section.name) {
        return `Do you want to remove "${section.name}" section? Some data will be lost!`
      } else {
        return 'Do you want to remove the section? Some data will be lost!'
      }
    } else {
      return null
    }
  }
}

export default TypeFormDialogRemoveSection
