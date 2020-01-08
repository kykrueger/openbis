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

const styles = theme => ({
  dialog: {
    position: 'relative',
    zIndex: '20000 !important'
  },
  actions: {
    margin: theme.spacing(1)
  }
})

const Transition = React.forwardRef(function Transition(props, ref) {
  return <Slide ref={ref} direction='up' {...props} />
})

class DialogWindow extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'DialogWindow.render')

    const { open, title, content, actions, onClose, classes } = this.props

    return (
      <Dialog
        open={open}
        onClose={onClose}
        scroll='paper'
        fullWidth={true}
        maxWidth='md'
        classes={{ root: classes.dialog }}
        TransitionComponent={Transition}
      >
        <DialogTitle>{_.isFunction(title) ? title(this) : title}</DialogTitle>
        <DialogContent>
          <DialogContentText component='div'>
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
