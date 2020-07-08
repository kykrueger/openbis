import Button from '@src/js/components/common/form/Button.jsx'
import Message from '@src/js/components/common/form/Message.jsx'

import ButtonWrapper from '@srcTest/js/common/wrapper/ButtonWrapper.js'
import MessageWrapper from '@srcTest/js/common/wrapper/MessageWrapper.js'

export default class TypeFormButtonsWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getEdit() {
    return new ButtonWrapper(this.wrapper.find(Button).filter({ name: 'edit' }))
  }

  getAddSection() {
    return new ButtonWrapper(
      this.wrapper.find(Button).filter({ name: 'addSection' })
    )
  }

  getAddProperty() {
    return new ButtonWrapper(
      this.wrapper.find(Button).filter({ name: 'addProperty' })
    )
  }

  getRemove() {
    return new ButtonWrapper(
      this.wrapper.find(Button).filter({ name: 'remove' })
    )
  }

  getSave() {
    return new ButtonWrapper(this.wrapper.find(Button).filter({ name: 'save' }))
  }

  getCancel() {
    return new ButtonWrapper(
      this.wrapper.find(Button).filter({ name: 'cancel' })
    )
  }

  getMessage() {
    return new MessageWrapper(this.wrapper.find(Message))
  }

  toJSON() {
    return {
      edit: this.getEdit().toJSON(),
      addSection: this.getAddSection().toJSON(),
      addProperty: this.getAddProperty().toJSON(),
      remove: this.getRemove().toJSON(),
      save: this.getSave().toJSON(),
      cancel: this.getCancel().toJSON(),
      message: this.getMessage().toJSON()
    }
  }
}
