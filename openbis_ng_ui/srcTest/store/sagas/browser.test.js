import _ from 'lodash'
import { facade, dto } from '../../../src/services/openbis.js'
import * as actions from '../../../src/store/actions/actions.js'
import * as selectors from '../../../src/store/selectors/selectors.js'
import * as pages from '../../../src/common/consts/pages.js'
import * as objectType from '../../../src/common/consts/objectType.js'
import * as common from '../../../src/store/common/browser.js'
import { createStore } from '../../../src/store/store.js'
import * as fixture from '../../common/fixture.js'

jest.mock('../../../src/services/openbis.js')

let store = null

beforeEach(() => {
  jest.resetAllMocks()

  facade.login.mockReturnValue(fixture.TEST_SESSION_TOKEN)

  store = createStore()
  store.dispatch(actions.login(fixture.TEST_USER, fixture.TEST_PASSWORD))
})

describe('browser', () => {

  test('init', () => {
    facade.searchPersons.mockReturnValue({
      objects: [ fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO ]
    })

    facade.searchAuthorizationGroups.mockReturnValue({
      objects: [ fixture.TEST_GROUP_DTO, fixture.ANOTHER_GROUP_DTO, fixture.ALL_USERS_GROUP_DTO ]
    })

    dto.AuthorizationGroupFetchOptions.mockImplementation(() => {
      return {
        withUsers: function(){}
      }
    })

    store.dispatch(actions.browserInit(pages.USERS))

    const state = store.getState()
    expectNodes(selectors.createGetBrowserNodes()(state, pages.USERS), [
      node(['users'], false, false, [
        node(['users', fixture.ANOTHER_USER_DTO.userId], false, false, [
          node(['users', fixture.ANOTHER_USER_DTO.userId, fixture.ALL_USERS_GROUP_DTO.code]),
          node(['users', fixture.ANOTHER_USER_DTO.userId, fixture.ANOTHER_GROUP_DTO.code])
        ]),
        node(['users', fixture.TEST_USER_DTO.userId], false, false, [
          node(['users', fixture.TEST_USER_DTO.userId, fixture.ALL_USERS_GROUP_DTO.code]),
          node(['users', fixture.TEST_USER_DTO.userId, fixture.TEST_GROUP_DTO.code])
        ])
      ]),
      node(['groups'], false, false, [
        node(['groups', fixture.ALL_USERS_GROUP_DTO.code], false, false, [
          node(['groups', fixture.ALL_USERS_GROUP_DTO.code, fixture.ANOTHER_USER_DTO.userId]),
          node(['groups', fixture.ALL_USERS_GROUP_DTO.code, fixture.TEST_USER_DTO.userId])
        ]),
        node(['groups', fixture.ANOTHER_GROUP_DTO.code], false, false, [
          node(['groups', fixture.ANOTHER_GROUP_DTO.code, fixture.ANOTHER_USER_DTO.userId])
        ]),
        node(['groups', fixture.TEST_GROUP_DTO.code], false, false, [
          node(['groups', fixture.TEST_GROUP_DTO.code, fixture.TEST_USER_DTO.userId])
        ])
      ])
    ])

    expectSelectedObject(pages.USERS, null)
    expectOpenObjects(pages.USERS, [])
  })

  test('filter', () => {
    facade.searchPersons.mockReturnValue({
      objects: [ fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO ]
    })

    facade.searchAuthorizationGroups.mockReturnValue({
      objects: [ fixture.TEST_GROUP_DTO, fixture.ANOTHER_GROUP_DTO, fixture.ALL_USERS_GROUP_DTO ]
    })

    dto.AuthorizationGroupFetchOptions.mockImplementation(() => {
      return {
        withUsers: function(){}
      }
    })

    store.dispatch(actions.browserInit(pages.USERS))
    store.dispatch(actions.browserFilterChange(pages.USERS, fixture.ANOTHER_GROUP_DTO.code.toUpperCase()))

    const state = store.getState()
    expectNodes(selectors.createGetBrowserNodes()(state, pages.USERS), [
      node(['users'], true, false, [
        node(['users', fixture.ANOTHER_USER_DTO.userId], true, false, [
          node(['users', fixture.ANOTHER_USER_DTO.userId, fixture.ANOTHER_GROUP_DTO.code])
        ])
      ]),
      node(['groups'], true, false, [
        node(['groups', fixture.ANOTHER_GROUP_DTO.code])
      ])
    ])

    expectSelectedObject(pages.USERS, null)
    expectOpenObjects(pages.USERS, [])
  })

  test('select node', () => {
    facade.searchPersons.mockReturnValue({
      objects: [ fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO ]
    })

    facade.searchAuthorizationGroups.mockReturnValue({
      objects: []
    })

    dto.AuthorizationGroupFetchOptions.mockImplementation(() => {
      return {
        withUsers: function(){}
      }
    })

    let testUserObject = fixture.object(objectType.USER, fixture.TEST_USER_DTO.userId)

    store.dispatch(actions.browserInit(pages.USERS))
    store.dispatch(actions.browserNodeSelect(pages.USERS, nodeId(['users', fixture.TEST_USER_DTO.userId])))

    let state = store.getState()
    expectNodes(selectors.createGetBrowserNodes()(state, pages.USERS), [
      node(['users'], false, false, [
        node(['users', fixture.ANOTHER_USER_DTO.userId], false, false),
        node(['users', fixture.TEST_USER_DTO.userId], false, true)
      ]),
      node(['groups'])
    ])

    expectSelectedObject(pages.USERS, testUserObject)
    expectOpenObjects(pages.USERS, [testUserObject])
  })

  test('select another node', () => {
    facade.searchPersons.mockReturnValue({
      objects: [ fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO ]
    })

    facade.searchAuthorizationGroups.mockReturnValue({
      objects: []
    })

    dto.AuthorizationGroupFetchOptions.mockImplementation(() => {
      return {
        withUsers: function(){}
      }
    })

    let testUserObject = fixture.object(objectType.USER, fixture.TEST_USER_DTO.userId)
    let anotherUserObject = fixture.object(objectType.USER, fixture.ANOTHER_USER_DTO.userId)

    store.dispatch(actions.browserInit(pages.USERS))
    store.dispatch(actions.browserNodeSelect(pages.USERS, nodeId(['users', fixture.TEST_USER_DTO.userId])))
    store.dispatch(actions.browserNodeSelect(pages.USERS, nodeId(['users', fixture.ANOTHER_USER_DTO.userId])))

    let state = store.getState()
    expectNodes(selectors.createGetBrowserNodes()(state, pages.USERS), [
      node(['users'], false, false, [
        node(['users', fixture.ANOTHER_USER_DTO.userId], false, true),
        node(['users', fixture.TEST_USER_DTO.userId], false, false)
      ]),
      node(['groups'])
    ])

    expectSelectedObject(pages.USERS, anotherUserObject)
    expectOpenObjects(pages.USERS, [testUserObject, anotherUserObject])
  })

  test('select virtual node', () => {
    facade.searchPersons.mockReturnValue({
      objects: [ fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO ]
    })

    facade.searchAuthorizationGroups.mockReturnValue({
      objects: []
    })

    dto.AuthorizationGroupFetchOptions.mockImplementation(() => {
      return {
        withUsers: function(){}
      }
    })

    store.dispatch(actions.browserInit(pages.USERS))
    store.dispatch(actions.browserNodeSelect(pages.USERS, nodeId(['users'])))

    let state = store.getState()
    expectNodes(selectors.createGetBrowserNodes()(state, pages.USERS), [
      node(['users'], false, true, [
        node(['users', fixture.ANOTHER_USER_DTO.userId], false, false),
        node(['users', fixture.TEST_USER_DTO.userId], false, false)
      ]),
      node(['groups'])
    ])

    expectSelectedObject(pages.USERS, null)
    expectOpenObjects(pages.USERS, [])
  })

  test('select two nodes that represent the same object', () => {
    facade.searchPersons.mockReturnValue({
      objects: [ fixture.TEST_USER_DTO ]
    })

    facade.searchAuthorizationGroups.mockReturnValue({
      objects: [ fixture.TEST_GROUP_DTO ]
    })

    dto.AuthorizationGroupFetchOptions.mockImplementation(() => {
      return {
        withUsers: function(){}
      }
    })

    let testUserObject = fixture.object(objectType.USER, fixture.TEST_USER_DTO.userId)

    store.dispatch(actions.browserInit(pages.USERS))
    store.dispatch(actions.browserNodeSelect(pages.USERS, nodeId(['users', fixture.TEST_USER_DTO.userId])))

    let state = store.getState()
    expectNodes(selectors.createGetBrowserNodes()(state, pages.USERS), [
      node(['users'], false, false, [
        node(['users', fixture.TEST_USER_DTO.userId], false, true, [
          node(['users', fixture.TEST_USER_DTO.userId, fixture.TEST_GROUP_DTO.code], false, false)
        ])
      ]),
      node(['groups'], false, false, [
        node(['groups', fixture.TEST_GROUP_DTO.code], false, false, [
          node(['groups', fixture.TEST_GROUP_DTO.code, fixture.TEST_USER_DTO.userId], false, true)
        ])
      ])
    ])

    expectSelectedObject(pages.USERS, testUserObject)
    expectOpenObjects(pages.USERS, [testUserObject])

    store.dispatch(actions.browserNodeSelect(pages.USERS, nodeId(['groups', fixture.TEST_GROUP_DTO.code, fixture.TEST_USER_DTO.userId])))

    state = store.getState()
    expectNodes(selectors.createGetBrowserNodes()(state, pages.USERS), [
      node(['users'], false, false, [
        node(['users', fixture.TEST_USER_DTO.userId], false, true, [
          node(['users', fixture.TEST_USER_DTO.userId, fixture.TEST_GROUP_DTO.code], false, false)
        ])
      ]),
      node(['groups'], false, false, [
        node(['groups', fixture.TEST_GROUP_DTO.code], false, false, [
          node(['groups', fixture.TEST_GROUP_DTO.code, fixture.TEST_USER_DTO.userId], false, true)
        ])
      ])
    ])

    expectSelectedObject(pages.USERS, testUserObject)
    expectOpenObjects(pages.USERS, [testUserObject])
  })

  test('expand and collapse node', () => {
    facade.searchPersons.mockReturnValue({
      objects: []
    })

    facade.searchAuthorizationGroups.mockReturnValue({
      objects: [ fixture.TEST_GROUP_DTO ]
    })

    dto.AuthorizationGroupFetchOptions.mockImplementation(() => {
      return {
        withUsers: function(){}
      }
    })

    store.dispatch(actions.browserInit(pages.USERS))
    store.dispatch(actions.browserNodeExpand(pages.USERS, nodeId(['groups'])))

    let state = store.getState()
    expectNodes(selectors.createGetBrowserNodes()(state, pages.USERS), [
      node(['users']),
      node(['groups'], true, false, [
        node(['groups', fixture.TEST_GROUP_DTO.code])
      ])
    ])

    expectSelectedObject(pages.USERS, null)
    expectOpenObjects(pages.USERS, [])

    store.dispatch(actions.browserNodeCollapse(pages.USERS, nodeId(['groups'])))

    state = store.getState()
    expectNodes(selectors.createGetBrowserNodes()(state, pages.USERS), [
      node(['users']),
      node(['groups'], false, false, [
        node(['groups', fixture.TEST_GROUP_DTO.code])
      ])
    ])

    expectSelectedObject(pages.USERS, null)
    expectOpenObjects(pages.USERS, [])
  })

})

function nodeId(idParts){
  return idParts.join('/')
}

function node(idParts, expanded = false, selected = false, children){
  let node = {
    id: nodeId(idParts),
    expanded,
    selected
  }

  if(children !== undefined){
    node['children'] = children
  }

  return node
}

function expectNodes(actualNodes, expectedNodes){
  const keys = new Set(['id', 'expanded', 'selected', 'children'])

  var actualNodesClone = _.cloneDeep(actualNodes)
  common.mapNodes(null, actualNodesClone, (parent, node) => {
    _.keys(node).forEach(key => {
      if(!keys.has(key)){
        delete node[key]
      }
    })
    return node
  })

  expect(actualNodesClone).toEqual(expectedNodes)
}

function expectSelectedObject(page, object){
  expect(selectors.createGetSelectedObject()(store.getState(), page)).toEqual(object)
}

function expectOpenObjects(page, objects){
  expect(selectors.getOpenObjects(store.getState(), page)).toEqual(objects)
}
