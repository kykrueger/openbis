import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  labelDefault: {
    marginRight: theme.spacing(1)
  },
  mandatoryDefault: {
    fontWeight: 'bold',
    color: theme.palette.error.main
  }
})

class FormFieldLabel extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'FormFieldLabel.render')

    const { label, mandatory, styles = {}, classes } = this.props

    return (
      <span className={classes.container}>
        {label && (
          <span
            data-part='label'
            className={`${classes.labelDefault} ${styles.label}`}
          >
            {label}
          </span>
        )}
        {mandatory && (
          <span
            data-part='mandatory'
            className={`${classes.mandatoryDefault} ${styles.mandatory}`}
          >
            *
          </span>
        )}
      </span>
    )
  }
}

export default withStyles(styles)(FormFieldLabel)
