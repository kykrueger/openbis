import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { Resizable } from 're-resizable'

import Loading from '@src/js/components/common/loading/Loading.jsx'
import logger from '@src/js/common/logger.js'

import ObjectTypeFacade from './ObjectTypeFacade.js'
import ObjectTypeHandlerLoad from './ObjectTypeHandlerLoad.js'
import ObjectTypeHandlerValidate from './ObjectTypeHandlerValidate.js'
import ObjectTypeHandlerSave from './ObjectTypeHandlerSave.js'
import ObjectTypeHandlerRemove from './ObjectTypeHandlerRemove.js'
import ObjectTypeHandlerAddSection from './ObjectTypeHandlerAddSection.js'
import ObjectTypeHandlerAddProperty from './ObjectTypeHandlerAddProperty.js'
import ObjectTypeHandlerChange from './ObjectTypeHandlerChange.js'
import ObjectTypeHandlerOrderChange from './ObjectTypeHandlerOrderChange.js'
import ObjectTypeHandlerSelectionChange from './ObjectTypeHandlerSelectionChange.js'

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

    this.facade = this.props.facade ? this.props.facade : new ObjectTypeFacade()

    this.state = {
      loading: true,
      validate: false
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
    new ObjectTypeHandlerLoad(
      this.props.objectId,
      this.state,
      this.setState.bind(this),
      this.facade
    ).execute()
  }

  handleOrderChange(type, params) {
    new ObjectTypeHandlerOrderChange(
      this.state,
      this.setState.bind(this)
    ).execute(type, params)
  }

  handleSelectionChange(type, params) {
    new ObjectTypeHandlerSelectionChange(
      this.state,
      this.setState.bind(this)
    ).execute(type, params)
  }

  handleChange(type, params) {
    new ObjectTypeHandlerChange(this.state, this.setState.bind(this)).execute(
      type,
      params
    )
  }

  handleBlur() {
    new ObjectTypeHandlerValidate(() => {
      return this.state
    }, this.setState.bind(this)).execute()
  }

  handleAddSection() {
    new ObjectTypeHandlerAddSection(
      this.state,
      this.setState.bind(this)
    ).execute()
  }

  handleAddProperty() {
    new ObjectTypeHandlerAddProperty(
      this.state,
      this.setState.bind(this)
    ).execute()
  }

  handleRemove() {
    new ObjectTypeHandlerRemove(
      this.state,
      this.setState.bind(this)
    ).executeRemove()
  }

  handleRemoveConfirm() {
    new ObjectTypeHandlerRemove(
      this.state,
      this.setState.bind(this)
    ).executeRemove(true)
  }

  handleRemoveCancel() {
    new ObjectTypeHandlerRemove(
      this.state,
      this.setState.bind(this)
    ).executeCancel()
  }

  handleSave() {
    const loadHandler = new ObjectTypeHandlerLoad(
      this.props.objectId,
      this.state,
      this.setState.bind(this),
      this.facade
    )

    const validateHandler = new ObjectTypeHandlerValidate(() => {
      return this.state
    }, this.setState.bind(this))

    new ObjectTypeHandlerSave(
      this.state,
      this.setState.bind(this),
      this.facade,
      loadHandler,
      validateHandler
    ).execute()
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
