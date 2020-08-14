import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridWrapper from '@srcTest/js/components/common/grid/wrapper/GridWrapper.js'
import VocabularyFormParameters from '@src/js/components/types/form/VocabularyFormParameters.jsx'
import VocabularyFormParametersWrapper from '@srcTest/js/components/types/form/wrapper/VocabularyFormParametersWrapper.js'
import VocabularyFormButtons from '@src/js/components/types/form/VocabularyFormButtons.jsx'
import VocabularyFormButtonsWrapper from '@srcTest/js/components/types/form/wrapper/VocabularyFormButtonsWrapper.js'

export default class VocabularyFormWrapper extends BaseWrapper {
  getGrid() {
    return new GridWrapper(this.findComponent(Grid))
  }

  getParameters() {
    return new VocabularyFormParametersWrapper(
      this.findComponent(VocabularyFormParameters)
    )
  }

  getButtons() {
    return new VocabularyFormButtonsWrapper(
      this.findComponent(VocabularyFormButtons)
    )
  }

  toJSON() {
    return {
      grid: this.getGrid().toJSON(),
      parameters: this.getParameters().toJSON(),
      buttons: this.getButtons().toJSON()
    }
  }
}
