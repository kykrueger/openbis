import React from 'react'
import Button from '@material-ui/core/Button'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    padding: theme.spacing(2),
    paddingLeft: theme.spacing(4),
    display: 'flex'
  },
  button: {
    marginRight: theme.spacing(2),
    whiteSpace: 'nowrap'
  }
})

class ObjectTypeButtons extends React.PureComponent {
  isSectionOrPropertySelected() {
    const { selection } = this.props
    return (
      selection &&
      (selection.type === 'property' || selection.type === 'section')
    )
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeButtons.render')

    const {
      classes,
      onAddSection,
      onAddProperty,
      onRemove,
      onSave
    } = this.props

    return (
      <div className={classes.container}>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='secondary'
          onClick={onAddSection}
        >
          Add Section
        </Button>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='secondary'
          disabled={!this.isSectionOrPropertySelected()}
          onClick={onAddProperty}
        >
          Add Property
        </Button>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='secondary'
          disabled={!this.isSectionOrPropertySelected()}
          onClick={onRemove}
        >
          Remove
        </Button>
        <Button
          classes={{ root: classes.button }}
          variant='contained'
          color='primary'
          onClick={onSave}
        >
          Save
        </Button>
      </div>
    )
  }
}

export default withStyles(styles)(ObjectTypeButtons)
