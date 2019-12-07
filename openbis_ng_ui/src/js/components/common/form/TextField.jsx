import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import InputAdornment from '@material-ui/core/InputAdornment'
import FormFieldContainer from './FormFieldContainer.jsx'
import FormFieldLabel from './FormFieldLabel.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({
  startAdornment: {
    marginRight: 0
  },
  endAdornment: {
    marginLeft: 0
  }
})

class TextFormField extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'TextFormField.render')

    const {
      reference,
      type,
      name,
      label,
      description,
      value,
      mandatory,
      disabled,
      error,
      metadata,
      startAdornment,
      endAdornment,
      styles,
      classes,
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
          inputRef={reference}
          type={type}
          label={
            <FormFieldLabel
              label={label}
              mandatory={mandatory}
              styles={styles}
            />
          }
          InputProps={{
            startAdornment: startAdornment ? (
              <InputAdornment
                position='start'
                classes={{ positionStart: classes.startAdornment }}
              >
                {startAdornment}
              </InputAdornment>
            ) : null,
            endAdornment: endAdornment ? (
              <InputAdornment
                position='end'
                classes={{ positionEnd: classes.endAdornment }}
              >
                {endAdornment}
              </InputAdornment>
            ) : null
          }}
          name={name}
          value={value || ''}
          error={!!error}
          disabled={disabled}
          onChange={onChange}
          onFocus={onFocus}
          onBlur={onBlur}
          fullWidth={true}
          autoComplete='off'
          variant='filled'
        />
      </FormFieldContainer>
    )
  }
}

export default withStyles(styles)(TextFormField)
