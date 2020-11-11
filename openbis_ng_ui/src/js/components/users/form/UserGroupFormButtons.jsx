import React from 'react'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import UserGroupFormSelectionType from '@src/js/components/users/form/UserGroupFormSelectionType.js'
import logger from '@src/js/common/logger.js'

class UserGroupFormButtons extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'UserFormButtons.render')

    const { mode, onEdit, onSave, onCancel, changed, group } = this.props

    return (
      <PageButtons
        mode={mode}
        changed={changed}
        onEdit={onEdit}
        onSave={onSave}
        onCancel={group.id ? onCancel : null}
        renderAdditionalButtons={classes =>
          this.renderAdditionalButtons(classes)
        }
      />
    )
  }

  renderAdditionalButtons(classes) {
    const { onAddUser, onAddRole, onRemove } = this.props

    return (
      <React.Fragment>
        <Button
          name='addUser'
          label='Add User'
          styles={{ root: classes.button }}
          onClick={onAddUser}
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
          disabled={!this.isUserOrRoleSelected()}
          onClick={onRemove}
        />
      </React.Fragment>
    )
  }

  isUserOrRoleSelected() {
    const { selection } = this.props

    if (!selection) {
      return false
    }

    if (selection.type === UserGroupFormSelectionType.USER) {
      return true
    }

    if (selection.type === UserGroupFormSelectionType.ROLE) {
      return true
    }

    return false
  }
}

export default UserGroupFormButtons
