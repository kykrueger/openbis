import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { Resizable } from 're-resizable'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import Loading from '@src/js/components/common/loading/Loading.jsx'
import logger from '@src/js/common/logger.js'

import ObjectTypeController from './ObjectTypeController.js'
import ObjectTypeFacade from './ObjectTypeFacade.js'
import ObjectTypeButtons from './ObjectTypeButtons.jsx'
import ObjectTypeParameters from './ObjectTypeParameters.jsx'
import ObjectTypePreview from './ObjectTypePreview.jsx'
import ObjectTypeDialogRemoveSection from './ObjectTypeDialogRemoveSection.jsx'
import ObjectTypeDialogRemoveProperty from './ObjectTypeDialogRemoveProperty.jsx'

const styles = theme => ({
  container: {
    height: '100%',
    display: 'flex',
    flexDirection: 'row'
  },
  content: {
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
    flex: '1 1 auto'
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
    borderColor: theme.palette.background.secondary,
    borderStyle: 'solid'
  },
  parameters: {
    backgroundColor: theme.palette.action.selected,
    height: '100%',
    overflow: 'auto',
    flex: '0 0 auto'
  }
})

class ObjectType extends React.PureComponent {
  constructor(props) {
    super(props)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new ObjectTypeController(new ObjectTypeFacade())
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectType.render')

    const { loading, type } = this.state

    return <Loading loading={loading}>{!!type && this.doRender()}</Loading>
  }

  doRender() {
    let { controller } = this

    let {
      type,
      properties,
      sections,
      selection,
      removePropertyDialogOpen,
      removeSectionDialogOpen
    } = this.state

    let { classes } = this.props

    return (
      <div className={classes.container}>
        <div className={classes.content}>
          <div className={classes.preview}>
            <ObjectTypePreview
              controller={controller}
              type={type}
              properties={properties}
              sections={sections}
              selection={selection}
              onOrderChange={controller.handleOrderChange}
              onSelectionChange={controller.handleSelectionChange}
            />
          </div>
          <div className={classes.buttons}>
            <ObjectTypeButtons
              onAddSection={controller.handleAddSection}
              onAddProperty={controller.handleAddProperty}
              onRemove={controller.handleRemove}
              onSave={controller.handleSave}
              selection={selection}
            />
            <ObjectTypeDialogRemoveSection
              open={removeSectionDialogOpen}
              selection={selection}
              sections={sections}
              onConfirm={controller.handleRemoveConfirm}
              onCancel={controller.handleRemoveCancel}
            />
            <ObjectTypeDialogRemoveProperty
              open={removePropertyDialogOpen}
              selection={selection}
              properties={properties}
              onConfirm={controller.handleRemoveConfirm}
              onCancel={controller.handleRemoveCancel}
            />
          </div>
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
            <ObjectTypeParameters
              controller={controller}
              type={type}
              properties={properties}
              sections={sections}
              selection={selection}
              onChange={controller.handleChange}
              onSelectionChange={controller.handleSelectionChange}
              onBlur={controller.handleBlur}
            />
          </div>
        </Resizable>
      </div>
    )
  }
}

export default _.flow(withStyles(styles))(ObjectType)
