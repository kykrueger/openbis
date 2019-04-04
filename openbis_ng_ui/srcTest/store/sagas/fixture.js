export const TEST_USER = 'test-user'
export const TEST_PASSWORD = 'test-password'
export const TEST_SESSION_TOKEN = 'test-session-token'

export const TEST_USER_OBJECT = {
  userId: 'test-user',
  firstName: 'test-first-name',
  lastName: 'test-last-name'
}

export const ANOTHER_USER_OBJECT = {
  userId: 'another-user',
  firstName: 'another-first-name',
  lastName: 'another-last-name'
}

export const ALL_USERS_GROUP_OBJECT = {
  code: 'all-users-group',
  users: [ TEST_USER_OBJECT, ANOTHER_USER_OBJECT ]
}

export const TEST_GROUP_OBJECT = {
  code: 'test-group',
  users: [ TEST_USER_OBJECT ]
}

export const ANOTHER_GROUP_OBJECT = {
  code: 'another-group',
  users: [ ANOTHER_USER_OBJECT ]
}
