import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'

export default class TypeFormPreviewPropertyWrapper extends BaseWrapper {
  getCode() {
    return this.wrapper.find({ 'data-part': 'code' })
  }

  getLabel() {
    return this.wrapper.find({ 'data-part': 'label' })
  }

  getDataType() {
    return this.wrapper.find({ 'data-part': 'dataType' })
  }

  click() {
    this.wrapper.instance().handleDraggableClick({
      stopPropagation: () => {}
    })
  }

  toJSON() {
    const code = this.getCode().text().trim()
    const label = this.getLabel().text().trim()
    const dataType = this.getDataType().text().trim()
    return {
      code: code.length > 0 ? code : null,
      label: label.length > 0 ? label : null,
      dataType: dataType.length > 0 ? dataType : null
    }
  }
}
