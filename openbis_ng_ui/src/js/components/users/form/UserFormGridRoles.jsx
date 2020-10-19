import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import LinkToObject from '@src/js/components/common/form/LinkToObject.jsx'
import openbis from '@src/js/services/openbis.js'
import ids from '@src/js/common/consts/ids.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  inherited: {
    color: theme.palette.hint.main
  },
  implicit: {
    fontStyle: 'italic'
  }
})

const EMPTY_SPACE_VALUE = '(all)'
const EMPTY_PROJECT_VALUE = '(all)'

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
            getValue: ({ row }) => row.inheritedFrom.value,
            renderValue: this.renderInheritedFrom,
            compareValue: this.compareInheritedFrom
          },
          {
            name: 'level',
            label: 'Level',
            getValue: ({ row }) => row.level.value,
            renderValue: this.renderLevel,
            compareValue: this.compareLevel
          },
          {
            name: 'space',
            label: 'Space',
            getValue: ({ row }) => row.space.value || EMPTY_SPACE_VALUE,
            renderValue: this.renderSpace,
            compareValue: this.compareSpace
          },
          {
            name: 'project',
            label: 'Project',
            getValue: ({ row }) => row.project.value || EMPTY_PROJECT_VALUE,
            renderValue: this.renderProject,
            compareValue: this.compareProject
          },
          {
            name: 'role',
            label: 'Role',
            getValue: ({ row }) => row.role.value,
            renderValue: this.renderRole,
            compareValue: this.compareRole
          }
        ]}
        rows={rows}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }

  renderInheritedFrom({ value, row }) {
    if (value) {
      return this.renderDefault({
        value: (
          <LinkToObject
            page={pages.USERS}
            object={{
              type: objectTypes.USER_GROUP,
              id: row.inheritedFrom.value
            }}
          >
            {row.inheritedFrom.value}
          </LinkToObject>
        ),
        row
      })
    } else {
      return null
    }
  }

  renderLevel({ value, row }) {
    return this.renderDefault({ value, row })
  }

  renderSpace({ value, row }) {
    if (!row.space.value) {
      return (
        <div className={this.props.classes.implicit}>
          {this.renderDefault({ value: EMPTY_SPACE_VALUE, row })}
        </div>
      )
    } else {
      return this.renderDefault({ value, row })
    }
  }

  renderProject({ value, row }) {
    if (!row.project.value) {
      return (
        <div className={this.props.classes.implicit}>
          {this.renderDefault({ value: EMPTY_PROJECT_VALUE, row })}
        </div>
      )
    } else {
      return this.renderDefault({ value, row })
    }
  }

  renderRole({ value, row }) {
    return this.renderDefault({ value, row })
  }

  renderDefault({ value, row }) {
    const { classes } = this.props

    if (row.inheritedFrom.value) {
      return <div className={classes.inherited}>{value}</div>
    } else {
      return value
    }
  }

  compareInheritedFrom({ row1, row2, defaultCompare }) {
    const inheritedFrom1 = row1.inheritedFrom.value
    const inheritedFrom2 = row2.inheritedFrom.value

    if (inheritedFrom1 && !inheritedFrom2) {
      return -1
    } else if (!inheritedFrom1 && inheritedFrom2) {
      return 1
    } else {
      return this.normalizeCompare(
        10 * defaultCompare(inheritedFrom1, inheritedFrom2) +
          this.compareLevel({ row1, row2, defaultCompare })
      )
    }
  }

  compareLevel({ row1, row2, defaultCompare }) {
    const LEVEL_SORTING = {
      [openbis.RoleLevel.INSTANCE]: 0,
      [openbis.RoleLevel.SPACE]: 1,
      [openbis.RoleLevel.PROJECT]: 2
    }
    const level1 = row1.level.value
    const level2 = row2.level.value
    return this.normalizeCompare(
      10 * defaultCompare(LEVEL_SORTING[level1], LEVEL_SORTING[level2]) +
        this.compareRole({ row1, row2, defaultCompare })
    )
  }

  compareRole({ row1, row2, defaultCompare }) {
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

  compareSpace({ row1, row2, defaultCompare }) {
    const space1 = row1.space.value
    const space2 = row2.space.value
    return this.normalizeCompare(
      10 * defaultCompare(space1, space2) +
        this.compareProject({ row1, row2, defaultCompare })
    )
  }

  compareProject({ row1, row2, defaultCompare }) {
    const project1 = row1.project.value
    const project2 = row2.project.value
    return defaultCompare(project1, project2)
  }

  normalizeCompare(value) {
    if (value > 0) {
      return 1
    } else if (value < 0) {
      return -1
    } else {
      return 0
    }
  }
}

export default _.flow(withStyles(styles))(UserFormGridRoles)
