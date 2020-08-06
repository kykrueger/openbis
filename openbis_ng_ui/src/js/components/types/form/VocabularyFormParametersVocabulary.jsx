import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import logger from '@src/js/common/logger.js'

import TypeFormHeader from './TypeFormHeader.jsx'

const styles = theme => ({
  header: {
    paddingBottom: theme.spacing(1)
  },
  field: {
    paddingBottom: theme.spacing(1)
  }
})

class VocabularyFormParametersVocabulary extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      code: React.createRef(),
      description: React.createRef()
    }
    this.handleChange = this.handleChange.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
  }

  componentDidMount() {
    this.focus()
  }

  componentDidUpdate(prevProps) {
    const prevSelection = prevProps.selection
    const selection = this.props.selection

    if (prevSelection !== selection) {
      this.focus()
    }
  }

  focus() {
    const type = this.getVocabulary(this.props)
    if (type && this.props.selection) {
      const { part } = this.props.selection.params
      if (part) {
        const reference = this.references[part]
        if (reference && reference.current) {
          reference.current.focus()
        }
      }
    }
  }

  handleChange(event) {
    this.props.onChange('vocabulary', {
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    this.props.onSelectionChange('vocabulary', {
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'VocabularyFormParametersVocabulary.render')

    const vocabulary = this.getVocabulary(this.props)
    if (!vocabulary) {
      return null
    }

    const { classes } = this.props

    return (
      <Container>
        <TypeFormHeader className={classes.header}>Vocabulary</TypeFormHeader>
        {this.renderCode(vocabulary)}
        {this.renderDescription(vocabulary)}
      </Container>
    )
  }

  renderCode(vocabulary) {
    const { visible, enabled, error, value } = { ...vocabulary.code }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.code}
          label='Code'
          name='code'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderDescription(vocabulary) {
    const { visible, enabled, error, value } = { ...vocabulary.description }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.description}
          label='Description'
          name='description'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  getVocabulary(props) {
    let { vocabulary, selection } = props

    if (!selection || selection.type === 'vocabulary') {
      return vocabulary
    } else {
      return null
    }
  }
}

export default withStyles(styles)(VocabularyFormParametersVocabulary)
