import TypeFormPreview from '@src/js/components/types/form/TypeFormPreview.jsx'
import TypeFormParameters from '@src/js/components/types/form/TypeFormParameters.jsx'
import TypeFormButtons from '@src/js/components/types/form/TypeFormButtons.jsx'

import BaseWrapper from '@srcTest/js/common/wrapper/BaseWrapper.js'

import TypeFormPreviewWrapper from './TypeFormPreviewWrapper.js'
import TypeFormParametersWrapper from './TypeFormParametersWrapper.js'
import TypeFormButtonsWrapper from './TypeFormButtonsWrapper.js'

export default class TypeFormWrapper extends BaseWrapper {
  getPreview() {
    return new TypeFormPreviewWrapper(this.wrapper.find(TypeFormPreview))
  }

  getParameters() {
    return new TypeFormParametersWrapper(this.wrapper.find(TypeFormParameters))
  }

  getButtons() {
    return new TypeFormButtonsWrapper(this.wrapper.find(TypeFormButtons))
  }

  toJSON() {
    return {
      preview: this.getPreview().toJSON(),
      parameters: this.getParameters().toJSON(),
      buttons: this.getButtons().toJSON()
    }
  }
}
