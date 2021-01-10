import React from 'react'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import UserGroupLink from '@src/js/components/common/link/UserGroupLink.jsx'
import logger from '@src/js/common/logger.js'

export default class GroupsGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'GroupsGrid.render')

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
        header='Groups'
        columns={[
          {
            name: 'code',
            label: 'Code',
            sort: 'asc',
            getValue: ({ row }) => row.code.value,
            renderValue: ({ value }) => {
              return <UserGroupLink groupCode={value} />
            }
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
