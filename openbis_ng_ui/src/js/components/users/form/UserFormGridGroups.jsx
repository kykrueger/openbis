import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class UserFormGridGroups extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  render() {
    logger.log(logger.DEBUG, 'UserFormGridGroups.render')

    const {
      rows,
      selectedRowId,
      onSelectedRowChange,
      controllerRef
    } = this.props

    return (
      <Grid
        id={ids.USER_GROUPS_GRID_ID}
        controllerRef={controllerRef}
        columns={[
          {
            name: 'code',
            label: 'Code',
            sort: 'asc',
            getValue: ({ row }) => row.code.value
          },
          {
            name: 'description',
            label: 'Description',
            getValue: ({ row }) => row.description.value
          }
        ]}
        rows={rows}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}

export default _.flow(withStyles(styles))(UserFormGridGroups)
