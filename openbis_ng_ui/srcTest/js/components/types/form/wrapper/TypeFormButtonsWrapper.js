import Button from '@src/js/components/common/form/Button.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import ButtonWrapper from '@srcTest/js/components/common/form/wrapper/ButtonWrapper.js'
import MessageWrapper from '@srcTest/js/components/common/form/wrapper/MessageWrapper.js'

export default class TypeFormButtonsWrapper extends BaseWrapper {
  getEdit() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'edit' })
    )
  }

  getAddSection() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'addSection' })
    )
  }

  getAddProperty() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'addProperty' })
    )
  }

  getRemove() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'remove' })
    )
  }

  getSave() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'save' })
    )
  }

  getCancel() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'cancel' })
    )
  }

  getMessage() {
    return new MessageWrapper(this.findComponent(Message))
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
