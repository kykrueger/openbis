import * as actions from '../../../src/store/actions/actions.js'
import * as selectors from '../../../src/store/selectors/selectors.js'
import * as objectType from '../../../src/store/consts/objectType.js'
import * as pages from '../../../src/store/consts/pages.js'
import { createStore } from '../../../src/store/store.js'
import * as fixture from './fixture.js'

jest.mock('../../../src/services/openbis.js')

let store = null

beforeEach(() => {
  jest.resetAllMocks()
  store = createStore()
})

describe('page', () => {

  test('objectOpen objectClose', () => {
    let object1 = fixture.object(objectType.USER, fixture.TEST_USER_DTO.userId)
    let object2 = fixture.object(objectType.USER, fixture.ANOTHER_USER_DTO.userId)
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

function expectSelectedObject(page, object){
  expect(selectors.getSelectedObject(store.getState(), page)).toEqual(object)
}

function expectOpenObjects(page, objects){
  expect(selectors.getOpenObjects(store.getState(), page)).toEqual(objects)
}
