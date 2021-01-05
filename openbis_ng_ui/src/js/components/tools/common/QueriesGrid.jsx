import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import QueryLink from '@src/js/components/common/link/QueryLink.jsx'
import UserLink from '@src/js/components/common/link/UserLink.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class QueriesGrid extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  render() {
    logger.log(logger.DEBUG, 'QueriesGrid.render')

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
        header='Queries'
        columns={[
          {
            name: 'name',
            label: 'Name',
            sort: 'asc',
            getValue: ({ row }) => row.name.value,
            renderValue: ({ value }) => {
              return <QueryLink queryName={value} />
            }
          },
          {
            name: 'description',
            label: 'Description',
            getValue: ({ row }) => row.description.value
          },
          {
            name: 'database',
            label: 'Database',
            getValue: ({ row }) => row.database.value
          },
          {
            name: 'queryType',
            label: 'Query Type',
            getValue: ({ row }) => row.queryType.value
          },
          {
            name: 'entityTypeCodePattern',
            label: 'Entity Type Pattern',
            getValue: ({ row }) => row.entityTypeCodePattern.value
          },
          {
            name: 'publicFlag',
            label: 'Public',
            getValue: ({ row }) => row.publicFlag.value
          },
          {
            name: 'registrator',
            label: 'Registrator',
            getValue: ({ row }) => row.registrator.value,
            renderValue: ({ value }) => {
              return <UserLink userId={value} />
            }
          }
        ]}
        rows={rows}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}

export default _.flow(withStyles(styles))(QueriesGrid)
