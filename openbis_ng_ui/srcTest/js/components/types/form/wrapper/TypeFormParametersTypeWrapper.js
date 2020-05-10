import TextFieldWrapper from '@srcTest/js/common/wrapper/TextFieldWrapper.js'
import TypeFormParametersCommonWrapper from './TypeFormParametersCommonWrapper.js'

export default class TypeFormParametersTypeWrapper extends TypeFormParametersCommonWrapper {
  constructor(wrapper) {
    super(wrapper)
  }

  getCode() {
    return new TextFieldWrapper(this.wrapper.find('TextFormField[name="code"]'))
  }

  getDescription() {
    return new TextFieldWrapper(
      this.wrapper.find('TextFormField[name="description"]')
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      code: this.getCode().toJSON(),
      description: this.getDescription().toJSON()
    }
  }
}
