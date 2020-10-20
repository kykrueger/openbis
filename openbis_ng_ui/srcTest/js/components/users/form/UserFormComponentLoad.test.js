import UserFormComponentTest from '@srcTest/js/components/users/form/UserFormComponentTest.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserFormComponentTest()
  common.beforeEach()
})

describe(UserFormComponentTest.SUITE, () => {
  test('load new', testLoadNew)
  test('load existing', testLoadExisting)
})

async function testLoadNew() {
  const form = await common.mountNew()

  form.expectJSON({
    groupsGrid: {
      rows: []
    },
    rolesGrid: {
      rows: []
    },
    parameters: {
      user: {
        title: 'User',
        userId: {
          label: 'User Id',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        homeSpace: {
          label: 'Home Space',
          value: null,
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      addGroup: {
        enabled: true
      },
      addRole: {
        enabled: true
      },
      remove: {
        enabled: false
      },
      save: {
        enabled: true
      },
      edit: null,
      cancel: null,
      message: null
    }
  })
}

async function testLoadExisting() {
  const mySpace = new openbis.Space()
  mySpace.setCode('my-space')

  const testSpace = new openbis.Space()
  testSpace.setCode('test-space')

  const testProject = new openbis.Project()
  testProject.setCode('test-project')
  testProject.setSpace(testSpace)

  const instanceObserverGroup = new openbis.AuthorizationGroup()
  instanceObserverGroup.setCode('instance-observer-group')

  const instanceObserverGroupAssignment = new openbis.RoleAssignment()
  instanceObserverGroupAssignment.setRoleLevel(openbis.RoleLevel.INSTANCE)
  instanceObserverGroupAssignment.setRole(openbis.Role.OBSERVER)

  instanceObserverGroup.setRoleAssignments([instanceObserverGroupAssignment])
  instanceObserverGroupAssignment.setAuthorizationGroup(instanceObserverGroup)

  const testSpacePowerUserGroup = new openbis.AuthorizationGroup()
  testSpacePowerUserGroup.setCode('test-space-power-user-group')

  const testSpacePowerUserGroupAssignment = new openbis.RoleAssignment()
  testSpacePowerUserGroupAssignment.setRoleLevel(openbis.RoleLevel.SPACE)
  testSpacePowerUserGroupAssignment.setRole(openbis.Role.POWER_USER)
  testSpacePowerUserGroupAssignment.setSpace(testSpace)

  testSpacePowerUserGroup.setRoleAssignments([
    testSpacePowerUserGroupAssignment
  ])
  testSpacePowerUserGroupAssignment.setAuthorizationGroup(
    testSpacePowerUserGroup
  )

  const testProjectAdminGroup = new openbis.AuthorizationGroup()
  testProjectAdminGroup.setCode('test-project-admin-group')

  const testProjectAdminGroupAssignment = new openbis.RoleAssignment()
  testProjectAdminGroupAssignment.setRoleLevel(openbis.RoleLevel.PROJECT)
  testProjectAdminGroupAssignment.setRole(openbis.Role.ADMIN)
  testProjectAdminGroupAssignment.setProject(testProject)

  testProjectAdminGroup.setRoleAssignments([testProjectAdminGroupAssignment])
  testProjectAdminGroupAssignment.setAuthorizationGroup(testProjectAdminGroup)

  const mySpaceAdminAssignment = new openbis.RoleAssignment()
  mySpaceAdminAssignment.setRoleLevel(openbis.RoleLevel.SPACE)
  mySpaceAdminAssignment.setRole(openbis.Role.ADMIN)
  mySpaceAdminAssignment.setSpace(mySpace)

  const user = new openbis.Person()
  user.setUserId('test-user')
  user.setFirstName('test-first-name')
  user.setLastName('test-last-name')
  user.setSpace(mySpace)
  user.setActive(true)
  user.setRoleAssignments([mySpaceAdminAssignment])

  common.facade.loadUserGroups.mockReturnValue(
    Promise.resolve([
      instanceObserverGroup,
      testSpacePowerUserGroup,
      testProjectAdminGroup
    ])
  )

  const form = await common.mountExisting(user)

  const groupsGridJSON = {
    columns: [
      {
        name: 'code',
        label: 'Code',
        filter: {
          value: null
        },
        sort: 'asc'
      },
      {
        name: 'description',
        label: 'Description',
        filter: {
          value: null
        },
        sort: null
      }
    ],
    rows: [
      instanceObserverGroup,
      testProjectAdminGroup,
      testSpacePowerUserGroup
    ].map(group => ({
      values: {
        code: group.getCode(),
        description: group.getDescription()
      },
      selected: false
    }))
  }

  const rolesGridJSON = {
    columns: [
      {
        name: 'inheritedFrom',
        label: 'Inherited From',
        filter: {
          value: null
        },
        sort: 'asc'
      },
      {
        name: 'level',
        label: 'Level',
        filter: {
          value: null
        },
        sort: null
      },
      {
        name: 'space',
        label: 'Space',
        filter: {
          value: null
        },
        sort: null
      },
      {
        name: 'project',
        label: 'Project',
        filter: {
          value: null
        },
        sort: null
      },
      {
        name: 'role',
        label: 'Role',
        filter: {
          value: null
        },
        sort: null
      }
    ],
    rows: [
      {
        values: {
          inheritedFrom: instanceObserverGroup.getCode(),
          level: instanceObserverGroupAssignment.getRoleLevel(),
          space: '(all)',
          project: '(all)',
          role: instanceObserverGroupAssignment.getRole()
        },
        selected: false
      },
      {
        values: {
          inheritedFrom: testProjectAdminGroup.getCode(),
          level: testProjectAdminGroupAssignment.getRoleLevel(),
          space: testProjectAdminGroupAssignment.project.space.code,
          project: testProjectAdminGroupAssignment.project.code,
          role: testProjectAdminGroupAssignment.getRole()
        },
        selected: false
      },
      {
        values: {
          inheritedFrom: testSpacePowerUserGroup.getCode(),
          level: testSpacePowerUserGroupAssignment.getRoleLevel(),
          space: testSpacePowerUserGroupAssignment.space.code,
          project: '(all)',
          role: testSpacePowerUserGroupAssignment.getRole()
        },
        selected: false
      },
      {
        values: {
          inheritedFrom: null,
          level: mySpaceAdminAssignment.getRoleLevel(),
          space: mySpaceAdminAssignment.space.code,
          project: '(all)',
          role: mySpaceAdminAssignment.getRole()
        },
        selected: false
      }
    ]
  }

  form.expectJSON({
    groupsGrid: groupsGridJSON,
    rolesGrid: rolesGridJSON,
    parameters: {
      user: {
        title: 'User',
        userId: {
          label: 'User Id',
          value: user.getUserId(),
          mode: 'view'
        },
        firstName: {
          label: 'First Name',
          value: user.getFirstName(),
          mode: 'view'
        },
        lastName: {
          label: 'Last Name',
          value: user.getLastName(),
          mode: 'view'
        },
        email: {
          label: 'Email',
          value: user.getEmail(),
          mode: 'view'
        },
        homeSpace: {
          label: 'Home Space',
          value: user.space.code,
          mode: 'view'
        },
        active: {
          label: 'Active',
          value: user.isActive(),
          mode: 'view'
        }
      }
    },
    buttons: {
      edit: {
        enabled: true
      },
      addGroup: null,
      addRole: null,
      remove: null,
      save: null,
      cancel: null,
      message: null
    }
  })
}
