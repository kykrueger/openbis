import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import TypeFormPreviewHeader from '@src/js/components/types/form/TypeFormPreviewHeader.jsx'
import TypeFormPreviewSection from '@src/js/components/types/form/TypeFormPreviewSection.jsx'
import TypeFormPreviewHeaderWrapper from './TypeFormPreviewHeaderWrapper.js'
import TypeFormPreviewSectionWrapper from './TypeFormPreviewSectionWrapper.js'

export default class TypeFormPreviewWrapper extends BaseWrapper {
  getHeader() {
    return new TypeFormPreviewHeaderWrapper(
      this.findComponent(TypeFormPreviewHeader)
    )
  }

  getSections() {
    const sections = []
    this.findComponent(TypeFormPreviewSection).forEach(sectionWrapper => {
      sections.push(new TypeFormPreviewSectionWrapper(sectionWrapper))
    })
    return sections
  }

  toJSON() {
    return {
      header: this.getHeader().toJSON(),
      sections: this.getSections().map(section => section.toJSON())
    }
  }
}
