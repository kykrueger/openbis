import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import FormControl from '@material-ui/core/FormControl'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import FormHelperText from '@material-ui/core/FormHelperText'
import logger from '../../../common/logger.js'

const styles = () => ({
  transparent: {
    opacity: 0.4
  },
  labelContainer: {
    margin: 0,
    alignItems: 'start'
  },
  labelText: {
    '& b': {
      fontWeight: 'bold',
      color: 'red'
    }
  }
})

class FormField extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'FormField.render')

    const { description, transparent, children, classes } = this.props

    return (
      <div className={transparent ? classes.transparent : null}>
        <FormControl fullWidth={true}>
          <FormControlLabel
            label={this.renderLabel()}
            labelPlacement='top'
            control={children}
            classes={{
              root: classes.labelContainer,
              label: classes.labelText
            }}
          />
          <FormHelperText>{description}</FormHelperText>
        </FormControl>
      </div>
    )
  }

  renderLabel() {
    const { label, mandatory } = this.props

    return (
      <React.Fragment>
        {label} {mandatory && <b>*</b>}
      </React.Fragment>
    )
  }
}

export default withStyles(styles)(FormField)
