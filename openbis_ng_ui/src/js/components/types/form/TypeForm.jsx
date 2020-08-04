import _ from 'lodash'
import React from 'react'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import FormLayout from '@src/js/components/common/form/FormLayout.jsx'
import UnsavedChangesDialog from '@src/js/components/common/dialog/UnsavedChangesDialog.jsx'
import logger from '@src/js/common/logger.js'

import TypeFormController from './TypeFormController.js'
import TypeFormFacade from './TypeFormFacade.js'
import TypeFormButtons from './TypeFormButtons.jsx'
import TypeFormParameters from './TypeFormParameters.jsx'
import TypeFormPreview from './TypeFormPreview.jsx'
import TypeFormDialogRemoveSection from './TypeFormDialogRemoveSection.jsx'
import TypeFormDialogRemoveProperty from './TypeFormDialogRemoveProperty.jsx'

const styles = () => ({})

class TypeForm extends React.PureComponent {
  constructor(props) {
    super(props)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new TypeFormController(new TypeFormFacade())
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  render() {
    logger.log(logger.DEBUG, 'TypeForm.render')

    const { loading, loaded, type } = this.state

    return (
      <FormLayout
        loading={loading}
        loaded={loaded}
        object={type}
        renderMainPanel={() => this.renderMainPanel()}
        renderAdditionalPanel={() => this.renderAdditionalPanel()}
        renderButtons={() => this.renderButtons()}
      />
    )
  }

  renderMainPanel() {
    const { controller } = this
    const { type, properties, sections, selection, mode } = this.state

    return (
      <TypeFormPreview
        controller={controller}
        type={type}
        properties={properties}
        sections={sections}
        selection={selection}
        mode={mode}
        onOrderChange={controller.handleOrderChange}
        onSelectionChange={controller.handleSelectionChange}
      />
    )
  }

  renderAdditionalPanel() {
    let { controller } = this
    let { type, properties, sections, selection, mode } = this.state

    return (
      <TypeFormParameters
        controller={controller}
        type={type}
        properties={properties}
        sections={sections}
        selection={selection}
        mode={mode}
        onChange={controller.handleChange}
        onSelectionChange={controller.handleSelectionChange}
        onBlur={controller.handleBlur}
      />
    )
  }

  renderButtons() {
    const { controller } = this
    const {
      properties,
      sections,
      selection,
      removePropertyDialogOpen,
      removeSectionDialogOpen,
      unsavedChangesDialogOpen,
      changed,
      mode
    } = this.state
    const { object } = this.props

    return (
      <React.Fragment>
        <TypeFormButtons
          onAddSection={controller.handleAddSection}
          onAddProperty={controller.handleAddProperty}
          onRemove={controller.handleRemove}
          onEdit={controller.handleEdit}
          onSave={controller.handleSave}
          onCancel={controller.handleCancel}
          object={object}
          selection={selection}
          changed={changed}
          mode={mode}
        />
        <TypeFormDialogRemoveSection
          open={removeSectionDialogOpen}
          selection={selection}
          sections={sections}
          onConfirm={controller.handleRemoveConfirm}
          onCancel={controller.handleRemoveCancel}
        />
        <TypeFormDialogRemoveProperty
          open={removePropertyDialogOpen}
          selection={selection}
          properties={properties}
          onConfirm={controller.handleRemoveConfirm}
          onCancel={controller.handleRemoveCancel}
        />
        <UnsavedChangesDialog
          open={unsavedChangesDialogOpen}
          onConfirm={controller.handleCancelConfirm}
          onCancel={controller.handleCancelCancel}
        />
      </React.Fragment>
    )
  }
}

export default _.flow(connect(), withStyles(styles))(TypeForm)
