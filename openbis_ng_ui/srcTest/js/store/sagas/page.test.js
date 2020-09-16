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
    let object1 = {
      type: objectType.OBJECT_TYPE,
      id: fixture.TEST_SAMPLE_TYPE_DTO.code
    }
    let object2 = {
      type: objectType.COLLECTION_TYPE,
      id: fixture.TEST_EXPERIMENT_TYPE_DTO.code
    }
    let object3 = {
      type: objectType.VOCABULARY_TYPE,
      id: fixture.TEST_VOCABULARY_DTO.code
    }

    store.dispatch(actions.objectOpen(pages.TYPES, object1.type, object1.id))

    expectSelectedObject(pages.TYPES, object1)
    expectOpenObjects(pages.TYPES, [object1])

    store.dispatch(actions.objectOpen(pages.TYPES, object2.type, object2.id))

    expectSelectedObject(pages.TYPES, object2)
    expectOpenObjects(pages.TYPES, [object1, object2])

    store.dispatch(actions.objectOpen(pages.TYPES, object3.type, object3.id))

    expectSelectedObject(pages.TYPES, object3)
    expectOpenObjects(pages.TYPES, [object1, object2, object3])

    store.dispatch(actions.objectClose(pages.TYPES, object1.type, object1.id))

    expectSelectedObject(pages.TYPES, object3)
    expectOpenObjects(pages.TYPES, [object2, object3])

    store.dispatch(actions.objectClose(pages.TYPES, object3.type, object3.id))

    expectSelectedObject(pages.TYPES, object2)
    expectOpenObjects(pages.TYPES, [object2])

    store.dispatch(actions.objectClose(pages.TYPES, object2.type, object2.id))

    expectSelectedObject(pages.TYPES, null)
    expectOpenObjects(pages.TYPES, [])
  })
})

function expectSelectedObject(page, object) {
  expect(selectors.getSelectedObject(store.getState(), page)).toEqual(object)
}

function expectOpenObjects(page, objects) {
  expect(selectors.getOpenObjects(store.getState(), page)).toEqual(objects)
}
