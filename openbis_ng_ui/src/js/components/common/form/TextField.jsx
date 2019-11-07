import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import FormField from './FormField.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class SelectFormField extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'SelectFormField.render')

    const { type, value, disabled } = this.props

    return (
      <FormField {...this.props}>
        <TextField
          type={type}
          value={value}
          disabled={disabled}
          fullWidth={true}
        />
      </FormField>
    )
  }
}

export default withStyles(styles)(SelectFormField)
