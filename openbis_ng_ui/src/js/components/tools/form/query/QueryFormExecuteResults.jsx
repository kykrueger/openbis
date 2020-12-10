import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Loading from '@src/js/components/common/loading/Loading.jsx'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class QueryFormExecuteResults extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'QueryFormExecuteResults.render')

    const { results } = this.props

    if (results) {
      const { loading, loaded, tableModel, timestamp } = results
      return (
        <Loading loading={loading}>
          {loaded && (
            <GridContainer>
              <Grid
                key={timestamp}
                header='Results'
                columns={this.getColumns(tableModel)}
                rows={this.getRows(tableModel)}
              />
            </GridContainer>
          )}
        </Loading>
      )
    } else {
      return null
    }
  }

  getColumns(tableModel) {
    return tableModel.columns.map((column, index) => ({
      name: column.title,
      label: column.title,
      getValue: ({ row }) => row[index] && row[index].value
    }))
  }

  getRows(tableModel) {
    return tableModel.rows.map((row, index) => ({
      id: index,
      ...row
    }))
  }
}

export default withStyles(styles)(QueryFormExecuteResults)
