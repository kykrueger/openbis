import React from 'react'
import { Provider } from 'react-redux'
import { mount } from 'enzyme'
import { createStore } from '@src/js/store/store.js'
import ThemeProvider from '@src/js/components/common/theme/ThemeProvider.jsx'
import BrowserWrapper from '@srcTest/js/components/common/browser/wrapper/BrowserWrapper.js'
import TypeBrowser from '@src/js/components/types/browser/TypeBrowser.jsx'
import openbis from '@srcTest/js/services/openbis.js'
import actions from '@src/js/store/actions/actions.js'
import fixture from '@srcTest/js/common/fixture.js'

let store = null

beforeEach(() => {
  jest.resetAllMocks()
  store = createStore()
  store.dispatch(actions.init())
})

describe('browser', () => {
  test('test', async () => {
    openbis.mockSearchSampleTypes([
      fixture.TEST_SAMPLE_TYPE_DTO,
      fixture.ANOTHER_SAMPLE_TYPE_DTO
    ])

    openbis.mockSearchExperimentTypes([fixture.TEST_EXPERIMENT_TYPE_DTO])
    openbis.mockSearchDataSetTypes([fixture.TEST_DATA_SET_TYPE_DTO])

    openbis.mockSearchMaterialTypes([
      fixture.TEST_MATERIAL_TYPE_DTO,
      fixture.ANOTHER_MATERIAL_TYPE_DTO
    ])

    const browser = await mountBrowser()
    await browser.update()

    browser.expectJSON({
      filter: {
        value: null
      },
      nodes: [
        { level: 0, text: 'Object Types' },
        { level: 0, text: 'Collection Types' },
        { level: 0, text: 'Data Set Types' },
        { level: 0, text: 'Material Types' }
      ]
    })

    browser.getNodes()[0].getIcon().click()
    await browser.update()

    browser.expectJSON({
      filter: {
        value: null
      },
      nodes: [
        { level: 0, text: 'Object Types' },
        { level: 1, text: fixture.ANOTHER_SAMPLE_TYPE_DTO.code },
        { level: 1, text: fixture.TEST_SAMPLE_TYPE_DTO.code },
        { level: 0, text: 'Collection Types' },
        { level: 0, text: 'Data Set Types' },
        { level: 0, text: 'Material Types' }
      ]
    })

    browser.getFilter().change('ANOTHER')
    await browser.update()

    browser.expectJSON({
      filter: {
        value: 'ANOTHER'
      },
      nodes: [
        { level: 0, text: 'Object Types' },
        { level: 1, text: fixture.ANOTHER_SAMPLE_TYPE_DTO.code },
        { level: 0, text: 'Material Types' },
        { level: 1, text: fixture.ANOTHER_MATERIAL_TYPE_DTO.code }
      ]
    })
  })
})

async function mountBrowser() {
  const wrapper = mount(
    <Provider store={store}>
      <ThemeProvider>
        <TypeBrowser />
      </ThemeProvider>
    </Provider>
  )

  const browser = new BrowserWrapper(wrapper)
  return browser.update().then(() => browser)
}
