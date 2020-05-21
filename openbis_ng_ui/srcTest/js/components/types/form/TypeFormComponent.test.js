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
import openbis from '@srcTest/js/services/openbis.js'
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

  facade.loadType.mockReturnValue(Promise.resolve({}))
  facade.loadUsages.mockReturnValue(Promise.resolve({}))
  facade.loadDynamicPlugins.mockReturnValue(Promise.resolve([]))
  facade.loadValidationPlugins.mockReturnValue(Promise.resolve([]))
  facade.loadMaterials.mockReturnValue(Promise.resolve([]))
  facade.loadVocabularyTerms.mockReturnValue(Promise.resolve([]))
  facade.loadGlobalPropertyTypes.mockReturnValue(Promise.resolve([]))
})

describe('TypeForm', () => {
  test('load new', async () => {
    const form = await mountForm({
      type: objectTypes.NEW_OBJECT_TYPE
    })

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: []
      },
      parameters: {
        type: {
          title: 'Type',
          code: {
            label: 'Code',
            value: null,
            enabled: true
          },
          description: {
            label: 'Description',
            value: null,
            enabled: true
          },
          validationPlugin: {
            label: 'Validation Plugin',
            value: null,
            enabled: true
          }
        }
      }
    })
  })

  test('load existing', async () => {
    facade.loadType.mockReturnValue(
      Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
    )
    facade.loadValidationPlugins.mockReturnValue(
      Promise.resolve([fixture.TEST_SAMPLE_TYPE_DTO.validationPlugin])
    )

    const form = await mountForm({
      id: fixture.TEST_SAMPLE_TYPE_DTO.getCode(),
      type: objectTypes.OBJECT_TYPE
    })

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
            value: fixture.TEST_SAMPLE_TYPE_DTO.getCode(),
            enabled: false
          },
          description: {
            label: 'Description',
            value: fixture.TEST_SAMPLE_TYPE_DTO.getDescription(),
            enabled: true
          },
          validationPlugin: {
            label: 'Validation Plugin',
            value: fixture.TEST_SAMPLE_TYPE_DTO.validationPlugin.name,
            enabled: true
          }
        }
      }
    })
  })

  test('select property (local)', async () => {
    const testLocal = async used => {
      const plugin = new openbis.Plugin()
      plugin.setName('TEST_PLUGIN')

      const propertyType = new openbis.PropertyType()
      propertyType.setCode('TEST_TYPE.LOCAL_PROPERTY')
      propertyType.setLabel('Test Label')
      propertyType.setDescription('Test Description')
      propertyType.setDataType(openbis.DataType.VARCHAR)

      const propertyAssignment = new openbis.PropertyAssignment()
      propertyAssignment.setPropertyType(propertyType)
      propertyAssignment.setPlugin(plugin)

      const type = new openbis.SampleType()
      type.setCode('TEST_TYPE')
      type.setPropertyAssignments([propertyAssignment])

      facade.loadType.mockReturnValue(Promise.resolve(type))
      facade.loadDynamicPlugins.mockReturnValue(Promise.resolve([plugin]))

      if (used) {
        facade.loadUsages.mockReturnValue(
          Promise.resolve({
            propertyLocal: {
              [propertyType.getCode()]: 1
            },
            propertyGlobal: {
              [propertyType.getCode()]: 1
            }
          })
        )
      }

      const form = await mountForm({
        id: type.getCode(),
        type: objectTypes.OBJECT_TYPE
      })

      form.getPreview().getSections()[0].getProperties()[0].click()

      expect(form.toJSON()).toMatchObject({
        parameters: {
          property: {
            title: 'Property',
            scope: {
              label: 'Scope',
              value: 'local',
              enabled: false
            },
            code: {
              label: 'Code',
              value: propertyType.getCode(),
              enabled: false
            },
            dataType: {
              label: 'Data Type',
              value: propertyType.getDataType(),
              enabled: !used
            },
            label: {
              label: 'Label',
              value: propertyType.getLabel(),
              enabled: true
            },
            description: {
              label: 'Description',
              value: propertyType.getDescription(),
              enabled: true
            },
            plugin: {
              label: 'Dynamic Plugin',
              value: plugin.getName(),
              enabled: !used
            }
          }
        }
      })
    }

    await testLocal(false)
    await testLocal(true)
  })

  test('select property (global)', async () => {
    const testGlobal = async used => {
      const plugin = new openbis.Plugin()
      plugin.setName('TEST_PLUGIN')

      const propertyType = new openbis.PropertyType()
      propertyType.setCode('GLOBAL_PROPERTY')
      propertyType.setLabel('Test Label')
      propertyType.setDescription('Test Description')
      propertyType.setDataType(openbis.DataType.VARCHAR)

      const propertyAssignment = new openbis.PropertyAssignment()
      propertyAssignment.setPropertyType(propertyType)
      propertyAssignment.setPlugin(plugin)

      const type = new openbis.SampleType()
      type.setCode('TEST_TYPE')
      type.setPropertyAssignments([propertyAssignment])

      facade.loadType.mockReturnValue(Promise.resolve(type))
      facade.loadDynamicPlugins.mockReturnValue(Promise.resolve([plugin]))

      if (used) {
        facade.loadUsages.mockReturnValue(
          Promise.resolve({
            propertyLocal: {
              [propertyType.getCode()]: 1
            },
            propertyGlobal: {
              [propertyType.getCode()]: 1
            }
          })
        )
      }

      const form = await mountForm({
        id: type.getCode(),
        type: objectTypes.OBJECT_TYPE
      })

      form.getPreview().getSections()[0].getProperties()[0].click()

      expect(form.toJSON()).toMatchObject({
        parameters: {
          property: {
            title: 'Property',
            scope: {
              label: 'Scope',
              value: 'global',
              enabled: false
            },
            code: {
              label: 'Code',
              value: propertyType.getCode(),
              enabled: false
            },
            dataType: {
              label: 'Data Type',
              value: propertyType.getDataType(),
              enabled: !used
            },
            label: {
              label: 'Label',
              value: propertyType.getLabel()
            },
            description: {
              label: 'Description',
              value: propertyType.getDescription()
            },
            plugin: {
              label: 'Dynamic Plugin',
              value: plugin.getName(),
              enabled: !used
            }
          }
        }
      })
    }

    await testGlobal(false)
    await testGlobal(true)
  })

  test('select section', async () => {
    const propertyType = new openbis.PropertyType()
    propertyType.setCode('TEST_PROPERTY')
    propertyType.setDataType(openbis.DataType.VARCHAR)

    const propertyAssignment = new openbis.PropertyAssignment()
    propertyAssignment.setPropertyType(propertyType)
    propertyAssignment.setSection('TEST_SECTION')

    const type = new openbis.SampleType()
    type.setCode('TEST_TYPE')
    type.setPropertyAssignments([propertyAssignment])

    facade.loadType.mockReturnValue(Promise.resolve(type))

    const form = await mountForm({
      id: type.getCode(),
      type: objectTypes.OBJECT_TYPE
    })

    form.getPreview().getSections()[0].click()

    expect(form.toJSON()).toMatchObject({
      parameters: {
        section: {
          title: 'Section',
          name: {
            label: 'Name',
            value: propertyAssignment.getSection(),
            enabled: true
          }
        }
      }
    })
  })

  test('add section', async () => {
    const form = await mountForm({
      type: objectTypes.NEW_OBJECT_TYPE
    })

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: []
      },
      buttons: {
        addSection: {
          enabled: true
        },
        addProperty: {
          enabled: false
        },
        remove: {
          enabled: false
        },
        save: {
          enabled: true
        }
      }
    })

    form.getButtons().getAddSection().click()

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: [
          {
            name: null
          }
        ]
      },
      parameters: {
        section: {
          title: 'Section',
          name: {
            label: 'Name',
            value: null,
            enabled: true
          }
        }
      },
      buttons: {
        addSection: {
          enabled: true
        },
        addProperty: {
          enabled: true
        },
        remove: {
          enabled: true
        },
        save: {
          enabled: true
        }
      }
    })
  })

  test('add property', async () => {
    const form = await mountForm({
      type: objectTypes.NEW_OBJECT_TYPE
    })

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: []
      },
      buttons: {
        addSection: {
          enabled: true
        },
        addProperty: {
          enabled: false
        },
        remove: {
          enabled: false
        },
        save: {
          enabled: true
        }
      }
    })

    form.getButtons().getAddSection().click()
    form.getButtons().getAddProperty().click()

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: [
          {
            name: null,
            properties: [{ code: 'empty', label: 'empty', dataType: 'VARCHAR' }]
          }
        ]
      },
      parameters: {
        property: {
          title: 'Property',
          scope: {
            label: 'Scope',
            value: 'local',
            enabled: true
          },
          code: {
            label: 'Code',
            value: null,
            enabled: true
          },
          dataType: {
            label: 'Data Type',
            value: 'VARCHAR',
            enabled: true
          },
          label: {
            label: 'Label',
            value: null,
            enabled: true
          },
          description: {
            label: 'Description',
            value: null,
            enabled: true
          },
          plugin: {
            label: 'Dynamic Plugin',
            value: null,
            enabled: true
          }
        }
      },
      buttons: {
        addSection: {
          enabled: true
        },
        addProperty: {
          enabled: true
        },
        remove: {
          enabled: true
        },
        save: {
          enabled: true
        }
      }
    })
  })

  test('change type', async () => {
    const form = await mountForm({
      type: objectTypes.NEW_OBJECT_TYPE
    })

    expect(form.toJSON()).toMatchObject({
      preview: {
        header: {
          code: {
            label: 'Code',
            value: null,
            enabled: true
          }
        }
      },
      parameters: {
        type: {
          title: 'Type',
          autoGeneratedCode: {
            label: 'Generate Codes',
            value: false,
            enabled: true
          },
          generatedCodePrefix: {
            label: 'Generated code prefix',
            value: null,
            enabled: true
          }
        }
      }
    })

    form.getParameters().getType().change('autoGeneratedCode', true)
    form.getParameters().getType().change('generatedCodePrefix', 'TEST_PREFIX_')

    expect(form.toJSON()).toMatchObject({
      preview: {
        header: {
          code: {
            label: 'Code',
            value: 'TEST_PREFIX_',
            enabled: false
          }
        }
      },
      parameters: {
        type: {
          title: 'Type',
          autoGeneratedCode: {
            label: 'Generate Codes',
            value: true,
            enabled: true
          },
          generatedCodePrefix: {
            label: 'Generated code prefix',
            value: 'TEST_PREFIX_',
            enabled: true
          }
        }
      }
    })
  })

  test('change property', async () => {
    const form = await mountForm({
      type: objectTypes.NEW_OBJECT_TYPE
    })

    form.getButtons().getAddSection().click()
    form.getButtons().getAddProperty().click()

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: [
          {
            name: null,
            properties: [{ dataType: 'VARCHAR' }]
          }
        ]
      },
      parameters: {
        property: {
          title: 'Property',
          dataType: {
            label: 'Data Type',
            value: 'VARCHAR',
            enabled: true
          },
          vocabulary: null,
          materialType: null,
          schema: null,
          transformation: null
        }
      }
    })

    form
      .getParameters()
      .getProperty()
      .change('dataType', 'CONTROLLEDVOCABULARY')

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: [
          {
            name: null,
            properties: [{ dataType: 'CONTROLLEDVOCABULARY' }]
          }
        ]
      },
      parameters: {
        property: {
          title: 'Property',
          dataType: {
            label: 'Data Type',
            value: 'CONTROLLEDVOCABULARY',
            enabled: true
          },
          vocabulary: {
            label: 'Vocabulary',
            value: null,
            enabled: true
          },
          materialType: null,
          schema: null,
          transformation: null
        }
      }
    })

    form.getParameters().getProperty().change('dataType', 'MATERIAL')

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: [
          {
            name: null,
            properties: [{ dataType: 'MATERIAL' }]
          }
        ]
      },
      parameters: {
        property: {
          title: 'Property',
          dataType: {
            label: 'Data Type',
            value: 'MATERIAL',
            enabled: true
          },
          vocabulary: null,
          materialType: {
            label: 'Material Type',
            value: null,
            enabled: true
          },
          schema: null,
          transformation: null
        }
      }
    })

    form.getParameters().getProperty().change('dataType', 'XML')

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: [
          {
            name: null,
            properties: [{ dataType: 'XML' }]
          }
        ]
      },
      parameters: {
        property: {
          title: 'Property',
          dataType: {
            label: 'Data Type',
            value: 'XML',
            enabled: true
          },
          vocabulary: null,
          materialType: null,
          schema: {
            label: 'XML Schema',
            value: null,
            enabled: true
          },
          transformation: {
            label: 'XSLT Script',
            value: null,
            enabled: true
          }
        }
      }
    })
  })

  test('change section', async () => {
    const form = await mountForm({
      type: objectTypes.NEW_OBJECT_TYPE
    })

    form.getButtons().getAddSection().click()

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: [
          {
            name: null
          }
        ]
      },
      parameters: {
        section: {
          title: 'Section',
          name: {
            label: 'Name',
            value: null,
            enabled: true
          }
        }
      }
    })

    form.getParameters().getSection().change('name', 'Test Name')

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: [
          {
            name: 'Test Name'
          }
        ]
      },
      parameters: {
        section: {
          title: 'Section',
          name: {
            label: 'Name',
            value: 'Test Name',
            enabled: true
          }
        }
      }
    })
  })

  test('remove property', async () => {
    const form = await mountForm({
      type: objectTypes.NEW_OBJECT_TYPE
    })

    form.getButtons().getAddSection().click()
    form.getButtons().getAddProperty().click()

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: [
          {
            name: null,
            properties: [{ code: 'empty' }]
          }
        ]
      }
    })

    form.getButtons().getRemove().click()

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: [
          {
            name: null,
            properties: []
          }
        ]
      }
    })
  })

  test('remove section', async () => {
    const form = await mountForm({
      type: objectTypes.NEW_OBJECT_TYPE
    })

    form.getButtons().getAddSection().click()

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: [
          {
            name: null,
            properties: []
          }
        ]
      }
    })

    form.getButtons().getRemove().click()

    expect(form.toJSON()).toMatchObject({
      preview: {
        sections: []
      }
    })
  })

  async function mountForm(object) {
    const wrapper = mount(
      <Provider store={store}>
        <ThemeProvider>
          <TypeForm object={object} controller={controller} />
        </ThemeProvider>
      </Provider>
    )

    return new Promise(resolve => {
      setTimeout(() => {
        wrapper.update()

        const form = new TypeFormWrapper(wrapper)

        resolve(form)
      })
    })
  }
})
