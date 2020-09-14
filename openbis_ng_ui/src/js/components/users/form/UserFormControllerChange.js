import _ from 'lodash'
import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import UserFormControllerRecalculateInheritedRoles from '@src/js/components/users/form/UserFormControllerRecalculateInheritedRoles.js'
import UserFormSelectionType from '@src/js/components/users/form/UserFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class UserFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === UserFormSelectionType.USER) {
      await this._handleChangeUser(params)
    } else if (type === UserFormSelectionType.GROUP) {
      await this._handleChangeGroup(params)
    } else if (type === UserFormSelectionType.ROLE) {
      await this._handleChangeRole(params)
    }
  }

  async _handleChangeUser(params) {
    await this.context.setState(state => {
      const { newObject } = FormUtil.changeObjectField(
        state.user,
        params.field,
        params.value
      )
      return {
        user: newObject
      }
    })
    await this.controller.changed(true)
  }

  async _handleChangeGroup(params) {
    await this.context.setState(state => {
      const newState = { ...state }

      const {
        newCollection,
        oldObject,
        newObject
      } = FormUtil.changeCollectionItemField(
        state.groups,
        params.id,
        params.field,
        params.value
      )
      newState.groups = newCollection

      this._handleChangeGroupCode(oldObject, newObject, newState)

      return newState
    })

    if (this.controller.groupsGridController) {
      await this.controller.groupsGridController.showSelectedRow()
    }

    await this.controller.changed(true)
  }

  _handleChangeGroupCode(oldGroup, newGroup, newState) {
    const oldCode = oldGroup.code.value
    const newCode = newGroup.code.value

    if (oldCode !== newCode) {
      const { groups: groupDefinitions } = this.controller.getDictionaries()

      const groupDefinition = groupDefinitions.find(
        groupDefinition => groupDefinition.code === newCode
      )

      _.assign(newGroup, {
        description: {
          ...newGroup.description,
          value: groupDefinition.description
        }
      })

      new UserFormControllerRecalculateInheritedRoles(this.controller).execute(
        newState
      )
    }
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
