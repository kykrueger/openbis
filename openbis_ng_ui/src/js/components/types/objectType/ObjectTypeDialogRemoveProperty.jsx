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
        title={'Remove "' + this.getPropertyName() + '" property'}
        content='The property is used by some entities. Are you sure you want to remove it?'
      />
    )
  }

  getPropertyName() {
    const { open, selection, properties } = this.props

    if (open) {
      const property = _.find(properties, ['id', selection.params.id])
      return property.code
    } else {
      return null
    }
  }
}

export default ObjectTypeDialogRemoveProperty
