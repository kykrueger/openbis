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
          onClick={this.props.onAdd}
        >
          Add
        </Button>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='secondary'
          disabled={!this.props.removeEnabled}
          onClick={this.props.onRemove}
        >
          Remove
        </Button>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='primary'
          disabled={!this.props.saveEnabled}
          onClick={this.props.onSave}
        >
          Save
        </Button>
      </div>
    )
  }
}

export default withStyles(styles)(ObjectTypeButtons)
