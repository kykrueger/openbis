import _ from 'lodash'
import openbis from '../../../src/services/openbis.js'
import * as actions from '../../../src/store/actions/actions.js'
import * as selectors from '../../../src/store/selectors/selectors.js'
import * as pages from '../../../src/store/consts/pages.js'
import * as common from '../../../src/store/common/browser.js'
import { createStore } from '../../../src/store/store.js'
import * as fixture from './fixture.js'

jest.mock('../../../src/services/openbis.js')

let store = null

beforeEach(() => {
  jest.resetAllMocks()

  openbis.login.mockReturnValue(fixture.TEST_SESSION_TOKEN)

  store = createStore()
  store.dispatch(actions.login(fixture.TEST_USER, fixture.TEST_PASSWORD))
})

test('browser init', () => {
  openbis.getUsers.mockReturnValue({
    objects: [ fixture.TEST_USER_OBJECT, fixture.ANOTHER_USER_OBJECT ]
  })

  openbis.getGroups.mockReturnValue({
    objects: [ fixture.TEST_GROUP_OBJECT, fixture.ANOTHER_GROUP_OBJECT, fixture.ALL_USERS_GROUP_OBJECT ]
  })

  store.dispatch(actions.browserInit(pages.USERS))

  const state = store.getState()
  expectNodes(selectors.getBrowserNodes(state, pages.USERS), [
    node(['users'], false, false, [
      node(['users', fixture.ANOTHER_USER_OBJECT.userId], false, false, [
        node(['users', fixture.ANOTHER_USER_OBJECT.userId, fixture.ALL_USERS_GROUP_OBJECT.code]),
        node(['users', fixture.ANOTHER_USER_OBJECT.userId, fixture.ANOTHER_GROUP_OBJECT.code])
      ]),
      node(['users', fixture.TEST_USER_OBJECT.userId], false, false, [
        node(['users', fixture.TEST_USER_OBJECT.userId, fixture.ALL_USERS_GROUP_OBJECT.code]),
        node(['users', fixture.TEST_USER_OBJECT.userId, fixture.TEST_GROUP_OBJECT.code])
      ])
    ]),
    node(['groups'], false, false, [
      node(['groups', fixture.ALL_USERS_GROUP_OBJECT.code], false, false, [
        node(['groups', fixture.ALL_USERS_GROUP_OBJECT.code, fixture.ANOTHER_USER_OBJECT.userId]),
        node(['groups', fixture.ALL_USERS_GROUP_OBJECT.code, fixture.TEST_USER_OBJECT.userId])
      ]),
      node(['groups', fixture.ANOTHER_GROUP_OBJECT.code], false, false, [
        node(['groups', fixture.ANOTHER_GROUP_OBJECT.code, fixture.ANOTHER_USER_OBJECT.userId])
      ]),
      node(['groups', fixture.TEST_GROUP_OBJECT.code], false, false, [
        node(['groups', fixture.TEST_GROUP_OBJECT.code, fixture.TEST_USER_OBJECT.userId])
      ])
    ])
  ])
})

test('browser filter', () => {
  openbis.getUsers.mockReturnValue({
    objects: [ fixture.TEST_USER_OBJECT, fixture.ANOTHER_USER_OBJECT ]
  })

  openbis.getGroups.mockReturnValue({
    objects: [ fixture.TEST_GROUP_OBJECT, fixture.ANOTHER_GROUP_OBJECT, fixture.ALL_USERS_GROUP_OBJECT ]
  })

  store.dispatch(actions.browserInit(pages.USERS))
  store.dispatch(actions.browserFilterChanged(pages.USERS, fixture.ANOTHER_GROUP_OBJECT.code.toUpperCase()))

  const state = store.getState()
  expectNodes(selectors.getBrowserNodes(state, pages.USERS), [
    node(['users'], true, false, [
      node(['users', fixture.ANOTHER_USER_OBJECT.userId], true, false, [
        node(['users', fixture.ANOTHER_USER_OBJECT.userId, fixture.ANOTHER_GROUP_OBJECT.code])
      ])
    ]),
    node(['groups'], true, false, [
      node(['groups', fixture.ANOTHER_GROUP_OBJECT.code])
    ])
  ])
})

test('browser selectNode', () => {
  openbis.getUsers.mockReturnValue({
    objects: [ fixture.TEST_USER_OBJECT ]
  })

  openbis.getGroups.mockReturnValue({
    objects: []
  })

  store.dispatch(actions.browserInit(pages.USERS))
  store.dispatch(actions.browserNodeSelected(pages.USERS, nodeId(['users', fixture.TEST_USER_OBJECT.userId])))

  const state = store.getState()
  expectNodes(selectors.getBrowserNodes(state, pages.USERS), [
    node(['users'], false, false, [
      node(['users', fixture.TEST_USER_OBJECT.userId], false, true)
    ]),
    node(['groups'])
  ])
})

test('browser expandNode collapseNode', () => {
  openbis.getUsers.mockReturnValue({
    objects: []
  })

  openbis.getGroups.mockReturnValue({
    objects: [ fixture.TEST_GROUP_OBJECT ]
  })

  store.dispatch(actions.browserInit(pages.USERS))
  store.dispatch(actions.browserNodeExpanded(pages.USERS, nodeId(['groups'])))

  let state = store.getState()
  expectNodes(selectors.getBrowserNodes(state, pages.USERS), [
    node(['users']),
    node(['groups'], true, false, [
      node(['groups', fixture.TEST_GROUP_OBJECT.code])
    ])
  ])

  store.dispatch(actions.browserNodeCollapsed(pages.USERS, nodeId(['groups'])))

  state = store.getState()
  expectNodes(selectors.getBrowserNodes(state, pages.USERS), [
    node(['users']),
    node(['groups'], false, false, [
      node(['groups', fixture.TEST_GROUP_OBJECT.code])
    ])
  ])
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
