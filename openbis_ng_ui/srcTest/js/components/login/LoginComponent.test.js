import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import Login from '@src/js/components/login/Login.jsx'
import LoginWrapper from '@srcTest/js/components/login/wrapper/LoginWrapper.js'

let common = null

beforeEach(() => {
  common = new ComponentTest(
    () => <Login />,
    wrapper => new LoginWrapper(wrapper)
  )
  common.beforeEach()
})

describe('login', () => {
  test('test', async () => {
    const login = await common.mount()

    login.expectJSON({
      user: {
        value: null,
        focused: true,
        error: null
      },
      password: {
        value: null,
        focused: false,
        error: null
      },
      button: {
        enabled: true
      }
    })

    login.getButton().click()
    await login.update()

    login.expectJSON({
      user: {
        value: null,
        focused: true,
        error: 'User cannot be empty'
      },
      password: {
        value: null,
        focused: false,
        error: 'Password cannot be empty'
      },
      button: {
        enabled: true
      }
    })

    login.getUser().change('testUser')
    login.getButton().click()
    await login.update()

    login.expectJSON({
      user: {
        value: 'testUser',
        focused: false,
        error: null
      },
      password: {
        value: null,
        focused: true,
        error: 'Password cannot be empty'
      },
      button: {
        enabled: true
      }
    })

    login.getPassword().change('testPassword')
    login.getButton().click()
    await login.update()

    login.expectJSON({
      user: {
        value: 'testUser',
        focused: false,
        error: null
      },
      password: {
        value: 'testPassword',
        focused: true,
        error: null
      },
      button: {
        enabled: true
      }
    })
  })
})
