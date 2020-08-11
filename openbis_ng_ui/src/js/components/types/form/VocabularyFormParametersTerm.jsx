import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
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

class VocabularyFormParametersTerm extends React.PureComponent {
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
    const term = this.getTerm(this.props)
    if (term && this.props.selection) {
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
    const term = this.getTerm(this.props)
    this.props.onChange('term', {
      id: term.id,
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    const term = this.getTerm(this.props)
    this.props.onSelectionChange('term', {
      id: term.id,
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'VocabularyFormParametersTerm.render')

    const term = this.getTerm(this.props)
    if (!term) {
      return null
    }

    const { classes } = this.props

    return (
      <Container>
        <TypeFormHeader className={classes.header}>Term</TypeFormHeader>
        {this.renderMessageVisible(term)}
        {this.renderCode(term)}
        {this.renderLabel(term)}
        {this.renderDescription(term)}
        {this.renderOfficial(term)}
      </Container>
    )
  }

  renderMessageVisible() {
    const { classes, selectedRow } = this.props

    if (selectedRow && !selectedRow.visible) {
      return (
        <div className={classes.field}>
          <Message type='warning'>
            The selected term is currently not visible in the term list due to
            the chosen filtering and paging.
          </Message>
        </div>
      )
    } else {
      return null
    }
  }

  renderCode(term) {
    const { visible, enabled, error, value } = { ...term.code }

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

  renderLabel(term) {
    const { visible, enabled, error, value } = { ...term.label }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.label}
          label='Label'
          name='label'
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

  renderDescription(term) {
    const { visible, enabled, error, value } = { ...term.description }

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

  renderOfficial(term) {
    const { visible, enabled, error, value } = { ...term.official }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.official}
          label='Official'
          name='official'
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

  getTerm(props) {
    let { terms, selection } = props

    if (selection && selection.type === 'term') {
      let [term] = terms.filter(term => term.id === selection.params.id)
      return term
    } else {
      return null
    }
  }
}

export default withStyles(styles)(VocabularyFormParametersTerm)
