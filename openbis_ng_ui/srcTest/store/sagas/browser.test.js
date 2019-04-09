import _ from 'lodash'
import openbis from '../../../src/services/openbis.js'
import * as actions from '../../../src/store/actions/actions.js'
import * as selectors from '../../../src/store/selectors/selectors.js'
import * as pages from '../../../src/store/consts/pages.js'
import * as objectType from '../../../src/store/consts/objectType.js'
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

describe('browser', () => {
  test('init', () => {
    openbis.getUsers.mockReturnValue({
      objects: [ fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO ]
    })

    openbis.getGroups.mockReturnValue({
      objects: [ fixture.TEST_GROUP_DTO, fixture.ANOTHER_GROUP_DTO, fixture.ALL_USERS_GROUP_DTO ]
    })

    store.dispatch(actions.browserInit(pages.USERS))

    const state = store.getState()
    expectNodes(selectors.getBrowserNodes(state, pages.USERS), [
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

    let selectedObject = selectors.getSelectedObject(state, pages.USERS)
    let openObjects = selectors.getOpenObjects(state, pages.USERS)

    expect(selectedObject).toEqual(null)
    expect(openObjects).toEqual([])
  })

  test('filter', () => {
    openbis.getUsers.mockReturnValue({
      objects: [ fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO ]
    })

    openbis.getGroups.mockReturnValue({
      objects: [ fixture.TEST_GROUP_DTO, fixture.ANOTHER_GROUP_DTO, fixture.ALL_USERS_GROUP_DTO ]
    })

    store.dispatch(actions.browserInit(pages.USERS))
    store.dispatch(actions.browserFilterChange(pages.USERS, fixture.ANOTHER_GROUP_DTO.code.toUpperCase()))

    const state = store.getState()
    expectNodes(selectors.getBrowserNodes(state, pages.USERS), [
      node(['users'], true, false, [
        node(['users', fixture.ANOTHER_USER_DTO.userId], true, false, [
          node(['users', fixture.ANOTHER_USER_DTO.userId, fixture.ANOTHER_GROUP_DTO.code])
        ])
      ]),
      node(['groups'], true, false, [
        node(['groups', fixture.ANOTHER_GROUP_DTO.code])
      ])
    ])

    let selectedObject = selectors.getSelectedObject(state, pages.USERS)
    let openObjects = selectors.getOpenObjects(state, pages.USERS)

    expect(selectedObject).toEqual(null)
    expect(openObjects).toEqual([])
  })

  test('selectNode', () => {
    openbis.getUsers.mockReturnValue({
      objects: [ fixture.TEST_USER_DTO ]
    })

    openbis.getGroups.mockReturnValue({
      objects: []
    })

    let object = fixture.object(objectType.USER, fixture.TEST_USER_DTO.userId)

    store.dispatch(actions.browserInit(pages.USERS))
    store.dispatch(actions.browserNodeSelect(pages.USERS, nodeId(['users', fixture.TEST_USER_DTO.userId])))

    let state = store.getState()
    expectNodes(selectors.getBrowserNodes(state, pages.USERS), [
      node(['users'], false, false, [
        node(['users', fixture.TEST_USER_DTO.userId], false, true)
      ]),
      node(['groups'])
    ])

    expect(selectors.getSelectedObject(state, pages.USERS)).toEqual(object)
    expect(selectors.getOpenObjects(state, pages.USERS)).toEqual([object])

    store.dispatch(actions.objectClose(pages.USERS, object.type, object.id))

    state = store.getState()
    expectNodes(selectors.getBrowserNodes(state, pages.USERS), [
      node(['users'], false, false, [
        node(['users', fixture.TEST_USER_DTO.userId], false, false)
      ]),
      node(['groups'])
    ])

    expect(selectors.getSelectedObject(state, pages.USERS)).toEqual(null)
    expect(selectors.getOpenObjects(state, pages.USERS)).toEqual([])
  })

  test('expandNode collapseNode', () => {
    openbis.getUsers.mockReturnValue({
      objects: []
    })

    openbis.getGroups.mockReturnValue({
      objects: [ fixture.TEST_GROUP_DTO ]
    })

    store.dispatch(actions.browserInit(pages.USERS))
    store.dispatch(actions.browserNodeExpand(pages.USERS, nodeId(['groups'])))

    let state = store.getState()
    expectNodes(selectors.getBrowserNodes(state, pages.USERS), [
      node(['users']),
      node(['groups'], true, false, [
        node(['groups', fixture.TEST_GROUP_DTO.code])
      ])
    ])

    store.dispatch(actions.browserNodeCollapse(pages.USERS, nodeId(['groups'])))

    state = store.getState()
    expectNodes(selectors.getBrowserNodes(state, pages.USERS), [
      node(['users']),
      node(['groups'], false, false, [
        node(['groups', fixture.TEST_GROUP_DTO.code])
      ])
    ])

    let selectedObject = selectors.getSelectedObject(state, pages.USERS)
    let openObjects = selectors.getOpenObjects(state, pages.USERS)

    expect(selectedObject).toEqual(null)
    expect(openObjects).toEqual([])
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
