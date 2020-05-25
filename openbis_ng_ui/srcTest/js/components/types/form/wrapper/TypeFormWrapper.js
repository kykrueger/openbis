import TypeFormPreviewWrapper from './TypeFormPreviewWrapper.js'
import TypeFormParametersWrapper from './TypeFormParametersWrapper.js'
import TypeFormButtonsWrapper from './TypeFormButtonsWrapper.js'

export default class TypeFormWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getPreview() {
    return new TypeFormPreviewWrapper(this.wrapper.find('TypeFormPreview'))
  }

  getParameters() {
    return new TypeFormParametersWrapper(
      this.wrapper.find('TypeFormParameters')
    )
  }

  getButtons() {
    return new TypeFormButtonsWrapper(this.wrapper.find('TypeFormButtons'))
  }

  toJSON() {
    return {
      preview: this.getPreview().toJSON(),
      parameters: this.getParameters().toJSON(),
      buttons: this.getButtons().toJSON()
    }
  }
}
