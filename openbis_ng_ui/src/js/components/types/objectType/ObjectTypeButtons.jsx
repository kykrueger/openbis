import React from 'react'
import Button from '@material-ui/core/Button'
import { withStyles } from '@material-ui/core/styles'
import ObjectTypeButtonsRemoveDialog from './ObjectTypeButtonsRemoveDialog.jsx'
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
  constructor(props) {
    super(props)
    this.state = {
      removeDialogOpen: false
    }
    this.handleRemove = this.handleRemove.bind(this)
    this.handleRemoveConfirmed = this.handleRemoveConfirmed.bind(this)
    this.handleRemoveCanceled = this.handleRemoveCanceled.bind(this)
  }

  handleRemove() {
    const { removeConfirmationEnabled } = this.props

    if (removeConfirmationEnabled) {
      this.setState({
        removeDialogOpen: true
      })
    } else {
      this.props.onRemove()
    }
  }

  handleRemoveConfirmed() {
    this.setState({
      removeDialogOpen: false
    })
    this.props.onRemove()
  }

  handleRemoveCanceled() {
    this.setState({
      removeDialogOpen: false
    })
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeButtons.render')

    const {
      classes,
      onAddSection,
      onAddProperty,
      onSave,
      addSectionEnabled,
      addPropertyEnabled,
      removeEnabled,
      saveEnabled
    } = this.props

    const { removeDialogOpen } = this.state

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
          onClick={this.handleRemove}
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

        <ObjectTypeButtonsRemoveDialog
          open={removeDialogOpen}
          onConfirm={this.handleRemoveConfirmed}
          onCancel={this.handleRemoveCanceled}
        />
      </div>
    )
  }
}

export default withStyles(styles)(ObjectTypeButtons)
