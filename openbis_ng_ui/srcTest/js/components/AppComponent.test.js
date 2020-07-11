import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import AppWrapper from '@srcTest/js/components/wrapper/AppWrapper.js'
import App from '@src/js/components/App.jsx'
import openbis from '@srcTest/js/services/openbis.js'

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
  openbis.mockSearchPersons([])
  openbis.mockSearchGroups([])
})

describe('app', () => {
  test('login', testLogin)
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

  app.getLogin().getUser().change('testUser')
  app.getLogin().getPassword().change('testPassword')
  app.getLogin().getButton().click()
  await app.update()

  app.expectJSON({
    login: null,
    menu: {
      tabs: [
        {
          label: 'Types',
          selected: true
        },
        {
          label: 'Users',
          selected: false
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
          { level: 0, text: 'Material Types' }
        ]
      },
      content: {
        tabs: []
      }
    },
    users: {
      browser: {
        filter: {
          value: null
        },
        nodes: [
          { level: 0, text: 'Users' },
          { level: 0, text: 'Groups' }
        ]
      },
      content: {
        tabs: []
      }
    }
  })
}
