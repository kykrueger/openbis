import React from 'react'
import { Provider } from 'react-redux'
import { mount } from 'enzyme'
import { createStore } from '@src/js/store/store.js'
import ThemeProvider from '@src/js/components/common/theme/ThemeProvider.jsx'
import ObjectType from '@src/js/components/types/objectType/ObjectType.jsx'
import ObjectTypeController from '@src/js/components/types/objectType/ObjectTypeController.js'
import ObjectTypeFacade from '@src/js/components/types/objectType/ObjectTypeFacade'
import objectTypes from '@src/js/common/consts/objectType.js'
import fixture from '@srcTest/js/common/fixture.js'

jest.mock('@src/js/components/types/objectType/ObjectTypeFacade')

let store = null
let facade = null
let controller = null

beforeEach(() => {
  jest.resetAllMocks()
  store = createStore()
  facade = new ObjectTypeFacade()
  controller = new ObjectTypeController(facade)
})

describe('ObjectType', () => {
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
          <ObjectType
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

      const objectType = new ObjectTypeWrapper(wrapper)

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

class ObjectTypeWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getPreview() {
    return new ObjectTypePreview(this.wrapper.find('ObjectTypePreview'))
  }

  getParameters() {
    return new ObjectTypeParameters(this.wrapper.find('ObjectTypeParameters'))
  }

  toJSON() {
    return {
      preview: this.getPreview().toJSON(),
      parameters: this.getParameters().toJSON()
    }
  }
}

class ObjectTypePreview {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getHeader() {
    return new ObjectTypePreviewHeader(
      this.wrapper.find('ObjectTypePreviewHeader')
    )
  }

  getSections() {
    const sections = []
    this.wrapper.find('ObjectTypePreviewSection').forEach(sectionWrapper => {
      sections.push(new ObjectTypePreviewSection(sectionWrapper))
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

class ObjectTypeParameters {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getType() {
    return new ObjectTypeParametersForm(
      this.wrapper.find('ObjectTypeParametersType')
    )
  }

  getProperty() {
    return new ObjectTypeParametersForm(
      this.wrapper.find('ObjectTypeParametersProperty')
    )
  }

  toJSON() {
    return {
      type: this.getType().toJSON(),
      property: this.getProperty().toJSON()
    }
  }
}

class ObjectTypeParametersForm {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getTitle() {
    return this.wrapper.find('ObjectTypeHeader')
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

class ObjectTypePreviewHeader {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getTitle() {
    return this.wrapper.find('ObjectTypeHeader')
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

class ObjectTypePreviewSection {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getName() {
    return this.wrapper.find('ObjectTypeHeader')
  }

  getProperties() {
    const properties = []
    this.wrapper.find('ObjectTypePreviewProperty').forEach(propertyWrapper => {
      properties.push(new ObjectTypePreviewProperty(propertyWrapper))
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

class ObjectTypePreviewProperty {
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
