import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import VocabularyFormParametersVocabulary from '@src/js/components/types/form/VocabularyFormParametersVocabulary.jsx'
import VocabularyFormParametersVocabularyWrapper from './VocabularyFormParametersVocabularyWrapper.js'
import VocabularyFormParametersTerm from '@src/js/components/types/form/VocabularyFormParametersTerm.jsx'
import VocabularyFormParametersTermWrapper from './VocabularyFormParametersTermWrapper.js'

export default class VocabularyFormParametersWrapper extends BaseWrapper {
  getVocabulary() {
    return new VocabularyFormParametersVocabularyWrapper(
      this.findComponent(VocabularyFormParametersVocabulary)
    )
  }

  getTerm() {
    return new VocabularyFormParametersTermWrapper(
      this.findComponent(VocabularyFormParametersTerm)
    )
  }

  toJSON() {
    return {
      vocabulary: this.getVocabulary().toJSON(),
      term: this.getTerm().toJSON()
    }
  }
}
