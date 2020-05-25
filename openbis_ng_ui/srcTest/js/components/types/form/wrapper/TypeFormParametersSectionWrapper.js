import TextFieldWrapper from '@srcTest/js/common/wrapper/TextFieldWrapper.js'
import TypeFormParametersCommonWrapper from './TypeFormParametersCommonWrapper.js'

export default class TypeFormParametersFormWrapper extends TypeFormParametersCommonWrapper {
  constructor(wrapper) {
    super(wrapper)
  }

  getName() {
    return new TextFieldWrapper(this.wrapper.find('TextFormField[name="name"]'))
  }

  change(fieldName, fieldValue) {
    this.wrapper.instance().handleChange({
      target: {
        name: fieldName,
        value: fieldValue
      }
    })
    this.wrapper.update()
  }

  toJSON() {
    return {
      ...super.toJSON(),
      name: this.getName().toJSON()
    }
  }
}
