import _ from 'lodash'
import React from 'react'
import ConfirmationDialog from '@src/js/components/common/dialog/ConfirmationDialog.jsx'
import logger from '@src/js/common/logger.js'

class TypeFormDialogRemoveProperty extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'TypeFormDialogRemoveProperty.render')

    const { open, onConfirm, onCancel } = this.props

    return (
      <ConfirmationDialog
        open={open}
        onConfirm={onConfirm}
        onCancel={onCancel}
        title={this.getTitle()}
        content={this.getContent()}
      />
    )
  }

  getTitle() {
    const property = this.getProperty()

    if (property) {
      if (property.code.value) {
        return `Do you want to remove "${property.code.value}" property? Some data will be lost!`
      } else {
        return 'Do you want to remove the property? Some data will be lost!'
      }
    } else {
      return null
    }
  }

  getContent() {
    const property = this.getProperty()

    if (property) {
      return `This property is already used by ${property.usagesLocal} ${
        property.usagesLocal > 1 ? 'entities' : 'entity'
      } of this type. Removing the property assignment is going to remove the existing property values as well - data will be lost! Are you sure you want to proceed?`
    } else {
      return null
    }
  }

  getProperty() {
    const { selection, properties } = this.props
    if (selection && properties) {
      return _.find(properties, ['id', selection.params.id])
    } else {
      return null
    }
  }
}

export default TypeFormDialogRemoveProperty
