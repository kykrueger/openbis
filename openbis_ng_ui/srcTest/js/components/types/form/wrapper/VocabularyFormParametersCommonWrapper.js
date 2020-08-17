import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import Header from '@src/js/components/common/form/Header.jsx'

export default class VocabularyFormParametersCommonWrapper extends BaseWrapper {
  getTitle() {
    return this.findComponent(Header)
  }

  toJSON() {
    return {
      title: this.getTitle().exists() ? this.getTitle().text() : null
    }
  }
}
