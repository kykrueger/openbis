import openbis from '@srcTest/js/services/openbis.js'

const testSpace = new openbis.Space()
testSpace.setCode('TEST_SPACE')

const testUser = new openbis.Person()
testUser.setUserId('test_user')
testUser.setFirstName('Test First Name')
testUser.setLastName('Test Last Name')
testUser.setEmail('test@email.com')
testUser.setSpace(testSpace)
testUser.setActive(true)

const testUser2 = new openbis.Person()
testUser2.setUserId('test_user_2')
testUser2.setFirstName('Test First Name 2')
testUser2.setLastName('Test Last Name 2')

const anotherUser = new openbis.Person()
anotherUser.setUserId('another_user')
anotherUser.setFirstName('Another First Name')
anotherUser.setLastName('Another Last Name')

const testUserGroup = new openbis.AuthorizationGroup()
testUserGroup.setCode('TEST_USER_GROUP')
testUserGroup.setDescription('Test Description')

const anotherUserGroup = new openbis.AuthorizationGroup()
anotherUserGroup.setCode('ANOTHER_USER_GROUP')
anotherUserGroup.setDescription('Another Description')

export default {
  testUser,
  testUser2,
  anotherUser,
  testUserGroup,
  anotherUserGroup
}
