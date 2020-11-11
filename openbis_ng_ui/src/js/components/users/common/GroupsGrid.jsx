import React from 'react'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import LinkToObject from '@src/js/components/common/form/LinkToObject.jsx'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'
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
              if (value) {
                return (
                  <LinkToObject
                    page={pages.USERS}
                    object={{
                      type: objectTypes.USER_GROUP,
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
