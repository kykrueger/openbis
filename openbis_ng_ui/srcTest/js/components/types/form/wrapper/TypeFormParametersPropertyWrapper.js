import AutocompleterField from '@src/js/components/common/form/AutocompleterField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import TextFieldWrapper from '@srcTest/js/components/common/form/wrapper/TextFieldWrapper.js'
import SelectFieldWrapper from '@srcTest/js/components/common/form/wrapper/SelectFieldWrapper.js'
import AutocompleterFieldWrapper from '@srcTest/js/components/common/form/wrapper/AutocompleterFieldWrapper.js'
import MessageWrapper from '@srcTest/js/components/common/form/wrapper/MessageWrapper.js'
import TypeFormParametersCommonWrapper from './TypeFormParametersCommonWrapper.js'

export default class TypeFormParametersPropertyWrapper extends TypeFormParametersCommonWrapper {
  getMessages() {
    const messages = []
    this.findComponent(Message).forEach(message => {
      messages.push(new MessageWrapper(message))
    })
    return messages
  }

  getScope() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'scope' })
    )
  }

  getCode() {
    const textFieldWrapper = this.findComponent(TextField).filter({
      name: 'code'
    })

    if (textFieldWrapper.exists()) {
      return new TextFieldWrapper(textFieldWrapper)
    }

    const autocompleterFieldWrapper = this.findComponent(
      AutocompleterField
    ).filter({ name: 'code' })

    if (autocompleterFieldWrapper.exists()) {
      return new AutocompleterFieldWrapper(autocompleterFieldWrapper)
    }

    return null
  }

  getDataType() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'dataType' })
    )
  }

  getLabel() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'label' })
    )
  }

  getDescription() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'description' })
    )
  }

  getPlugin() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'plugin' })
    )
  }

  getVocabulary() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'vocabulary' })
    )
  }

  getMaterialType() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'materialType' })
    )
  }

  getSchema() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'schema' })
    )
  }

  getTransformation() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'transformation' })
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
