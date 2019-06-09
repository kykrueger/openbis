import _ from 'lodash'
import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import IconButton from '@material-ui/core/IconButton'
import FirstPageIcon from '@material-ui/icons/FirstPage'
import KeyboardArrowLeft from '@material-ui/icons/KeyboardArrowLeft'
import KeyboardArrowRight from '@material-ui/icons/KeyboardArrowRight'
import LastPageIcon from '@material-ui/icons/LastPage'
import logger from '../../../common/logger.js'

const styles = () => ({
  buttons: {
    flexShrink: 0
  }
})

class PaginationButtons extends React.Component {

  constructor(props){
    super(props)
    this.handleFirstPageButtonClick = this.handleFirstPageButtonClick.bind(this)
    this.handleBackButtonClick = this.handleBackButtonClick.bind(this)
    this.handleNextButtonClick = this.handleNextButtonClick.bind(this)
    this.handleLastPageButtonClick = this.handleLastPageButtonClick.bind(this)
  }

  handleFirstPageButtonClick() {
    this.props.onChangePage(0)
  }

  handleBackButtonClick() {
    this.props.onChangePage(this.props.page - 1)
  }

  handleNextButtonClick() {
    this.props.onChangePage(this.props.page + 1)
  }

  handleLastPageButtonClick() {
    this.props.onChangePage(Math.max(0, Math.ceil(this.props.count / this.props.rowsPerPage) - 1))
  }

  render() {
    logger.log(logger.DEBUG, 'PaginationButtons.render')

    const { classes, count, page, rowsPerPage } = this.props

    return (
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
          disabled={page >= Math.ceil(count / rowsPerPage) - 1}
          aria-label="Next Page"
        >
          <KeyboardArrowRight />
        </IconButton>
        <IconButton
          onClick={this.handleLastPageButtonClick}
          disabled={page >= Math.ceil(count / rowsPerPage) - 1}
          aria-label="Last Page"
        >
          <LastPageIcon />
        </IconButton>
      </div>
    )
  }
}

export default _.flow(
  withStyles(styles)
)(PaginationButtons)
