import React from 'react'
import AppBar from '@material-ui/core/AppBar'
import Toolbar from '@material-ui/core/Toolbar'
import Grid from '@material-ui/core/Grid'
import Button from '@material-ui/core/Button'
import RemoveIcon from '@material-ui/icons/Remove'
import AddIcon from '@material-ui/icons/Add'

class BrowserButtons extends React.Component {

    render() {
        return (
            <AppBar position='static'>
                <Toolbar>
                    <Grid container alignItems='center'>
                        <Grid item xs={2}>
                            <Button
                                variant="contained"
                                color="primary">
                                <RemoveIcon/>
                            </Button>
                        </Grid>
                        <Grid item xs={8}/>
                        <Grid item xs={2}>
                            <Button
                                variant="contained"
                                color="primary">
                                <AddIcon/>
                            </Button>
                        </Grid>
                    </Grid>
                </Toolbar>
            </AppBar>
        )
    }
}

export default BrowserButtons
