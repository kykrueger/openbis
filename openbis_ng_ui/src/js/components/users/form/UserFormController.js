import PageController from '@src/js/components/common/page/PageController.js'
import UserFormControllerLoad from './UserFormControllerLoad.js'
import UserFormControllerAddGroup from './UserFormControllerAddGroup.js'
import UserFormControllerAddRole from './UserFormControllerAddRole.js'
import UserFormControllerRemove from './UserFormControllerRemove.js'
import UserFormControllerValidate from './UserFormControllerValidate.js'
import UserFormControllerChange from './UserFormControllerChange.js'
import UserFormControllerSave from './UserFormControllerSave.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'

export default class UserFormController extends PageController {
  constructor(facade) {
    super(facade)
  }

  getPage() {
    return pages.USERS
  }

  getNewObjectType() {
    return objectTypes.NEW_USER
  }

  getExistingObjectType() {
    return objectTypes.USER
  }

  load() {
    return new UserFormControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new UserFormControllerValidate(this).execute(autofocus)
  }

  handleAddGroup() {
    return new UserFormControllerAddGroup(this).execute()
  }

  handleAddRole() {
    return new UserFormControllerAddRole(this).execute()
  }

  handleRemove() {
    return new UserFormControllerRemove(this).execute()
  }

  handleChange(type, params) {
    return new UserFormControllerChange(this).execute(type, params)
  }

  handleSave() {
    return new UserFormControllerSave(this).execute()
  }

  getDictionaries() {
    const { dictionaries } = this.context.getState()
    return dictionaries || {}
  }
}
