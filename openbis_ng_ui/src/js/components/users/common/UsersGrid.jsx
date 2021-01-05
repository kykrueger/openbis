import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import UserLink from '@src/js/components/common/link/UserLink.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class UsersGrid extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  render() {
    logger.log(logger.DEBUG, 'UsersGrid.render')

    const {
      id,
      rows,
      selectedRowId,
      onSelectedRowChange,
      controllerRef
    } = this.props

    return (
      <Grid
        id={id}
        controllerRef={controllerRef}
        header='Users'
        columns={[
          {
            name: 'userId',
            label: 'User Id',
            sort: 'asc',
            getValue: ({ row }) => row.userId.value,
            renderValue: ({ value }) => {
              return <UserLink userId={value} />
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

export default _.flow(withStyles(styles))(UsersGrid)
