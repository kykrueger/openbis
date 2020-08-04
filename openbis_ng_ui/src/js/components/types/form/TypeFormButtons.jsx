import React from 'react'
import FormButtons from '@src/js/components/common/form/FormButtons.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import logger from '@src/js/common/logger.js'

import TypeFormControllerStrategies from './TypeFormControllerStrategies.js'

class TypeFormButtons extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormButtons.render')

    const { mode, onEdit, onSave, onCancel, changed, object } = this.props

    const strategy = new TypeFormControllerStrategies().getStrategy(object.type)
    const existing = object.type === strategy.getExistingObjectType()

    return (
      <FormButtons
        mode={mode}
        changed={changed}
        onEdit={onEdit}
        onSave={onSave}
        onCancel={existing ? onCancel : null}
        renderAdditionalButtons={classes =>
          this.renderAdditionalButtons(classes)
        }
      />
    )
  }

  renderAdditionalButtons(classes) {
    const { onAddSection, onAddProperty, onRemove } = this.props

    return (
      <React.Fragment>
        <Button
          name='addSection'
          label='Add Section'
          styles={{ root: classes.button }}
          onClick={onAddSection}
        />
        <Button
          name='addProperty'
          label='Add Property'
          styles={{ root: classes.button }}
          disabled={!this.isSectionOrPropertySelected()}
          onClick={onAddProperty}
        />
        <Button
          name='remove'
          label='Remove'
          styles={{ root: classes.button }}
          disabled={!this.isSectionOrPropertySelected()}
          onClick={onRemove}
        />
      </React.Fragment>
    )
  }

  isSectionOrPropertySelected() {
    const { selection } = this.props
    return (
      selection &&
      (selection.type === 'property' || selection.type === 'section')
    )
  }
}

export default TypeFormButtons
