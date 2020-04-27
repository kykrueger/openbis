import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import MenuItem from '@material-ui/core/MenuItem'
import logger from '@src/js/common/logger.js'

import FormFieldContainer from './FormFieldContainer.jsx'
import FormFieldLabel from './FormFieldLabel.jsx'

const styles = () => ({
  option: {
    '&:after': {
      content: '"\\00a0"'
    }
  }
})

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
      onBlur,
      classes
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
          value={value || ''}
          error={!!error}
          disabled={disabled}
          onChange={onChange}
          onFocus={onFocus}
          onBlur={onBlur}
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
}

export default withStyles(styles)(SelectFormField)
