import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import FormControl from '@material-ui/core/FormControl'
import FormHelperText from '@material-ui/core/FormHelperText'
import Typography from '@material-ui/core/Typography'
import logger from '../../../common/logger.js'

const styles = theme => ({
  labelContainer: {
    display: 'flex',
    alignItems: 'stretch',
    '& div': {
      flex: '0 0 auto'
    }
  },
  labelDefault: {
    margin: 0,
    marginRight: theme.spacing(1)
  },
  mandatoryDefault: {
    fontWeight: 'bold',
    color: theme.palette.error.main,
    margin: 0,
    marginRight: theme.spacing(1)
  },
  metadataDefault: {
    flex: '0 0 auto',
    margin: 0,
    color: theme.palette.grey.main
  },
  controlContainer: {
    width: '100%'
  },
  controlDefault: {},
  descriptionDefault: {}
})

class FormField extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'FormField.render')

    const { onClick, styles = {} } = this.props

    return (
      <div onClick={onClick} className={styles.container}>
        <FormControl fullWidth={true}>
          {this.renderLabel()}
          {this.renderControl()}
          {this.renderDescription()}
        </FormControl>
      </div>
    )
  }

  renderLabel() {
    const {
      label,
      mandatory = false,
      metadata,
      classes,
      styles = {}
    } = this.props

    if (label || mandatory || metadata) {
      return (
        <Typography component='label'>
          <div className={classes.labelContainer}>
            {(label || mandatory) && (
              <div>
                <span
                  data-part='label'
                  className={`${classes.labelDefault} ${styles.label}`}
                >
                  {label}
                </span>{' '}
                {mandatory && (
                  <b
                    data-part='mandatory'
                    className={`${classes.mandatoryDefault} ${styles.mandatory}`}
                  >
                    *
                  </b>
                )}
              </div>
            )}
            {metadata && (
              <pre
                data-part='metadata'
                className={`${classes.metadataDefault} ${styles.metadata}`}
              >
                {metadata}
              </pre>
            )}
          </div>
        </Typography>
      )
    } else {
      return null
    }
  }

  renderControl() {
    const { children, classes, styles = {} } = this.props
    return (
      <div data-part='control' className={classes.controlContainer}>
        <div className={`${classes.controlDefault} ${styles.control}`}>
          {children}
        </div>
      </div>
    )
  }

  renderDescription() {
    const { description, classes, styles = {} } = this.props

    if (description) {
      return (
        <FormHelperText>
          <span
            data-part='description'
            className={`${classes.descriptionDefault} ${styles.description}`}
          >
            {description}
          </span>
        </FormHelperText>
      )
    } else {
      return null
    }
  }
}

export default withStyles(styles)(FormField)
