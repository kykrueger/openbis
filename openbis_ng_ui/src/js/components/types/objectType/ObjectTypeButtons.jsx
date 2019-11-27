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

    const {
      classes,
      onAddSection,
      onAddProperty,
      onRemove,
      onSave,
      addSectionEnabled,
      addPropertyEnabled,
      removeEnabled,
      saveEnabled
    } = this.props

    return (
      <div className={classes.container}>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='secondary'
          disabled={!addSectionEnabled}
          onClick={onAddSection}
        >
          Add Section
        </Button>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='secondary'
          disabled={!addPropertyEnabled}
          onClick={onAddProperty}
        >
          Add Property
        </Button>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='secondary'
          disabled={!removeEnabled}
          onClick={onRemove}
        >
          Remove
        </Button>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='primary'
          disabled={!saveEnabled}
          onClick={onSave}
        >
          Save
        </Button>
      </div>
    )
  }
}

export default withStyles(styles)(ObjectTypeButtons)
