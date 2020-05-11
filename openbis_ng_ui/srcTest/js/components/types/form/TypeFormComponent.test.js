import React from 'react'
import { Provider } from 'react-redux'
import { mount } from 'enzyme'
import { createStore } from '@src/js/store/store.js'
import ThemeProvider from '@src/js/components/common/theme/ThemeProvider.jsx'
import TypeForm from '@src/js/components/types/form/TypeForm.jsx'
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

      const objectType = new TypeFormWrapper(wrapper)

      expect(objectType.toJSON()).toMatchObject({
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

      objectType
        .getPreview()
        .getSections()[0]
        .getProperties()[0]
        .wrapper.simulate('click')

      expect(objectType.toJSON()).toMatchObject({
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

class TypeFormWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getPreview() {
    return new TypeFormPreview(this.wrapper.find('TypeFormPreview'))
  }

  getParameters() {
    return new TypeFormParameters(this.wrapper.find('TypeFormParameters'))
  }

  toJSON() {
    return {
      preview: this.getPreview().toJSON(),
      parameters: this.getParameters().toJSON()
    }
  }
}

class TypeFormPreview {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getHeader() {
    return new TypeFormPreviewHeader(this.wrapper.find('TypeFormPreviewHeader'))
  }

  getSections() {
    const sections = []
    this.wrapper.find('TypeFormPreviewSection').forEach(sectionWrapper => {
      sections.push(new TypeFormPreviewSection(sectionWrapper))
    })
    return sections
  }

  toJSON() {
    return {
      header: this.getHeader().toJSON(),
      sections: this.getSections().map(section => section.toJSON())
    }
  }
}

class TypeFormParameters {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getType() {
    return new TypeFormParametersForm(
      this.wrapper.find('TypeFormParametersType')
    )
  }

  getProperty() {
    return new TypeFormParametersForm(
      this.wrapper.find('TypeFormParametersProperty')
    )
  }

  toJSON() {
    return {
      type: this.getType().toJSON(),
      property: this.getProperty().toJSON()
    }
  }
}

class TypeFormParametersForm {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getTitle() {
    return this.wrapper.find('TypeFormHeader')
  }

  getCode() {
    return new TextFormField(this.wrapper.find('TextFormField[name="code"]'))
  }

  getDescription() {
    return new TextFormField(
      this.wrapper.find('TextFormField[name="description"]')
    )
  }

  toJSON() {
    return {
      title: this.getTitle().exists() ? this.getTitle().text() : null,
      code: this.getCode().toJSON(),
      description: this.getDescription().toJSON()
    }
  }
}

class TypeFormPreviewHeader {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getTitle() {
    return this.wrapper.find('TypeFormHeader')
  }

  getCode() {
    return new TextFormField(this.wrapper.find('TextFormField[name="code"]'))
  }

  getParents() {
    return new TextFormField(this.wrapper.find('TextFormField[name="parents"]'))
  }

  getContainer() {
    return new TextFormField(
      this.wrapper.find('TextFormField[name="container"]')
    )
  }

  toJSON() {
    return {
      title: this.getTitle().text(),
      code: this.getCode().toJSON(),
      parents: this.getParents().toJSON(),
      container: this.getContainer().toJSON()
    }
  }
}

class TypeFormPreviewSection {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getName() {
    return this.wrapper.find('TypeFormHeader')
  }

  getProperties() {
    const properties = []
    this.wrapper.find('TypeFormPreviewProperty').forEach(propertyWrapper => {
      properties.push(new TypeFormPreviewProperty(propertyWrapper))
    })
    return properties
  }

  toJSON() {
    return {
      name: this.getName().text(),
      properties: this.getProperties().map(property => property.toJSON())
    }
  }
}

class TypeFormPreviewProperty {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getCode() {
    return this.wrapper.find('span[data-part="code"]')
  }

  getLabel() {
    return this.wrapper.find('span[data-part="label"]')
  }

  toJSON() {
    return {
      code: this.getCode().text(),
      label: this.getLabel().text()
    }
  }
}

class TextFormField {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getLabel() {
    return this.wrapper.prop('label')
  }

  getValue() {
    return this.wrapper.prop('value')
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        label: this.getLabel(),
        value: this.getValue()
      }
    } else {
      return {}
    }
  }
}
