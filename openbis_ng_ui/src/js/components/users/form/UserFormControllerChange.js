import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import openbis from '@src/js/services/openbis.js'

export default class UserFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === 'user') {
      const { field, value } = params
      this.changeObjectField('user', field, value)
    } else if (type === 'group') {
      const { id, field, value } = params
      await this.changeCollectionItemField(
        'groups',
        id,
        field,
        value,
        (oldGroup, newGroup) => {
          newGroup = this._handleChangeGroupCode(oldGroup, newGroup)
          return newGroup
        }
      )

      if (this.controller.groupsGridController) {
        await this.controller.groupsGridController.showSelectedRow()
      }
    } else if (type === 'role') {
      const { id, field, value } = params
      await this.changeCollectionItemField(
        'roles',
        id,
        field,
        value,
        (oldRole, newRole) => {
          newRole = this._handleChangeRoleLevel(oldRole, newRole)
          newRole = this._handleChangeRoleSpace(oldRole, newRole)
          return newRole
        }
      )

      if (this.controller.rolesGridController) {
        await this.controller.rolesGridController.showSelectedRow()
      }
    }
  }

  _handleChangeGroupCode(oldGroup, newGroup) {
    const oldCode = oldGroup.code.value
    const newCode = newGroup.code.value

    if (oldCode !== newCode) {
      const { groups } = this.controller.getDictionaries()

      const group = groups.find(group => group.code === newCode)

      newGroup = {
        ...newGroup,
        description: {
          ...newGroup.description,
          value: group !== null ? group.description : null
        }
      }
    }

    return newGroup
  }

  _handleChangeRoleLevel(oldRole, newRole) {
    const oldLevel = oldRole.level.value
    const newLevel = newRole.level.value

    if (oldLevel !== newLevel) {
      newRole = {
        ...newRole,
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
      }
    }

    return newRole
  }

  _handleChangeRoleSpace(oldRole, newRole) {
    const oldSpace = oldRole.space.value
    const newSpace = newRole.space.value

    if (oldSpace !== newSpace) {
      newRole = {
        ...newRole,
        project: {
          ...newRole.project,
          value: null
        },
        role: {
          ...newRole.role,
          value: null
        }
      }
    }

    return newRole
  }
}
