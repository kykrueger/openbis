import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import VocabularyForm from '@src/js/components/types/form/VocabularyForm.jsx'
import VocabularyFormWrapper from '@srcTest/js/components/types/form/wrapper/VocabularyFormWrapper.js'
import VocabularyFormController from '@src/js/components/types/form/VocabularyFormController.js'
import VocabularyFormFacade from '@src/js/components/types/form/VocabularyFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'

jest.mock('@src/js/components/types/form/VocabularyFormFacade')

let common = null
let facade = null
let controller = null

beforeEach(() => {
  common = new ComponentTest(
    object => <VocabularyForm object={object} controller={controller} />,
    wrapper => new VocabularyFormWrapper(wrapper)
  )
  common.beforeEach()

  facade = new VocabularyFormFacade()
  controller = new VocabularyFormController(facade)
})

describe('VocabularyFormComponent', () => {
  test('load new', testLoadNew)
})

async function testLoadNew() {
  const form = await mountNew()

  form.expectJSON({
    parameters: {
      vocabulary: {
        title: 'Vocabulary',
        code: {
          label: 'Code',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        urlTemplate: {
          label: 'URL template',
          value: null,
          enabled: true,
          mode: 'edit'
        }
      }
    }
  })
}

async function mountNew() {
  return await common.mount({
    type: objectTypes.NEW_VOCABULARY_TYPE
  })
}
