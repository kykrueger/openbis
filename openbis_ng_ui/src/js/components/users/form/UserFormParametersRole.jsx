import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  field: {
    paddingBottom: theme.spacing(1)
  }
})

class UserFormParametersRole extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      space: React.createRef(),
      project: React.createRef(),
      role: React.createRef()
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
    const role = this.getRole(this.props)
    if (role && this.props.selection) {
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
    const role = this.getRole(this.props)
    this.props.onChange('role', {
      id: role.id,
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    const role = this.getRole(this.props)
    this.props.onSelectionChange('role', {
      id: role.id,
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'UserFormParametersRole.render')

    const role = this.getRole(this.props)
    if (!role) {
      return null
    }

    return (
      <Container>
        <Header>Role</Header>
        {this.renderSpace(role)}
        {this.renderProject(role)}
        {this.renderRole(role)}
      </Container>
    )
  }

  renderSpace(role) {
    const { visible, enabled, error, value } = { ...role.space }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.space}
          label='Space'
          name='space'
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

  renderProject(role) {
    const { visible, enabled, error, value } = { ...role.project }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.project}
          label='Project'
          name='project'
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

  renderRole(role) {
    const { visible, enabled, error, value } = { ...role.role }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.role}
          label='Role'
          name='role'
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

  getRole(props) {
    let { roles, selection } = props

    if (selection && selection.type === 'role') {
      let [role] = roles.filter(role => role.id === selection.params.id)
      return role
    } else {
      return null
    }
  }
}

export default withStyles(styles)(UserFormParametersRole)
