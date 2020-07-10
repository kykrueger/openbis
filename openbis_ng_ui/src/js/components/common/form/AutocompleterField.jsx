import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Autocomplete from '@material-ui/lab/Autocomplete'
import TextField from '@material-ui/core/TextField'
import logger from '@src/js/common/logger.js'

import FormFieldContainer from './FormFieldContainer.jsx'
import FormFieldLabel from './FormFieldLabel.jsx'
import FormFieldView from './FormFieldView.jsx'

const styles = theme => ({
  paper: {
    margin: 0
  },
  textField: {
    margin: 0
  },
  input: {
    fontSize: theme.typography.body2.fontSize
  },
  option: {
    fontSize: theme.typography.body2.fontSize
  }
})

class AutocompleterFormField extends React.PureComponent {
  static defaultProps = {
    mode: 'edit',
    variant: 'filled'
  }

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
    const { label, value } = this.props
    return <FormFieldView label={label} value={value} />
  }

  renderEdit() {
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
      variant,
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
            paper: classes.paper,
            option: classes.option
          }}
          renderInput={params => (
            <TextField
              {...params}
              inputRef={this.getReference()}
              InputProps={{
                ...params.InputProps,
                classes: {
                  ...params.InputProps.classes,
                  input: classes.input
                }
              }}
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
              variant={variant}
              margin='dense'
              classes={{
                root: classes.textField
              }}
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
