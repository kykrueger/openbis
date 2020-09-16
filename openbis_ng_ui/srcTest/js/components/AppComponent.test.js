import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import AppWrapper from '@srcTest/js/components/wrapper/AppWrapper.js'
import App from '@src/js/components/App.jsx'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new ComponentTest(
    () => <App />,
    wrapper => new AppWrapper(wrapper)
  )
  common.beforeEach()

  openbis.login.mockReturnValue(Promise.resolve('testSession'))
  openbis.mockSearchSampleTypes([])
  openbis.mockSearchExperimentTypes([])
  openbis.mockSearchDataSetTypes([])
  openbis.mockSearchMaterialTypes([])
  openbis.mockSearchVocabularies([])
  openbis.mockSearchPersons([])
  openbis.mockSearchGroups([])
})

describe('app', () => {
  test('login', testLogin)
  test('open/close types', testOpenCloseTypes)
})

async function testLogin() {
  const app = await common.mount()

  app.expectJSON({
    login: {
      user: {
        value: null,
        enabled: true
      },
      password: {
        value: null,
        enabled: true
      },
      button: {
        enabled: true
      }
    },
    menu: null,
    types: null,
    users: null
  })

  await login(app)

  app.expectJSON({
    login: null,
    menu: {
      tabs: [
        {
          label: 'Types',
          selected: true
        }
      ]
    },
    types: {
      browser: {
        filter: {
          value: null
        },
        nodes: [
          { level: 0, text: 'Object Types' },
          { level: 0, text: 'Collection Types' },
          { level: 0, text: 'Data Set Types' },
          { level: 0, text: 'Material Types' },
          { level: 0, text: 'Vocabulary Types' }
        ]
      },
      content: {
        tabs: []
      }
    },
    users: null
  })
}

async function testOpenCloseTypes() {
  openbis.mockSearchSampleTypes([
    fixture.TEST_SAMPLE_TYPE_DTO,
    fixture.ANOTHER_SAMPLE_TYPE_DTO
  ])

  const app = await common.mount()

  await login(app)

  app.getTypes().getBrowser().getNodes()[0].getIcon().click()
  await app.update()

  app.getTypes().getBrowser().getNodes()[2].click()
  app.getTypes().getBrowser().getNodes()[1].click()
  await app.update()

  app.expectJSON({
    types: {
      browser: {
        nodes: [
          { level: 0, text: 'Object Types', selected: false },
          {
            level: 1,
            text: fixture.ANOTHER_SAMPLE_TYPE_DTO.code,
            selected: true
          },
          {
            level: 1,
            text: fixture.TEST_SAMPLE_TYPE_DTO.code,
            selected: false
          },
          { level: 0, text: 'Collection Types', selected: false },
          { level: 0, text: 'Data Set Types', selected: false },
          { level: 0, text: 'Material Types', selected: false },
          { level: 0, text: 'Vocabulary Types', selected: false }
        ]
      },
      content: {
        tabs: [
          {
            label: 'Object Type: ' + fixture.TEST_SAMPLE_TYPE_DTO.code,
            selected: false
          },
          {
            label: 'Object Type: ' + fixture.ANOTHER_SAMPLE_TYPE_DTO.code,
            selected: true
          }
        ]
      }
    }
  })

  app.getTypes().getContent().getTabs()[1].getCloseIcon().click()
  await app.update()

  app.expectJSON({
    types: {
      browser: {
        nodes: [
          { level: 0, text: 'Object Types', selected: false },
          {
            level: 1,
            text: fixture.ANOTHER_SAMPLE_TYPE_DTO.code,
            selected: false
          },
          {
            level: 1,
            text: fixture.TEST_SAMPLE_TYPE_DTO.code,
            selected: true
          },
          { level: 0, text: 'Collection Types', selected: false },
          { level: 0, text: 'Data Set Types', selected: false },
          { level: 0, text: 'Material Types', selected: false },
          { level: 0, text: 'Vocabulary Types', selected: false }
        ]
      },
      content: {
        tabs: [
          {
            label: 'Object Type: ' + fixture.TEST_SAMPLE_TYPE_DTO.code,
            selected: true
          }
        ]
      }
    }
  })
}

async function login(app) {
  app.getLogin().getUser().change('testUser')
  app.getLogin().getPassword().change('testPassword')
  app.getLogin().getButton().click()
  await app.update()
}
