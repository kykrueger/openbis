import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import PluginLink from '@src/js/components/common/link/PluginLink.jsx'
import UserLink from '@src/js/components/common/link/UserLink.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class PluginsGrid extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  render() {
    logger.log(logger.DEBUG, 'PluginsGrid.render')

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
        header='Plugins'
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
                  pluginType={row.pluginType.value}
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
            name: 'pluginType',
            label: 'Plugin Type',
            getValue: ({ row }) => row.pluginType.value
          },
          {
            name: 'pluginKind',
            label: 'Plugin Kind',
            getValue: ({ row }) => row.pluginKind.value
          },
          {
            name: 'entityKind',
            label: 'Entity Kind',
            getValue: ({ row }) => row.entityKind.value
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

export default _.flow(withStyles(styles))(PluginsGrid)
