import _ from 'lodash'
import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import UserGroupFormSelectionType from '@src/js/components/users/form/UserGroupFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class UserGroupFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === UserGroupFormSelectionType.GROUP) {
      await this._handleChangeGroup(params)
    } else if (type === UserGroupFormSelectionType.USER) {
      await this._handleChangeUser(params)
    } else if (type === UserGroupFormSelectionType.ROLE) {
      await this._handleChangeRole(params)
    }
  }

  async _handleChangeGroup(params) {
    await this.context.setState(state => {
      const { newObject } = FormUtil.changeObjectField(
        state.group,
        params.field,
        params.value
      )
      return {
        group: newObject
      }
    })
    await this.controller.changed(true)
  }

  async _handleChangeUser(params) {
    await this.context.setState(state => {
      const newState = { ...state }

      const { newCollection } = FormUtil.changeCollectionItemField(
        state.users,
        params.id,
        params.field,
        params.value
      )
      newState.users = newCollection

      return newState
    })

    if (this.controller.usersGridController) {
      await this.controller.usersGridController.showSelectedRow()
    }

    await this.controller.changed(true)
  }

  async _handleChangeRole(params) {
    await this.context.setState(state => {
      const {
        newCollection,
        oldObject,
        newObject
      } = FormUtil.changeCollectionItemField(
        state.roles,
        params.id,
        params.field,
        params.value
      )

      this._handleChangeRoleLevel(oldObject, newObject)
      this._handleChangeRoleSpace(oldObject, newObject)

      return {
        roles: newCollection
      }
    })

    if (this.controller.rolesGridController) {
      await this.controller.rolesGridController.showSelectedRow()
    }

    await this.controller.changed(true)
  }

  _handleChangeRoleLevel(oldRole, newRole) {
    const oldLevel = oldRole.level.value
    const newLevel = newRole.level.value

    if (oldLevel !== newLevel) {
      _.assign(newRole, {
        space: {
          ...newRole.space,
          visible:
            newLevel === openbis.RoleLevel.SPACE ||
            newLevel === openbis.RoleLevel.PROJECT,
          value: null
        },
        project: {
          ...newRole.project,
          visible: newLevel === openbis.RoleLevel.PROJECT,
          value: null
        },
        role: {
          ...newRole.role,
          visible: newLevel !== null,
          value: null
        }
      })
    }
  }

  _handleChangeRoleSpace(oldRole, newRole) {
    const oldSpace = oldRole.space.value
    const newSpace = newRole.space.value

    if (oldSpace !== newSpace) {
      _.assign(newRole, {
        project: {
          ...newRole.project,
          value: null
        },
        role: {
          ...newRole.role,
          value: null
        }
      })
    }
  }
}
