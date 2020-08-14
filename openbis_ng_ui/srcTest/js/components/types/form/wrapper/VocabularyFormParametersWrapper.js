import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import VocabularyFormParametersVocabulary from '@src/js/components/types/form/VocabularyFormParametersVocabulary.jsx'
import VocabularyFormParametersVocabularyWrapper from './VocabularyFormParametersVocabularyWrapper.js'

export default class VocabularyFormParametersWrapper extends BaseWrapper {
  getVocabulary() {
    return new VocabularyFormParametersVocabularyWrapper(
      this.findComponent(VocabularyFormParametersVocabulary)
    )
  }

  toJSON() {
    return {
      vocabulary: this.getVocabulary().toJSON()
    }
  }
}
