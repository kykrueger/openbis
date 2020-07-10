import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import BrowserWrapper from '@srcTest/js/components/common/browser/wrapper/BrowserWrapper.js'
import UserBrowser from '@src/js/components/users/browser/UserBrowser.jsx'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new ComponentTest(
    () => <UserBrowser />,
    wrapper => new BrowserWrapper(wrapper)
  )
  common.beforeEach()
})

describe('browser', () => {
  test('test', async () => {
    openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
    openbis.mockSearchGroups([
      fixture.TEST_GROUP_DTO,
      fixture.ANOTHER_GROUP_DTO,
      fixture.ALL_USERS_GROUP_DTO
    ])

    const browser = await common.mount()

    browser.expectJSON({
      filter: {
        value: null
      },
      nodes: [
        { level: 0, text: 'Users' },
        { level: 0, text: 'Groups' }
      ]
    })

    browser.getNodes()[0].getIcon().click()
    await browser.update()

    browser.expectJSON({
      filter: {
        value: null
      },
      nodes: [
        { level: 0, text: 'Users' },
        { level: 1, text: fixture.ANOTHER_USER_DTO.userId },
        { level: 1, text: fixture.TEST_USER_DTO.userId },
        { level: 0, text: 'Groups' }
      ]
    })

    browser.getFilter().change(fixture.ANOTHER_GROUP_DTO.code.toUpperCase())
    await browser.update()

    browser.expectJSON({
      filter: {
        value: fixture.ANOTHER_GROUP_DTO.code.toUpperCase()
      },
      nodes: [
        { level: 0, text: 'Users' },
        { level: 1, text: fixture.ANOTHER_USER_DTO.userId },
        { level: 2, text: fixture.ANOTHER_GROUP_DTO.code },
        { level: 0, text: 'Groups' },
        { level: 1, text: fixture.ANOTHER_GROUP_DTO.code },
        { level: 2, text: fixture.ANOTHER_USER_DTO.userId }
      ]
    })
  })
})
