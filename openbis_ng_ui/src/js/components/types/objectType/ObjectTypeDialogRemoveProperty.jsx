import _ from 'lodash'
import React from 'react'
import ConfirmationDialog from '../../common/dialog/ConfirmationDialog.jsx'
import logger from '../../../common/logger.js'

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
        content='This property is already used by some entities. Removing the property definition is going to remove the existing property values as well - data will be lost! Are you sure you want to proceed?'
      />
    )
  }

  getTitle() {
    const { open, selection, properties } = this.props

    if (open) {
      const property = _.find(properties, ['id', selection.params.id])
      if (property.code) {
        return `Do you want to remove "${property.code}" property? Some data will be lost!`
      } else {
        return 'Do you want to remove the property? Some data will be lost!'
      }
    } else {
      return null
    }
  }
}

export default ObjectTypeDialogRemoveProperty
