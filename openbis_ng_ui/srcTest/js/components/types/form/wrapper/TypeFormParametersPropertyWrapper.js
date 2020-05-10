import TextFieldWrapper from '@srcTest/js/common/wrapper/TextFieldWrapper.js'
import AutocompleterFieldWrapper from '@srcTest/js/common/wrapper/AutocompleterFieldWrapper.js'
import TypeFormParametersCommonWrapper from './TypeFormParametersCommonWrapper.js'

export default class TypeFormParametersFormWrapper extends TypeFormParametersCommonWrapper {
  constructor(wrapper) {
    super(wrapper)
  }

  getCode() {
    const textFieldWrapper = this.wrapper.find('TextFormField[name="code"]')
    if (textFieldWrapper.exists()) {
      return new TextFieldWrapper(textFieldWrapper)
    }
    const autocompleterFieldWrapper = this.wrapper.find(
      'AutocompleterFormField[name="code"]'
    )
    if (autocompleterFieldWrapper.exists()) {
      return new AutocompleterFieldWrapper(autocompleterFieldWrapper)
    }
    return null
  }

  getLabel() {
    return new TextFieldWrapper(
      this.wrapper.find('TextFormField[name="label"]')
    )
  }

  getDescription() {
    return new TextFieldWrapper(
      this.wrapper.find('TextFormField[name="description"]')
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      code: this.getCode() ? this.getCode().toJSON() : null,
      label: this.getLabel().toJSON(),
      description: this.getDescription().toJSON()
    }
  }
}
