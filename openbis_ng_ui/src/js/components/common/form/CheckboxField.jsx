import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Checkbox from '@material-ui/core/Checkbox'
import FormField from './FormField.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class CheckboxFormField extends React.PureComponent {
  constructor(props) {
    super(props)
    this.reference = React.createRef()
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

    const reference = this.props.reference
      ? this.props.reference
      : this.reference

    return (
      <FormField
        reference={reference}
        label={label}
        labelPlacement='right'
        description={description}
        mandatory={mandatory}
        metadata={metadata}
        styles={styles}
        onClick={onClick}
      >
        <Checkbox
          inputRef={reference}
          value={name}
          checked={value}
          disabled={disabled}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
        />
      </FormField>
    )
  }
}

export default withStyles(styles)(CheckboxFormField)
