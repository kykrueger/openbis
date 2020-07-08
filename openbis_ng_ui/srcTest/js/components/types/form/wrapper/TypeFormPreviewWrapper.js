import TypeFormPreviewHeader from '@src/js/components/types/form/TypeFormPreviewHeader.jsx'
import TypeFormPreviewSection from '@src/js/components/types/form/TypeFormPreviewSection.jsx'

import TypeFormPreviewHeaderWrapper from './TypeFormPreviewHeaderWrapper.js'
import TypeFormPreviewSectionWrapper from './TypeFormPreviewSectionWrapper.js'

export default class TypeFormPreviewWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getHeader() {
    return new TypeFormPreviewHeaderWrapper(
      this.wrapper.find(TypeFormPreviewHeader)
    )
  }

  getSections() {
    const sections = []
    this.wrapper.find(TypeFormPreviewSection).forEach(sectionWrapper => {
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
