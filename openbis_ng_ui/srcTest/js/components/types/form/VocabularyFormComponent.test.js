import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import VocabularyForm from '@src/js/components/types/form/VocabularyForm.jsx'
import VocabularyFormWrapper from '@srcTest/js/components/types/form/wrapper/VocabularyFormWrapper.js'
import VocabularyFormController from '@src/js/components/types/form/VocabularyFormController.js'
import VocabularyFormFacade from '@src/js/components/types/form/VocabularyFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'
import fixture from '@srcTest/js/common/fixture.js'

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
  test('load existing', testLoadExisting)
})

async function testLoadNew() {
  const form = await mountNew()

  form.expectJSON({
    grid: {
      rows: []
    },
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
    },
    buttons: {
      addTerm: {
        enabled: true
      },
      removeTerm: {
        enabled: false
      },
      save: {
        enabled: true
      },
      edit: null,
      cancel: null,
      message: null
    }
  })
}

async function testLoadExisting() {
  const form = await mountExisting()

  const gridJSON = {
    columns: [
      {
        field: 'code.value',
        label: 'Code',
        filter: null,
        sort: false
      },
      {
        field: 'label.value',
        label: 'Label',
        filter: null,
        sort: false
      },
      {
        field: 'description.value',
        label: 'Description',
        filter: null,
        sort: false
      },
      {
        field: 'official.value',
        label: 'Official',
        filter: null,
        sort: false
      }
    ],
    rows: fixture.TEST_VOCABULARY_DTO.terms.map(term => ({
      'code.value': term.getCode()
    }))
  }

  form.expectJSON({
    grid: gridJSON,
    parameters: {
      vocabulary: {
        code: {
          label: 'Code',
          value: fixture.TEST_VOCABULARY_DTO.getCode(),
          mode: 'view'
        },
        description: {
          label: 'Description',
          value: fixture.TEST_VOCABULARY_DTO.getDescription(),
          mode: 'view'
        },
        urlTemplate: {
          label: 'URL template',
          value: fixture.TEST_VOCABULARY_DTO.getUrlTemplate(),
          mode: 'view'
        }
      }
    },
    buttons: {
      edit: {
        enabled: true
      },
      addTerm: null,
      removeTerm: null,
      save: null,
      cancel: null,
      message: null
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    grid: gridJSON,
    parameters: {
      vocabulary: {
        title: 'Vocabulary',
        code: {
          label: 'Code',
          value: fixture.TEST_VOCABULARY_DTO.getCode(),
          enabled: false,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: fixture.TEST_VOCABULARY_DTO.getDescription(),
          enabled: true,
          mode: 'edit'
        },
        urlTemplate: {
          label: 'URL template',
          value: fixture.TEST_VOCABULARY_DTO.getUrlTemplate(),
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      addTerm: {
        enabled: true
      },
      removeTerm: {
        enabled: false
      },
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      edit: null,
      message: null
    }
  })
}

async function mountNew() {
  return await common.mount({
    type: objectTypes.NEW_VOCABULARY_TYPE
  })
}

async function mountExisting() {
  facade.loadVocabulary.mockReturnValue(
    Promise.resolve(fixture.TEST_VOCABULARY_DTO)
  )

  return await common.mount({
    id: fixture.TEST_VOCABULARY_DTO.getCode(),
    type: objectTypes.VOCABULARY_TYPE
  })
}
