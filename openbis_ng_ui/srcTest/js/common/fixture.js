import openbis from '@src/js/services/openbis.js'

const TEST_USER = 'test-user'
const TEST_PASSWORD = 'test-password'
const TEST_SESSION_TOKEN = 'test-session-token'

const TEST_USER_DTO = new openbis.Person()
TEST_USER_DTO.setUserId('test-user')
TEST_USER_DTO.setFirstName('test-first-name')
TEST_USER_DTO.setLastName('test-last-name')

const ANOTHER_USER_DTO = new openbis.Person()
ANOTHER_USER_DTO.setUserId('another-user')
ANOTHER_USER_DTO.setFirstName('another-first-name')
ANOTHER_USER_DTO.setLastName('another-last-name')

const ALL_USERS_GROUP_DTO = new openbis.AuthorizationGroup()
ALL_USERS_GROUP_DTO.setCode('all-users-group')
ALL_USERS_GROUP_DTO.setUsers([TEST_USER_DTO, ANOTHER_USER_DTO])

const TEST_GROUP_DTO = new openbis.AuthorizationGroup()
TEST_GROUP_DTO.setCode('test-group')
TEST_GROUP_DTO.setUsers([TEST_USER_DTO])

const ANOTHER_GROUP_DTO = new openbis.AuthorizationGroup()
ANOTHER_GROUP_DTO.setCode('another-group')
ANOTHER_GROUP_DTO.setUsers([ANOTHER_USER_DTO])

function object(type, id) {
  return { type, id }
}

export default {
  TEST_USER,
  TEST_PASSWORD,
  TEST_SESSION_TOKEN,
  TEST_USER_DTO,
  ANOTHER_USER_DTO,
  ALL_USERS_GROUP_DTO,
  TEST_GROUP_DTO,
  ANOTHER_GROUP_DTO,
  object
}
