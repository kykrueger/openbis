import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import TypeFormHeader from '@src/js/components/types/form/TypeFormHeader.jsx'

export default class TypeFormParametersCommonWrapper extends BaseWrapper {
  getTitle() {
    return this.findComponent(TypeFormHeader)
  }

  toJSON() {
    return {
      title: this.getTitle().exists() ? this.getTitle().text() : null
    }
  }
}
