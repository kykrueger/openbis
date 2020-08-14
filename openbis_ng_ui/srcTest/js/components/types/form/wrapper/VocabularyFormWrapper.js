import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import VocabularyFormParameters from '@src/js/components/types/form/VocabularyFormParameters.jsx'
import VocabularyFormParametersWrapper from './VocabularyFormParametersWrapper.js'

export default class VocabularyFormWrapper extends BaseWrapper {
  getParameters() {
    return new VocabularyFormParametersWrapper(
      this.findComponent(VocabularyFormParameters)
    )
  }

  toJSON() {
    return {
      parameters: this.getParameters().toJSON()
    }
  }
}
