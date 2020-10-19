import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import openbis from '@src/js/services/openbis.js'
import util from '@src/js/common/util.js'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  inherited: {
    color: theme.palette.hint.main
  },
  implicit: {
    fontStyle: 'italic'
  }
})

class UserFormGridRoles extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  render() {
    logger.log(logger.DEBUG, 'UserFormGridRoles.render')

    const {
      rows,
      selectedRowId,
      onSelectedRowChange,
      controllerRef
    } = this.props

    return (
      <Grid
        id={ids.USER_ROLES_GRID_ID}
        controllerRef={controllerRef}
        columns={[
          {
            name: 'inheritedFrom',
            label: 'Inherited From',
            sort: 'asc',
            getValue: this.getValue,
            renderValue: this.renderValue,
            compareValue: this.compareValue
          },
          {
            name: 'level',
            label: 'Level',
            getValue: this.getValue,
            renderValue: this.renderValue,
            compareValue: this.compareValue
          },
          {
            name: 'space',
            label: 'Space',
            getValue: this.getValue,
            renderValue: this.renderValue,
            compareValue: this.compareValue
          },
          {
            name: 'project',
            label: 'Project',
            getValue: this.getValue,
            renderValue: this.renderValue,
            compareValue: this.compareValue
          },
          {
            name: 'role',
            label: 'Role',
            getValue: this.getValue,
            renderValue: this.renderValue,
            compareValue: this.compareValue
          }
        ]}
        rows={rows}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }

  getValue({ row, column }) {
    const value = row[column.name].value

    if (column.name === 'space' || column.name === 'project') {
      return value || '(all)'
    } else {
      return value
    }
  }

  renderValue({ value, row, column }) {
    const { classes } = this.props

    const classNames = []

    if (column.name === 'space' || column.name === 'project') {
      const rawValue = row[column.name].value
      if (!rawValue) {
        classNames.push(classes.implicit)
      }
    }

    if (row.inheritedFrom.value) {
      classNames.push(classes.inherited)
    }

    return <span className={util.classNames(...classNames)}>{value}</span>
  }

  compareValue({ row1, row2, column, defaultCompare }) {
    const normalize = value => {
      if (value > 0) {
        return 1
      } else if (value < 0) {
        return -1
      } else {
        return 0
      }
    }

    const compareInheritedFrom = () => {
      const inheritedFrom1 = row1.inheritedFrom.value
      const inheritedFrom2 = row2.inheritedFrom.value

      if (inheritedFrom1 && !inheritedFrom2) {
        return -1
      } else if (!inheritedFrom1 && inheritedFrom2) {
        return 1
      } else {
        return normalize(
          10 * defaultCompare(inheritedFrom1, inheritedFrom2) + compareLevel()
        )
      }
    }

    const compareLevel = () => {
      const LEVEL_SORTING = {
        [openbis.RoleLevel.INSTANCE]: 0,
        [openbis.RoleLevel.SPACE]: 1,
        [openbis.RoleLevel.PROJECT]: 2
      }
      const level1 = row1.level.value
      const level2 = row2.level.value
      return normalize(
        10 * defaultCompare(LEVEL_SORTING[level1], LEVEL_SORTING[level2]) +
          compareRole()
      )
    }

    const compareRole = () => {
      const ROLE_SORTING = {
        [openbis.Role.ETL_SERVER]: 0,
        [openbis.Role.ADMIN]: 1,
        [openbis.Role.POWER_USER]: 2,
        [openbis.Role.USER]: 3,
        [openbis.Role.OBSERVER]: 4,
        [openbis.Role.DISABLED]: 5
      }
      const role1 = row1.role.value
      const role2 = row2.role.value
      return defaultCompare(ROLE_SORTING[role1], ROLE_SORTING[role2])
    }

    const compareSpace = () => {
      const space1 = row1.space.value
      const space2 = row2.space.value
      return normalize(10 * defaultCompare(space1, space2) + compareProject())
    }

    const compareProject = () => {
      const project1 = row1.project.value
      const project2 = row2.project.value
      return defaultCompare(project1, project2)
    }

    if (column.name === 'inheritedFrom') {
      return compareInheritedFrom()
    } else if (column.name === 'level') {
      return compareLevel()
    } else if (column.name === 'role') {
      return compareRole()
    } else if (column.name === 'space') {
      return compareSpace()
    } else if (column.name === 'project') {
      return compareProject()
    }
  }
}

export default _.flow(withStyles(styles))(UserFormGridRoles)
