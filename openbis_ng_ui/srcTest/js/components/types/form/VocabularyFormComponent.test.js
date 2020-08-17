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
  test('select term', testSelectTerm)
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
        filter: {
          value: null
        },
        sort: null
      },
      {
        field: 'label.value',
        label: 'Label',
        filter: {
          value: null
        },
        sort: null
      },
      {
        field: 'description.value',
        label: 'Description',
        filter: {
          value: null
        },
        sort: null
      },
      {
        field: 'official.value',
        label: 'Official',
        filter: {
          value: null
        },
        sort: null
      }
    ],
    rows: fixture.TEST_VOCABULARY_DTO.terms.map(term => ({
      values: {
        'code.value': term.getCode(),
        'label.value': term.getLabel(),
        'description.value': term.getDescription(),
        'official.value': String(term.isOfficial())
      },
      selected: false
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

async function testSelectTerm() {
  const form = await mountExisting()

  form.getGrid().getRows()[1].click()
  await form.update()

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { selected: false },
        { selected: true },
        { selected: false },
        { selected: false },
        { selected: false },
        { selected: false }
      ]
    },
    parameters: {
      term: {
        title: 'Term',
        code: {
          label: 'Code',
          value: fixture.TEST_TERM_2_DTO.getCode(),
          enabled: false,
          mode: 'edit'
        },
        label: {
          label: 'Label',
          value: fixture.TEST_TERM_2_DTO.getLabel(),
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: fixture.TEST_TERM_2_DTO.getDescription(),
          enabled: true,
          mode: 'edit'
        },
        official: {
          label: 'Official',
          value: fixture.TEST_TERM_2_DTO.isOfficial(),
          enabled: false,
          mode: 'edit'
        }
      }
    },
    buttons: {
      addTerm: {
        enabled: true
      },
      removeTerm: {
        enabled: true
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
