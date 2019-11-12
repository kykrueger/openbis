import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Checkbox from '@material-ui/core/Checkbox'
import FormFieldContainer from './FormFieldContainer.jsx'
import FormFieldLabel from './FormFieldLabel.jsx'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import logger from '../../../common/logger.js'

const styles = () => ({})

class CheckboxFormField extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
  }

  handleChange(event) {
    this.handleEvent(event, this.props.onChange)
  }

  handleFocus(event) {
    this.handleEvent(event, this.props.onFocus)
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
      reference,
      name,
      label,
      description,
      value,
      mandatory,
      disabled,
      metadata,
      styles,
      onClick
    } = this.props

    return (
      <FormFieldContainer
        description={description}
        metadata={metadata}
        styles={styles}
        onClick={onClick}
      >
        <FormControlLabel
          control={
            <Checkbox
              inputRef={reference}
              value={name}
              checked={value}
              disabled={disabled}
              inputProps={{
                'data-part': 'label'
              }}
              onChange={this.handleChange}
              onFocus={this.handleFocus}
            />
          }
          label={
            <FormFieldLabel
              label={label}
              mandatory={mandatory}
              styles={styles}
            />
          }
        />
      </FormFieldContainer>
    )
  }
}

export default withStyles(styles)(CheckboxFormField)
