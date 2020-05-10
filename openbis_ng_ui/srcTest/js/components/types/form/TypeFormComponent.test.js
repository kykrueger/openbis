import React from 'react'
import { Provider } from 'react-redux'
import { mount } from 'enzyme'
import { createStore } from '@src/js/store/store.js'
import ThemeProvider from '@src/js/components/common/theme/ThemeProvider.jsx'
import TypeForm from '@src/js/components/types/form/TypeForm.jsx'
import TypeFormWrapper from '@srcTest/js/components/types/form/wrapper/TypeFormWrapper.js'
import TypeFormController from '@src/js/components/types/form/TypeFormController.js'
import TypeFormFacade from '@src/js/components/types/form/TypeFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'
import fixture from '@srcTest/js/common/fixture.js'

jest.mock('@src/js/components/types/form/TypeFormFacade')

let store = null
let facade = null
let controller = null

beforeEach(() => {
  jest.resetAllMocks()
  store = createStore()
  facade = new TypeFormFacade()
  controller = new TypeFormController(facade)
})

describe('TypeForm', () => {
  test('test', done => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadUsages.mockReturnValue(Promise.resolve({}))
    facade.loadDynamicPlugins.mockReturnValueOnce(Promise.resolve([]))
    facade.loadValidationPlugins.mockReturnValueOnce(Promise.resolve([]))
    facade.loadMaterials.mockReturnValueOnce(Promise.resolve([]))
    facade.loadVocabularyTerms.mockReturnValueOnce(Promise.resolve([]))
    facade.loadGlobalPropertyTypes.mockReturnValueOnce(Promise.resolve([]))

    const wrapper = mount(
      <Provider store={store}>
        <ThemeProvider>
          <TypeForm
            object={{
              id: fixture.TEST_SAMPLE_TYPE_DTO.getCode(),
              type: objectTypes.OBJECT_TYPE
            }}
            controller={controller}
          />
        </ThemeProvider>
      </Provider>
    )

    setTimeout(() => {
      wrapper.update()

      const form = new TypeFormWrapper(wrapper)

      expect(form.toJSON()).toMatchObject({
        preview: {
          sections: [
            {
              name: 'TEST_SECTION_1',
              properties: [{ code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }]
            },
            {
              name: 'TEST_SECTION_2',
              properties: [
                { code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
                { code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
              ]
            }
          ]
        },
        parameters: {
          type: {
            title: 'Type',
            code: {
              label: 'Code',
              value: fixture.TEST_SAMPLE_TYPE_DTO.getCode()
            }
          }
        }
      })

      form
        .getPreview()
        .getSections()[0]
        .getProperties()[0]
        .wrapper.simulate('click')

      expect(form.toJSON()).toMatchObject({
        preview: {
          sections: [
            {
              name: 'TEST_SECTION_1',
              properties: [{ code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }]
            },
            {
              name: 'TEST_SECTION_2',
              properties: [
                { code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
                { code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
              ]
            }
          ]
        },
        parameters: {
          property: {
            title: 'Property',
            code: {
              label: 'Code',
              value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode()
            }
          }
        }
      })

      done()
    })
  })
})
