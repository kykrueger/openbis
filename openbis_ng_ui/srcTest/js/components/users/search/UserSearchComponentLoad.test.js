import UserSearchComponent from '@srcTest/js/components/users/search/UserSearchComponent.js'
import UserSearchTestData from '@srcTest/js/components/users/search/UserSearchTestData.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserSearchComponent()
  common.beforeEach()
})

describe(UserSearchComponent.SUITE, () => {
  test('load with searchText (results found)', async () =>
    await testLoadWithSearchText(true))
  test('load with searchText (no results)', async () =>
    await testLoadWithSearchText(false))
  test('load with objectType (results found)', async () =>
    await testLoadWithObjectType(true))
  test('load with objectType (no results)', async () =>
    await testLoadWithObjectType(false))
})

async function testLoadWithSearchText(resultsFound) {
  const {
    testUser,
    testUser2,
    anotherUser,
    testUserGroup,
    anotherUserGroup
  } = UserSearchTestData

  openbis.mockSearchPersons(
    resultsFound ? [testUser, testUser2, anotherUser] : []
  )
  openbis.mockSearchGroups(
    resultsFound ? [testUserGroup, anotherUserGroup] : []
  )

  const form = await common.mount({ searchText: 'test' })

  if (resultsFound) {
    form.expectJSON({
      messages: [],
      users: {
        columns: [
          {
            name: 'userId',
            label: 'User Id'
          },
          {
            name: 'firstName',
            label: 'First Name'
          },
          {
            name: 'lastName',
            label: 'Last Name'
          },
          {
            name: 'email',
            label: 'Email'
          },
          {
            name: 'space',
            label: 'Home Space'
          },
          {
            name: 'active',
            label: 'Active'
          }
        ],
        rows: [
          {
            values: {
              userId: testUser.getUserId(),
              firstName: testUser.getFirstName(),
              lastName: testUser.getLastName()
            }
          },
          {
            values: {
              userId: testUser2.getUserId(),
              firstName: testUser2.getFirstName(),
              lastName: testUser2.getLastName()
            }
          }
        ]
      },
      userGroups: {
        columns: [
          {
            name: 'code',
            label: 'Code'
          },
          {
            name: 'description',
            label: 'Description'
          }
        ],
        rows: [
          {
            values: {
              code: testUserGroup.getCode(),
              description: testUserGroup.getDescription()
            }
          }
        ]
      }
    })
  } else {
    form.expectJSON({
      messages: [
        {
          text: 'No results found.',
          type: 'info'
        }
      ],
      users: null,
      userGroups: null
    })
  }
}

async function testLoadWithObjectType(resultsFound) {
  const { testUser, testUser2, anotherUser } = UserSearchTestData

  openbis.mockSearchPersons(
    resultsFound ? [testUser, testUser2, anotherUser] : []
  )

  const form = await common.mount({
    objectType: objectTypes.USER
  })

  form.expectJSON({
    messages: [],
    users: {
      columns: [
        {
          name: 'userId',
          label: 'User Id'
        },
        {
          name: 'firstName',
          label: 'First Name'
        },
        {
          name: 'lastName',
          label: 'Last Name'
        },
        {
          name: 'email',
          label: 'Email'
        },
        {
          name: 'space',
          label: 'Home Space'
        },
        {
          name: 'active',
          label: 'Active'
        }
      ],
      rows: resultsFound
        ? [
            {
              values: {
                userId: anotherUser.getUserId(),
                firstName: anotherUser.getFirstName(),
                lastName: anotherUser.getLastName()
              }
            },
            {
              values: {
                userId: testUser.getUserId(),
                firstName: testUser.getFirstName(),
                lastName: testUser.getLastName()
              }
            },
            {
              values: {
                userId: testUser2.getUserId(),
                firstName: testUser2.getFirstName(),
                lastName: testUser2.getLastName()
              }
            }
          ]
        : []
    },
    userGroups: null
  })
}
