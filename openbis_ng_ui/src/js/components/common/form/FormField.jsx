import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import FormControl from '@material-ui/core/FormControl'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import FormHelperText from '@material-ui/core/FormHelperText'
import logger from '../../../common/logger.js'

const styles = theme => ({
  transparent: {
    opacity: 0.4
  },
  labelContainer: {
    margin: 0,
    alignItems: 'start'
  },
  labelLabel: {
    width: '100%'
  },
  labelText: {
    display: 'flex',
    alignItems: 'stretch',
    '& div': {
      flex: '0 0 auto'
    },
    '& b': {
      fontWeight: 'bold',
      color: theme.palette.error.main
    },
    '& pre': {
      flex: '0 0 auto',
      margin: 0,
      paddingLeft: theme.spacing(1),
      color: theme.palette.grey.main
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
              label: classes.labelLabel
            }}
          />
          <FormHelperText>{description}</FormHelperText>
        </FormControl>
      </div>
    )
  }

  renderLabel() {
    const { label, mandatory, metadata, classes } = this.props

    return (
      <div className={classes.labelText}>
        <div>
          {label} {mandatory && <b>*</b>}
        </div>
        {metadata && <pre>{metadata}</pre>}
      </div>
    )
  }
}

export default withStyles(styles)(FormField)
