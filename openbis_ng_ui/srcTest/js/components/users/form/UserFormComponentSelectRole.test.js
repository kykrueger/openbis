import UserFormComponentTest from '@srcTest/js/components/users/form/UserFormComponentTest.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserFormComponentTest()
  common.beforeEach()
})

describe(UserFormComponentTest.SUITE, () => {
  test('select role', testSelectRole)
})

async function testSelectRole() {
  const {
    mySpace,
    mySpaceAdminAssignment,
    myProjectAdminAssignment
  } = common.getTestData()

  const user = new openbis.Person()
  user.setUserId('test-user')
  user.setRoleAssignments([mySpaceAdminAssignment, myProjectAdminAssignment])

  common.facade.loadSpaces.mockReturnValue(Promise.resolve([mySpace]))
  common.facade.loadUserGroups.mockReturnValue(Promise.resolve([]))

  const form = await common.mountExisting(user)

  form.expectJSON({
    rolesGrid: {
      rows: [
        {
          values: {
            inheritedFrom: null,
            level: mySpaceAdminAssignment.getRoleLevel(),
            space: mySpaceAdminAssignment.space.code,
            project: '(all)',
            role: mySpaceAdminAssignment.getRole()
          },
          selected: false
        },
        {
          values: {
            inheritedFrom: null,
            level: myProjectAdminAssignment.getRoleLevel(),
            space: myProjectAdminAssignment.project.space.code,
            project: myProjectAdminAssignment.project.code,
            role: myProjectAdminAssignment.getRole()
          },
          selected: false
        }
      ]
    },
    parameters: {
      user: {
        title: 'User',
        userId: {
          label: 'User Id',
          value: user.getUserId(),
          mode: 'view'
        }
      }
    }
  })

  form.getRolesGrid().getRows()[0].click()
  await form.update()

  form.expectJSON({
    rolesGrid: {
      rows: [
        {
          values: {
            inheritedFrom: null,
            level: mySpaceAdminAssignment.getRoleLevel(),
            space: mySpaceAdminAssignment.space.code,
            project: '(all)',
            role: mySpaceAdminAssignment.getRole()
          },
          selected: true
        },
        {
          values: {
            inheritedFrom: null,
            level: myProjectAdminAssignment.getRoleLevel(),
            space: myProjectAdminAssignment.project.space.code,
            project: myProjectAdminAssignment.project.code,
            role: myProjectAdminAssignment.getRole()
          },
          selected: false
        }
      ]
    },
    parameters: {
      role: {
        title: 'Role',
        level: {
          label: 'Level',
          value: mySpaceAdminAssignment.getRoleLevel(),
          mode: 'view'
        },
        space: {
          label: 'Space',
          value: mySpaceAdminAssignment.space.code,
          mode: 'view'
        },
        project: null,
        role: {
          label: 'Role',
          value: mySpaceAdminAssignment.getRole(),
          mode: 'view'
        }
      }
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    rolesGrid: {
      rows: [
        {
          values: {
            inheritedFrom: null,
            level: mySpaceAdminAssignment.getRoleLevel(),
            space: mySpaceAdminAssignment.space.code,
            project: '(all)',
            role: mySpaceAdminAssignment.getRole()
          },
          selected: true
        },
        {
          values: {
            inheritedFrom: null,
            level: myProjectAdminAssignment.getRoleLevel(),
            space: myProjectAdminAssignment.project.space.code,
            project: myProjectAdminAssignment.project.code,
            role: myProjectAdminAssignment.getRole()
          },
          selected: false
        }
      ]
    },
    parameters: {
      role: {
        title: 'Role',
        level: {
          label: 'Level',
          value: mySpaceAdminAssignment.getRoleLevel(),
          mode: 'edit'
        },
        space: {
          label: 'Space',
          value: mySpaceAdminAssignment.space.code,
          mode: 'edit'
        },
        project: null,
        role: {
          label: 'Role',
          value: mySpaceAdminAssignment.getRole(),
          mode: 'edit'
        }
      }
    }
  })
}
