import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    display: 'flex'
  },
  button: {
    marginRight: theme.spacing(1),
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
        <Button
          name='edit'
          label='Edit'
          styles={{ root: classes.button }}
          onClick={onEdit}
        />
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
      onCancel
    } = this.props

    return (
      <Container className={classes.container}>
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
        <Button
          name='save'
          label='Save'
          type='final'
          styles={{ root: classes.button }}
          onClick={onSave}
        />
        <Button
          name='cancel'
          label='Cancel'
          styles={{ root: classes.button }}
          onClick={onCancel}
        />
      </Container>
    )
  }
}

export default withStyles(styles)(TypeFormButtons)
