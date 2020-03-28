import _ from 'lodash'
import UsersBrowserController from '@src/js/components/users/browser/UsersBrowserController.js'
import ComponentState from '@srcTest/js/common/ComponentState.js'
import openbis from '@src/js/services/openbis.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import actions from '@src/js/store/actions/actions.js'
import fixture from '@srcTest/js/common/fixture.js'

let controller = null
let state = null

beforeEach(() => {
  jest.resetAllMocks()
  controller = new UsersBrowserController()
  state = ComponentState.fromController(controller)
})

describe('browser', () => {
  test('load', async () => {
    mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
    mockSearchGroups([
      fixture.TEST_GROUP_DTO,
      fixture.ANOTHER_GROUP_DTO,
      fixture.ALL_USERS_GROUP_DTO
    ])

    await controller.load()

    expectNodes(controller.getNodes(), [
      node(['users'], false, false, [
        node(['users', fixture.ANOTHER_USER_DTO.userId], false, false, [
          node([
            'users',
            fixture.ANOTHER_USER_DTO.userId,
            fixture.ALL_USERS_GROUP_DTO.code
          ]),
          node([
            'users',
            fixture.ANOTHER_USER_DTO.userId,
            fixture.ANOTHER_GROUP_DTO.code
          ])
        ]),
        node(['users', fixture.TEST_USER_DTO.userId], false, false, [
          node([
            'users',
            fixture.TEST_USER_DTO.userId,
            fixture.ALL_USERS_GROUP_DTO.code
          ]),
          node([
            'users',
            fixture.TEST_USER_DTO.userId,
            fixture.TEST_GROUP_DTO.code
          ])
        ])
      ]),
      node(['groups'], false, false, [
        node(['groups', fixture.ALL_USERS_GROUP_DTO.code], false, false, [
          node([
            'groups',
            fixture.ALL_USERS_GROUP_DTO.code,
            fixture.ANOTHER_USER_DTO.userId
          ]),
          node([
            'groups',
            fixture.ALL_USERS_GROUP_DTO.code,
            fixture.TEST_USER_DTO.userId
          ])
        ]),
        node(['groups', fixture.ANOTHER_GROUP_DTO.code], false, false, [
          node([
            'groups',
            fixture.ANOTHER_GROUP_DTO.code,
            fixture.ANOTHER_USER_DTO.userId
          ])
        ]),
        node(['groups', fixture.TEST_GROUP_DTO.code], false, false, [
          node([
            'groups',
            fixture.TEST_GROUP_DTO.code,
            fixture.TEST_USER_DTO.userId
          ])
        ])
      ])
    ])

    expectNoActions(state)
  })

  test('filter', async () => {
    mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
    mockSearchGroups([
      fixture.TEST_GROUP_DTO,
      fixture.ANOTHER_GROUP_DTO,
      fixture.ALL_USERS_GROUP_DTO
    ])

    await controller.load()
    controller.filterChange(fixture.ANOTHER_GROUP_DTO.code.toUpperCase())

    expectNodes(controller.getNodes(), [
      node(['users'], true, false, [
        node(['users', fixture.ANOTHER_USER_DTO.userId], true, false, [
          node([
            'users',
            fixture.ANOTHER_USER_DTO.userId,
            fixture.ANOTHER_GROUP_DTO.code
          ])
        ])
      ]),
      node(['groups'], true, false, [
        node(['groups', fixture.ANOTHER_GROUP_DTO.code], true, false, [
          node([
            'groups',
            fixture.ANOTHER_GROUP_DTO.code,
            fixture.ANOTHER_USER_DTO.userId
          ])
        ])
      ])
    ])

    expectNoActions(state)
  })

  test('select node', async () => {
    mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
    mockSearchGroups([])

    await controller.load()
    controller.nodeSelect(nodeId(['users', fixture.TEST_USER_DTO.userId]))

    expectNodes(controller.getNodes(), [
      node(['users'], false, false, [
        node(['users', fixture.ANOTHER_USER_DTO.userId], false, false),
        node(['users', fixture.TEST_USER_DTO.userId], false, true)
      ]),
      node(['groups'])
    ])

    expectOpenUserAction(state, fixture.TEST_USER_DTO.userId)
  })

  test('select another node', async () => {
    mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
    mockSearchGroups([])

    await controller.load()
    controller.nodeSelect(nodeId(['users', fixture.TEST_USER_DTO.userId]))
    controller.nodeSelect(nodeId(['users', fixture.ANOTHER_USER_DTO.userId]))

    expectNodes(controller.getNodes(), [
      node(['users'], false, false, [
        node(['users', fixture.ANOTHER_USER_DTO.userId], false, true),
        node(['users', fixture.TEST_USER_DTO.userId], false, false)
      ]),
      node(['groups'])
    ])

    expectOpenUserAction(state, fixture.TEST_USER_DTO.userId)
    expectOpenUserAction(state, fixture.ANOTHER_USER_DTO.userId)
  })

  test('select virtual node', async () => {
    mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
    mockSearchGroups([])

    await controller.load()
    controller.nodeSelect(nodeId(['users']))

    expectNodes(controller.getNodes(), [
      node(['users'], false, true, [
        node(['users', fixture.ANOTHER_USER_DTO.userId], false, false),
        node(['users', fixture.TEST_USER_DTO.userId], false, false)
      ]),
      node(['groups'])
    ])

    expectNoActions(state)
  })

  test('select two nodes that represent the same object', async () => {
    mockSearchPersons([fixture.TEST_USER_DTO])
    mockSearchGroups([fixture.TEST_GROUP_DTO])

    await controller.load()
    controller.nodeSelect(nodeId(['users', fixture.TEST_USER_DTO.userId]))

    expectNodes(controller.getNodes(), [
      node(['users'], false, false, [
        node(['users', fixture.TEST_USER_DTO.userId], false, true, [
          node(
            [
              'users',
              fixture.TEST_USER_DTO.userId,
              fixture.TEST_GROUP_DTO.code
            ],
            false,
            false
          )
        ])
      ]),
      node(['groups'], false, false, [
        node(['groups', fixture.TEST_GROUP_DTO.code], false, false, [
          node(
            [
              'groups',
              fixture.TEST_GROUP_DTO.code,
              fixture.TEST_USER_DTO.userId
            ],
            false,
            true
          )
        ])
      ])
    ])

    expectOpenUserAction(state, fixture.TEST_USER_DTO.userId)
    controller.nodeSelect(nodeId(['groups', fixture.TEST_GROUP_DTO.code]))

    expectNodes(controller.getNodes(), [
      node(['users'], false, false, [
        node(['users', fixture.TEST_USER_DTO.userId], false, false, [
          node(
            [
              'users',
              fixture.TEST_USER_DTO.userId,
              fixture.TEST_GROUP_DTO.code
            ],
            false,
            true
          )
        ])
      ]),
      node(['groups'], false, false, [
        node(['groups', fixture.TEST_GROUP_DTO.code], false, true, [
          node(
            [
              'groups',
              fixture.TEST_GROUP_DTO.code,
              fixture.TEST_USER_DTO.userId
            ],
            false,
            false
          )
        ])
      ])
    ])

    expectOpenGroupAction(state, fixture.TEST_GROUP_DTO.code)
  })

  test('expand and collapse node', async () => {
    mockSearchPersons([])
    mockSearchGroups([fixture.TEST_GROUP_DTO])

    await controller.load()
    controller.nodeExpand(nodeId(['groups']))

    expectNodes(controller.getNodes(), [
      node(['users']),
      node(['groups'], true, false, [
        node(['groups', fixture.TEST_GROUP_DTO.code])
      ])
    ])

    expectNoActions(state)
    controller.nodeCollapse(nodeId(['groups']))

    expectNodes(controller.getNodes(), [
      node(['users']),
      node(['groups'], false, false, [
        node(['groups', fixture.TEST_GROUP_DTO.code])
      ])
    ])

    expectNoActions(state)
  })
})

function nodeId(idParts) {
  return idParts.join('/')
}

function node(idParts, expanded = false, selected = false, children) {
  let node = {
    id: nodeId(idParts),
    expanded,
    selected
  }

  if (children !== undefined) {
    node['children'] = children
  }

  return node
}

function mockSearchPersons(persons) {
  const searchPersonResult = new openbis.SearchResult()
  searchPersonResult.setObjects(persons)

  openbis.searchPersons.mockReturnValue(Promise.resolve(searchPersonResult))
}

function mockSearchGroups(groups) {
  const searchGroupsResult = new openbis.SearchResult()
  searchGroupsResult.setObjects(groups)

  openbis.searchAuthorizationGroups.mockReturnValue(
    Promise.resolve(searchGroupsResult)
  )
}

function expectNodes(actualNodes, expectedNodes) {
  function removeKeys(nodes, keys) {
    if (!nodes) {
      return
    }
    nodes.forEach(node => {
      _.keys(node).forEach(key => {
        if (!keys.has(key)) {
          delete node[key]
        }
      })
      removeKeys(node.children, keys)
    })
  }

  const keys = new Set(['id', 'expanded', 'selected', 'children'])

  var actualNodesClone = _.cloneDeep(actualNodes)
  removeKeys(actualNodesClone, keys)

  expect(actualNodesClone).toEqual(expectedNodes)
}

function expectOpenUserAction(state, userId) {
  expect(state.getDispatch()).toHaveBeenCalledWith(
    actions.objectOpen(pages.USERS, objectType.USER, userId)
  )
}

function expectOpenGroupAction(state, groupId) {
  expect(state.getDispatch()).toHaveBeenCalledWith(
    actions.objectOpen(pages.USERS, objectType.GROUP, groupId)
  )
}

function expectNoActions(state) {
  expect(state.getDispatch()).toHaveBeenCalledTimes(0)
}
