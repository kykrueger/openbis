import React from 'react'
import { Provider } from 'react-redux'
import { mount } from 'enzyme'
import { createStore } from '@src/js/store/store.js'
import ThemeProvider from '@src/js/components/common/theme/ThemeProvider.jsx'
import BrowserTest from '@srcTest/js/components/common/browser/BrowserTest.js'
import UserBrowser from '@src/js/components/users/browser/UserBrowser.jsx'
import UserBrowserController from '@src/js/components/users/browser/UserBrowserController.js'
import openbis from '@srcTest/js/services/openbis.js'
import actions from '@src/js/store/actions/actions.js'
import fixture from '@srcTest/js/common/fixture.js'

let store = null
let controller = null

beforeEach(() => {
  jest.resetAllMocks()
  store = createStore()
  controller = new UserBrowserController()
})

describe('browser', () => {
  test('test', done => {
    openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
    openbis.mockSearchGroups([
      fixture.TEST_GROUP_DTO,
      fixture.ANOTHER_GROUP_DTO,
      fixture.ALL_USERS_GROUP_DTO
    ])

    store.dispatch(actions.init())

    let wrapper = mount(
      <Provider store={store}>
        <ThemeProvider>
          <UserBrowser controller={controller} />
        </ThemeProvider>
      </Provider>
    )

    setTimeout(() => {
      wrapper.update()

      BrowserTest.expectFilter(wrapper, '')
      BrowserTest.expectNodes(wrapper, [
        { level: 0, text: 'Users' },
        { level: 0, text: 'Groups' }
      ])

      BrowserTest.simulateNodeIconClick(wrapper, 'users')
      wrapper.update()

      BrowserTest.expectFilter(wrapper, '')
      BrowserTest.expectNodes(wrapper, [
        { level: 0, text: 'Users' },
        { level: 1, text: fixture.ANOTHER_USER_DTO.userId },
        { level: 1, text: fixture.TEST_USER_DTO.userId },
        { level: 0, text: 'Groups' }
      ])

      BrowserTest.simulateFilterChange(
        wrapper,
        fixture.ANOTHER_GROUP_DTO.code.toUpperCase()
      )
      wrapper.update()

      BrowserTest.expectFilter(
        wrapper,
        fixture.ANOTHER_GROUP_DTO.code.toUpperCase()
      )
      BrowserTest.expectNodes(wrapper, [
        { level: 0, text: 'Users' },
        { level: 1, text: fixture.ANOTHER_USER_DTO.userId },
        { level: 2, text: fixture.ANOTHER_GROUP_DTO.code },
        { level: 0, text: 'Groups' },
        { level: 1, text: fixture.ANOTHER_GROUP_DTO.code },
        { level: 2, text: fixture.ANOTHER_USER_DTO.userId }
      ])

      done()
    })
  })
})
