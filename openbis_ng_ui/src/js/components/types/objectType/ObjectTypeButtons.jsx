import React from 'react'
import Button from '@material-ui/core/Button'
import { withStyles } from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = theme => ({
  container: {
    padding: theme.spacing(2),
    display: 'flex'
  },
  button: {
    marginRight: theme.spacing(2)
  }
})

class ObjectTypeButtons extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'ObjectTypeButtons.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='secondary'
          onClick={this.props.onAddSection}
        >
          Add Section
        </Button>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='secondary'
          onClick={this.props.onAddProperty}
        >
          Add Property
        </Button>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='primary'
          onClick={this.props.onSave}
        >
          Save
        </Button>
      </div>
    )
  }
}

export default withStyles(styles)(ObjectTypeButtons)
