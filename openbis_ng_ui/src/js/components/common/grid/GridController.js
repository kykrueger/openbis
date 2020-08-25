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

    const columns = []
    let initialSort = null
    let initialSortDirection = null

    props.columns.forEach(column => {
      if (column.sort) {
        initialSort = column.field
        initialSortDirection = column.sort
      }
      columns.push({
        ...column,
        field: column.field,
        label: column.label || _.upperFirst(column.field),
        render: column.render || (row => this._getValue(row, column.field)),
        sortable: column.sortable === undefined ? true : column.sortable,
        visible: true
      })
    })

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
      selectedRow: null,
      sort: initialSort,
      sortDirection: initialSortDirection
    })

    this.context = context
  }

  async load() {
    const { rows, selectedRowId } = this.context.getProps()

    await this._loadSettings()
    await this.updateAllRows(rows)
    await this.updateSelectedRowId(selectedRowId)

    await this.context.setState(() => ({
      loaded: true
    }))
  }

  _loadSettings() {
    const props = this.context.getProps()
    const state = this.context.getState()

    if (!props.session) {
      return Promise.resolve()
    }

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

    if (!props.session) {
      return Promise.resolve()
    }

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
    await this._recalculateCurrentRows(rows)
  }

  async updateSelectedRowId(selectedRowId) {
    await this._recalculateSelectedRow(selectedRowId)
  }

  async _recalculateCurrentRows(rows) {
    const {
      allRows,
      columns,
      filters,
      sort,
      sortDirection,
      page,
      pageSize
    } = this.context.getState()

    if (!rows) {
      rows = allRows
    }

    const filteredRows = this._filter(rows, columns, filters)

    const pageCount = Math.max(Math.ceil(filteredRows.length / pageSize), 1)
    const newPage = Math.min(page, pageCount - 1)

    const sortedRows = this._sort(filteredRows, columns, sort, sortDirection)
    const currentRows = this._page(sortedRows, newPage, pageSize)

    await this.context.setState({
      allRows: rows,
      filteredRows,
      sortedRows,
      currentRows,
      page: newPage
    })

    const { selectedRow } = this.context.getState()

    if (selectedRow) {
      await this._recalculateSelectedRow(selectedRow.id)
    }
  }

  async _recalculateSelectedRow(selectedRowId) {
    const { selectedRow, currentRows } = this.context.getState()
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
    if (!column.sortable) {
      return
    }

    this.context
      .setState(state => {
        if (column.field === state.sort) {
          return {
            sortDirection: state.sortDirection === 'asc' ? 'desc' : 'asc'
          }
        } else {
          return {
            sort: column.field,
            sortDirection: 'asc'
          }
        }
      })
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
      const column = _.find(columns, ['field', sort])
      if (column) {
        const collator = new Intl.Collator(undefined, {
          numeric: true,
          sensitivity: 'base'
        })
        return rows.sort((t1, t2) => {
          let sign = sortDirection === 'asc' ? 1 : -1
          let v1 = this._getValue(t1, column.field)
          let v2 = this._getValue(t2, column.field)
          return sign * collator.compare(v1, v2)
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
