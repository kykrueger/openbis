import TextFieldWrapper from '@srcTest/js/common/wrapper/TextFieldWrapper.js'
import SelectFieldWrapper from '@srcTest/js/common/wrapper/SelectFieldWrapper.js'
import AutocompleterFieldWrapper from '@srcTest/js/common/wrapper/AutocompleterFieldWrapper.js'
import TypeFormParametersCommonWrapper from './TypeFormParametersCommonWrapper.js'
import TypeFormMessageWrapper from './TypeFormMessageWrapper.js'

export default class TypeFormParametersPropertyWrapper extends TypeFormParametersCommonWrapper {
  constructor(wrapper) {
    super(wrapper)
  }

  getMessages() {
    const messages = []
    this.wrapper.find('TypeFormMessage').forEach(message => {
      messages.push(new TypeFormMessageWrapper(message))
    })
    return messages
  }

  getScope() {
    return new SelectFieldWrapper(
      this.wrapper.find('SelectFormField[name="scope"]')
    )
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

  getDataType() {
    return new SelectFieldWrapper(
      this.wrapper.find('SelectFormField[name="dataType"]')
    )
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

  getPlugin() {
    return new SelectFieldWrapper(
      this.wrapper.find('SelectFormField[name="plugin"]')
    )
  }

  getVocabulary() {
    return new SelectFieldWrapper(
      this.wrapper.find('SelectFormField[name="vocabulary"]')
    )
  }

  getMaterialType() {
    return new SelectFieldWrapper(
      this.wrapper.find('SelectFormField[name="materialType"]')
    )
  }

  getSchema() {
    return new TextFieldWrapper(
      this.wrapper.find('TextFormField[name="schema"]')
    )
  }

  getTransformation() {
    return new TextFieldWrapper(
      this.wrapper.find('TextFormField[name="transformation"]')
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      messages: this.getMessages().map(message => message.toJSON()),
      scope: this.getScope().toJSON(),
      code: this.getCode() ? this.getCode().toJSON() : null,
      dataType: this.getDataType().toJSON(),
      label: this.getLabel().toJSON(),
      description: this.getDescription().toJSON(),
      plugin: this.getPlugin().toJSON(),
      vocabulary: this.getVocabulary().toJSON(),
      materialType: this.getMaterialType().toJSON(),
      schema: this.getSchema().toJSON(),
      transformation: this.getTransformation().toJSON()
    }
  }
}
