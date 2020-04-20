import _ from 'lodash'
import React from 'react'
import ConfirmationDialog from '@src/js/components/common/dialog/ConfirmationDialog.jsx'
import logger from '@src/js/common/logger.js'

class ObjectTypeDialogRemoveProperty extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'ObjectTypeDialogRemoveProperty.render')

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
    const { open } = this.props

    if (open) {
      const property = this.getProperty()
      if (property.code) {
        return `Do you want to remove "${property.code}" property? Some data will be lost!`
      } else {
        return 'Do you want to remove the property? Some data will be lost!'
      }
    } else {
      return null
    }
  }

  getContent() {
    const { open } = this.props

    if (open) {
      const property = this.getProperty()
      return `This property is already used by ${property.usages} ${
        property.usages > 1 ? 'entities' : 'entity'
      }. Removing the property definition is going to remove the existing property values as well - data will be lost! Are you sure you want to proceed?`
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

export default ObjectTypeDialogRemoveProperty
