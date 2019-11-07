import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import FormField from './FormField.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class SelectFormField extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'SelectFormField.render')

    const {
      reference: inputRef,
      name,
      label,
      description,
      value,
      mandatory,
      disabled,
      options,
      metadata,
      styles,
      onClick,
      onChange,
      onFocus
    } = this.props

    return (
      <FormField
        label={label}
        description={description}
        mandatory={mandatory}
        metadata={metadata}
        styles={styles}
        onClick={onClick}
      >
        <TextField
          select
          inputRef={inputRef}
          name={name}
          value={value}
          disabled={disabled}
          onChange={onChange}
          onFocus={onFocus}
          fullWidth={true}
          SelectProps={{
            native: true
          }}
        >
          {options &&
            options.map(option => (
              <option key={option.value} value={option.value}>
                {option.label || option.value}
              </option>
            ))}
        </TextField>
      </FormField>
    )
  }
}

export default withStyles(styles)(SelectFormField)
