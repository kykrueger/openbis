import React from 'react'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import UserFormSelectionType from '@src/js/components/users/form/UserFormSelectionType.js'
import logger from '@src/js/common/logger.js'

class UserFormButtons extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'UserFormButtons.render')

    const { mode, onEdit, onSave, onCancel, changed, user } = this.props

    return (
      <PageButtons
        mode={mode}
        changed={changed}
        onEdit={onEdit}
        onSave={onSave}
        onCancel={user.id ? onCancel : null}
        renderAdditionalButtons={classes =>
          this.renderAdditionalButtons(classes)
        }
      />
    )
  }

  renderAdditionalButtons(classes) {
    const { onAddGroup, onAddRole, onRemove } = this.props

    return (
      <React.Fragment>
        <Button
          name='addGroup'
          label='Add Group'
          styles={{ root: classes.button }}
          onClick={onAddGroup}
        />
        <Button
          name='addRole'
          label='Add Role'
          styles={{ root: classes.button }}
          onClick={onAddRole}
        />
        <Button
          name='remove'
          label='Remove'
          styles={{ root: classes.button }}
          disabled={!this.isGroupOrRoleSelected()}
          onClick={onRemove}
        />
      </React.Fragment>
    )
  }

  isGroupOrRoleSelected() {
    const { selection, roles } = this.props

    if (!selection) {
      return false
    }

    if (selection.type === UserFormSelectionType.GROUP) {
      return true
    }

    if (selection.type === UserFormSelectionType.ROLE) {
      const role = roles.find(role => role.id === selection.params.id)
      return role && !role.inheritedFrom.value
    }

    return false
  }
}

export default UserFormButtons
