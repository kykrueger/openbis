import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import TypeForm from '@src/js/components/types/form/TypeForm.jsx'
import TypeFormWrapper from '@srcTest/js/components/types/form/wrapper/TypeFormWrapper.js'
import TypeFormController from '@src/js/components/types/form/TypeFormController.js'
import TypeFormFacade from '@src/js/components/types/form/TypeFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

jest.mock('@src/js/components/types/form/TypeFormFacade')

let common = null
let facade = null
let controller = null

beforeEach(() => {
  common = new ComponentTest(
    object => <TypeForm object={object} controller={controller} />,
    wrapper => new TypeFormWrapper(wrapper)
  )
  common.beforeEach()

  facade = new TypeFormFacade()
  controller = new TypeFormController(facade)

  facade.loadType.mockReturnValue(Promise.resolve({}))
  facade.loadUsages.mockReturnValue(Promise.resolve({}))
  facade.loadDynamicPlugins.mockReturnValue(Promise.resolve([]))
  facade.loadValidationPlugins.mockReturnValue(Promise.resolve([]))
  facade.loadMaterials.mockReturnValue(Promise.resolve([]))
  facade.loadSamples.mockReturnValue(Promise.resolve([]))
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
  test('convert property', testConvertProperty)
  test('change section', testChangeSection)
  test('remove property', testRemoveProperty)
  test('remove section', testRemoveSection)
  test('validate type', testValidateType)
  test('validate property', testValidateProperty)
  test('validate type and property', testValidateTypeAndProperty)
  test('internal', testInternal)
})

async function testLoadNew() {
  const form = await mountNew()

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
      cancel: null,
      message: null
    }
  })
}

async function testLoadExisting() {
  const form = await mountExisting()

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
      cancel: null,
      message: null
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
      edit: null,
      message: null
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
    facade.loadAssignments.mockReturnValue(
      Promise.resolve({
        [propertyType.getCode()]: 2
      })
    )
    messages.push({
      text: 'This property is already assigned to 2 types.',
      type: 'info'
    })

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
  }

  const form = await common.mount({
    id: type.getCode(),
    type: objectTypes.OBJECT_TYPE
  })

  form.getPreview().getSections()[0].getProperties()[0].click()
  await form.update()

  form.getButtons().getEdit().click()
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
          enabled: true,
          mode: 'edit',
          options: [
            {
              label: openbis.DataType.VARCHAR,
              value: openbis.DataType.VARCHAR
            },
            {
              label: openbis.DataType.MULTILINE_VARCHAR + ' (converted)',
              value: openbis.DataType.MULTILINE_VARCHAR
            }
          ]
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
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: null
    }
  })
}

async function testSelectSection() {
  const form = await mountExisting()

  form.getPreview().getSections()[1].click()
  await form.update()

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    parameters: {
      section: {
        title: 'Section',
        name: {
          label: 'Name',
          value: 'TEST_SECTION_2',
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: null
    }
  })
}

async function testAddSection() {
  const form = await mountExisting()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_1'
        },
        {
          name: 'TEST_SECTION_2'
        }
      ]
    },
    buttons: {
      edit: {
        enabled: true
      },
      addSection: null,
      addProperty: null,
      remove: null,
      save: null,
      cancel: null,
      message: null
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.getPreview().getSections()[0].click()
  form.getButtons().getAddSection().click()
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_1'
        },
        {
          name: null
        },
        {
          name: 'TEST_SECTION_2'
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
      edit: null,
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
      },
      cancel: {
        enabled: true
      },
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}

async function testAddProperty() {
  const form = await mountExisting()

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
    buttons: {
      edit: {
        enabled: true
      },
      addSection: null,
      addProperty: null,
      remove: null,
      save: null,
      cancel: null,
      message: null
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.getPreview().getSections()[1].getProperties()[0].click()
  form.getButtons().getAddProperty().click()
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
            {
              message: {
                type: 'info',
                text: 'Please select a data type to display the field preview.'
              }
            },
            { code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
          ]
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
          value: null,
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
      edit: null,
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
      },
      cancel: {
        enabled: true
      },
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}

async function testChangeType() {
  const form = await mountExisting()

  form.expectJSON({
    preview: {
      header: {
        code: {
          label: 'Code',
          value: null,
          enabled: false,
          mode: 'edit'
        }
      }
    },
    parameters: {
      type: {
        title: 'Type',
        autoGeneratedCode: {
          label: 'Generate Codes',
          value: false,
          mode: 'view'
        },
        generatedCodePrefix: {
          label: 'Generated code prefix',
          value: 'TEST_PREFIX_',
          mode: 'view'
        }
      }
    },
    buttons: {
      message: null
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    preview: {
      header: {
        code: {
          label: 'Code',
          value: null,
          enabled: true,
          mode: 'edit'
        }
      }
    }
  })

  form.getParameters().getType().getAutoGeneratedCode().change(true)
  form.getParameters().getType().getGeneratedCodePrefix().change('NEW_PREFIX_')
  await form.update()

  form.expectJSON({
    preview: {
      header: {
        code: {
          label: 'Code',
          value: 'NEW_PREFIX_',
          enabled: false,
          mode: 'edit'
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
          value: 'NEW_PREFIX_',
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}

async function testChangeProperty() {
  const form = await mountNew()

  form.getButtons().getAddSection().click()
  await form.update()

  form.getButtons().getAddProperty().click()
  await form.update()

  form.getParameters().getProperty().getCode().change('TEST_CODE')
  await form.update()

  form.getParameters().getProperty().getDataType().change('VARCHAR')
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: null,
          properties: [{ code: 'TEST_CODE', dataType: 'VARCHAR' }]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        code: {
          label: 'Code',
          value: 'TEST_CODE',
          enabled: true,
          mode: 'edit'
        },
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
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
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
          properties: [{ code: 'TEST_CODE', dataType: 'CONTROLLEDVOCABULARY' }]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        code: {
          label: 'Code',
          value: 'TEST_CODE',
          enabled: true,
          mode: 'edit'
        },
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
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
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
          properties: [{ code: 'TEST_CODE', dataType: 'MATERIAL' }]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        code: {
          label: 'Code',
          value: 'TEST_CODE',
          enabled: true,
          mode: 'edit'
        },
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
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
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
          properties: [{ code: 'TEST_CODE', dataType: 'XML' }]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        code: {
          label: 'Code',
          value: 'TEST_CODE',
          enabled: true,
          mode: 'edit'
        },
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
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}

async function testConvertProperty() {
  const properties = [
    openbis.DataType.INTEGER,
    openbis.DataType.REAL,
    openbis.DataType.VARCHAR,
    openbis.DataType.MULTILINE_VARCHAR,
    openbis.DataType.XML,
    openbis.DataType.HYPERLINK,
    openbis.DataType.TIMESTAMP,
    openbis.DataType.DATE,
    openbis.DataType.BOOLEAN,
    openbis.DataType.CONTROLLEDVOCABULARY,
    openbis.DataType.MATERIAL,
    openbis.DataType.SAMPLE
  ].map(dataType => {
    const propertyType = new openbis.PropertyType()
    propertyType.setCode(dataType)
    propertyType.setDataType(dataType)
    const property = new openbis.PropertyAssignment()
    property.setPropertyType(propertyType)
    return property
  })

  const type = new openbis.SampleType()
  type.setCode('TEST_TYPE')
  type.setPropertyAssignments(properties)

  facade.loadType.mockReturnValue(Promise.resolve(type))
  facade.loadUsages.mockReturnValue(
    Promise.resolve({
      propertyGlobal: properties.reduce((map, property) => {
        map[property.propertyType.code] = 1
        return map
      }, {})
    })
  )

  const suffix = ' (converted)'
  let index = 0

  const form = await common.mount({
    id: type.getCode(),
    type: objectTypes.OBJECT_TYPE
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'INTEGER',
          enabled: true,
          options: [
            { label: openbis.DataType.INTEGER },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix },
            { label: openbis.DataType.REAL + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'REAL',
          enabled: true,
          options: [
            { label: openbis.DataType.REAL },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'VARCHAR',
          enabled: true,
          options: [
            { label: openbis.DataType.VARCHAR },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'MULTILINE_VARCHAR',
          enabled: true,
          options: [
            { label: openbis.DataType.MULTILINE_VARCHAR },
            { label: openbis.DataType.VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'XML',
          enabled: true,
          options: [
            { label: openbis.DataType.XML },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'HYPERLINK',
          enabled: true,
          options: [
            { label: openbis.DataType.HYPERLINK },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'TIMESTAMP',
          enabled: true,
          options: [
            { label: openbis.DataType.TIMESTAMP },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix },
            { label: openbis.DataType.DATE + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'DATE',
          enabled: true,
          options: [
            { label: openbis.DataType.DATE },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'BOOLEAN',
          enabled: true,
          options: [
            { label: openbis.DataType.BOOLEAN },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'CONTROLLEDVOCABULARY',
          enabled: true,
          options: [
            { label: openbis.DataType.CONTROLLEDVOCABULARY },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'MATERIAL',
          enabled: true,
          options: [
            { label: openbis.DataType.MATERIAL },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'SAMPLE',
          enabled: true,
          options: [
            { label: openbis.DataType.SAMPLE },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })
}

async function testChangeSection() {
  const form = await mountExisting()

  form.getButtons().getEdit().click()
  await form.update()

  form.getPreview().getSections()[1].click()
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_1'
        },
        {
          name: 'TEST_SECTION_2'
        }
      ]
    },
    parameters: {
      section: {
        title: 'Section',
        name: {
          label: 'Name',
          value: 'TEST_SECTION_2',
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: null
    }
  })

  form.getParameters().getSection().getName().change('NEW_NAME')
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_1'
        },
        {
          name: 'NEW_NAME'
        }
      ]
    },
    parameters: {
      section: {
        title: 'Section',
        name: {
          label: 'Name',
          value: 'NEW_NAME',
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}

async function testRemoveProperty() {
  const form = await mountExisting()

  form.getButtons().getEdit().click()
  await form.update()

  form.getPreview().getSections()[1].getProperties()[0].click()
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
    buttons: {
      message: null
    }
  })

  form.getButtons().getRemove().click()
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
          properties: [{ code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }]
        }
      ]
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}

async function testRemoveSection() {
  const form = await mountExisting()

  form.getButtons().getEdit().click()
  await form.update()

  form.getPreview().getSections()[0].click()
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_1'
        },
        {
          name: 'TEST_SECTION_2'
        }
      ]
    },
    buttons: {
      message: null
    }
  })

  form.getButtons().getRemove().click()
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_2'
        }
      ]
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}

async function testValidateType() {
  const form = await mountNew()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      type: {
        title: 'Type',
        code: {
          error: 'Code cannot be empty',
          focused: true
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
    },
    buttons: {
      message: null
    }
  })

  form.getParameters().getType().getCode().change('I am illegal')
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      type: {
        code: {
          value: 'I am illegal',
          error: 'Code can only contain A-Z, a-z, 0-9 and _, -, .',
          focused: true
        }
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}

async function testValidateProperty() {
  const form = await mountNew()

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
          error: 'Code cannot be empty',
          focused: true
        },
        dataType: {
          error: 'Data Type cannot be empty'
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
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })

  form.getParameters().getProperty().getCode().change('I am illegal')
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        code: {
          value: 'I am illegal',
          error: 'Code can only contain A-Z, a-z, 0-9 and _, -, .',
          focused: true
        }
      }
    }
  })
}

async function testValidateTypeAndProperty() {
  const form = await mountNew()

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
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}

async function testInternal() {
  await doTestInternal(true, fixture.SYSTEM_USER_DTO, fixture.SYSTEM_USER_DTO)
  await doTestInternal(false, fixture.SYSTEM_USER_DTO, fixture.SYSTEM_USER_DTO)
  await doTestInternal(true, fixture.SYSTEM_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(false, fixture.SYSTEM_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(true, fixture.TEST_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(false, fixture.TEST_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(true, fixture.TEST_USER_DTO, fixture.SYSTEM_USER_DTO)
  await doTestInternal(false, fixture.TEST_USER_DTO, fixture.SYSTEM_USER_DTO)
}

async function doTestInternal(
  propertyTypeInternal,
  propertyTypeRegistrator,
  propertyAssignmentRegistrator
) {
  const isSystemInternalPropertyType =
    propertyTypeInternal &&
    propertyTypeRegistrator.userId === fixture.SYSTEM_USER_DTO.userId

  const isSystemInternalPropertyAssignment =
    propertyTypeInternal &&
    propertyAssignmentRegistrator.userId === fixture.SYSTEM_USER_DTO.userId

  const propertyType = new openbis.PropertyType()
  propertyType.setCode('TEST_PROPERTY')
  propertyType.setManagedInternally(propertyTypeInternal)
  propertyType.setRegistrator(propertyTypeRegistrator)
  propertyType.setDataType(openbis.DataType.VARCHAR)

  const propertyAssignment = new openbis.PropertyAssignment()
  propertyAssignment.setPropertyType(propertyType)
  propertyAssignment.setPlugin(fixture.TEST_PLUGIN_DTO)
  propertyAssignment.setRegistrator(propertyAssignmentRegistrator)

  const type = new openbis.SampleType()
  type.setCode('TEST_TYPE')
  type.setPropertyAssignments([propertyAssignment])

  facade.loadType.mockReturnValue(Promise.resolve(type))
  facade.loadDynamicPlugins.mockReturnValue(
    Promise.resolve([propertyAssignment.plugin])
  )

  const form = await common.mount({
    id: type.getCode(),
    type: objectTypes.OBJECT_TYPE
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    parameters: {
      type: {
        title: 'Type',
        code: {
          value: type.getCode(),
          enabled: false
        },
        description: {
          value: type.getDescription(),
          enabled: true
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[0].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        title: 'Property',
        code: {
          value: propertyType.getCode(),
          enabled: false
        },
        dataType: {
          value: propertyType.getDataType(),
          enabled: !isSystemInternalPropertyType
        },
        label: {
          value: propertyType.getLabel(),
          enabled: !isSystemInternalPropertyType
        },
        description: {
          value: propertyType.getDescription(),
          enabled: !isSystemInternalPropertyType
        },
        plugin: {
          value: propertyAssignment.plugin.getName(),
          enabled: !isSystemInternalPropertyAssignment
        },
        mandatory: {
          value: propertyAssignment.isMandatory(),
          enabled: !isSystemInternalPropertyAssignment
        },
        visible: {
          value: propertyAssignment.isShowInEditView(),
          enabled: !isSystemInternalPropertyAssignment
        }
      }
    },
    buttons: {
      remove: {
        enabled: !isSystemInternalPropertyAssignment
      }
    }
  })

  form.getPreview().getSections()[0].click()
  await form.update()

  form.expectJSON({
    parameters: {
      section: {
        title: 'Section',
        name: {
          value: propertyAssignment.getSection(),
          enabled: true
        }
      }
    },
    buttons: {
      remove: {
        enabled: !isSystemInternalPropertyAssignment
      }
    }
  })
}

async function mountNew() {
  return await common.mount({
    type: objectTypes.NEW_OBJECT_TYPE
  })
}

async function mountExisting() {
  facade.loadType.mockReturnValue(Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO))
  facade.loadValidationPlugins.mockReturnValue(
    Promise.resolve([fixture.TEST_SAMPLE_TYPE_DTO.validationPlugin])
  )

  return await common.mount({
    id: fixture.TEST_SAMPLE_TYPE_DTO.getCode(),
    type: objectTypes.OBJECT_TYPE
  })
}
