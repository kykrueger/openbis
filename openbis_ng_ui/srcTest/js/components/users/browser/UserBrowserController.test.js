import UserBrowserController from '@src/js/components/users/browser/UserBrowserController.js'
import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import openbis from '@srcTest/js/services/openbis.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import actions from '@src/js/store/actions/actions.js'
import fixture from '@srcTest/js/common/fixture.js'

let context = null
let controller = null

beforeEach(() => {
  jest.resetAllMocks()

  context = new ComponentContext()
  controller = new UserBrowserController()
  controller.init(context)
})

describe('browser', () => {
  test('load', async () => {
    openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
    openbis.mockSearchGroups([
      fixture.TEST_GROUP_DTO,
      fixture.ANOTHER_GROUP_DTO,
      fixture.ALL_USERS_GROUP_DTO
    ])

    await controller.load()

    expect(controller.getNodes()).toMatchObject([
      {
        text: 'Users',
        expanded: false,
        selected: false
      },
      {
        text: 'Groups',
        expanded: false,
        selected: false
      }
    ])

    context.expectNoActions()
  })

  test('filter', async () => {
    openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
    openbis.mockSearchGroups([
      fixture.TEST_GROUP_DTO,
      fixture.ANOTHER_GROUP_DTO,
      fixture.ALL_USERS_GROUP_DTO
    ])

    await controller.load()
    controller.filterChange('ANOTHER')

    expect(controller.getNodes()).toMatchObject([
      {
        text: 'Users',
        expanded: true,
        selected: false,
        children: [
          {
            text: fixture.ANOTHER_USER_DTO.userId,
            expanded: true,
            selected: false,
            children: [
              {
                text: fixture.ALL_USERS_GROUP_DTO.code,
                expanded: false,
                selected: false
              },
              {
                text: fixture.ANOTHER_GROUP_DTO.code,
                expanded: false,
                selected: false
              }
            ]
          }
        ]
      },
      {
        text: 'Groups',
        expanded: true,
        selected: false,
        children: [
          {
            text: fixture.ALL_USERS_GROUP_DTO.code,
            expanded: true,
            selected: false,
            children: [
              {
                text: fixture.ANOTHER_USER_DTO.userId,
                expanded: false,
                selected: false
              }
            ]
          },
          {
            text: fixture.ANOTHER_GROUP_DTO.code,
            expanded: true,
            selected: false,
            children: [
              {
                text: fixture.ANOTHER_USER_DTO.userId,
                expanded: false,
                selected: false
              }
            ]
          }
        ]
      }
    ])

    context.expectNoActions()
  })

  test('select node', async () => {
    openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
    openbis.mockSearchGroups([])

    await controller.load()
    controller.nodeSelect('users/' + fixture.TEST_USER_DTO.userId)

    expect(controller.getNodes()).toMatchObject([
      {
        text: 'Users',
        expanded: false,
        selected: false,
        children: [
          {
            text: fixture.ANOTHER_USER_DTO.userId,
            expanded: false,
            selected: false
          },
          {
            text: fixture.TEST_USER_DTO.userId,
            expanded: false,
            selected: true
          }
        ]
      },
      {
        text: 'Groups',
        expanded: false,
        selected: false
      }
    ])

    expectOpenUserAction(fixture.TEST_USER_DTO.userId)
  })

  test('select another node', async () => {
    openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
    openbis.mockSearchGroups([])

    await controller.load()
    controller.nodeSelect('users/' + fixture.TEST_USER_DTO.userId)
    controller.nodeSelect('users/' + fixture.ANOTHER_USER_DTO.userId)

    expect(controller.getNodes()).toMatchObject([
      {
        text: 'Users',
        expanded: false,
        selected: false,
        children: [
          {
            text: fixture.ANOTHER_USER_DTO.userId,
            expanded: false,
            selected: true
          },
          {
            text: fixture.TEST_USER_DTO.userId,
            expanded: false,
            selected: false
          }
        ]
      },
      {
        text: 'Groups',
        expanded: false,
        selected: false,
        children: []
      }
    ])

    expectOpenUserAction(fixture.TEST_USER_DTO.userId)
    expectOpenUserAction(fixture.ANOTHER_USER_DTO.userId)
  })

  test('select virtual node', async () => {
    openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
    openbis.mockSearchGroups([])

    await controller.load()
    controller.nodeSelect('users')

    expect(controller.getNodes()).toMatchObject([
      {
        text: 'Users',
        expanded: false,
        selected: true
      },
      {
        text: 'Groups',
        expanded: false,
        selected: false
      }
    ])

    context.expectNoActions()
  })

  test('select two nodes that represent the same object', async () => {
    openbis.mockSearchPersons([fixture.TEST_USER_DTO])
    openbis.mockSearchGroups([fixture.TEST_GROUP_DTO])

    await controller.load()
    controller.nodeSelect('users/' + fixture.TEST_USER_DTO.userId)

    expect(controller.getNodes()).toMatchObject([
      {
        text: 'Users',
        expanded: false,
        selected: false,
        children: [
          {
            text: fixture.TEST_USER_DTO.userId,
            expanded: false,
            selected: true,
            children: [
              {
                text: fixture.TEST_GROUP_DTO.code,
                expanded: false,
                selected: false
              }
            ]
          }
        ]
      },
      {
        text: 'Groups',
        expanded: false,
        selected: false,
        children: [
          {
            text: fixture.TEST_GROUP_DTO.code,
            expanded: false,
            selected: false,
            children: [
              {
                text: fixture.TEST_USER_DTO.userId,
                expanded: false,
                selected: true
              }
            ]
          }
        ]
      }
    ])

    expectOpenUserAction(fixture.TEST_USER_DTO.userId)
    controller.nodeSelect('groups/' + fixture.TEST_GROUP_DTO.code)

    expect(controller.getNodes()).toMatchObject([
      {
        text: 'Users',
        expanded: false,
        selected: false,
        children: [
          {
            text: fixture.TEST_USER_DTO.userId,
            expanded: false,
            selected: false,
            children: [
              {
                text: fixture.TEST_GROUP_DTO.code,
                expanded: false,
                selected: true
              }
            ]
          }
        ]
      },
      {
        text: 'Groups',
        expanded: false,
        selected: false,
        children: [
          {
            text: fixture.TEST_GROUP_DTO.code,
            expanded: false,
            selected: true,
            children: [
              {
                text: fixture.TEST_USER_DTO.userId,
                expanded: false,
                selected: false
              }
            ]
          }
        ]
      }
    ])

    expectOpenGroupAction(fixture.TEST_GROUP_DTO.code)
  })

  test('expand and collapse node', async () => {
    openbis.mockSearchPersons([])
    openbis.mockSearchGroups([fixture.TEST_GROUP_DTO])

    await controller.load()
    controller.nodeExpand('groups')

    expect(controller.getNodes()).toMatchObject([
      {
        text: 'Users',
        expanded: false,
        selected: false
      },
      {
        text: 'Groups',
        expanded: true,
        selected: false
      }
    ])

    context.expectNoActions()
    controller.nodeCollapse('groups')

    expect(controller.getNodes()).toMatchObject([
      {
        text: 'Users',
        expanded: false,
        selected: false
      },
      {
        text: 'Groups',
        expanded: false,
        selected: false
      }
    ])

    context.expectNoActions()
  })
})

function expectOpenUserAction(userId) {
  context.expectAction(actions.objectOpen(pages.USERS, objectType.USER, userId))
}

function expectOpenGroupAction(groupId) {
  context.expectAction(
    actions.objectOpen(pages.USERS, objectType.GROUP, groupId)
  )
}
