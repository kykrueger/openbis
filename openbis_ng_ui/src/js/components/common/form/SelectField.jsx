import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import MenuItem from '@material-ui/core/MenuItem'
import FormField from './FormField.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class SelectFormField extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'SelectFormField.render')

    const { value, disabled, options } = this.props

    return (
      <FormField {...this.props}>
        <TextField select value={value} disabled={disabled} fullWidth={true}>
          {options &&
            options.map(option => (
              <MenuItem key={option.value} value={option.value}>
                {option.label || option.value}
              </MenuItem>
            ))}
        </TextField>
      </FormField>
    )
  }
}

export default withStyles(styles)(SelectFormField)
