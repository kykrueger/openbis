import _ from 'lodash'
import autoBind from 'auto-bind'
import React from 'react'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import PluginsGrid from '@src/js/components/tools/common/PluginsGrid.jsx'
import QueriesGrid from '@src/js/components/tools/common/QueriesGrid.jsx'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import ids from '@src/js/common/consts/ids.js'
import store from '@src/js/store/store.js'
import actions from '@src/js/store/actions/actions.js'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

class ToolSearch extends React.Component {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {
      loaded: false,
      selection: null
    }
  }

  componentDidMount() {
    Promise.all([this.loadPlugins(), this.loadQueries()])
      .then(([plugins, queries]) => {
        this.setState(() => ({
          loaded: true,
          plugins,
          queries
        }))
      })
      .catch(error => {
        store.dispatch(actions.errorChange(error))
      })
  }

  loadPlugins() {
    let query = this.props.objectId

    let criteria = new openbis.PluginSearchCriteria()
    criteria.withName().thatContains(query)

    let fo = new openbis.PluginFetchOptions()
    fo.withScript()
    fo.withRegistrator()

    return openbis.searchPlugins(criteria, fo).then(result => {
      return result.objects
        .filter(plugin => {
          return (
            plugin.pluginType === openbis.PluginType.DYNAMIC_PROPERTY ||
            plugin.pluginType === openbis.PluginType.ENTITY_VALIDATION
          )
        })
        .map(plugin => {
          const entityKinds = _.get(plugin, 'entityKinds', [])

          return {
            id: _.get(plugin, 'name'),
            name: FormUtil.createField({ value: _.get(plugin, 'name') }),
            description: FormUtil.createField({
              value: _.get(plugin, 'description')
            }),
            pluginType: FormUtil.createField({
              value: _.get(plugin, 'pluginType')
            }),
            pluginKind: FormUtil.createField({
              value: _.get(plugin, 'pluginKind')
            }),
            entityKind: FormUtil.createField({
              value: entityKinds.length === 1 ? entityKinds[0] : null
            }),
            script: FormUtil.createField({ value: _.get(plugin, 'script') }),
            registrator: FormUtil.createField({
              value: _.get(plugin, 'registrator.userId')
            })
          }
        })
    })
  }

  loadQueries() {
    let query = this.props.objectId

    let criteria = new openbis.QuerySearchCriteria()
    criteria.withName().thatContains(query)

    let fo = new openbis.QueryFetchOptions()
    fo.withRegistrator()

    return openbis.searchQueries(criteria, fo).then(result => {
      return result.objects.map(query => ({
        id: _.get(query, 'name'),
        name: FormUtil.createField({ value: _.get(query, 'name') }),
        description: FormUtil.createField({
          value: _.get(query, 'description')
        }),
        database: FormUtil.createField({
          value: _.get(query, 'databaseLabel')
        }),
        queryType: FormUtil.createField({
          value: _.get(query, 'queryType')
        }),
        entityTypeCodePattern: FormUtil.createField({
          value: _.get(query, 'entityTypeCodePattern')
        }),
        sql: FormUtil.createField({
          value: _.get(query, 'sql')
        }),
        publicFlag: FormUtil.createField({
          value: _.get(query, 'publicFlag')
        }),
        registrator: FormUtil.createField({
          value: _.get(query, 'registrator.userId')
        })
      }))
    })
  }

  handleClickContainer() {
    this.setState({
      selection: null
    })
  }

  handleSelectedPluginRowChange(row) {
    if (row) {
      this.setState({
        selection: {
          type: 'plugin',
          id: row.id
        }
      })
    }
  }

  handleSelectedQueryRowChange(row) {
    if (row) {
      this.setState({
        selection: {
          type: 'query',
          id: row.id
        }
      })
    }
  }

  render() {
    logger.log(logger.DEBUG, 'ToolSearch.render')

    if (!this.state.loaded) {
      return null
    }

    const { selection } = this.state

    return (
      <GridContainer onClick={this.handleClickContainer}>
        <PluginsGrid
          id={ids.PLUGINS_GRID_ID}
          rows={this.state.plugins}
          onSelectedRowChange={this.handleSelectedPluginRowChange}
          selectedRowId={
            selection && selection.type === 'plugin' ? selection.id : null
          }
        />
        <QueriesGrid
          id={ids.QUERIES_GRID_ID}
          rows={this.state.queries}
          onSelectedRowChange={this.handleSelectedQueryRowChange}
          selectedRowId={
            selection && selection.type === 'query' ? selection.id : null
          }
        />
      </GridContainer>
    )
  }
}

export default ToolSearch
