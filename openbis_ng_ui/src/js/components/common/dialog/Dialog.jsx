import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'

import Dialog from '@material-ui/core/Dialog'
import DialogActions from '@material-ui/core/DialogActions'
import DialogContent from '@material-ui/core/DialogContent'
import DialogContentText from '@material-ui/core/DialogContentText'
import DialogTitle from '@material-ui/core/DialogTitle'
import Slide from '@material-ui/core/Slide'
import logger from '../../../common/logger.js'

const ANIMATION_TIME_MS = 250

const styles = theme => ({
  actions: {
    marginLeft: theme.spacing(2),
    marginRight: theme.spacing(2),
    marginBottom: theme.spacing(1)
  }
})

const Transition = React.forwardRef(function Transition(props, ref) {
  return (
    <Slide
      ref={ref}
      direction='up'
      timeout={{ enter: ANIMATION_TIME_MS, exit: ANIMATION_TIME_MS }}
      {...props}
    />
  )
})

class DialogWindow extends React.Component {
  constructor(props) {
    super(props)
    this.handleClose = this.handleClose.bind(this)
  }

  handleClose() {
    const { onClose } = this.props
    if (onClose) {
      setTimeout(onClose, ANIMATION_TIME_MS)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'DialogWindow.render')

    const { open, title, content, actions, classes } = this.props

    return (
      <Dialog
        open={open}
        onClose={this.handleClose}
        scroll='paper'
        fullWidth={true}
        maxWidth='md'
        TransitionComponent={Transition}
      >
        <DialogTitle>{_.isFunction(title) ? title(this) : title}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {_.isFunction(content) ? content(this) : content}
          </DialogContentText>
        </DialogContent>
        <DialogActions classes={{ root: classes.actions }}>
          {_.isFunction(actions) ? actions(this) : actions}
        </DialogActions>
      </Dialog>
    )
  }
}

export default withStyles(styles)(DialogWindow)
