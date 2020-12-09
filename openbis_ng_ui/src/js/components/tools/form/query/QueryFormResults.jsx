import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Loading from '@src/js/components/common/loading/Loading.jsx'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class QueryFormResults extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
  }

  componentDidMount() {
    this.generateKey()
  }

  componentDidUpdate(prevProps) {
    if (this.props.results !== prevProps.results) {
      this.generateKey()
    }
  }

  render() {
    logger.log(logger.DEBUG, 'QueryFormResults.render')

    const { results } = this.props
    const { key } = this.state

    if (results) {
      const { loading, loaded, tableModel } = results
      return (
        <Loading loading={loading}>
          {loaded && (
            <GridContainer>
              <Grid
                key={key}
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
    return tableModel.rows
  }

  generateKey() {
    this.setState({
      key: Date.now()
    })
  }
}

export default withStyles(styles)(QueryFormResults)
