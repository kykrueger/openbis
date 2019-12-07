import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import FormFieldContainer from './FormFieldContainer.jsx'
import FormFieldLabel from './FormFieldLabel.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class SelectFormField extends React.PureComponent {
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
      onClick,
      onChange,
      onFocus,
      onBlur
    } = this.props

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
          inputRef={reference}
          label={
            <FormFieldLabel
              label={label}
              mandatory={mandatory}
              styles={styles}
            />
          }
          name={name}
          value={value}
          error={error !== null && error !== undefined}
          disabled={disabled}
          onChange={onChange}
          onFocus={onFocus}
          onBlur={onBlur}
          fullWidth={true}
          SelectProps={{
            native: true
          }}
          variant='filled'
        >
          {options &&
            options.map(option => (
              <option key={option.value} value={option.value}>
                {option.label || option.value}
              </option>
            ))}
        </TextField>
      </FormFieldContainer>
    )
  }
}

export default withStyles(styles)(SelectFormField)
