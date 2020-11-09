import PageController from '@src/js/components/common/page/PageController.js'
import DynamicPropertyPluginFormControllerLoad from '@src/js/components/tools/form/dynamicproperty/DynamicPropertyPluginFormControllerLoad.js'
import DynamicPropertyPluginFormControllerValidate from '@src/js/components/tools/form/dynamicproperty/DynamicPropertyPluginFormControllerValidate.js'
import DynamicPropertyPluginFormControllerChange from '@src/js/components/tools/form/dynamicproperty/DynamicPropertyPluginFormControllerChange.js'
import DynamicPropertyPluginFormControllerSave from '@src/js/components/tools/form/dynamicproperty/DynamicPropertyPluginFormControllerSave.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'

export default class DynamicPropertyPluginFormController extends PageController {
  constructor(facade) {
    super(facade)
  }

  getPage() {
    return pages.TOOLS
  }

  getNewObjectType() {
    return objectTypes.NEW_DYNAMIC_PROPERTY_PLUGIN
  }

  getExistingObjectType() {
    return objectTypes.DYNAMIC_PROPERTY_PLUGIN
  }

  load() {
    return new DynamicPropertyPluginFormControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new DynamicPropertyPluginFormControllerValidate(this).execute(
      autofocus
    )
  }

  handleChange(type, params) {
    return new DynamicPropertyPluginFormControllerChange(this).execute(
      type,
      params
    )
  }

  handleSave() {
    return new DynamicPropertyPluginFormControllerSave(this).execute()
  }
}
