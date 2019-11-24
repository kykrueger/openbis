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

class SelectFormField extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'SelectFormField.render')

    const {
      reference,
      type,
      name,
      label,
      description,
      value,
      mandatory,
      disabled,
      metadata,
      startAdornment,
      endAdornment,
      styles,
      classes,
      onClick,
      onChange,
      onFocus
    } = this.props

    return (
      <FormFieldContainer
        description={description}
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
          value={value}
          disabled={disabled}
          onChange={onChange}
          onFocus={onFocus}
          fullWidth={true}
          variant='filled'
        />
      </FormFieldContainer>
    )
  }
}

export default withStyles(styles)(SelectFormField)
