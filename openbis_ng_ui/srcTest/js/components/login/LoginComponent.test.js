import React from 'react'
import { Provider } from 'react-redux'
import { mount } from 'enzyme'
import { createStore } from '@src/js/store/store.js'
import ThemeProvider from '@src/js/components/common/theme/ThemeProvider.jsx'
import Login from '@src/js/components/login/Login.jsx'
import LoginWrapper from '@srcTest/js/components/login/wrapper/LoginWrapper.js'
import actions from '@src/js/store/actions/actions.js'

let store = null

beforeEach(() => {
  jest.resetAllMocks()
  store = createStore()
  store.dispatch(actions.init())
})

describe('login', () => {
  test('test', async () => {
    const login = await mountLogin()

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

async function mountLogin() {
  document.body.innerHTML = '<div></div>'

  const wrapper = mount(
    <Provider store={store}>
      <ThemeProvider>
        <Login />
      </ThemeProvider>
    </Provider>,
    {
      attachTo: document.getElementsByTagName('div')[0]
    }
  )

  const login = new LoginWrapper(wrapper)
  return login.update().then(() => login)
}
