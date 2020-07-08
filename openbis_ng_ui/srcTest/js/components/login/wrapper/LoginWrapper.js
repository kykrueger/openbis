import TextField from '@src/js/components/common/form/TextField.jsx'
import Button from '@src/js/components/common/form/Button.jsx'

import TextFieldWrapper from '@srcTest/js/common/wrapper/TextFieldWrapper.js'
import ButtonWrapper from '@srcTest/js/common/wrapper/ButtonWrapper.js'

import BaseWrapper from '@srcTest/js/common/wrapper/BaseWrapper.js'

export default class LoginWrapper extends BaseWrapper {
  getUser() {
    return new TextFieldWrapper(
      this.wrapper.find(TextField).filter({ name: 'user' })
    )
  }

  getPassword() {
    return new TextFieldWrapper(
      this.wrapper.find(TextField).filter({ name: 'password' })
    )
  }

  getButton() {
    return new ButtonWrapper(this.wrapper.find(Button))
  }

  toJSON() {
    return {
      user: this.getUser().toJSON(),
      password: this.getPassword().toJSON(),
      button: this.getButton().toJSON()
    }
  }
}
