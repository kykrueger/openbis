import actions from '@src/js/store/actions/actions.js'
import selectors from '@src/js/store/selectors/selectors.js'
import objectType from '@src/js/common/consts/objectType.js'
import pages from '@src/js/common/consts/pages.js'
import { createStore } from '@src/js/store/store.js'
import fixture from '@srcTest/js/common/fixture.js'

let store = null

beforeEach(() => {
  jest.resetAllMocks()
  store = createStore()
})

describe('page', () => {
  test('objectOpen objectClose', () => {
    let object1 = fixture.object(objectType.USER, fixture.TEST_USER_DTO.userId)
    let object2 = fixture.object(
      objectType.USER,
      fixture.ANOTHER_USER_DTO.userId
    )
    let object3 = fixture.object(objectType.GROUP, fixture.TEST_GROUP_DTO.code)

    store.dispatch(actions.objectOpen(pages.USERS, object1.type, object1.id))

    expectSelectedObject(pages.USERS, object1)
    expectOpenObjects(pages.USERS, [object1])

    store.dispatch(actions.objectOpen(pages.USERS, object2.type, object2.id))

    expectSelectedObject(pages.USERS, object2)
    expectOpenObjects(pages.USERS, [object1, object2])

    store.dispatch(actions.objectOpen(pages.USERS, object3.type, object3.id))

    expectSelectedObject(pages.USERS, object3)
    expectOpenObjects(pages.USERS, [object1, object2, object3])

    store.dispatch(actions.objectClose(pages.USERS, object1.type, object1.id))

    expectSelectedObject(pages.USERS, object3)
    expectOpenObjects(pages.USERS, [object2, object3])

    store.dispatch(actions.objectClose(pages.USERS, object3.type, object3.id))

    expectSelectedObject(pages.USERS, object2)
    expectOpenObjects(pages.USERS, [object2])

    store.dispatch(actions.objectClose(pages.USERS, object2.type, object2.id))

    expectSelectedObject(pages.USERS, null)
    expectOpenObjects(pages.USERS, [])
  })
})

function expectSelectedObject(page, object) {
  let getSelectedObject = selectors.createGetSelectedObject()
  expect(getSelectedObject(store.getState(), page)).toEqual(object)
}

function expectOpenObjects(page, objects) {
  expect(selectors.getOpenObjects(store.getState(), page)).toEqual(objects)
}
