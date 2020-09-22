import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import VocabularyForm from '@src/js/components/types/form/VocabularyForm.jsx'
import VocabularyFormWrapper from '@srcTest/js/components/types/form/wrapper/VocabularyFormWrapper.js'
import VocabularyFormController from '@src/js/components/types/form/VocabularyFormController.js'
import VocabularyFormFacade from '@src/js/components/types/form/VocabularyFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'
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
  test('sort', testSort)
  test('filter', testFilter)
  test('page', testPage)
  test('select term', testSelectTerm)
  test('follow selected term', testFollowSelectedTerm)
  test('add term', testAddTerm)
  test('remove term', testRemoveTerm)
  test('change term', testChangeTerm)
  test('change vocabulary', testChangeVocabulary)
  test('validate term', testValidateTerm)
  test('validate vocabulary', testValidateVocabulary)
  test('internal', testInternal)
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

async function testSort() {
  const form = await mountNew()

  const labels = [
    'Term 1',
    'term 11',
    'Term 2',
    'TERM A',
    'term B',
    'Term A1',
    'tErM A11',
    'term A2'
  ]

  for (let i = 0; i < labels.length; i++) {
    form.getButtons().getAddTerm().click()
    await form.update()
    form.getParameters().getTerm().getLabel().change(labels[i])
    await form.update()
  }

  form.getGrid().getColumns()[1].getLabel().click()
  await form.update()

  form.expectJSON({
    grid: {
      columns: [
        { field: 'code.value', sort: null },
        { field: 'label.value', sort: 'asc' },
        { field: 'description.value', sort: null },
        { field: 'official.value', sort: null }
      ],
      rows: [
        { values: { 'label.value': 'Term 1' } },
        { values: { 'label.value': 'Term 2' } },
        { values: { 'label.value': 'term 11' } },
        { values: { 'label.value': 'TERM A' } },
        { values: { 'label.value': 'Term A1' } },
        { values: { 'label.value': 'term A2' } },
        { values: { 'label.value': 'tErM A11' } },
        { values: { 'label.value': 'term B' } }
      ]
    }
  })

  form.getGrid().getColumns()[1].getLabel().click()
  await form.update()

  form.expectJSON({
    grid: {
      columns: [
        { field: 'code.value', sort: null },
        { field: 'label.value', sort: 'desc' },
        { field: 'description.value', sort: null },
        { field: 'official.value', sort: null }
      ],
      rows: [
        { values: { 'label.value': 'term B' } },
        { values: { 'label.value': 'tErM A11' } },
        { values: { 'label.value': 'term A2' } },
        { values: { 'label.value': 'Term A1' } },
        { values: { 'label.value': 'TERM A' } },
        { values: { 'label.value': 'term 11' } },
        { values: { 'label.value': 'Term 2' } },
        { values: { 'label.value': 'Term 1' } }
      ]
    }
  })
}

async function testFilter() {
  const form = await mountNew()

  const labels = [
    'some 1',
    'SOME 2',
    'Some 3',
    'another 1',
    'ANOTHER 2',
    'Another 3'
  ]

  for (let i = 0; i < labels.length; i++) {
    form.getButtons().getAddTerm().click()
    await form.update()
    form.getParameters().getTerm().getLabel().change(labels[i])
    await form.update()
  }

  form.getGrid().getColumns()[1].getFilter().change('some')
  await form.update()

  form.expectJSON({
    grid: {
      columns: [
        { field: 'code.value', filter: { value: null } },
        { field: 'label.value', filter: { value: 'some' } },
        { field: 'description.value', filter: { value: null } },
        { field: 'official.value', filter: { value: null } }
      ],
      rows: [
        { values: { 'label.value': 'some 1' } },
        { values: { 'label.value': 'SOME 2' } },
        { values: { 'label.value': 'Some 3' } }
      ],
      paging: {
        range: '1-3 of 3'
      }
    }
  })

  form.getGrid().getColumns()[1].getFilter().change('1')
  await form.update()

  form.expectJSON({
    grid: {
      columns: [
        { field: 'code.value', filter: { value: null } },
        { field: 'label.value', filter: { value: '1' } },
        { field: 'description.value', filter: { value: null } },
        { field: 'official.value', filter: { value: null } }
      ],
      rows: [
        { values: { 'label.value': 'some 1' } },
        { values: { 'label.value': 'another 1' } }
      ],
      paging: {
        range: '1-2 of 2'
      }
    }
  })
}

async function testPage() {
  const form = await mountNew()

  for (let i = 1; i <= 23; i++) {
    form.getButtons().getAddTerm().click()
    await form.update()
    form.getParameters().getTerm().getLabel().change(String(i))
    await form.update()
  }

  form.getGrid().getColumns()[1].getLabel().click()
  await form.update()

  form.getGrid().getPaging().getPageSize().change(5)
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { 'label.value': '1' } },
        { values: { 'label.value': '2' } },
        { values: { 'label.value': '3' } },
        { values: { 'label.value': '4' } },
        { values: { 'label.value': '5' } }
      ],
      paging: {
        range: '1-5 of 23'
      }
    }
  })

  form.getGrid().getPaging().getNextPage().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { 'label.value': '6' } },
        { values: { 'label.value': '7' } },
        { values: { 'label.value': '8' } },
        { values: { 'label.value': '9' } },
        { values: { 'label.value': '10' } }
      ],
      paging: {
        range: '6-10 of 23'
      }
    }
  })

  form.getGrid().getPaging().getLastPage().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { 'label.value': '21' } },
        { values: { 'label.value': '22' } },
        { values: { 'label.value': '23' } }
      ],
      paging: {
        range: '21-23 of 23'
      }
    }
  })

  form.getGrid().getPaging().getPrevPage().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { 'label.value': '16' } },
        { values: { 'label.value': '17' } },
        { values: { 'label.value': '18' } },
        { values: { 'label.value': '19' } },
        { values: { 'label.value': '20' } }
      ],
      paging: {
        range: '16-20 of 23'
      }
    }
  })

  form.getGrid().getPaging().getFirstPage().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { 'label.value': '1' } },
        { values: { 'label.value': '2' } },
        { values: { 'label.value': '3' } },
        { values: { 'label.value': '4' } },
        { values: { 'label.value': '5' } }
      ],
      paging: {
        range: '1-5 of 23'
      }
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

async function testFollowSelectedTerm() {
  const form = await mountNew()

  const labels = [
    'Term 10',
    'Term 20',
    'Term 30',
    'Term 40',
    'Term 50',
    'Term 60'
  ]

  for (let i = 0; i < labels.length; i++) {
    form.getButtons().getAddTerm().click()
    await form.update()
    form.getParameters().getTerm().getLabel().change(labels[i])
    await form.update()
  }

  form.getGrid().getPaging().getPageSize().change(5)
  await form.update()

  form.getGrid().getColumns()[1].getLabel().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { 'label.value': 'Term 10' }, selected: false },
        { values: { 'label.value': 'Term 20' }, selected: false },
        { values: { 'label.value': 'Term 30' }, selected: false },
        { values: { 'label.value': 'Term 40' }, selected: false },
        { values: { 'label.value': 'Term 50' }, selected: false }
      ],
      paging: {
        range: '1-5 of 6'
      }
    },
    parameters: {
      term: {
        label: {
          value: 'Term 60'
        },
        messages: [
          {
            type: 'warning',
            text:
              'The selected term is currently not visible in the term list due to the chosen filtering and paging.'
          }
        ]
      }
    }
  })

  form.getGrid().getRows()[0].click()
  await form.update()

  form.getParameters().getTerm().getLabel().change('Term 25')
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { 'label.value': 'Term 20' }, selected: false },
        { values: { 'label.value': 'Term 25' }, selected: true },
        { values: { 'label.value': 'Term 30' }, selected: false },
        { values: { 'label.value': 'Term 40' }, selected: false },
        { values: { 'label.value': 'Term 50' }, selected: false }
      ],
      paging: {
        range: '1-5 of 6'
      }
    },
    parameters: {
      term: {
        label: {
          value: 'Term 25'
        },
        messages: []
      }
    }
  })

  form.getParameters().getTerm().getLabel().change('Term 65')
  await form.update()

  form.expectJSON({
    grid: {
      rows: [{ values: { 'label.value': 'Term 65' }, selected: true }],
      paging: {
        range: '6-6 of 6'
      }
    },
    parameters: {
      term: {
        label: {
          value: 'Term 65'
        },
        messages: []
      }
    }
  })

  form.getGrid().getPaging().getFirstPage().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { 'label.value': 'Term 20' }, selected: false },
        { values: { 'label.value': 'Term 30' }, selected: false },
        { values: { 'label.value': 'Term 40' }, selected: false },
        { values: { 'label.value': 'Term 50' }, selected: false },
        { values: { 'label.value': 'Term 60' }, selected: false }
      ],
      paging: {
        range: '1-5 of 6'
      }
    },
    parameters: {
      term: {
        label: {
          value: 'Term 65'
        },
        messages: [
          {
            type: 'warning',
            text:
              'The selected term is currently not visible in the term list due to the chosen filtering and paging.'
          }
        ]
      }
    }
  })
}

async function testAddTerm() {
  const form = await mountExisting()

  form.expectJSON({
    grid: {
      columns: [
        { field: 'code.value', sort: 'asc' },
        { field: 'label.value', sort: null },
        { field: 'description.value', sort: null },
        { field: 'official.value', sort: null }
      ],
      rows: [
        fixture.TEST_TERM_1_DTO,
        fixture.TEST_TERM_2_DTO,
        fixture.TEST_TERM_3_DTO,
        fixture.TEST_TERM_4_DTO,
        fixture.TEST_TERM_5_DTO,
        fixture.TEST_TERM_6_DTO
      ].map(term => ({
        values: {
          'code.value': term.getCode()
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

  form.getGrid().getColumns()[0].getLabel().click()
  await form.update()

  form.getGrid().getPaging().getPageSize().change(5)
  await form.update()

  form.getButtons().getEdit().click()
  await form.update()

  form.getButtons().getAddTerm().click()
  await form.update()

  form.expectJSON({
    grid: {
      columns: [
        { field: 'code.value', sort: 'desc' },
        { field: 'label.value', sort: null },
        { field: 'description.value', sort: null },
        { field: 'official.value', sort: null }
      ],
      rows: [
        {
          values: { 'code.value': fixture.TEST_TERM_1_DTO.getCode() },
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

  form.expectJSON({
    grid: {
      rows: [
        { values: { 'label.value': fixture.TEST_TERM_1_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_2_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_3_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_4_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_5_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_6_DTO.getLabel() } }
      ]
    },
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

  form.expectJSON({
    grid: {
      rows: [
        { values: { 'label.value': fixture.TEST_TERM_1_DTO.getLabel() } },
        { values: { 'label.value': 'New Label' } },
        { values: { 'label.value': fixture.TEST_TERM_3_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_4_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_5_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_6_DTO.getLabel() } }
      ]
    },
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

  form.getGrid().getPaging().getNextPage().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [fixture.TEST_TERM_5_DTO, fixture.TEST_TERM_6_DTO].map(term => ({
        values: {
          'code.value': term.getCode()
        },
        selected: false
      })),
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
            'code.value': null,
            'label.value': null,
            'description.value': null,
            'official.value': String(true)
          },
          selected: true
        },
        { values: { 'code.value': fixture.TEST_TERM_1_DTO.getCode() } },
        { values: { 'code.value': fixture.TEST_TERM_2_DTO.getCode() } },
        { values: { 'code.value': fixture.TEST_TERM_3_DTO.getCode() } },
        { values: { 'code.value': fixture.TEST_TERM_4_DTO.getCode() } }
      ],
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
          error: 'Code cannot be empty',
          focused: true
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

  form.getParameters().getTerm().getCode().change('I am illegal')
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      term: {
        code: {
          value: 'I am illegal',
          error: 'Code can only contain A-Z, a-z, 0-9 and _, -, ., :',
          focused: true
        }
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
          value: null,
          error: 'Code cannot be empty',
          focused: true
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

  form.getParameters().getVocabulary().getCode().change('I am illegal')
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      vocabulary: {
        code: {
          value: 'I am illegal',
          error: 'Code can only contain A-Z, a-z, 0-9 and _, -, .',
          focused: true
        }
      }
    }
  })
}

async function testInternal() {
  await doTestInternal(false, fixture.TEST_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(true, fixture.TEST_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(false, fixture.TEST_USER_DTO, fixture.SYSTEM_USER_DTO)
  await doTestInternal(true, fixture.TEST_USER_DTO, fixture.SYSTEM_USER_DTO)
  await doTestInternal(false, fixture.SYSTEM_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(true, fixture.SYSTEM_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(false, fixture.SYSTEM_USER_DTO, fixture.SYSTEM_USER_DTO)
  await doTestInternal(true, fixture.SYSTEM_USER_DTO, fixture.SYSTEM_USER_DTO)
}

async function doTestInternal(
  vocabularyInternal,
  vocabularyRegistrator,
  termRegistrator
) {
  const isSystemInternalVocabulary =
    vocabularyInternal &&
    vocabularyRegistrator.userId === fixture.SYSTEM_USER_DTO.userId

  const isSystemInternalTerm =
    vocabularyInternal &&
    termRegistrator.userId === fixture.SYSTEM_USER_DTO.userId

  const term = new openbis.VocabularyTerm()
  term.setCode('TEST_TERM')
  term.setDescription('Test Term Description')
  term.setLabel('Test Term Label')
  term.setRegistrator(termRegistrator)
  term.setOfficial(true)

  const vocabulary = new openbis.Vocabulary()
  vocabulary.setCode('TEST_VOCABULARY')
  vocabulary.setDescription('Test Vocabulary Description')
  vocabulary.setUrlTemplate('Test Vocabulary Url Template')
  vocabulary.setTerms([term])
  vocabulary.setManagedInternally(vocabularyInternal)
  vocabulary.setRegistrator(vocabularyRegistrator)

  facade.loadVocabulary.mockReturnValue(Promise.resolve(vocabulary))

  const form = await common.mount({
    id: vocabulary.getCode(),
    type: objectTypes.VOCABULARY_TYPE
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    parameters: {
      vocabulary: {
        title: 'Vocabulary',
        messages: isSystemInternalVocabulary
          ? [
              {
                type: 'lock',
                text:
                  'This is a system internal vocabulary. The vocabulary parameters cannot be changed.'
              }
            ]
          : [],
        code: {
          value: vocabulary.getCode(),
          enabled: false
        },
        description: {
          value: vocabulary.getDescription(),
          enabled: !isSystemInternalVocabulary
        },
        urlTemplate: {
          value: vocabulary.getUrlTemplate(),
          enabled: !isSystemInternalVocabulary
        }
      }
    }
  })

  form.getGrid().getRows()[0].click()
  await form.update()

  form.expectJSON({
    parameters: {
      term: {
        title: 'Term',
        messages: isSystemInternalTerm
          ? [
              {
                type: 'lock',
                text:
                  'This is a system internal term. The term parameters cannot be changed. The term cannot be removed.'
              }
            ]
          : [],
        code: {
          value: term.getCode(),
          enabled: false
        },
        description: {
          value: term.getDescription(),
          enabled: !isSystemInternalTerm
        },
        label: {
          value: term.getLabel(),
          enabled: !isSystemInternalTerm
        }
      }
    },
    buttons: {
      removeTerm: {
        enabled: !isSystemInternalTerm
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
