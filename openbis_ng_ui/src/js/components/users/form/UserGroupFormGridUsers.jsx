import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import LinkToObject from '@src/js/components/common/form/LinkToObject.jsx'
import ids from '@src/js/common/consts/ids.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class UserGroupFormGridUsers extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  render() {
    logger.log(logger.DEBUG, 'UserGroupFormGridUsers.render')

    const {
      rows,
      selectedRowId,
      onSelectedRowChange,
      controllerRef
    } = this.props

    return (
      <Grid
        id={ids.USER_GROUP_USERS_GRID_ID}
        controllerRef={controllerRef}
        header='Users'
        columns={[
          {
            name: 'userId',
            label: 'User Id',
            sort: 'asc',
            getValue: ({ row }) => row.userId.value,
            renderValue: ({ value }) => {
              if (value) {
                return (
                  <LinkToObject
                    page={pages.USERS}
                    object={{
                      type: objectTypes.USER,
                      id: value
                    }}
                  >
                    {value}
                  </LinkToObject>
                )
              } else {
                return ''
              }
            }
          },
          {
            name: 'firstName',
            label: 'First Name',
            getValue: ({ row }) => row.firstName.value
          },
          {
            name: 'lastName',
            label: 'Last Name',
            getValue: ({ row }) => row.lastName.value
          },
          {
            name: 'email',
            label: 'Email',
            getValue: ({ row }) => row.email.value
          },
          {
            name: 'space',
            label: 'Home Space',
            getValue: ({ row }) => row.space.value
          },
          {
            name: 'active',
            label: 'Active',
            getValue: ({ row }) => row.active.value
          }
        ]}
        rows={rows}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}

export default _.flow(withStyles(styles))(UserGroupFormGridUsers)
