import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import logger from '@src/js/common/logger.js'

import TypeFormControllerStrategies from './TypeFormControllerStrategies.js'

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

class TypeFormButtons extends React.PureComponent {
  isSectionOrPropertySelected() {
    const { selection } = this.props
    return (
      selection &&
      (selection.type === 'property' || selection.type === 'section')
    )
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormButtons.render')

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
          <Button
            name='edit'
            label='Edit'
            styles={{ root: classes.button }}
            onClick={onEdit}
          />
        </div>
      </Container>
    )
  }

  renderEdit() {
    const {
      classes,
      onAddSection,
      onAddProperty,
      onRemove,
      onSave,
      onCancel,
      changed,
      object
    } = this.props

    const strategy = new TypeFormControllerStrategies().getStrategy(object.type)
    const existing = object.type === strategy.getExistingObjectType()

    return (
      <Container className={classes.container}>
        <div className={classes.leftContainer}>
          <Button
            name='addSection'
            label='Add Section'
            styles={{ root: classes.button }}
            onClick={onAddSection}
          />
          <Button
            name='addProperty'
            label='Add Property'
            styles={{ root: classes.button }}
            disabled={!this.isSectionOrPropertySelected()}
            onClick={onAddProperty}
          />
          <Button
            name='remove'
            label='Remove'
            styles={{ root: classes.button }}
            disabled={!this.isSectionOrPropertySelected()}
            onClick={onRemove}
          />
        </div>
        <div className={classes.rightContainer}>
          {changed && (
            <Message type='warning'>You have unsaved changes.</Message>
          )}
          <Button
            name='save'
            label='Save'
            type='final'
            styles={{ root: classes.button }}
            onClick={onSave}
          />
          {existing && (
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

export default withStyles(styles)(TypeFormButtons)
