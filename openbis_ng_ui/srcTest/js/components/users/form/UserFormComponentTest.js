import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import UserForm from '@src/js/components/users/form/UserForm.jsx'
import UserFormWrapper from '@srcTest/js/components/users/form/wrapper/UserFormWrapper.js'
import UserFormController from '@src/js/components/users/form/UserFormController.js'
import UserFormFacade from '@src/js/components/users/form/UserFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'

jest.mock('@src/js/components/users/form/UserFormFacade')

export default class UserFormComponentTest extends ComponentTest {
  static SUITE = 'UserFormComponent'

  constructor() {
    super(
      object => <UserForm object={object} controller={this.controller} />,
      wrapper => new UserFormWrapper(wrapper)
    )
    this.facade = null
    this.controller = null
  }

  async beforeEach() {
    super.beforeEach()

    this.facade = new UserFormFacade()
    this.controller = new UserFormController(this.facade)
  }

  async mountNew() {
    return await this.mount({
      type: objectTypes.NEW_USER
    })
  }

  async mountExisting(user) {
    this.facade.loadUser.mockReturnValue(Promise.resolve(user))

    return await this.mount({
      id: user.getUserId(),
      type: objectTypes.USER
    })
  }

  getTestData() {
    const mySpace = new openbis.Space()
    mySpace.setCode('my-space')

    const myProject = new openbis.Project()
    myProject.setCode('my-project')
    myProject.setSpace(mySpace)

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

    const instanceAdminAssignment = new openbis.RoleAssignment()
    instanceAdminAssignment.setRoleLevel(openbis.RoleLevel.INSTANCE)
    instanceAdminAssignment.setRole(openbis.Role.ADMIN)

    const mySpaceAdminAssignment = new openbis.RoleAssignment()
    mySpaceAdminAssignment.setRoleLevel(openbis.RoleLevel.SPACE)
    mySpaceAdminAssignment.setRole(openbis.Role.ADMIN)
    mySpaceAdminAssignment.setSpace(mySpace)

    const myProjectAdminAssignment = new openbis.RoleAssignment()
    myProjectAdminAssignment.setRoleLevel(openbis.RoleLevel.PROJECT)
    myProjectAdminAssignment.setRole(openbis.Role.ADMIN)
    myProjectAdminAssignment.setProject(myProject)

    return {
      mySpace,
      myProject,
      testSpace,
      testProject,
      instanceObserverGroup,
      instanceObserverGroupAssignment,
      testSpacePowerUserGroup,
      testSpacePowerUserGroupAssignment,
      testProjectAdminGroup,
      testProjectAdminGroupAssignment,
      instanceAdminAssignment,
      mySpaceAdminAssignment,
      myProjectAdminAssignment
    }
  }
}
