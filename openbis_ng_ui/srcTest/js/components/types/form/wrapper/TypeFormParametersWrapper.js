import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import TypeFormParametersType from '@src/js/components/types/form/TypeFormParametersType.jsx'
import TypeFormParametersProperty from '@src/js/components/types/form/TypeFormParametersProperty.jsx'
import TypeFormParametersSection from '@src/js/components/types/form/TypeFormParametersSection.jsx'
import TypeFormParametersTypeWrapper from './TypeFormParametersTypeWrapper.js'
import TypeFormParametersPropertyWrapper from './TypeFormParametersPropertyWrapper.js'
import TypeFormParametersSectionWrapper from './TypeFormParametersSectionWrapper.js'

export default class TypeFormParametersWrapper extends BaseWrapper {
  getType() {
    return new TypeFormParametersTypeWrapper(
      this.findComponent(TypeFormParametersType)
    )
  }

  getProperty() {
    return new TypeFormParametersPropertyWrapper(
      this.findComponent(TypeFormParametersProperty)
    )
  }

  getSection() {
    return new TypeFormParametersSectionWrapper(
      this.findComponent(TypeFormParametersSection)
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
