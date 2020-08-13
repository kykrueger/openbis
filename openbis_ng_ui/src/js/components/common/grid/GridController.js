import _ from 'lodash'
import autoBind from 'auto-bind'
import openbis from '@src/js/services/openbis.js'
import ids from '@src/js/common/consts/ids.js'

export default class GridController {
  constructor() {
    autoBind(this)
  }

  init(context) {
    const props = context.getProps()

    const columns = props.columns.map(column => ({
      ...column,
      label: column.label || _.upperFirst(column.field),
      render: column.render || (row => this._getValue(row, column.field)),
      sort: column.sort === undefined ? true : column.sort,
      visible: true
    }))

    context.initState({
      loaded: false,
      filters: {},
      page: 0,
      pageSize: 10,
      columns,
      allRows: [],
      filteredRows: [],
      sortedRows: [],
      currentRows: [],
      selectedRowId: null,
      selectedRow: null,
      sort: null,
      sortDirection: null
    })

    this.context = context
  }

  load() {
    this._loadSettings().then(() => {
      const { rows, selectedRowId } = this.context.getProps()
      this.updateAllRows(rows)
      this.updateSelectedRowId(selectedRowId)
      this.context.setState(() => ({
        loaded: true
      }))
    })
  }

  _loadSettings() {
    const props = this.context.getProps()
    const state = this.context.getState()

    let id = new openbis.PersonPermId(props.session.userName)
    let fo = new openbis.PersonFetchOptions()
    fo.withWebAppSettings(ids.WEB_APP_ID).withAllSettings()

    return openbis.getPersons([id], fo).then(map => {
      let person = map[id]
      let webAppSettings = person.webAppSettings[ids.WEB_APP_ID]
      if (webAppSettings && webAppSettings.settings) {
        let gridSettings = webAppSettings.settings[props.id]
        if (gridSettings) {
          let settings = JSON.parse(gridSettings.value)
          if (settings) {
            let newColumns = [...state.columns]
            newColumns.sort((c1, c2) => {
              let index1 = _.findIndex(settings.columns, ['field', c1.field])
              let index2 = _.findIndex(settings.columns, ['field', c2.field])
              return index1 - index2
            })
            newColumns = newColumns.map(column => {
              let setting = _.find(settings.columns, ['field', column.field])
              if (setting) {
                return {
                  ...column,
                  visible: setting.visible
                }
              } else {
                return column
              }
            })
            this.context.setState(() => ({
              ...settings,
              columns: newColumns
            }))
          }
        }
      }
    })
  }

  _saveSettings() {
    const props = this.context.getProps()
    const state = this.context.getState()

    let columns = state.columns.map(column => ({
      field: column.field,
      visible: column.visible
    }))

    let settings = {
      pageSize: state.pageSize,
      sort: state.sort,
      sortDirection: state.sortDirection,
      columns
    }

    let gridSettings = new openbis.WebAppSettingCreation()
    gridSettings.setName(props.id)
    gridSettings.setValue(JSON.stringify(settings))

    let update = new openbis.PersonUpdate()
    update.setUserId(new openbis.PersonPermId(props.session.userName))
    update.getWebAppSettings(ids.WEB_APP_ID).add(gridSettings)

    openbis.updatePersons([update])
  }

  async updateAllRows(rows) {
    await this.context.setState({
      allRows: rows
    })
    await this._recalculateCurrentRows()
  }

  async updateSelectedRowId(selectedRowId) {
    await this.context.setState({
      selectedRowId
    })
    await this._recalculateSelectedRow()
  }

  async _recalculateCurrentRows() {
    const {
      allRows,
      columns,
      filters,
      sort,
      sortDirection,
      page,
      pageSize,
      selectedRowId
    } = this.context.getState()

    const filteredRows = this._filter(allRows, columns, filters)
    const sortedRows = this._sort(filteredRows, columns, sort, sortDirection)
    const currentRows = this._page(sortedRows, page, pageSize)

    await this.context.setState({
      filteredRows,
      sortedRows,
      currentRows
    })
    if (selectedRowId) {
      await this._recalculateSelectedRow()
    }
  }

  async _recalculateSelectedRow() {
    const { selectedRowId, selectedRow, currentRows } = this.context.getState()
    const { onSelectedRowChange } = this.context.getProps()

    let newSelectedRow = null

    if (selectedRowId) {
      const visible = _.some(
        currentRows,
        currentRow => currentRow.id === selectedRowId
      )
      newSelectedRow = {
        id: selectedRowId,
        visible
      }
    }

    if (!_.isEqual(selectedRow, newSelectedRow)) {
      await this.context.setState({
        selectedRow: newSelectedRow
      })
      if (onSelectedRowChange) {
        await onSelectedRowChange(newSelectedRow)
      }
    }
  }

  async showSelectedRow() {
    const { selectedRow, sortedRows, page, pageSize } = this.context.getState()

    if (!selectedRow) {
      return
    }

    const index = _.findIndex(sortedRows, ['id', selectedRow.id])

    if (index === -1) {
      return
    }

    const newPage = Math.floor(index / pageSize)

    if (newPage !== page) {
      await this.context.setState({
        page: newPage
      })
      await this._recalculateCurrentRows()
    }
  }

  handleFilterChange(column, filter) {
    const state = this.context.getState()

    let filters = {
      ...state.filters,
      [column]: filter
    }

    this.context
      .setState(() => ({
        page: 0,
        filters
      }))
      .then(() => {
        this._recalculateCurrentRows()
      })
  }

  handleColumnVisibleChange(field) {
    const state = this.context.getState()

    let columns = state.columns.map(column => {
      if (column.field === field) {
        return {
          ...column,
          visible: !column.visible
        }
      } else {
        return column
      }
    })

    this.context
      .setState(() => ({
        columns
      }))
      .then(() => {
        this._saveSettings()
      })
  }

  handleColumnOrderChange(sourceIndex, destinationIndex) {
    const state = this.context.getState()

    let columns = [...state.columns]
    let source = columns[sourceIndex]
    columns.splice(sourceIndex, 1)
    columns.splice(destinationIndex, 0, source)

    this.context
      .setState(() => ({
        columns
      }))
      .then(() => {
        this._saveSettings()
      })
  }

  handleSortChange(column) {
    if (!column.sort) {
      return
    }
    this.context
      .setState(prevState => ({
        sort: column.field,
        sortDirection: prevState.sortDirection === 'asc' ? 'desc' : 'asc'
      }))
      .then(() => {
        this._saveSettings()
        this._recalculateCurrentRows()
      })
  }

  handlePageChange(page) {
    this.context
      .setState(() => ({
        page
      }))
      .then(() => {
        this._recalculateCurrentRows()
      })
  }

  handlePageSizeChange(pageSize) {
    this.context
      .setState(() => ({
        page: 0,
        pageSize
      }))
      .then(() => {
        this._saveSettings()
        this._recalculateCurrentRows()
      })
  }

  handleRowSelect(row) {
    const { selectedRow } = this.context.getState()

    if (selectedRow && selectedRow.id === row.id) {
      this.updateSelectedRowId(null)
    } else {
      this.updateSelectedRowId(row.id)
    }
  }

  _filter(rows, columns, filters) {
    function matches(value, filter) {
      if (filter) {
        return value
          ? value.trim().toUpperCase().includes(filter.trim().toUpperCase())
          : false
      } else {
        return true
      }
    }

    return _.filter([...rows], row => {
      let matchesAll = true
      columns.forEach(column => {
        let value = this._getValue(row, column.field)
        let filter = filters[column.field]
        matchesAll = matchesAll && matches(value, filter)
      })
      return matchesAll
    })
  }

  _sort(rows, columns, sort, sortDirection) {
    if (sort) {
      let column = _.find(columns, ['field', sort])
      if (column) {
        return rows.sort((t1, t2) => {
          let sign = sortDirection === 'asc' ? 1 : -1
          let v1 = this._getValue(t1, column.field)
          let v2 = this._getValue(t2, column.field)
          return sign * v1.localeCompare(v2)
        })
      }
    }

    return rows
  }

  _page(rows, page, pageSize) {
    return rows.slice(
      page * pageSize,
      Math.min(rows.length, (page + 1) * pageSize)
    )
  }

  _getValue(row, field) {
    const value = _.get(row, field)
    if (value !== null && value !== undefined) {
      return String(value)
    } else {
      return ''
    }
  }

  getSelectedRow() {
    const { selectedRow } = this.context.getState()
    return selectedRow
  }
}
