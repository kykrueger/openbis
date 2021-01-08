import React from 'react'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import PluginLink from '@src/js/components/common/link/PluginLink.jsx'
import UserLink from '@src/js/components/common/link/UserLink.jsx'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

const ALL_VALUE = '(all)'

class PluginsGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'PluginsGrid.render')

    const {
      id,
      pluginType,
      rows,
      selectedRowId,
      onSelectedRowChange,
      controllerRef
    } = this.props

    return (
      <Grid
        id={id}
        controllerRef={controllerRef}
        header={this.getHeader()}
        columns={[
          {
            name: 'name',
            label: 'Name',
            sort: 'asc',
            getValue: ({ row }) => row.name.value,
            renderValue: ({ row }) => {
              return (
                <PluginLink
                  pluginName={row.name.value}
                  pluginType={pluginType}
                />
              )
            }
          },
          {
            name: 'description',
            label: 'Description',
            getValue: ({ row }) => row.description.value
          },
          {
            name: 'pluginKind',
            label: 'Plugin Kind',
            getValue: ({ row }) => row.pluginKind.value
          },
          {
            name: 'entityKind',
            label: 'Entity Kind',
            getValue: ({ row }) => {
              return row.entityKind.value ? row.entityKind.value : ALL_VALUE
            }
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

  getHeader() {
    const { pluginType } = this.props

    if (pluginType === openbis.PluginType.DYNAMIC_PROPERTY) {
      return 'Dynamic Property Plugins'
    } else if (pluginType === openbis.PluginType.ENTITY_VALIDATION) {
      return 'Entity Validation Plugins'
    }
  }
}

export default PluginsGrid
