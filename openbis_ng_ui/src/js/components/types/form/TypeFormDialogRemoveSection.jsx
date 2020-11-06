import _ from 'lodash'
import React from 'react'
import ConfirmationDialog from '@src/js/components/common/dialog/ConfirmationDialog.jsx'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import logger from '@src/js/common/logger.js'

class TypeFormDialogRemoveSection extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'TypeFormDialogRemoveSection.render')

    const { open, object, onConfirm, onCancel } = this.props

    const section = this.getSection()

    if (section) {
      return (
        <ConfirmationDialog
          open={open}
          onConfirm={onConfirm}
          onCancel={onCancel}
          title={this.getTitle(section)}
          content={this.getContent(object, section)}
          type={section.usages > 0 ? 'warning' : 'info'}
        />
      )
    } else {
      return null
    }
  }

  getTitle(section) {
    if (section.name.value) {
      return `Do you want to remove "${section.name.value}" section?`
    } else {
      return `Do you want to remove the section?`
    }
  }

  getContent(object, section) {
    if (section.usages > 0) {
      return `This section contains property assignments which are already used by existing entities of "${object.id}" type. Removing it is also going to remove ${section.usages} existing property value(s) - data will be lost! Are you sure you want to proceed?`
    } else {
      return `This section contains only property assignments which are not yet used by any entities of "${object.id}" type.`
    }
  }

  getSection() {
    const { selection, sections } = this.props

    if (selection && selection.type === TypeFormSelectionType.SECTION) {
      return _.find(sections, ['id', selection.params.id])
    } else {
      return null
    }
  }
}

export default TypeFormDialogRemoveSection
