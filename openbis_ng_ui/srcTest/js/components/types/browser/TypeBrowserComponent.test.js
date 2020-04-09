import React from 'react'
import { Provider } from 'react-redux'
import { mount } from 'enzyme'
import { createStore } from '@src/js/store/store.js'
import BrowserTest from '@srcTest/js/components/common/browser/BrowserTest.js'
import TypeBrowser from '@src/js/components/types/browser/TypeBrowser.jsx'
import TypeBrowserController from '@src/js/components/types/browser/TypeBrowserController.js'
import openbis from '@srcTest/js/services/openbis.js'
import actions from '@src/js/store/actions/actions.js'
import fixture from '@srcTest/js/common/fixture.js'

let store = null
let controller = null

beforeEach(() => {
  jest.resetAllMocks()
  store = createStore()
  controller = new TypeBrowserController()
})

describe('browser', () => {
  test('test', done => {
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

    store.dispatch(actions.init())

    let wrapper = mount(
      <Provider store={store}>
        <TypeBrowser controller={controller} />
      </Provider>
    )

    setTimeout(() => {
      wrapper.update()

      BrowserTest.expectFilter(wrapper, '')
      BrowserTest.expectNodes(wrapper, [
        { level: 0, text: 'Object Types' },
        { level: 0, text: 'Collection Types' },
        { level: 0, text: 'Data Set Types' },
        { level: 0, text: 'Material Types' }
      ])

      BrowserTest.simulateNodeIconClick(wrapper, 'objectTypes')
      wrapper.update()

      BrowserTest.expectFilter(wrapper, '')
      BrowserTest.expectNodes(wrapper, [
        { level: 0, text: 'Object Types' },
        { level: 1, text: fixture.ANOTHER_SAMPLE_TYPE_DTO.code },
        { level: 1, text: fixture.TEST_SAMPLE_TYPE_DTO.code },
        { level: 0, text: 'Collection Types' },
        { level: 0, text: 'Data Set Types' },
        { level: 0, text: 'Material Types' }
      ])

      BrowserTest.simulateFilterChange(wrapper, 'ANOTHER')
      wrapper.update()

      BrowserTest.expectFilter(wrapper, 'ANOTHER')
      BrowserTest.expectNodes(wrapper, [
        { level: 0, text: 'Object Types' },
        { level: 1, text: fixture.ANOTHER_SAMPLE_TYPE_DTO.code },
        { level: 0, text: 'Material Types' },
        { level: 1, text: fixture.ANOTHER_MATERIAL_TYPE_DTO.code }
      ])

      done()
    })
  })
})
