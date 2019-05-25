import React from 'react'
import { mount } from 'enzyme'
import ObjectType from '../../../../src/components/types/objectType/ObjectType.jsx'
import { facade, dto } from '../../../../src/services/openbis.js'
import * as actions from '../../../../src/store/actions/actions.js'
import { createStore } from '../../../../src/store/store.js'

jest.mock('../../../../src/services/openbis.js')

let store = null

beforeEach(() => {
  jest.resetAllMocks()
  store = createStore()
})

describe('browser', () => {

  test('test', (done) => {

    dto.SampleTypeFetchOptions.mockImplementation(() => ({
      withPropertyAssignments: () => ({
        withPropertyType: () => ({
          withMaterialType: () => ({}),
          withVocabulary: () => ({})
        }),
        sortBy: () => ({
          ordinal: () => ({})
        })
      })
    }))

    dto.PropertyTypeFetchOptions.mockImplementation(() => ({
      withVocabulary: () => ({
        withTerms: () => ({})
      }),
      withMaterialType: () => ({})
    }))

    facade.getSampleTypes.mockReturnValue(Promise.resolve({
      'TEST_OBJECT_TYPE': {
        code: 'TEST_OBJECT_TYPE',
        propertyAssignments: []
      }
    }))

    facade.searchPropertyTypes.mockReturnValue(Promise.resolve({
      objects: [{
        code: 'TEST_PROPERTY_TYPE'
      }]
    }))

    store.dispatch(actions.init())

    let wrapper = mount(<ObjectType store={store} objectId="TEST_OBJECT_TYPE"/>)

    setTimeout(()=>{
      wrapper.update()

      expectTitle(wrapper, 'TEST_OBJECT_TYPE')

      done()
    }, 0)
  })

})

function expectTitle(wrapper, expectedTitle){
  const actualTitle = wrapper.find('ObjectTypeTitle').text()
  expect(actualTitle).toEqual(expectedTitle)
}
