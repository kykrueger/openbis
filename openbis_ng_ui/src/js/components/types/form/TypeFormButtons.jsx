import React from 'react'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import TypeFormControllerStrategies from '@src/js/components/types/form/TypeFormControllerStrategies.js'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import users from '@src/js/common/consts/users.js'
import logger from '@src/js/common/logger.js'

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
      <PageButtons
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
          disabled={
            !(
              this.isNonSystemInternalSectionSelected() ||
              this.isNonSystemInternalPropertySelected()
            )
          }
          onClick={onRemove}
        />
      </React.Fragment>
    )
  }

  isSectionOrPropertySelected() {
    const { selection } = this.props
    return (
      selection &&
      (selection.type === TypeFormSelectionType.PROPERTY ||
        selection.type === TypeFormSelectionType.SECTION)
    )
  }

  isNonSystemInternalSectionSelected() {
    const { selection, sections, properties } = this.props

    if (selection && selection.type === TypeFormSelectionType.SECTION) {
      const section = sections.find(
        section => section.id === selection.params.id
      )
      return !section.properties.some(propertyId => {
        const property = properties.find(property => property.id === propertyId)
        return (
          property.internal.value && property.registrator.value === users.SYSTEM
        )
      })
    } else {
      return false
    }
  }

  isNonSystemInternalPropertySelected() {
    const { selection, properties } = this.props

    if (selection && selection.type === TypeFormSelectionType.PROPERTY) {
      const property = properties.find(
        property => property.id === selection.params.id
      )
      return !(
        property.internal.value && property.registrator.value === users.SYSTEM
      )
    } else {
      return false
    }
  }
}

export default TypeFormButtons
