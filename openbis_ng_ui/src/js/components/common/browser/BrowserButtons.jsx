import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Button from '@src/js/components/common/form/Button.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`,
    display: 'flex',
    borderWidth: '1px 0px 0px 0px',
    borderColor: theme.palette.background.secondary,
    borderStyle: 'solid'
  },
  button: {
    marginRight: theme.spacing(1),
    whiteSpace: 'nowrap'
  }
})

class BrowserButtons extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'BrowserButtons.render')

    const { controller, classes, addEnabled, removeEnabled } = this.props

    return (
      <div className={classes.container}>
        <Button
          label='Add'
          classes={{ root: classes.button }}
          onClick={controller.nodeAdd}
          disabled={!addEnabled}
        />
        <Button
          label='Remove'
          classes={{ root: classes.button }}
          onClick={controller.nodeRemove}
          disabled={!removeEnabled}
        />
      </div>
    )
  }
}

export default withStyles(styles)(BrowserButtons)
