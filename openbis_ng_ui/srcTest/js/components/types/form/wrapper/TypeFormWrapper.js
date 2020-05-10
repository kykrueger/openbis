import TypeFormPreviewWrapper from './TypeFormPreviewWrapper.js'
import TypeFormParametersWrapper from './TypeFormParametersWrapper.js'

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

  toJSON() {
    return {
      preview: this.getPreview().toJSON(),
      parameters: this.getParameters().toJSON()
    }
  }
}
