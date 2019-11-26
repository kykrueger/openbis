import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import ConfirmationDialog from '../../common/dialog/ConfirmationDialog.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class ObjectTypeButtonsRemoveDialog extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeButtonsRemoveDialog.render')

    const { open, onConfirm, onCancel } = this.props

    return (
      <ConfirmationDialog
        open={open}
        onConfirm={onConfirm}
        onCancel={onCancel}
        content='The property is used by some entities. Are you sure you want to remove it?'
      />
    )
  }
}

export default withStyles(styles)(ObjectTypeButtonsRemoveDialog)
