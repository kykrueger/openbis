import _ from 'lodash'
import PageControllerSave from '@src/js/components/common/page/PageControllerSave.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class UserFormControllerSave extends PageControllerSave {
  async save() {
    const state = this.context.getState()

    const user = FormUtil.trimFields({ ...state.user })
    const groups = state.groups

    const operations = []

    if (user.original) {
      if (this._isUserUpdateNeeded(user)) {
        operations.push(this._updateUserOperation(user))
      }
    } else {
      operations.push(this._createUserOperation(user))
    }

    state.original.groups.forEach(originalGroup => {
      const group = _.find(groups, ['id', originalGroup.id])
      if (!group) {
        operations.push(
          this._deleteGroupAssignmentOperation(user, originalGroup)
        )
      }
    })

    groups.forEach(group => {
      if (group.original) {
        if (this._isGroupAssignmentUpdateNeeded(group)) {
          operations.push(
            this._deleteGroupAssignmentOperation(user, group.original)
          )
          operations.push(this._createGroupAssignmentOperation(user, group))
        }
      } else {
        operations.push(this._createGroupAssignmentOperation(user, group))
      }
    })

    const options = new openbis.SynchronousOperationExecutionOptions()
    options.setExecuteInOrder(true)
    await this.facade.executeOperations(operations, options)

    return user.userId.value
  }

  _isUserUpdateNeeded(user) {
    return FormUtil.haveFieldsChanged(user, user.original, ['space', 'active'])
  }

  _isGroupAssignmentUpdateNeeded(group) {
    return FormUtil.haveFieldsChanged(group, group.original, ['code'])
  }

  _createUserOperation(user) {
    const creation = new openbis.PersonCreation()
    creation.setUserId(user.userId.value)
    if (user.space.value) {
      creation.setSpaceId(new openbis.SpacePermId(user.space.value))
    }
    return new openbis.CreatePersonsOperation([creation])
  }

  _updateUserOperation(user) {
    const update = new openbis.PersonUpdate()
    update.setUserId(new openbis.PersonPermId(user.userId.value))
    update.setSpaceId(
      user.space.value ? new openbis.SpacePermId(user.space.value) : null
    )
    update.active = user.active.value
    return new openbis.UpdatePersonsOperation([update])
  }

  _createGroupAssignmentOperation(user, group) {
    const update = new openbis.AuthorizationGroupUpdate()
    update.setAuthorizationGroupId(
      new openbis.AuthorizationGroupPermId(group.code.value)
    )
    update.getUserIds().add(new openbis.PersonPermId(user.userId.value))
    return new openbis.UpdateAuthorizationGroupsOperation([update])
  }

  _deleteGroupAssignmentOperation(user, group) {
    const update = new openbis.AuthorizationGroupUpdate()
    update.setAuthorizationGroupId(
      new openbis.AuthorizationGroupPermId(group.code.value)
    )
    update.getUserIds().remove(new openbis.PersonPermId(user.userId.value))
    return new openbis.UpdateAuthorizationGroupsOperation([update])
  }
}
