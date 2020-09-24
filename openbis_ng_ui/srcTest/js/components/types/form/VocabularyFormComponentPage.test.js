import VocabularyFormComponentTest from '@srcTest/js/components/types/form/VocabularyFormComponentTest.js'

let common = null

beforeEach(() => {
  common = new VocabularyFormComponentTest()
  common.beforeEach()
})

describe('VocabularyFormComponentPage', () => {
  test('page', testPage)
})

async function testPage() {
  const form = await common.mountNew()

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
