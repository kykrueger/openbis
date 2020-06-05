import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import MenuItem from '@material-ui/core/MenuItem'
import logger from '@src/js/common/logger.js'

import FormFieldContainer from './FormFieldContainer.jsx'
import FormFieldLabel from './FormFieldLabel.jsx'

const styles = () => ({
  textField: {
    margin: 0
  },
  option: {
    '&:after': {
      content: '"\\00a0"'
    }
  }
})

class SelectFormField extends React.PureComponent {
  constructor(props) {
    super(props)
    this.inputReference = React.createRef()
    this.handleFocus = this.handleFocus.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
  }

  handleFocus(event) {
    this.handleEvent(event, this.props.onFocus)
  }

  handleBlur(event) {
    this.handleEvent(event, this.props.onBlur)
  }

  handleEvent(event, handler) {
    if (handler) {
      const newEvent = {
        ...event,
        target: {
          name: this.props.name,
          value: this.props.value
        }
      }
      handler(newEvent)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'SelectFormField.render')

    const {
      reference,
      name,
      label,
      description,
      value,
      mandatory,
      disabled,
      error,
      options,
      metadata,
      styles,
      onChange,
      onClick,
      classes
    } = this.props

    this.fixReference(reference)

    return (
      <FormFieldContainer
        description={description}
        error={error}
        metadata={metadata}
        styles={styles}
        onClick={onClick}
      >
        <TextField
          select
          inputRef={this.inputReference}
          label={
            <FormFieldLabel
              label={label}
              mandatory={mandatory}
              styles={styles}
            />
          }
          name={name}
          value={value || ''}
          error={!!error}
          disabled={disabled}
          onChange={onChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
          fullWidth={true}
          InputLabelProps={{ shrink: !!value }}
          SelectProps={{
            MenuProps: {
              getContentAnchorEl: null,
              anchorOrigin: {
                vertical: 'bottom',
                horizontal: 'left'
              }
            }
          }}
          variant='filled'
          margin='dense'
          classes={{
            root: classes.textField
          }}
        >
          {options &&
            options.map(option => (
              <MenuItem
                key={option.value || ''}
                value={option.value || ''}
                classes={{ root: classes.option }}
              >
                {option.label || option.value || ''}
              </MenuItem>
            ))}
        </TextField>
      </FormFieldContainer>
    )
  }

  fixReference(reference) {
    if (reference) {
      reference.current = {
        focus: () => {
          if (this.inputReference.current && this.inputReference.current.node) {
            const input = this.inputReference.current.node
            const div = input.previousSibling
            div.focus()
          }
        }
      }
    }
  }
}

export default withStyles(styles)(SelectFormField)
