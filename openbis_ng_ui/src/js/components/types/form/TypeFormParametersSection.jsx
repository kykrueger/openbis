import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@src/js/components/common/form/TextField.jsx'
import logger from '@src/js/common/logger.js'

import TypeFormHeader from './TypeFormHeader.jsx'

const styles = theme => ({
  container: {
    padding: theme.spacing(2)
  },
  header: {
    paddingBottom: theme.spacing(1)
  },
  field: {
    paddingBottom: theme.spacing(1)
  }
})

class TypeFormParametersSection extends React.PureComponent {
  constructor(props) {
    super(props)
    this.reference = React.createRef()
    this.handleChange = this.handleChange.bind(this)
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
    const section = this.getSection(this.props)
    if (section) {
      this.reference.current.focus()
    }
  }

  params(event) {
    const section = this.getSection(this.props)

    return {
      id: section.id,
      field: event.target.name,
      part: event.target.name,
      value: event.target.value
    }
  }

  handleChange(event) {
    this.props.onChange('section', this.params(event))
  }

  handleBlur(event) {
    this.props.onBlur('section', this.params(event))
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormParametersSection.render')

    const section = this.getSection(this.props)
    if (!section) {
      return null
    }

    const { classes } = this.props

    return (
      <div className={classes.container}>
        <TypeFormHeader className={classes.header}>Section</TypeFormHeader>
        {this.renderName(section)}
      </div>
    )
  }

  renderName(section) {
    const { visible, enabled, error, value } = { ...section.name }

    if (!visible) {
      return null
    }

    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.reference}
          label='Name'
          name='name'
          error={error}
          disabled={!enabled}
          value={value}
          onChange={this.handleChange}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  getSection(props) {
    let { sections, selection } = props

    if (selection && selection.type === 'section') {
      let [section] = sections.filter(
        section => section.id === selection.params.id
      )
      return section
    } else {
      return null
    }
  }
}

export default withStyles(styles)(TypeFormParametersSection)
