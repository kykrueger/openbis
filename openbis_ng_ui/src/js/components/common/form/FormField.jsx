import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import FormControl from '@material-ui/core/FormControl'
import FormHelperText from '@material-ui/core/FormHelperText'
import Typography from '@material-ui/core/Typography'
import logger from '../../../common/logger.js'

const styles = theme => ({
  containerHorizontal: {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center'
  },
  containerVertical: {
    display: 'flex',
    flexDirection: 'column'
  },
  labelDefault: {
    cursor: 'pointer',
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
    lineHeight: '20px',
    margin: 0,
    color: theme.palette.grey.main
  },
  controlDefault: {
    flex: '0 0'
  },
  descriptionDefault: {}
})

class FormField extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleLabelClick = this.handleLabelClick.bind(this)
  }

  handleLabelClick() {
    const { reference } = this.props
    if (reference) {
      reference.current.focus()
      reference.current.click()
    }
  }

  render() {
    logger.log(logger.DEBUG, 'FormField.render')

    const { labelPlacement = 'top' } = this.props

    if (labelPlacement === 'top') {
      return this.renderWithTopLabel()
    } else if (labelPlacement === 'right') {
      return this.renderWithRightLabel()
    } else {
      return null
    }
  }

  renderWithTopLabel() {
    const { onClick, styles = {}, classes } = this.props

    return (
      <div onClick={onClick} className={styles.container}>
        <FormControl fullWidth={true}>
          <div className={classes.containerVertical}>
            <div className={classes.containerHorizontal}>
              {this.renderLabel()}
              {this.renderMandatory()}
              {this.renderMetadata()}
            </div>
            {this.renderControl()}
            {this.renderDescription()}
          </div>
        </FormControl>
      </div>
    )
  }

  renderWithRightLabel() {
    const { onClick, styles = {}, classes } = this.props

    return (
      <div onClick={onClick} className={styles.container}>
        <FormControl fullWidth={true}>
          <div className={classes.containerVertical}>
            {this.renderMetadata()}
            <div className={classes.containerHorizontal}>
              {this.renderControl()}
              {this.renderLabel()}
              {this.renderMandatory()}
            </div>
            {this.renderDescription()}
          </div>
        </FormControl>
      </div>
    )
  }

  renderLabel() {
    const { label, classes, styles = {} } = this.props

    if (label) {
      return (
        <Typography component='label' onClick={this.handleLabelClick}>
          <span
            data-part='label'
            className={`${classes.labelDefault} ${styles.label}`}
          >
            {label}
          </span>
        </Typography>
      )
    } else {
      return null
    }
  }

  renderMandatory() {
    const { mandatory = false, classes, styles = {} } = this.props

    if (mandatory) {
      return (
        <b
          data-part='mandatory'
          className={`${classes.mandatoryDefault} ${styles.mandatory}`}
        >
          *
        </b>
      )
    } else {
      return null
    }
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
}

export default withStyles(styles)(FormField)
