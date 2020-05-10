import TypeFormParametersTypeWrapper from './TypeFormParametersTypeWrapper.js'
import TypeFormParametersPropertyWrapper from './TypeFormParametersPropertyWrapper.js'
import TypeFormParametersSectionWrapper from './TypeFormParametersSectionWrapper.js'

export default class TypeFormParametersWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getType() {
    return new TypeFormParametersTypeWrapper(
      this.wrapper.find('TypeFormParametersType')
    )
  }

  getProperty() {
    return new TypeFormParametersPropertyWrapper(
      this.wrapper.find('TypeFormParametersProperty')
    )
  }

  getSection() {
    return new TypeFormParametersSectionWrapper(
      this.wrapper.find('TypeFormParametersSection')
    )
  }

  toJSON() {
    return {
      type: this.getType().toJSON(),
      property: this.getProperty().toJSON(),
      section: this.getSection().toJSON()
    }
  }
}
