import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { Resizable } from 're-resizable'

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
      this.facade = new ObjectTypeFacade()
      this.controller = new ObjectTypeController(
        this.props.objectId,
        () => {
          return this.state
        },
        this.setState.bind(this),
        this.facade
      )
    }

    this.handleOrderChange = this.handleOrderChange.bind(this)
    this.handleSelectionChange = this.handleSelectionChange.bind(this)
    this.handleChange = this.handleChange.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
    this.handleAddSection = this.handleAddSection.bind(this)
    this.handleAddProperty = this.handleAddProperty.bind(this)
    this.handleRemove = this.handleRemove.bind(this)
    this.handleRemoveConfirm = this.handleRemoveConfirm.bind(this)
    this.handleRemoveCancel = this.handleRemoveCancel.bind(this)
    this.handleSave = this.handleSave.bind(this)
  }

  componentDidMount() {
    this.controller.load()
  }

  handleOrderChange(type, params) {
    this.controller.handleOrderChange(type, params)
  }

  handleSelectionChange(type, params) {
    this.controller.handleSelectionChange(type, params)
  }

  handleChange(type, params) {
    this.controller.handleChange(type, params)
  }

  handleBlur() {
    this.controller.handleBlur()
  }

  handleAddSection() {
    this.controller.handleAddSection()
  }

  handleAddProperty() {
    this.controller.handleAddProperty()
  }

  handleRemove() {
    this.controller.handleRemove()
  }

  handleRemoveConfirm() {
    this.controller.handleRemoveConfirm()
  }

  handleRemoveCancel() {
    this.controller.handleRemoveCancel()
  }

  handleSave() {
    this.controller.handleSave()
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectType.render')

    const { loading, type } = this.state

    return <Loading loading={loading}>{!!type && this.doRender()}</Loading>
  }

  doRender() {
    let { facade } = this

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
              facade={facade}
              type={type}
              properties={properties}
              sections={sections}
              selection={selection}
              onOrderChange={this.handleOrderChange}
              onSelectionChange={this.handleSelectionChange}
            />
          </div>
          <div className={classes.buttons}>
            <ObjectTypeButtons
              onAddSection={this.handleAddSection}
              onAddProperty={this.handleAddProperty}
              onRemove={this.handleRemove}
              onSave={this.handleSave}
              selection={selection}
            />
            <ObjectTypeDialogRemoveSection
              open={removeSectionDialogOpen}
              selection={selection}
              sections={sections}
              onConfirm={this.handleRemoveConfirm}
              onCancel={this.handleRemoveCancel}
            />
            <ObjectTypeDialogRemoveProperty
              open={removePropertyDialogOpen}
              selection={selection}
              properties={properties}
              onConfirm={this.handleRemoveConfirm}
              onCancel={this.handleRemoveCancel}
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
              facade={facade}
              type={type}
              properties={properties}
              sections={sections}
              selection={selection}
              onChange={this.handleChange}
              onSelectionChange={this.handleSelectionChange}
              onBlur={this.handleBlur}
            />
          </div>
        </Resizable>
      </div>
    )
  }
}

export default _.flow(withStyles(styles))(ObjectType)
