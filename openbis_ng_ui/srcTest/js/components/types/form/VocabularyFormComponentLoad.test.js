import VocabularyFormComponentTest from '@srcTest/js/components/types/form/VocabularyFormComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new VocabularyFormComponentTest()
  common.beforeEach()
})

describe(VocabularyFormComponentTest.SUITE, () => {
  test('load new', testLoadNew)
  test('load existing', testLoadExisting)
})

async function testLoadNew() {
  const form = await common.mountNew()

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
  const form = await common.mountExisting(fixture.TEST_VOCABULARY_DTO)

  const gridJSON = {
    columns: [
      {
        field: 'code.value',
        label: 'Code',
        filter: {
          value: null
        },
        sort: 'asc'
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
        title: 'Vocabulary',
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
