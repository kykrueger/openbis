import * as actions from './login.js'


// note: This is just an example. It would not make much sense to
//       test simple action creators such as this.
describe('actions', () => {
  it('should create a login action', () => {
    const expectedAction = {
      type: 'LOGIN',
      username: 'mark.watney',
      password: 'secret',
    }
    expect(actions.login('mark.watney', 'secret')).toEqual(expectedAction)
  })
})
