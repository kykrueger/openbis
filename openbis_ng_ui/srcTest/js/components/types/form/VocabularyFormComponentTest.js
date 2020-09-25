import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import VocabularyForm from '@src/js/components/types/form/VocabularyForm.jsx'
import VocabularyFormWrapper from '@srcTest/js/components/types/form/wrapper/VocabularyFormWrapper.js'
import VocabularyFormController from '@src/js/components/types/form/VocabularyFormController.js'
import VocabularyFormFacade from '@src/js/components/types/form/VocabularyFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'

jest.mock('@src/js/components/types/form/VocabularyFormFacade')

export default class VocabularyFormComponentTest extends ComponentTest {
  static SUITE = 'VocabularyFormComponent'

  constructor() {
    super(
      object => <VocabularyForm object={object} controller={this.controller} />,
      wrapper => new VocabularyFormWrapper(wrapper)
    )
    this.facade = null
    this.controller = null
  }

  async beforeEach() {
    super.beforeEach()

    this.facade = new VocabularyFormFacade()
    this.controller = new VocabularyFormController(this.facade)
  }

  async mountNew() {
    return await this.mount({
      type: objectTypes.NEW_VOCABULARY_TYPE
    })
  }

  async mountExisting(vocabulary) {
    this.facade.loadVocabulary.mockReturnValue(Promise.resolve(vocabulary))

    return await this.mount({
      id: vocabulary.getCode(),
      type: objectTypes.VOCABULARY_TYPE
    })
  }
}
