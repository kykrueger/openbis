import autoBind from 'auto-bind'

import PageControllerChanged from './PageControllerChanged.js'
import PageControllerEdit from './PageControllerEdit.js'
import PageControllerCancel from './PageControllerCancel.js'
import PageControllerSelectionChange from './PageControllerSelectionChange.js'

export default class PageController {
  constructor(facade) {
    autoBind(this)
    this.facade = facade
  }

  init(context) {
    this.context = context
    this.object = context.getProps().object
  }

  getPage() {
    throw 'Method not implemented'
  }

  getNewObjectType() {
    throw 'Method not implemented'
  }

  getExistingObjectType() {
    throw 'Method not implemented'
  }

  load() {
    throw 'Method not implemented'
  }

  // eslint-disable-next-line no-unused-vars
  validate(autofocus) {
    throw 'Method not implemented'
  }

  handleEdit() {
    return new PageControllerEdit(this).execute()
  }

  handleCancel() {
    return new PageControllerCancel(this).execute()
  }

  handleSelectionChange(type, params) {
    return new PageControllerSelectionChange(this).execute(type, params)
  }

  handleBlur() {
    return this.validate()
  }

  changed(changed) {
    return new PageControllerChanged(this).execute(changed)
  }

  getFacade() {
    return this.facade
  }

  getContext() {
    return this.context
  }

  getObject() {
    return this.object
  }
}
