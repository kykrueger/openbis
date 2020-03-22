import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Checkbox from '@material-ui/core/Checkbox'
import Typography from '@material-ui/core/Typography'
import logger from '@src/js/common/logger.js'

import FormFieldContainer from './FormFieldContainer.jsx'
import FormFieldLabel from './FormFieldLabel.jsx'

const styles = () => ({
  container: {
    display: 'flex',
    alignItems: 'center'
  },
  label: {
    cursor: 'pointer'
  }
})

class CheckboxFormField extends React.PureComponent {
  constructor(props) {
    super(props)
    this.reference = React.createRef()
    this.action = null
    this.handleLabelClick = this.handleLabelClick.bind(this)
    this.handleChange = this.handleChange.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
  }

  handleLabelClick() {
    this.getReference().current.click()
  }

  handleChange(event) {
    this.handleEvent(event, this.props.onChange)
  }

  handleFocus(event) {
    this.handleEvent(event, this.props.onFocus)
    if (this.action) {
      this.action.focusVisible()
    }
  }

  handleEvent(event, handler) {
    if (handler) {
      const newEvent = {
        ...event,
        target: {
          ...event.target,
          name: event.target.value,
          value: event.target.checked
        }
      }
      delete newEvent.target.checked
      handler(newEvent)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'CheckboxFormField.render')

    const {
      name,
      label,
      description,
      value,
      mandatory,
      disabled,
      error,
      metadata,
      styles,
      classes,
      onClick
    } = this.props

    return (
      <FormFieldContainer
        description={description}
        error={error}
        metadata={metadata}
        styles={styles}
        onClick={onClick}
      >
        <div className={classes.container}>
          <Checkbox
            inputRef={this.getReference()}
            action={action => (this.action = action)}
            value={name}
            checked={!!value}
            disabled={disabled}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
          />
          <Typography
            component='label'
            className={classes.label}
            onClick={this.handleLabelClick}
          >
            <FormFieldLabel
              label={label}
              mandatory={mandatory}
              styles={styles}
            />
          </Typography>
        </div>
      </FormFieldContainer>
    )
  }

  getReference() {
    return this.props.reference ? this.props.reference : this.reference
  }
}

export default withStyles(styles)(CheckboxFormField)
