import React from 'react'
import TablePagination from '@material-ui/core/TablePagination'
import PaginationButtons from './PaginationButtons.jsx'
import logger from '../../../common/logger.js'

class Pagination extends React.Component {

  constructor(props){
    super(props)
    this.handlePageChange = this.handlePageChange.bind(this)
    this.handlePageSizeChange = this.handlePageSizeChange.bind(this)
  }

  handlePageChange(page){
    this.props.onPageChange(page)
  }

  handlePageSizeChange(event){
    this.props.onPageSizeChange(event.target.value)
  }

  render() {
    logger.log(logger.DEBUG, 'Pagination.render')

    const {count, page, pageSize} = this.props

    return (
      <TablePagination
        rowsPerPageOptions={[5, 10, 20, 50, 100]}
        count={count}
        rowsPerPage={pageSize}
        page={page}
        onChangePage={this.handlePageChange}
        onChangeRowsPerPage={this.handlePageSizeChange}
        SelectProps={{
          native: true,
        }}
        ActionsComponent={PaginationButtons}
      />
    )
  }

}

export default Pagination
