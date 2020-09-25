import VocabularyFormComponentTest from '@srcTest/js/components/types/form/VocabularyFormComponentTest.js'

let common = null

beforeEach(() => {
  common = new VocabularyFormComponentTest()
  common.beforeEach()
})

describe(VocabularyFormComponentTest.SUITE, () => {
  test('sort', testSort)
})

async function testSort() {
  const form = await common.mountNew()

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
