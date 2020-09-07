import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import openbis from '@src/js/services/openbis.js'
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
      level: React.createRef(),
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
        {this.renderMessageVisible()}
        {this.renderLevel(role)}
        {this.renderSpace(role)}
        {this.renderProject(role)}
        {this.renderRole(role)}
      </Container>
    )
  }

  renderMessageVisible() {
    const { classes, selectedRow } = this.props

    if (selectedRow && !selectedRow.visible) {
      return (
        <div className={classes.field}>
          <Message type='warning'>
            The selected role is currently not visible in the role list due to
            the chosen filtering and paging.
          </Message>
        </div>
      )
    } else {
      return null
    }
  }

  renderLevel(role) {
    const { visible, enabled, error, value } = { ...role.level }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props

    const options = [
      { value: openbis.RoleLevel.INSTANCE },
      { value: openbis.RoleLevel.SPACE },
      { value: openbis.RoleLevel.PROJECT }
    ]

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.level}
          label='Level'
          name='level'
          error={error}
          disabled={!enabled}
          mandatory={true}
          value={value}
          options={options}
          sort={false}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderSpace(role) {
    const { visible, enabled, error, value } = { ...role.space }

    if (!visible) {
      return null
    }

    const { mode, classes, controller } = this.props
    const { spaces } = controller.getDictionaries()

    let options = []

    if (spaces) {
      options = spaces.map(space => {
        return {
          label: space.code,
          value: space.code
        }
      })
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.space}
          label='Space'
          name='space'
          error={error}
          disabled={!enabled}
          mandatory={true}
          value={value}
          options={options}
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

    const { mode, classes, controller } = this.props
    const { projects } = controller.getDictionaries()

    let options = []

    if (projects) {
      projects.forEach(project => {
        if (project.space.code === role.space.value) {
          options.push({
            label: project.identifier.identifier,
            value: project.identifier.identifier
          })
        }
      })
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.project}
          label='Project'
          name='project'
          error={error}
          disabled={!enabled}
          mandatory={true}
          value={value}
          options={options}
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

    const options = []

    if (role.level.value === openbis.RoleLevel.INSTANCE) {
      options.push({ value: openbis.Role.ADMIN })
      options.push({ value: openbis.Role.OBSERVER })
    } else if (
      role.level.value === openbis.RoleLevel.SPACE ||
      role.level.value === openbis.RoleLevel.PROJECT
    ) {
      options.push({ value: openbis.Role.ADMIN })
      options.push({ value: openbis.Role.POWER_USER })
      options.push({ value: openbis.Role.USER })
      options.push({ value: openbis.Role.OBSERVER })
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.role}
          label='Role'
          name='role'
          error={error}
          disabled={!enabled}
          mandatory={true}
          value={value}
          options={options}
          sort={false}
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
