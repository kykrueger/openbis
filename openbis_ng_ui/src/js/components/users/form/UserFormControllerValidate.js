import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'

export default class UserFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { user, groups } = this.context.getState()

    const newUser = this._validateUser(validator, user)
    const newGroups = this._validateGroups(validator, groups)

    return {
      user: newUser,
      groups: newGroups
    }
  }

  async select(firstError) {
    const { user, groups } = this.context.getState()

    if (firstError.object === user) {
      await this.setSelection({
        type: 'user',
        params: {
          part: firstError.name
        }
      })
    } else if (groups.includes(firstError.object)) {
      await this.setSelection({
        type: 'group',
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      })

      if (this.controller.groupsGridController) {
        await this.controller.groupsGridController.showSelectedRow()
      }
    }
  }

  _validateUser(validator, user) {
    validator.validateNotEmpty(user, 'userId', 'User Id')
    return validator.withErrors(user)
  }

  _validateGroups(validator, groups) {
    groups.forEach(group => {
      this._validateGroup(validator, group)
    })
    return validator.withErrors(groups)
  }

  _validateGroup(validator, group) {
    validator.validateNotEmpty(group, 'code', 'Code')
  }
}
