import _ from 'lodash'
import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import IconButton from '@material-ui/core/IconButton'
import FirstPageIcon from '@material-ui/icons/FirstPage'
import KeyboardArrowLeft from '@material-ui/icons/KeyboardArrowLeft'
import KeyboardArrowRight from '@material-ui/icons/KeyboardArrowRight'
import LastPageIcon from '@material-ui/icons/LastPage'
import logger from '../../../common/logger.js'

const styles = () => ({
  container: {
    display: 'flex'
  },
  buttons: {
    flexShrink: 0
  }
})

class PageConfig extends React.Component {

  constructor(props){
    super(props)
    this.handlePageSizeChange = this.handlePageSizeChange.bind(this)
    this.handleFirstPageButtonClick = this.handleFirstPageButtonClick.bind(this)
    this.handleBackButtonClick = this.handleBackButtonClick.bind(this)
    this.handleNextButtonClick = this.handleNextButtonClick.bind(this)
    this.handleLastPageButtonClick = this.handleLastPageButtonClick.bind(this)
  }

  handlePageSizeChange(event){
    this.props.onPageSizeChange(event.target.value)
  }

  handleFirstPageButtonClick() {
    this.props.onPageChange(0)
  }

  handleBackButtonClick() {
    this.props.onPageChange(this.props.page - 1)
  }

  handleNextButtonClick() {
    this.props.onPageChange(this.props.page + 1)
  }

  handleLastPageButtonClick() {
    this.props.onPageChange(Math.max(0, Math.ceil(this.props.count / this.props.pageSize) - 1))
  }

  render() {
    logger.log(logger.DEBUG, 'PageConfig.render')

    const { classes, count, page, pageSize} = this.props

    return (
      <div className={classes.container}>
        <div>
          Rows per page:
          <TextField
            select
            SelectProps={{
              native: true,
            }}
            value={pageSize}
            onChange={this.handlePageSizeChange}
          >
            {[5, 10, 20, 50, 100].map(pageSize => (
              <option key={pageSize} value={pageSize}>{pageSize}</option>
            ))}
          </TextField>
        </div>
        <div>
          {page * pageSize + 1}-{Math.min(count, (page + 1) * pageSize)} of {count}
        </div>
        <div className={classes.buttons}>
          <IconButton
            onClick={this.handleFirstPageButtonClick}
            disabled={page === 0}
            aria-label="First Page"
          >
            <FirstPageIcon />
          </IconButton>
          <IconButton onClick={this.handleBackButtonClick} disabled={page === 0} aria-label="Previous Page">
            <KeyboardArrowLeft />
          </IconButton>
          <IconButton
            onClick={this.handleNextButtonClick}
            disabled={page >= Math.ceil(count / pageSize) - 1}
            aria-label="Next Page"
          >
            <KeyboardArrowRight />
          </IconButton>
          <IconButton
            onClick={this.handleLastPageButtonClick}
            disabled={page >= Math.ceil(count / pageSize) - 1}
            aria-label="Last Page"
          >
            <LastPageIcon />
          </IconButton>
        </div>
      </div>
    )
  }

}

export default _.flow(
  withStyles(styles)
)(PageConfig)
