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
  test('add term', testAddTerm)
  test('remove term', testRemoveTerm)
  test('change term', testChangeTerm)
  test('change vocabulary', testChangeVocabulary)
  test('validate term', testValidateTerm)
  test('validate vocabulary', testValidateVocabulary)
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

async function testAddTerm() {
  const form = await mountExisting()

  form.expectJSON({
    grid: {
      rows: fixture.TEST_VOCABULARY_DTO.terms.map(term => ({
        values: {
          'code.value': term.getCode(),
          'label.value': term.getLabel(),
          'description.value': term.getDescription(),
          'official.value': String(term.isOfficial())
        },
        selected: false
      })),
      paging: {
        pageSize: {
          value: 10
        },
        range: '1-6 of 6'
      }
    }
  })

  form.getGrid().getPaging().getPageSize().change(5)
  await form.update()

  form.getButtons().getEdit().click()
  await form.update()

  form.getButtons().getAddTerm().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        {
          values: { 'code.value': fixture.TEST_TERM_6_DTO.getCode() },
          selected: false
        },
        {
          values: { 'code.value': null },
          selected: true
        }
      ],
      paging: {
        pageSize: {
          value: 5
        },
        range: '6-7 of 7'
      }
    },
    parameters: {
      term: {
        title: 'Term',
        code: {
          label: 'Code',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        label: {
          label: 'Label',
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
        official: {
          label: 'Official',
          value: true,
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
        enabled: true
      },
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      edit: null,
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}

async function testRemoveTerm() {
  const form = await mountExisting()

  form.getGrid().getPaging().getPageSize().change(5)
  form.getGrid().getPaging().getNextPage().click()
  await form.update()

  form.getGrid().getRows()[0].click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [fixture.TEST_TERM_6_DTO].map(term => ({
        values: {
          'code.value': term.getCode(),
          'label.value': term.getLabel(),
          'description.value': term.getDescription(),
          'official.value': String(term.isOfficial())
        },
        selected: true
      })),
      paging: {
        pageSize: {
          value: 5
        },
        range: '6-6 of 6'
      }
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.getButtons().getRemoveTerm().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        fixture.TEST_TERM_1_DTO,
        fixture.TEST_TERM_2_DTO,
        fixture.TEST_TERM_3_DTO,
        fixture.TEST_TERM_4_DTO,
        fixture.TEST_TERM_5_DTO
      ].map(term => ({
        values: {
          'code.value': term.getCode(),
          'label.value': term.getLabel(),
          'description.value': term.getDescription(),
          'official.value': String(term.isOfficial())
        },
        selected: false
      })),
      paging: {
        pageSize: {
          value: 5
        },
        range: '1-5 of 5'
      }
    },
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
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}

async function testChangeTerm() {
  const form = await mountExisting()

  form.getButtons().getEdit().click()
  await form.update()

  form.getGrid().getRows()[1].click()
  await form.update()

  form
    .getGrid()
    .getRows()[1]
    .expectJSON({
      values: {
        'label.value': fixture.TEST_TERM_2_DTO.getLabel()
      }
    })

  form.expectJSON({
    parameters: {
      term: {
        title: 'Term',
        label: {
          label: 'Label',
          value: fixture.TEST_TERM_2_DTO.getLabel(),
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: null
    }
  })

  form.getParameters().getTerm().getLabel().change('New Label')
  await form.update()

  form
    .getGrid()
    .getRows()[1]
    .expectJSON({
      values: {
        'label.value': 'New Label'
      }
    })

  form.expectJSON({
    parameters: {
      term: {
        title: 'Term',
        label: {
          label: 'Label',
          value: 'New Label',
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}

async function testChangeVocabulary() {
  const form = await mountExisting()

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    parameters: {
      vocabulary: {
        title: 'Vocabulary',
        description: {
          label: 'Description',
          value: fixture.TEST_VOCABULARY_DTO.getDescription(),
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: null
    }
  })

  form
    .getParameters()
    .getVocabulary()
    .getDescription()
    .change('New Description')
  await form.update()

  form.expectJSON({
    parameters: {
      vocabulary: {
        title: 'Vocabulary',
        description: {
          label: 'Description',
          value: 'New Description',
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}

async function testValidateTerm() {
  const form = await mountExisting()

  form.getButtons().getEdit().click()
  await form.update()

  form.getButtons().getAddTerm().click()
  await form.update()

  form.getGrid().getPaging().getPageSize().change(5)
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        fixture.TEST_TERM_1_DTO,
        fixture.TEST_TERM_2_DTO,
        fixture.TEST_TERM_3_DTO,
        fixture.TEST_TERM_4_DTO,
        fixture.TEST_TERM_5_DTO
      ].map(term => ({
        values: {
          'code.value': term.getCode(),
          'label.value': term.getLabel(),
          'description.value': term.getDescription(),
          'official.value': String(term.isOfficial())
        },
        selected: false
      })),
      paging: {
        pageSize: {
          value: 5
        },
        range: '1-5 of 7'
      }
    },
    parameters: {
      term: {
        title: 'Term',
        code: {
          value: null,
          error: null
        }
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        {
          values: {
            'code.value': fixture.TEST_TERM_6_DTO.getCode(),
            'label.value': fixture.TEST_TERM_6_DTO.getLabel(),
            'description.value': fixture.TEST_TERM_6_DTO.getDescription(),
            'official.value': String(fixture.TEST_TERM_6_DTO.isOfficial())
          },
          selected: false
        },
        {
          values: {
            'code.value': null,
            'label.value': null,
            'description.value': null,
            'official.value': String(true)
          },
          selected: true
        }
      ],
      paging: {
        pageSize: {
          value: 5
        },
        range: '6-7 of 7'
      }
    },
    parameters: {
      term: {
        title: 'Term',
        code: {
          value: null,
          error: 'Code cannot be empty'
        }
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}

async function testValidateVocabulary() {
  const form = await mountNew()

  form.getButtons().getAddTerm().click()
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        {
          values: {
            'code.value': null,
            'label.value': null,
            'description.value': null,
            'official.value': String(true)
          },
          selected: false
        }
      ]
    },
    parameters: {
      vocabulary: {
        title: 'Vocabulary',
        code: {
          error: 'Code cannot be empty'
        }
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
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
