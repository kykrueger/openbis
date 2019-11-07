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
      flex: '0 0 auto',
      paddingRight: theme.spacing(1)
    },
    '& b': {
      fontWeight: 'bold',
      color: theme.palette.error.main
    },
    '& pre': {
      flex: '0 0 auto',
      margin: 0,
      marginLeft: theme.spacing(1),
      color: theme.palette.grey.main
    }
  },
  controlContainer: {
    width: '100%'
  }
})

class FormField extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'FormField.render')

    const { description, onClick, styles = {} } = this.props

    return (
      <div onClick={onClick} className={styles.container}>
        <FormControl fullWidth={true}>
          <Typography component='label'>{this.renderLabel()}</Typography>
          <div>{this.renderControl()}</div>
          <FormHelperText>
            <span data-part='description' className={styles.description}>
              {description}
            </span>
          </FormHelperText>
        </FormControl>
      </div>
    )
  }

  renderLabel() {
    const { label, mandatory, metadata, classes, styles = {} } = this.props
    return (
      <div className={classes.labelContainer}>
        <div>
          <span data-part='label' className={styles.label}>
            {label}
          </span>{' '}
          {mandatory && (
            <b data-part='mandatory' className={styles.mandatory}>
              *
            </b>
          )}
        </div>
        <pre data-part='metadata' className={styles.metadata}>
          {metadata}
        </pre>
      </div>
    )
  }

  renderControl() {
    const { children, classes, styles = {} } = this.props
    return (
      <div data-part='control' className={classes.controlContainer}>
        <div className={styles.control}>{children}</div>
      </div>
    )
  }
}

export default withStyles(styles)(FormField)
