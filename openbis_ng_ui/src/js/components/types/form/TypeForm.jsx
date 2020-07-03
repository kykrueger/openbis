import _ from 'lodash'
import React from 'react'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import { Resizable } from 're-resizable'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import Container from '@src/js/components/common/form/Container.jsx'
import Loading from '@src/js/components/common/loading/Loading.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import UnsavedChangesDialog from '@src/js/components/common/dialog/UnsavedChangesDialog.jsx'
import logger from '@src/js/common/logger.js'

import TypeFormController from './TypeFormController.js'
import TypeFormFacade from './TypeFormFacade.js'
import TypeFormButtons from './TypeFormButtons.jsx'
import TypeFormParameters from './TypeFormParameters.jsx'
import TypeFormPreview from './TypeFormPreview.jsx'
import TypeFormDialogRemoveSection from './TypeFormDialogRemoveSection.jsx'
import TypeFormDialogRemoveProperty from './TypeFormDialogRemoveProperty.jsx'

const styles = theme => ({
  container: {
    height: '100%',
    display: 'flex',
    flexDirection: 'column'
  },
  content: {
    display: 'flex',
    flexDirection: 'row',
    flex: '1 1 auto',
    overflow: 'auto'
  },
  preview: {
    height: '100%',
    flex: '1 1 auto',
    display: 'flex',
    flexDirection: 'column',
    overflow: 'auto'
  },
  buttons: {
    flex: '0 0 auto',
    borderWidth: '1px 0px 0px 0px',
    borderColor: theme.palette.border.primary,
    borderStyle: 'solid'
  },
  parameters: {
    borderLeft: `1px solid ${theme.palette.border.primary}`,
    height: '100%',
    overflow: 'auto',
    flex: '0 0 auto'
  }
})

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

    const { loaded, loading } = this.state

    return <Loading loading={loading}>{loaded && this.doRender()}</Loading>
  }

  doRender() {
    const { type } = this.state

    if (type) {
      return this.doRenderExisting()
    } else {
      return this.doRenderNonExistent()
    }
  }

  doRenderExisting() {
    let { controller } = this

    let {
      type,
      properties,
      sections,
      selection,
      removePropertyDialogOpen,
      removeSectionDialogOpen,
      unsavedChangesDialogOpen,
      changed,
      mode
    } = this.state

    let { object, classes } = this.props

    return (
      <div className={classes.container}>
        <div className={classes.content}>
          <div className={classes.preview}>
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
          </div>
          <Resizable
            defaultSize={{
              width: 400,
              height: 'auto'
            }}
            enable={{
              left: true,
              top: false,
              right: false,
              bottom: false,
              topRight: false,
              bottomRight: false,
              bottomLeft: false,
              topLeft: false
            }}
          >
            <div className={classes.parameters}>
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
            </div>
          </Resizable>
        </div>
        <div className={classes.buttons}>
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
        </div>
      </div>
    )
  }

  doRenderNonExistent() {
    return (
      <Container>
        <Message type='info'>Object does not exist.</Message>
      </Container>
    )
  }
}

export default _.flow(connect(), withStyles(styles))(TypeForm)
