import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Checkbox from '@material-ui/core/Checkbox'
import FormField from './FormField.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class CheckboxFormField extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'CheckboxFormField.render')

    const {
      reference: inputRef,
      name,
      label,
      description,
      value,
      mandatory,
      disabled,
      action,
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
        <Checkbox
          inputRef={inputRef}
          value={name}
          checked={value}
          disabled={disabled}
          action={action}
          onChange={onChange}
          onFocus={onFocus}
        />
      </FormField>
    )
  }
}

export default withStyles(styles)(CheckboxFormField)
