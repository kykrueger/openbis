import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import VocabularyFormSelectionType from '@src/js/components/types/form/VocabularyFormSelectionType.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
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
    this.props.onChange(VocabularyFormSelectionType.TERM, {
      id: term.id,
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    const term = this.getTerm(this.props)
    this.props.onSelectionChange(VocabularyFormSelectionType.TERM, {
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

    return (
      <Container>
        <Header>Term</Header>
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
          description='Unofficial (aka ad-hoc) terms can be created by regular users from the non-admin UI. Once verified a term can be made official by an admin. WARNING: Official terms cannot be made unofficial again.'
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

    if (selection && selection.type === VocabularyFormSelectionType.TERM) {
      let [term] = terms.filter(term => term.id === selection.params.id)
      return term
    } else {
      return null
    }
  }
}

export default withStyles(styles)(VocabularyFormParametersTerm)
