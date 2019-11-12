import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import FormFieldContainer from './FormFieldContainer.jsx'
import FormFieldLabel from './FormFieldLabel.jsx'
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
