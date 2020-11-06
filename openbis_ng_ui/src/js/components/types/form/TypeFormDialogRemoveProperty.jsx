import _ from 'lodash'
import React from 'react'
import ConfirmationDialog from '@src/js/components/common/dialog/ConfirmationDialog.jsx'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import logger from '@src/js/common/logger.js'

class TypeFormDialogRemoveProperty extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'TypeFormDialogRemoveProperty.render')

    const { open, object, onConfirm, onCancel } = this.props

    const property = this.getProperty()

    if (property) {
      return (
        <ConfirmationDialog
          open={open}
          onConfirm={onConfirm}
          onCancel={onCancel}
          title={this.getTitle(property)}
          content={this.getContent(object, property)}
          type={property.usages > 0 ? 'warning' : 'info'}
        />
      )
    } else {
      return null
    }
  }

  getTitle(property) {
    return `Do you want to remove "${property.code.value}" property?`
  }

  getContent(object, property) {
    if (property.usages > 0) {
      return `This property assignment is already used by existing entities of "${object.id}" type. Removing it is also going to remove ${property.usages} existing property value(s) - data will be lost! Are you sure you want to proceed?`
    } else {
      return `This property assignment is not yet used by any entities of "${object.id}" type.`
    }
  }

  getProperty() {
    const { selection, properties } = this.props

    if (selection && selection.type === TypeFormSelectionType.PROPERTY) {
      return _.find(properties, ['id', selection.params.id])
    } else {
      return null
    }
  }
}

export default TypeFormDialogRemoveProperty
