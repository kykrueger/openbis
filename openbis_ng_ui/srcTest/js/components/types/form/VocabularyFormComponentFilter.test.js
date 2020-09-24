import VocabularyFormComponentTest from '@srcTest/js/components/types/form/VocabularyFormComponentTest.js'

let common = null

beforeEach(() => {
  common = new VocabularyFormComponentTest()
  common.beforeEach()
})

describe('VocabularyFormComponentFilter', () => {
  test('filter', testFilter)
})

async function testFilter() {
  const form = await common.mountNew()

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
