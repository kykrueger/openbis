import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

import UserFormController from './UserFormController.js'
import UserFormFacade from './UserFormFacade.js'
import UserFormParameters from './UserFormParameters.jsx'
import UserFormButtons from './UserFormButtons.jsx'

const styles = () => ({})

const columns = [
  {
    field: 'code.value',
    label: 'Code',
    sort: 'asc'
  },
  {
    field: 'description.value',
    label: 'Description'
  }
]

class UserForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new UserFormController(new UserFormFacade())
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  handleClickContainer() {
    this.controller.handleSelectionChange()
  }

  handleSelectedRowChange(row) {
    const { controller } = this
    if (row) {
      controller.handleSelectionChange('group', { id: row.id })
    } else {
      controller.handleSelectionChange()
    }
  }

  handleGridControllerRef(gridController) {
    this.controller.gridController = gridController
  }

  render() {
    logger.log(logger.DEBUG, 'UserForm.render')

    const { loading, loaded, user } = this.state

    return (
      <PageWithTwoPanels
        loading={loading}
        loaded={loaded}
        object={user}
        renderMainPanel={() => this.renderMainPanel()}
        renderAdditionalPanel={() => this.renderAdditionalPanel()}
        renderButtons={() => this.renderButtons()}
      />
    )
  }

  renderMainPanel() {
    const { groups, selection } = this.state

    return (
      <GridContainer onClick={this.handleClickContainer}>
        <Header>Groups</Header>
        <Grid
          id={ids.USER_GROUPS_GRID_ID}
          controllerRef={this.handleGridControllerRef}
          columns={columns}
          rows={groups}
          selectedRowId={
            selection && selection.type === 'group' ? selection.params.id : null
          }
          onSelectedRowChange={this.handleSelectedRowChange}
        />
      </GridContainer>
    )
  }

  renderAdditionalPanel() {
    const { controller } = this
    const { user, groups, selection, mode } = this.state

    const selectedRow = controller.gridController
      ? controller.gridController.getSelectedRow()
      : null

    return (
      <UserFormParameters
        controller={controller}
        user={user}
        groups={groups}
        selection={selection}
        selectedRow={selectedRow}
        mode={mode}
        onChange={controller.handleChange}
        onSelectionChange={controller.handleSelectionChange}
        onBlur={controller.handleBlur}
      />
    )
  }

  renderButtons() {
    const { controller } = this
    const { user, selection, changed, mode } = this.state

    return (
      <UserFormButtons
        onEdit={controller.handleEdit}
        onSave={controller.handleSave}
        onCancel={controller.handleCancel}
        onAddGroup={controller.handleAddGroup}
        onRemove={controller.handleRemove}
        user={user}
        selection={selection}
        changed={changed}
        mode={mode}
      />
    )
  }
}

export default _.flow(connect(), withStyles(styles))(UserForm)
