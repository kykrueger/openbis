import React from 'react'
import { mount } from 'enzyme'
import Browser from '../../../../../src/js/components/common/browser/Browser.jsx'
import { facade, dto } from '../../../../../src/js/services/openbis.js'
import * as actions from '../../../../../src/js/store/actions/actions.js'
import * as pages from '../../../../../src/js/common/consts/pages.js'
import { createStore } from '../../../../../src/js/store/store.js'
import * as fixture from '../../../common/fixture.js'

jest.mock('../../../../../src/js/services/openbis.js')

let store = null

beforeEach(() => {
  jest.resetAllMocks()
  store = createStore()
})

describe('browser', () => {
  test('test', () => {
    facade.searchPersons.mockReturnValue({
      objects: [fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO]
    })

    facade.searchAuthorizationGroups.mockReturnValue({
      objects: [
        fixture.TEST_GROUP_DTO,
        fixture.ANOTHER_GROUP_DTO,
        fixture.ALL_USERS_GROUP_DTO
      ]
    })

    dto.AuthorizationGroupFetchOptions.mockImplementation(() => {
      return {
        withUsers: function() {}
      }
    })

    store.dispatch(actions.init())

    let wrapper = mount(<Browser store={store} page={pages.USERS} />)

    expectFilter(wrapper, '')
    expectNodes(wrapper, [
      { level: 0, text: 'Users' },
      { level: 0, text: 'Groups' }
    ])

    simulateNodeIconClick(wrapper, 'users')
    wrapper.update()

    expectFilter(wrapper, '')
    expectNodes(wrapper, [
      { level: 0, text: 'Users' },
      { level: 1, text: fixture.ANOTHER_USER_DTO.userId },
      { level: 1, text: fixture.TEST_USER_DTO.userId },
      { level: 0, text: 'Groups' }
    ])

    simulateFilterChange(wrapper, fixture.ANOTHER_GROUP_DTO.code.toUpperCase())
    wrapper.update()

    expectFilter(wrapper, fixture.ANOTHER_GROUP_DTO.code.toUpperCase())
    expectNodes(wrapper, [
      { level: 0, text: 'Users' },
      { level: 1, text: fixture.ANOTHER_USER_DTO.userId },
      { level: 2, text: fixture.ANOTHER_GROUP_DTO.code },
      { level: 0, text: 'Groups' },
      { level: 1, text: fixture.ANOTHER_GROUP_DTO.code }
    ])
  })
})

function simulateNodeIconClick(wrapper, id) {
  wrapper
    .findWhere(node => {
      return node.name() === 'BrowserNode' && node.prop('node').id === id
    })
    .find('svg')
    .first()
    .simulate('click')
}

function simulateFilterChange(wrapper, filter) {
  let input = wrapper.find('FilterField').find('input')
  input.instance().value = filter
  input.simulate('change')
}

function expectFilter(wrapper, expectedFilter) {
  const actualFilter = wrapper.find('FilterField').map(node => {
    return node.prop('filter')
  })[0]
  expect(actualFilter).toEqual(expectedFilter)
}

function expectNodes(wrapper, expectedNodes) {
  const actualNodes = wrapper.find('BrowserNode').map(node => {
    const text = node.prop('node').text
    const selected = node.prop('node').selected
    const level = node.prop('level')
    return {
      text,
      level,
      selected
    }
  })
  expect(actualNodes).toMatchObject(expectedNodes)
}
