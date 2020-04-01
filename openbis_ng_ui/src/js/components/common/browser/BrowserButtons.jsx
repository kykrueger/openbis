import React from 'react'
import Button from '@material-ui/core/Button'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  button: {}
})

class BrowserButtons extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'BrowserButtons.render')

    const { controller, classes, addEnabled, removeEnabled } = this.props

    return (
      <div>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='secondary'
          onClick={controller.nodeAdd}
          disabled={!addEnabled}
        >
          Add
        </Button>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='secondary'
          onClick={controller.nodeRemove}
          disabled={!removeEnabled}
        >
          Remove
        </Button>
      </div>
    )
  }
}

export default withStyles(styles)(BrowserButtons)
