const TEST_USER = 'test-user'
const TEST_PASSWORD = 'test-password'
const TEST_SESSION_TOKEN = 'test-session-token'

const TEST_USER_DTO = {
  userId: 'test-user',
  firstName: 'test-first-name',
  lastName: 'test-last-name'
}

const ANOTHER_USER_DTO = {
  userId: 'another-user',
  firstName: 'another-first-name',
  lastName: 'another-last-name'
}

const ALL_USERS_GROUP_DTO = {
  code: 'all-users-group',
  users: [TEST_USER_DTO, ANOTHER_USER_DTO]
}

const TEST_GROUP_DTO = {
  code: 'test-group',
  users: [TEST_USER_DTO]
}

const ANOTHER_GROUP_DTO = {
  code: 'another-group',
  users: [ANOTHER_USER_DTO]
}

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
