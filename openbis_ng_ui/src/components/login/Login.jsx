import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import {connect} from 'react-redux'
import flow from 'lodash/flow'

import Button from '@material-ui/core/Button'
import Card from '@material-ui/core/Card'
import CardContent from '@material-ui/core/CardContent'
import TextField from '@material-ui/core/TextField'
import Typography from '@material-ui/core/Typography'

import * as actions from '../../store/actions/actions.js'

const styles = {
  card: {
    marginTop: '10%',
    marginBottom: '10em',
    width: '30em',
    margin: '0 auto',
  },
  title: {
    fontSize: 24,
  },
  textField: {
    width: '100%',
  },
  button: {
    marginTop: '1em',
  },
  container: {
    width: '100%',
    height: '100%',
    overflow: 'auto',
  },
}

function mapStateToProps() {
  return {
  }
}

function mapDispatchToProps(dispatch) {
  return {
    login: (user, password) => dispatch(actions.login(user, password)),
  }
}

class WithLogin extends React.Component {

  state = {}

  handleChange = name => event => {
    this.setState({
      [name]: event.target.value,
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
    const classes = this.props.classes

    return (
      <div>
        <div className={classes.container}>
          <Card className={classes.card}>
            <CardContent>
              <Typography className={classes.title}>
                  Login
              </Typography>
              <TextField
                id="standard-name"
                label="User"
                className={classes.textField}
                margin="normal"
                autoFocus={true}
                onKeyPress={(e) => {
                  this.keyPress(e)
                }}
                onChange={this.handleChange('user')}
              />
              <TextField
                id="standard-password-input"
                label="Password"
                className={classes.textField}
                type="password"
                autoComplete="current-password"
                margin="normal"
                onKeyPress={(e) => {
                  this.keyPress(e)
                }}
                onChange={this.handleChange('password')}
              />
              <Button
                onClick={this.login}
                color="primary"
                className={classes.button}
                variant="contained">
                  Login
              </Button>

            </CardContent>
          </Card>
        </div>
      </div>
    )
  }
}

export default flow(
  connect(mapStateToProps, mapDispatchToProps),
  withStyles(styles)
)(WithLogin)
