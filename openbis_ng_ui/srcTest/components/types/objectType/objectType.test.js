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
  test('test', done => {
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

    dto.VocabularyTermSearchCriteria.mockImplementation(() => ({
      withVocabulary: () => ({
        withCode: () => ({
          thatEquals: () => ({})
        })
      })
    }))

    facade.getSampleTypes.mockReturnValue(
      Promise.resolve({
        TEST_OBJECT_TYPE: {
          code: 'TEST_OBJECT_TYPE',
          propertyAssignments: [
            {
              propertyType: {
                code: 'VARCHAR_PROPERTY_TYPE',
                label: 'varchar label',
                description: 'varchar description',
                dataType: 'VARCHAR'
              },
              mandatory: false
            },
            {
              propertyType: {
                code: 'MATERIAL_PROPERTY_TYPE',
                label: 'material label',
                description: 'material description',
                dataType: 'MATERIAL'
              },
              mandatory: true
            },
            {
              propertyType: {
                code: 'DICTIONARY_PROPERTY_TYPE',
                label: 'dictionary label',
                description: 'dictionary description',
                dataType: 'CONTROLLEDVOCABULARY',
                vocabulary: {
                  code: 'VOCABULARY_1'
                }
              },
              mandatory: true
            }
          ]
        }
      })
    )

    facade.searchPropertyTypes.mockReturnValue(
      Promise.resolve({
        objects: [
          {
            code: 'VARCHAR_PROPERTY_TYPE'
          },
          {
            code: 'MATERIAL_PROPERTY_TYPE'
          },
          {
            code: 'DICTIONARY_PROPERTY_TYPE'
          }
        ]
      })
    )

    facade.searchMaterials.mockReturnValue(
      Promise.resolve({
        objects: [
          {
            code: 'MATERIAL_1'
          },
          {
            code: 'MATERIAL_2'
          },
          {
            code: 'MATERIAL_3'
          }
        ]
      })
    )

    facade.searchVocabularyTerms.mockReturnValue(
      Promise.resolve({
        objects: [
          {
            code: 'TERM_1'
          },
          {
            code: 'TERM_2'
          },
          {
            code: 'TERM_3'
          }
        ]
      })
    )

    store.dispatch(actions.init())

    let wrapper = mount(
      <DragAndDropProvider>
        <ObjectType store={store} objectId='TEST_OBJECT_TYPE' />
      </DragAndDropProvider>
    )

    setTimeout(() => {
      wrapper.update()

      expectTitle(wrapper, 'TEST_OBJECT_TYPE')

      expectPropertyMandatory(wrapper, 0, 'false')
      expectPropertyType(wrapper, 0, 'VARCHAR_PROPERTY_TYPE')

      expectPropertyMandatory(wrapper, 1, 'true')
      expectPropertyType(wrapper, 1, 'MATERIAL_PROPERTY_TYPE')
      expectPropertyPreviewMaterial(wrapper, 1, [
        '',
        'MATERIAL_1',
        'MATERIAL_2',
        'MATERIAL_3'
      ])

      expectPropertyMandatory(wrapper, 2, 'true')
      expectPropertyType(wrapper, 2, 'DICTIONARY_PROPERTY_TYPE')
      expectPropertyPreviewDictionary(wrapper, 2, [
        '',
        'TERM_1',
        'TERM_2',
        'TERM_3'
      ])

      done()
    }, 0)
  })
})

function expectTitle(wrapper, expectedTitle) {
  const actualTitle = wrapper.find('ObjectTypeTitle').text()
  expect(actualTitle).toEqual(expectedTitle)
}

function expectPropertyMandatory(wrapper, index, expected) {
  let row = wrapper.find('ObjectTypePropertyRow').at(index)
  let actual = row.find('ObjectTypePropertyMandatory').text()
  expect(actual).toEqual(expected)
}

function expectPropertyType(wrapper, index, expected) {
  let row = wrapper.find('ObjectTypePropertyRow').at(index)
  let actual = row.find('ObjectTypePropertyType').text()
  expect(actual).toEqual(expected)
}

function expectPropertyPreviewMaterial(wrapper, index, expected) {
  let row = wrapper.find('ObjectTypePropertyRow').at(index)
  let actual = row
    .find('ObjectTypePropertyPreview')
    .find('option')
    .map(node => {
      return node.prop('value')
    })
  expect(actual).toEqual(expected)
}

function expectPropertyPreviewDictionary(wrapper, index, expected) {
  let row = wrapper.find('ObjectTypePropertyRow').at(index)
  let actual = row
    .find('ObjectTypePropertyPreview')
    .find('option')
    .map(node => {
      return node.prop('value')
    })
  expect(actual).toEqual(expected)
}
