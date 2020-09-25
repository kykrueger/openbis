import VocabularyFormComponentTest from '@srcTest/js/components/types/form/VocabularyFormComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new VocabularyFormComponentTest()
  common.beforeEach()
})

describe(VocabularyFormComponentTest.SUITE, () => {
  test('validate term', testValidateTerm)
  test('validate vocabulary', testValidateVocabulary)
})

async function testValidateTerm() {
  const form = await common.mountExisting(fixture.TEST_VOCABULARY_DTO)

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
  const form = await common.mountNew()

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
