import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import TableCell from '@material-ui/core/TableCell'
import TableSortLabel from '@material-ui/core/TableSortLabel'
import GridHeaderLabel from '@src/js/components/common/grid/GridHeaderLabel.jsx'
import GridHeaderFilter from '@src/js/components/common/grid/GridHeaderFilter.jsx'
import GridRow from '@src/js/components/common/grid/GridRow.jsx'

export default class GridWrapper extends BaseWrapper {
  getColumns() {
    const columns = this.wrapper.prop('columns')

    const sortLabels = this.findComponent(TableSortLabel)
    const labels = this.findComponent(GridHeaderLabel)
    const filters = this.findComponent(GridHeaderFilter)

    return columns.map((column, index) => {
      const label = labels.at(index).text()
      const filter = filters.at(index).text()
      const sort = sortLabels.at(index).prop('active')
      const sortDirection = sortLabels.at(index).prop('direction')

      return {
        field: this.getStringValue(column.field),
        label: this.getStringValue(label),
        filter: this.getStringValue(filter),
        sort: this.getBooleanValue(sort),
        sortDirection: this.getStringValue(sortDirection)
      }
    })
  }

  getRows() {
    const columns = this.wrapper.prop('columns')

    return this.findComponent(GridRow).map(row => {
      const values = {}

      this.findComponent(TableCell, row).forEach((cell, index) => {
        const column = columns[index]
        values[column.field] = this.getStringValue(cell.text())
      })

      return values
    })
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        columns: this.getColumns(),
        rows: this.getRows()
      }
    } else {
      return null
    }
  }
}
