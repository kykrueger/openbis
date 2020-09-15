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
import UserFormController from '@src/js/components/users/form/UserFormController.js'
import UserFormFacade from '@src/js/components/users/form/UserFormFacade.js'
import UserFormSelectionType from '@src/js/components/users/form/UserFormSelectionType.js'
import UserFormParametersUser from '@src/js/components/users/form/UserFormParametersUser.jsx'
import UserFormParametersGroup from '@src/js/components/users/form/UserFormParametersGroup.jsx'
import UserFormParametersRole from '@src/js/components/users/form/UserFormParametersRole.jsx'
import UserFormButtons from '@src/js/components/users/form/UserFormButtons.jsx'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

const USER_GROUPS_GRID_COLUMNS = [
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

const USER_ROLES_GRID_COLUMNS = [
  {
    field: 'inheritedFrom.value',
    label: 'Inherited From',
    sort: 'asc'
  },
  {
    field: 'role.value',
    label: 'Role'
  },
  {
    field: 'level.value',
    label: 'Level'
  },
  {
    field: 'space.value',
    label: 'Space'
  },
  {
    field: 'project.value',
    label: 'Project'
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

  handleSelectedGroupRowChange(row) {
    const { controller } = this
    if (row) {
      controller.handleSelectionChange(UserFormSelectionType.GROUP, {
        id: row.id
      })
    }
  }

  handleSelectedRoleRowChange(row) {
    const { controller } = this
    if (row) {
      controller.handleSelectionChange(UserFormSelectionType.ROLE, {
        id: row.id
      })
    }
  }

  handleGroupsGridControllerRef(gridController) {
    this.controller.groupsGridController = gridController
  }

  handleRolesGridControllerRef(gridController) {
    this.controller.rolesGridController = gridController
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
    const { groups, roles, selection } = this.state

    return (
      <GridContainer onClick={this.handleClickContainer}>
        <Header>Groups</Header>
        <Grid
          id={ids.USER_GROUPS_GRID_ID}
          controllerRef={this.handleGroupsGridControllerRef}
          columns={USER_GROUPS_GRID_COLUMNS}
          rows={groups}
          selectedRowId={
            selection && selection.type === UserFormSelectionType.GROUP
              ? selection.params.id
              : null
          }
          onSelectedRowChange={this.handleSelectedGroupRowChange}
        />
        <Header>Roles</Header>
        <Grid
          id={ids.USER_ROLES_GRID_ID}
          controllerRef={this.handleRolesGridControllerRef}
          columns={USER_ROLES_GRID_COLUMNS}
          rows={roles}
          selectedRowId={
            selection && selection.type === UserFormSelectionType.ROLE
              ? selection.params.id
              : null
          }
          onSelectedRowChange={this.handleSelectedRoleRowChange}
        />
      </GridContainer>
    )
  }

  renderAdditionalPanel() {
    const { controller } = this
    const { user, groups, roles, selection, mode } = this.state

    const selectedGroupRow = controller.groupsGridController
      ? controller.groupsGridController.getSelectedRow()
      : null

    const selectedRoleRow = controller.rolesGridController
      ? controller.rolesGridController.getSelectedRow()
      : null

    return (
      <div>
        <UserFormParametersUser
          controller={controller}
          user={user}
          selection={selection}
          mode={mode}
          onChange={controller.handleChange}
          onSelectionChange={controller.handleSelectionChange}
          onBlur={controller.handleBlur}
        />
        <UserFormParametersGroup
          controller={controller}
          groups={groups}
          selection={selection}
          selectedRow={selectedGroupRow}
          mode={mode}
          onChange={controller.handleChange}
          onSelectionChange={controller.handleSelectionChange}
          onBlur={controller.handleBlur}
        />
        <UserFormParametersRole
          controller={controller}
          roles={roles}
          selection={selection}
          selectedRow={selectedRoleRow}
          mode={mode}
          onChange={controller.handleChange}
          onSelectionChange={controller.handleSelectionChange}
          onBlur={controller.handleBlur}
        />
      </div>
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
        onAddRole={controller.handleAddRole}
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
