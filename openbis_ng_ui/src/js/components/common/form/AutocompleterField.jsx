import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Autocomplete from '@material-ui/lab/Autocomplete'
import TextField from '@material-ui/core/TextField'
import logger from '@src/js/common/logger.js'

import FormFieldContainer from './FormFieldContainer.jsx'
import FormFieldLabel from './FormFieldLabel.jsx'

const styles = () => ({
  paper: {
    margin: 0
  }
})

class AutocompleterFormField extends React.PureComponent {
  constructor(props) {
    super(props)
    this.reference = React.createRef()
    this.handleChange = this.handleChange.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
  }

  handleChange(event, value) {
    this.handleEvent(event, value, this.props.onChange)
  }

  handleFocus(event) {
    this.handleEvent(event, null, this.props.onFocus)
  }

  handleBlur(event) {
    this.handleEvent(event, null, this.props.onBlur)
  }

  handleEvent(event, value, handler) {
    if (handler) {
      const input = this.getReference().current
      const newEvent = {
        ...event,
        target: {
          ...input,
          name: this.props.name,
          value: value
        }
      }
      handler(newEvent)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'AutocompleterFormField.render')

    const {
      name,
      options,
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
        <Autocomplete
          freeSolo
          autoSelect
          autoComplete
          openOnFocus
          name={name}
          disabled={disabled}
          options={options}
          value={value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
          classes={{
            paper: classes.paper
          }}
          renderInput={params => (
            <TextField
              {...params}
              inputRef={this.getReference()}
              label={
                <FormFieldLabel
                  label={label}
                  mandatory={mandatory}
                  styles={styles}
                />
              }
              error={!!error}
              fullWidth={true}
              autoComplete='off'
              variant='filled'
              margin='dense'
            />
          )}
        />
      </FormFieldContainer>
    )
  }

  getReference() {
    return this.props.reference ? this.props.reference : this.reference
  }
}

export default withStyles(styles)(AutocompleterFormField)
