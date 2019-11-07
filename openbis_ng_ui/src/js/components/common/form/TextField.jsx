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
      type,
      name,
      label,
      description,
      value,
      mandatory,
      disabled,
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
          inputRef={inputRef}
          type={type}
          name={name}
          value={value}
          disabled={disabled}
          onChange={onChange}
          onFocus={onFocus}
          fullWidth={true}
        />
      </FormField>
    )
  }
}

export default withStyles(styles)(SelectFormField)
