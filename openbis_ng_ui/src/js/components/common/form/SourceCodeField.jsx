import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles, withTheme } from '@material-ui/core/styles'
import { highlight, languages } from 'prismjs/components/prism-core.js'
import 'prismjs/components/prism-clike.js'
import 'prismjs/components/prism-python.js'
import 'prismjs/themes/prism.css'
import Editor from 'react-simple-code-editor'
import FormFieldContainer from '@src/js/components/common/form/FormFieldContainer.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  view: {
    fontFamily: theme.typography.sourceCode.fontFamily,
    fontSize: theme.typography.body2.fontSize,
    whiteSpace: 'pre-wrap',
    padding: theme.spacing(2),
    border: `1px solid ${theme.palette.border.secondary}`
  },

  edit: {
    backgroundColor: theme.palette.background.field,
    '& *': {
      background: 'none !important'
    },
    '& textarea': {
      border: `1px solid ${theme.palette.border.primary} !important`,
      borderBottom: `1px solid ${theme.palette.border.field} !important`,
      outline: 'none !important'
    },
    '& textarea:focus': {
      borderBottom: `2px solid ${theme.palette.primary.main} !important`
    }
  },
  error: {
    '&$edit textarea': {
      borderBottom: `2px solid ${theme.palette.error.main} !important`
    }
  },
  disabled: {
    '&$edit pre': {
      opacity: 0.5
    }
  }
})

class SourceCodeField extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  handleValueChange(value) {
    const { name, onChange } = this.props
    if (onChange) {
      onChange({
        target: {
          name,
          value
        }
      })
    }
  }

  render() {
    logger.log(logger.DEBUG, 'SourceCodeField.render')

    const { mode } = this.props

    if (mode === 'view') {
      return this.renderView()
    } else if (mode === 'edit') {
      return this.renderEdit()
    } else {
      throw 'Unsupported mode: ' + mode
    }
  }

  renderView() {
    const { value, classes } = this.props
    const html = { __html: highlight(value || '', languages.python) }

    return <div className={classes.view} dangerouslySetInnerHTML={html} />
  }

  renderEdit() {
    const {
      name,
      value,
      description,
      disabled,
      error,
      onClick,
      onFocus,
      onBlur,
      theme,
      styles,
      classes
    } = this.props
    return (
      <FormFieldContainer
        description={description}
        error={error}
        styles={styles}
        onClick={onClick}
      >
        <div
          className={`
            ${classes.edit} 
            ${error ? classes.error : ''} 
            ${disabled ? classes.disabled : ''}
          `}
        >
          <Editor
            name={name}
            value={value || ''}
            highlight={code => highlight(code, languages.python)}
            disabled={disabled}
            padding={this.props.theme.spacing(2)}
            style={{
              fontFamily: theme.typography.sourceCode.fontFamily,
              fontSize: this.props.theme.typography.body2.fontSize
            }}
            onValueChange={this.handleValueChange}
            onFocus={onFocus}
            onBlur={onBlur}
          />
        </div>
      </FormFieldContainer>
    )
  }
}

export default _.flow(withStyles(styles), withTheme)(SourceCodeField)
