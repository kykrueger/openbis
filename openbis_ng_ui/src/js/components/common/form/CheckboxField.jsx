import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Checkbox from '@material-ui/core/Checkbox'
import FormField from './FormField.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class CheckboxFormField extends React.PureComponent {
  constructor(props) {
    super(props)
    this.reference = React.createRef()
  }

  render() {
    logger.log(logger.DEBUG, 'CheckboxFormField.render')

    const {
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
        labelPlacement='right'
        description={description}
        mandatory={mandatory}
        metadata={metadata}
        styles={styles}
        onClick={onClick}
      >
        <Checkbox
          inputRef={reference}
          value={name}
          checked={value}
          disabled={disabled}
          onChange={onChange}
          onFocus={onFocus}
        />
      </FormField>
    )
  }
}

export default withStyles(styles)(CheckboxFormField)
