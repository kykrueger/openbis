import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    display: 'flex'
  },
  leftContainer: {
    flexGrow: 1,
    display: 'flex',
    justifyContent: 'flex-start',
    '& $button': {
      marginRight: theme.spacing(1)
    },
    alignItems: 'center'
  },
  rightContainer: {
    flexGrow: 1,
    display: 'flex',
    justifyContent: 'flex-end',
    '& $button': {
      marginLeft: theme.spacing(1)
    },
    alignItems: 'center'
  },
  button: {
    whiteSpace: 'nowrap'
  }
})

class FormButtons extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'FormButtons.render')

    const { mode } = this.props

    if (mode === 'view') {
      return this.renderView()
    } else if (mode === 'edit') {
      return this.renderEdit()
    } else {
      throw 'Unsupported mode: ' + mode
    }
  }

  renderView() {
    const { classes, onEdit } = this.props

    return (
      <Container className={classes.container}>
        <div className={classes.rightContainer}>
          {onEdit && (
            <Button
              name='edit'
              label='Edit'
              styles={{ root: classes.button }}
              onClick={onEdit}
            />
          )}
        </div>
      </Container>
    )
  }

  renderEdit() {
    const {
      classes,
      onSave,
      onCancel,
      changed,
      renderAdditionalButtons
    } = this.props

    const additionalButtons = renderAdditionalButtons
      ? renderAdditionalButtons()
      : null

    return (
      <Container className={classes.container}>
        <div className={classes.leftContainer}>{additionalButtons}</div>
        <div className={classes.rightContainer}>
          {changed && (
            <Message type='warning'>You have unsaved changes.</Message>
          )}
          {onSave && (
            <Button
              name='save'
              label='Save'
              type='final'
              styles={{ root: classes.button }}
              onClick={onSave}
            />
          )}
          {onCancel && (
            <Button
              name='cancel'
              label='Cancel'
              styles={{ root: classes.button }}
              onClick={onCancel}
            />
          )}
        </div>
      </Container>
    )
  }
}

export default withStyles(styles)(FormButtons)
