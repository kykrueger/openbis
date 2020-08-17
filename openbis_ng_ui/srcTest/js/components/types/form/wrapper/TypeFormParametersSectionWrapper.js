import TextField from '@src/js/components/common/form/TextField.jsx'
import TextFieldWrapper from '@srcTest/js/components/common/form/wrapper/TextFieldWrapper.js'
import TypeFormParametersCommonWrapper from './TypeFormParametersCommonWrapper.js'

export default class TypeFormParametersSectionWrapper extends TypeFormParametersCommonWrapper {
  getName() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'name' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      name: this.getName().toJSON()
    }
  }
}
