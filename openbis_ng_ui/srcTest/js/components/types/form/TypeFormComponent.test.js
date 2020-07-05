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

describe('TypeFormComponent', () => {
  test('load new', testLoadNew)
  test('load existing', testLoadExisting)
  test('select property local unused', testSelectPropertyLocalUnused)
  test('select property local used', testSelectPropertyLocalUsed)
  test('select property global unused', testSelectPropertyGlobalUnused)
  test('select property global used', testSelectPropertyGlobalUsed)
  test('select section', testSelectSection)
  test('add section', testAddSection)
  test('add property', testAddProperty)
  test('change type', testChangeType)
  test('change property', testChangeProperty)
  test('change section', testChangeSection)
  test('remove property', testRemoveProperty)
  test('remove section', testRemoveSection)
  test('validate type', testValidateType)
  test('validate property', testValidateProperty)
  test('validate type and property', testValidateTypeAndProperty)
})

async function testLoadNew() {
  const form = await mountForm({
    type: objectTypes.NEW_OBJECT_TYPE
  })

  form.expectJSON({
    preview: {
      sections: []
    },
    parameters: {
      type: {
        title: 'Type',
        code: {
          label: 'Code',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        validationPlugin: {
          label: 'Validation Plugin',
          value: null,
          enabled: true,
          mode: 'edit'
        }
      }
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
      },
      edit: null,
      cancel: null
    }
  })
}

async function testLoadExisting() {
  facade.loadType.mockReturnValue(Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO))
  facade.loadValidationPlugins.mockReturnValue(
    Promise.resolve([fixture.TEST_SAMPLE_TYPE_DTO.validationPlugin])
  )

  const form = await mountForm({
    id: fixture.TEST_SAMPLE_TYPE_DTO.getCode(),
    type: objectTypes.OBJECT_TYPE
  })

  form.expectJSON({
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
          mode: 'view'
        },
        description: {
          label: 'Description',
          value: fixture.TEST_SAMPLE_TYPE_DTO.getDescription(),
          mode: 'view'
        },
        validationPlugin: {
          label: 'Validation Plugin',
          value: fixture.TEST_SAMPLE_TYPE_DTO.validationPlugin.name,
          mode: 'view'
        }
      }
    },
    buttons: {
      edit: {
        enabled: true
      },
      addSection: null,
      addProperty: null,
      remove: null,
      save: null,
      cancel: null
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
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
          enabled: false,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: fixture.TEST_SAMPLE_TYPE_DTO.getDescription(),
          enabled: true,
          mode: 'edit'
        },
        validationPlugin: {
          label: 'Validation Plugin',
          value: fixture.TEST_SAMPLE_TYPE_DTO.validationPlugin.name,
          enabled: true,
          mode: 'edit'
        }
      }
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
      },
      cancel: {
        enabled: true
      },
      edit: null
    }
  })
}

async function testSelectPropertyLocalUnused() {
  await doTestSelectProperty('local', false)
}

async function testSelectPropertyLocalUsed() {
  await doTestSelectProperty('local', true)
}

async function testSelectPropertyGlobalUnused() {
  await doTestSelectProperty('global', false)
}

async function testSelectPropertyGlobalUsed() {
  await doTestSelectProperty('global', true)
}

async function doTestSelectProperty(scope, used) {
  const plugin = new openbis.Plugin()
  plugin.setName('TEST_PLUGIN')

  const propertyType = new openbis.PropertyType()
  propertyType.setCode(
    scope === 'global' ? 'GLOBAL_PROPERTY' : 'TEST_TYPE.LOCAL_PROPERTY'
  )
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

  const messages = []

  if (scope === 'global') {
    messages.push({
      text:
        'This property is global. Changes will also influence other types where this property is used.',
      type: 'warning'
    })
  }

  if (used) {
    facade.loadUsages.mockReturnValue(
      Promise.resolve({
        propertyLocal: {
          [propertyType.getCode()]: 1
        },
        propertyGlobal: {
          [propertyType.getCode()]: 3
        }
      })
    )
    messages.push({
      text:
        'This property is already used by 3 entities (1 entity of this type and 2 entities of other types).',
      type: 'info'
    })

    facade.loadAssignments.mockReturnValue(
      Promise.resolve({
        [propertyType.getCode()]: 2
      })
    )
    messages.push({
      text: 'This property is already assigned to 2 types.',
      type: 'info'
    })
  }

  const form = await mountForm({
    id: type.getCode(),
    type: objectTypes.OBJECT_TYPE
  })

  form.getButtons().getEdit().click()
  form.getPreview().getSections()[0].getProperties()[0].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        title: 'Property',
        messages,
        scope: {
          label: 'Scope',
          value: scope,
          enabled: false,
          mode: 'edit'
        },
        code: {
          label: 'Code',
          value: propertyType.getCode(),
          enabled: false,
          mode: 'edit'
        },
        dataType: {
          label: 'Data Type',
          value: propertyType.getDataType(),
          enabled: !used,
          mode: 'edit'
        },
        label: {
          label: 'Label',
          value: propertyType.getLabel(),
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: propertyType.getDescription(),
          enabled: true,
          mode: 'edit'
        },
        plugin: {
          label: 'Dynamic Plugin',
          value: plugin.getName(),
          enabled: !used,
          mode: 'edit'
        }
      }
    }
  })
}

async function testSelectSection() {
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
  await form.update()

  form.expectJSON({
    parameters: {
      section: {
        title: 'Section',
        name: {
          label: 'Name',
          value: propertyAssignment.getSection(),
          enabled: true,
          mode: 'view'
        }
      }
    }
  })
}

async function testAddSection() {
  const form = await mountForm({
    type: objectTypes.NEW_OBJECT_TYPE
  })

  form.expectJSON({
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
  await form.update()

  form.expectJSON({
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
          enabled: true,
          mode: 'edit'
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
}

async function testAddProperty() {
  const form = await mountForm({
    type: objectTypes.NEW_OBJECT_TYPE
  })

  form.expectJSON({
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
  await form.update()

  form.expectJSON({
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
          enabled: true,
          mode: 'edit'
        },
        code: {
          label: 'Code',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        dataType: {
          label: 'Data Type',
          value: 'VARCHAR',
          enabled: true,
          mode: 'edit'
        },
        label: {
          label: 'Label',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        plugin: {
          label: 'Dynamic Plugin',
          value: null,
          enabled: true,
          mode: 'edit'
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
}

async function testChangeType() {
  const form = await mountForm({
    type: objectTypes.NEW_OBJECT_TYPE
  })

  form.expectJSON({
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
          enabled: true,
          mode: 'edit'
        },
        generatedCodePrefix: {
          label: 'Generated code prefix',
          value: null,
          enabled: true,
          mode: 'edit'
        }
      }
    }
  })

  form.getParameters().getType().getAutoGeneratedCode().change(true)
  form.getParameters().getType().getGeneratedCodePrefix().change('TEST_PREFIX_')
  await form.update()

  form.expectJSON({
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
          enabled: true,
          mode: 'edit'
        },
        generatedCodePrefix: {
          label: 'Generated code prefix',
          value: 'TEST_PREFIX_',
          enabled: true,
          mode: 'edit'
        }
      }
    }
  })
}

async function testChangeProperty() {
  const form = await mountForm({
    type: objectTypes.NEW_OBJECT_TYPE
  })

  form.getButtons().getAddSection().click()
  form.getButtons().getAddProperty().click()
  await form.update()

  form.expectJSON({
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
          enabled: true,
          mode: 'edit'
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
    .getDataType()
    .change('CONTROLLEDVOCABULARY')
  await form.update()

  form.expectJSON({
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
          enabled: true,
          mode: 'edit'
        },
        vocabulary: {
          label: 'Vocabulary',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        materialType: null,
        schema: null,
        transformation: null
      }
    }
  })

  form.getParameters().getProperty().getDataType().change('MATERIAL')
  await form.update()

  form.expectJSON({
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
          enabled: true,
          mode: 'edit'
        },
        vocabulary: null,
        materialType: {
          label: 'Material Type',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        schema: null,
        transformation: null
      }
    }
  })

  form.getParameters().getProperty().getDataType().change('XML')
  await form.update()

  form.expectJSON({
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
          enabled: true,
          mode: 'edit'
        },
        vocabulary: null,
        materialType: null,
        schema: {
          label: 'XML Schema',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        transformation: {
          label: 'XSLT Script',
          value: null,
          enabled: true,
          mode: 'edit'
        }
      }
    }
  })
}

async function testChangeSection() {
  const form = await mountForm({
    type: objectTypes.NEW_OBJECT_TYPE
  })

  form.getButtons().getAddSection().click()
  await form.update()

  form.expectJSON({
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
          enabled: true,
          mode: 'edit'
        }
      }
    }
  })

  form.getParameters().getSection().getName().change('Test Name')
  await form.update()

  form.expectJSON({
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
          enabled: true,
          mode: 'edit'
        }
      }
    }
  })
}

async function testRemoveProperty() {
  const form = await mountForm({
    type: objectTypes.NEW_OBJECT_TYPE
  })

  form.getButtons().getAddSection().click()
  form.getButtons().getAddProperty().click()
  await form.update()

  form.expectJSON({
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
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: null,
          properties: []
        }
      ]
    }
  })
}

async function testRemoveSection() {
  const form = await mountForm({
    type: objectTypes.NEW_OBJECT_TYPE
  })

  form.getButtons().getAddSection().click()
  await form.update()

  form.expectJSON({
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
  await form.update()

  form.expectJSON({
    preview: {
      sections: []
    }
  })
}

async function testValidateType() {
  const form = await mountForm({
    type: objectTypes.NEW_OBJECT_TYPE
  })

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      type: {
        title: 'Type',
        code: {
          error: 'Code cannot be empty'
        },
        description: {
          error: null
        },
        validationPlugin: {
          error: null
        },
        generatedCodePrefix: {
          error: 'Generated code prefix cannot be empty'
        }
      }
    }
  })
}

async function testValidateProperty() {
  const form = await mountForm({
    type: objectTypes.NEW_OBJECT_TYPE
  })

  form.getParameters().getType().getCode().change('TEST_CODE')
  form.getParameters().getType().getGeneratedCodePrefix().change('TEST_PREFIX_')

  form.getButtons().getAddSection().click()
  form.getButtons().getAddProperty().click()
  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        title: 'Property',
        scope: {
          error: null
        },
        code: {
          error: 'Code cannot be empty'
        },
        dataType: {
          error: null
        },
        label: {
          error: 'Label cannot be empty'
        },
        description: {
          error: 'Description cannot be empty'
        },
        plugin: {
          error: null
        }
      }
    }
  })
}

async function testValidateTypeAndProperty() {
  const form = await mountForm({
    type: objectTypes.NEW_OBJECT_TYPE
  })

  form.getButtons().getAddSection().click()
  form.getButtons().getAddProperty().click()
  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      type: {
        title: 'Type',
        code: {
          error: 'Code cannot be empty'
        },
        description: {
          error: null
        },
        validationPlugin: {
          error: null
        },
        generatedCodePrefix: {
          error: 'Generated code prefix cannot be empty'
        }
      }
    }
  })
}

async function mountForm(object) {
  const wrapper = mount(
    <Provider store={store}>
      <ThemeProvider>
        <TypeForm object={object} controller={controller} />
      </ThemeProvider>
    </Provider>
  )

  const form = new TypeFormWrapper(wrapper)
  return form.update().then(() => form)
}
