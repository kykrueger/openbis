import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Button from '@src/js/components/common/form/Button.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`,
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
          name='addSection'
          label='Add Section'
          classes={{ root: classes.button }}
          onClick={onAddSection}
        />
        <Button
          name='addProperty'
          label='Add Property'
          classes={{ root: classes.button }}
          disabled={!this.isSectionOrPropertySelected()}
          onClick={onAddProperty}
        />
        <Button
          name='remove'
          label='Remove'
          classes={{ root: classes.button }}
          disabled={!this.isSectionOrPropertySelected()}
          onClick={onRemove}
        />
        <Button
          name='save'
          label='Save'
          final={true}
          classes={{ root: classes.button }}
          onClick={onSave}
        />
      </div>
    )
  }
}

export default withStyles(styles)(TypeFormButtons)
