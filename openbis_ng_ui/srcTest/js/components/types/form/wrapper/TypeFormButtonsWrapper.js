import ButtonWrapper from '@srcTest/js/common/wrapper/ButtonWrapper.js'

export default class TypeFormButtonsWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getAddSection() {
    return new ButtonWrapper(this.wrapper.find('button[name="addSection"]'))
  }

  getAddProperty() {
    return new ButtonWrapper(this.wrapper.find('button[name="addProperty"]'))
  }

  getRemove() {
    return new ButtonWrapper(this.wrapper.find('button[name="remove"]'))
  }

  getSave() {
    return new ButtonWrapper(this.wrapper.find('button[name="save"]'))
  }

  toJSON() {
    return {
      addSection: this.getAddSection().toJSON(),
      addProperty: this.getAddProperty().toJSON(),
      remove: this.getRemove().toJSON(),
      save: this.getSave().toJSON()
    }
  }
}
