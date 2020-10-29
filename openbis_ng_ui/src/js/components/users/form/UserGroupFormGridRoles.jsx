import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import openbis from '@src/js/services/openbis.js'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  implicit: {
    fontStyle: 'italic'
  }
})

const ALL_VALUE = '(all)'

class UserGroupFormGridRoles extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  render() {
    logger.log(logger.DEBUG, 'UserGroupFormGridRoles.render')

    const {
      rows,
      selectedRowId,
      onSelectedRowChange,
      controllerRef
    } = this.props

    return (
      <Grid
        id={ids.USER_GROUP_ROLES_GRID_ID}
        controllerRef={controllerRef}
        header='Roles'
        columns={[
          {
            name: 'level',
            label: 'Level',
            getValue: this.getLevelValue,
            renderValue: this.renderLevelValue,
            compareValue: params => {
              return (
                1000 * this.compareLevelValue(params) +
                100 * this.compareSpaceValue(params) +
                10 * this.compareProjectValue(params) +
                this.compareRoleValue(params)
              )
            }
          },
          {
            name: 'space',
            label: 'Space',
            getValue: this.getSpaceValue,
            renderValue: this.renderSpaceValue,
            compareValue: params => {
              return (
                1000 * this.compareSpaceValue(params) +
                100 * this.compareLevelValue(params) +
                10 * this.compareProjectValue(params) +
                this.compareRoleValue(params)
              )
            }
          },
          {
            name: 'project',
            label: 'Project',
            getValue: this.getProjectValue,
            renderValue: this.renderProjectValue,
            compareValue: params => {
              return (
                1000 * this.compareProjectValue(params) +
                100 * this.compareLevelValue(params) +
                10 * this.compareSpaceValue(params) +
                this.compareRoleValue(params)
              )
            }
          },
          {
            name: 'role',
            label: 'Role',
            getValue: this.getRoleValue,
            renderValue: this.renderRoleValue,
            compareValue: params => {
              return (
                1000 * this.compareRoleValue(params) +
                100 * this.compareLevelValue(params) +
                10 * this.compareSpaceValue(params) +
                this.compareProjectValue(params)
              )
            }
          }
        ]}
        rows={rows}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }

  getLevelValue({ row }) {
    return row.level.value
  }

  getSpaceValue({ row }) {
    if (row.level.value === openbis.RoleLevel.INSTANCE) {
      return ALL_VALUE
    } else {
      return row.space.value
    }
  }

  getProjectValue({ row }) {
    if (row.level.value === openbis.RoleLevel.INSTANCE) {
      return ALL_VALUE
    } else if (row.level.value === openbis.RoleLevel.SPACE) {
      return row.space.value ? ALL_VALUE : null
    } else {
      return row.project.value
    }
  }

  getRoleValue({ row }) {
    return row.role.value
  }

  renderLevelValue({ value, row }) {
    return this.renderDefault({ value, row })
  }

  renderSpaceValue({ value, row }) {
    if (!row.space.value && value) {
      return (
        <div className={this.props.classes.implicit}>
          {this.renderDefault({ value, row })}
        </div>
      )
    } else {
      return this.renderDefault({ value, row })
    }
  }

  renderProjectValue({ value, row }) {
    if (!row.project.value && value) {
      return (
        <div className={this.props.classes.implicit}>
          {this.renderDefault({ value, row })}
        </div>
      )
    } else {
      return this.renderDefault({ value, row })
    }
  }

  renderRoleValue({ value, row }) {
    return this.renderDefault({ value, row })
  }

  renderDefault({ value }) {
    return value
  }

  compareLevelValue({ row1, row2, defaultCompare }) {
    const LEVEL_SORTING = {
      [null]: 0,
      [openbis.RoleLevel.INSTANCE]: 1,
      [openbis.RoleLevel.SPACE]: 2,
      [openbis.RoleLevel.PROJECT]: 3
    }
    const level1 = this.getLevelValue({ row: row1 })
    const level2 = this.getLevelValue({ row: row2 })
    return defaultCompare(LEVEL_SORTING[level1], LEVEL_SORTING[level2])
  }

  compareSpaceValue({ row1, row2, defaultCompare }) {
    const space1 = this.getSpaceValue({ row: row1 })
    const space2 = this.getSpaceValue({ row: row2 })
    return defaultCompare(space1, space2)
  }

  compareProjectValue({ row1, row2, defaultCompare }) {
    const project1 = this.getProjectValue({ row: row1 })
    const project2 = this.getProjectValue({ row: row2 })
    return defaultCompare(project1, project2)
  }

  compareRoleValue({ row1, row2, defaultCompare }) {
    const ROLE_SORTING = {
      [null]: 0,
      [openbis.Role.ETL_SERVER]: 1,
      [openbis.Role.ADMIN]: 2,
      [openbis.Role.POWER_USER]: 3,
      [openbis.Role.USER]: 4,
      [openbis.Role.OBSERVER]: 5,
      [openbis.Role.DISABLED]: 6
    }
    const role1 = this.getRoleValue({ row: row1 })
    const role2 = this.getRoleValue({ row: row2 })
    return defaultCompare(ROLE_SORTING[role1], ROLE_SORTING[role2])
  }
}

export default _.flow(withStyles(styles))(UserGroupFormGridRoles)
