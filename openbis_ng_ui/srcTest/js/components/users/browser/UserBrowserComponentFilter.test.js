import UserBrowserComponentTest from '@srcTest/js/components/users/browser/UserBrowserComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new UserBrowserComponentTest()
  common.beforeEach()
})

describe(UserBrowserComponentTest.SUITE, () => {
  test('filter', testFilter)
})

async function testFilter() {
  const browser = await common.mount()

  browser.getFilter().change(fixture.ANOTHER_USER_GROUP_DTO.code.toUpperCase())
  await browser.update()

  browser.expectJSON({
    filter: {
      value: fixture.ANOTHER_USER_GROUP_DTO.code.toUpperCase()
    },
    nodes: [
      { level: 0, text: 'Groups' },
      { level: 1, text: fixture.ANOTHER_USER_GROUP_DTO.code }
    ]
  })

  browser.getFilter().getClearIcon().click()
  await browser.update()

  browser.expectJSON({
    filter: {
      value: null
    },
    nodes: [
      { level: 0, text: 'Users' },
      { level: 1, text: fixture.ANOTHER_USER_DTO.userId },
      { level: 1, text: fixture.TEST_USER_DTO.userId },
      { level: 0, text: 'Groups' },
      { level: 1, text: fixture.ALL_USERS_GROUP_DTO.code },
      { level: 1, text: fixture.ANOTHER_USER_GROUP_DTO.code },
      { level: 1, text: fixture.TEST_USER_GROUP_DTO.code }
    ]
  })
}
