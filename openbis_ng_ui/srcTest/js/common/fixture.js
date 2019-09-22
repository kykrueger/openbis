export const TEST_USER = 'test-user'
export const TEST_PASSWORD = 'test-password'
export const TEST_SESSION_TOKEN = 'test-session-token'

export const TEST_USER_DTO = {
  userId: 'test-user',
  firstName: 'test-first-name',
  lastName: 'test-last-name'
}

export const ANOTHER_USER_DTO = {
  userId: 'another-user',
  firstName: 'another-first-name',
  lastName: 'another-last-name'
}

export const ALL_USERS_GROUP_DTO = {
  code: 'all-users-group',
  users: [TEST_USER_DTO, ANOTHER_USER_DTO]
}

export const TEST_GROUP_DTO = {
  code: 'test-group',
  users: [TEST_USER_DTO]
}

export const ANOTHER_GROUP_DTO = {
  code: 'another-group',
  users: [ANOTHER_USER_DTO]
}

export function object(type, id) {
  return { type, id }
}
