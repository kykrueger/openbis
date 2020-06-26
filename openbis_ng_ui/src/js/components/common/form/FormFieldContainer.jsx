import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import FormControl from '@material-ui/core/FormControl'
import FormHelperText from '@material-ui/core/FormHelperText'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    overflow: 'hidden'
  },
  metadataDefault: {
    fontSize: theme.typography.label.fontSize,
    flex: '0 0 auto',
    margin: 0,
    marginBottom: theme.spacing(1) / 2,
    color: theme.palette.grey.main
  },
  controlDefault: {
    flex: '0 0'
  },
  descriptionDefault: {
    fontSize: theme.typography.label.fontSize
  },
  errorDefault: {
    fontSize: theme.typography.label.fontSize,
    color: theme.palette.error.main
  }
})

class FormFieldContainer extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'FormField.render')

    const { onClick, styles = {}, classes } = this.props

    return (
      <FormControl
        fullWidth={true}
        margin='none'
        classes={{ root: classes.container }}
      >
        <div onClick={onClick} className={styles.container}>
          {this.renderMetadata()}
          {this.renderControl()}
          {this.renderError()}
          {this.renderDescription()}
        </div>
      </FormControl>
    )
  }

  renderMetadata() {
    const { metadata, classes, styles = {} } = this.props

    if (metadata) {
      return (
        <pre
          data-part='metadata'
          className={`${classes.metadataDefault} ${styles.metadata}`}
        >
          {metadata}
        </pre>
      )
    } else {
      return null
    }
  }

  renderControl() {
    const { children, classes, styles = {} } = this.props
    return (
      <div
        data-part='control'
        className={`${classes.controlDefault} ${styles.control}`}
      >
        {children}
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

  renderError() {
    const { error, classes, styles = {} } = this.props

    if (error) {
      return (
        <FormHelperText>
          <span
            data-part='error'
            className={`${classes.errorDefault} ${styles.error}`}
          >
            {error}
          </span>
        </FormHelperText>
      )
    } else {
      return null
    }
  }
}

export default withStyles(styles)(FormFieldContainer)
