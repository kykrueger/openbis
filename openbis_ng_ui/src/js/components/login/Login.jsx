import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { connect } from 'react-redux'
import flow from 'lodash/flow'

import Card from '@material-ui/core/Card'
import CardContent from '@material-ui/core/CardContent'
import Typography from '@material-ui/core/Typography'

import TextField from '@src/js/components/common/form/TextField.jsx'
import Button from '@src/js/components/common/form/Button.jsx'

import actions from '@src/js/store/actions/actions.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  card: {
    marginTop: '10%',
    marginBottom: '10em',
    width: '30em',
    margin: '0 auto'
  },
  content: {
    padding: `${theme.spacing(2)}px !important`
  },
  header: {
    marginBottom: theme.spacing(1)
  },
  field: {
    marginBottom: theme.spacing(1)
  },
  button: {
    marginTop: theme.spacing(1)
  },
  container: {
    width: '100%',
    height: '100%',
    overflow: 'auto'
  }
})

function mapStateToProps() {
  return {}
}

function mapDispatchToProps(dispatch) {
  return {
    login: (user, password) => dispatch(actions.login(user, password))
  }
}

class WithLogin extends React.Component {
  state = {}

  handleChange = name => event => {
    this.setState({
      [name]: event.target.value
    })
  }

  keyPress(e) {
    if (e.key === 'Enter') {
      this.login()
    }
  }

  login = () => {
    this.props.login(this.state.user, this.state.password)
  }

  render() {
    logger.log(logger.DEBUG, 'Login.render')

    const classes = this.props.classes

    return (
      <div>
        <div className={classes.container}>
          <form>
            <Card classes={{ root: classes.card }}>
              <CardContent classes={{ root: classes.content }}>
                <Typography variant='h6' classes={{ root: classes.header }}>
                  Login
                </Typography>
                <div className={classes.field}>
                  <TextField
                    id='standard-name'
                    label='User'
                    autoComplete='username'
                    value={this.state.user}
                    onKeyPress={e => {
                      this.keyPress(e)
                    }}
                    onChange={this.handleChange('user')}
                  />
                </div>
                <div className={classes.field}>
                  <TextField
                    id='standard-password-input'
                    label='Password'
                    type='password'
                    value={this.state.password}
                    autoComplete='current-password'
                    onKeyPress={e => {
                      this.keyPress(e)
                    }}
                    onChange={this.handleChange('password')}
                  />
                </div>
                <Button
                  label='Login'
                  type='final'
                  styles={{ root: classes.button }}
                  onClick={this.login}
                />
              </CardContent>
            </Card>
          </form>
        </div>
      </div>
    )
  }
}

export default flow(
  connect(mapStateToProps, mapDispatchToProps),
  withStyles(styles)
)(WithLogin)
