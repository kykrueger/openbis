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
    const { selection } = this.props
    return (
      selection &&
      (selection.type === UserFormSelectionType.GROUP ||
        selection.type === UserFormSelectionType.ROLE)
    )
  }
}

export default UserFormButtons
