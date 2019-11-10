import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import FormField from './FormField.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class SelectFormField extends React.PureComponent {
  constructor(props) {
    super(props)
    this.reference = React.createRef()
  }

  render() {
    logger.log(logger.DEBUG, 'SelectFormField.render')

    const {
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

    const reference = this.props.reference
      ? this.props.reference
      : this.reference

    return (
      <FormField
        reference={reference}
        label={label}
        labelPlacement='top'
        description={description}
        mandatory={mandatory}
        metadata={metadata}
        styles={styles}
        onClick={onClick}
      >
        <TextField
          inputRef={reference}
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
