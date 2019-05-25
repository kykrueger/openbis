import React from 'react'
import { mount } from 'enzyme'
import DragAndDropProvider from '../../../../src/components/common/dnd/DragAndDropProvider.jsx'
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
        propertyAssignments: [{
          propertyType: {
            code: 'VARCHAR_PROPERTY_TYPE',
            label: 'varchar label',
            description: 'varchar description',
            dataType: 'VARCHAR'
          },
          mandatory: false
        },{
          propertyType: {
            code: 'MATERIAL_PROPERTY_TYPE',
            label: 'material label',
            description: 'material description',
            dataType: 'MATERIAL'
          },
          mandatory: true
        }]
      }
    }))

    facade.searchPropertyTypes.mockReturnValue(Promise.resolve({
      objects: [{
        code: 'VARCHAR_PROPERTY_TYPE'
      },{
        code: 'MATERIAL_PROPERTY_TYPE'
      },{
        code: 'DICTIONARY_PROPERTY_TYPE'
      }]
    }))

    facade.searchMaterials.mockReturnValue(Promise.resolve({
      objects: [{
        code: 'MATERIAL_1'
      },{
        code: 'MATERIAL_2'
      }, {
        code: 'MATERIAL_3'
      }]
    }))

    store.dispatch(actions.init())

    let wrapper = mount(
      <DragAndDropProvider>
        <ObjectType store={store} objectId="TEST_OBJECT_TYPE"/>
      </DragAndDropProvider>
    )

    setTimeout(()=>{
      wrapper.update()

      expectTitle(wrapper, 'TEST_OBJECT_TYPE')
      expectProperty(wrapper, 0, {
        mandatory: false
      })
      expectProperty(wrapper, 1, {
        mandatory: true
      })

      done()
    }, 0)
  })

})

function expectTitle(wrapper, expectedTitle){
  const actualTitle = wrapper.find('ObjectTypeTitle').text()
  expect(actualTitle).toEqual(expectedTitle)
}

function expectProperty(wrapper, index, expectedProperty){
  let row = wrapper.find('ObjectTypePropertyRow').at(index)
  let mandatory = row.find('ObjectTypePropertyMandatory').find('Checkbox').prop('checked')
  expect({
    mandatory
  }).toEqual(expectedProperty)
}
