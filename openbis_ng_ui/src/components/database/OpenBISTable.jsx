import React from 'react'
import {connect} from 'react-redux'
import {withStyles} from '@material-ui/core/styles'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import Paper from '@material-ui/core/Paper'
import TableFooter from '@material-ui/core/TableFooter'
import TablePagination from '@material-ui/core/TablePagination'
import TableSortLabel from '@material-ui/core/TableSortLabel'
import InputAdornment from '@material-ui/core/InputAdornment'
import TextField from '@material-ui/core/TextField'
import FilterIcon from '@material-ui/icons/FilterList'
import SettingsIcon from '@material-ui/icons/Settings'

import OpenBISTableRow from './OpenBISTableRow.jsx'
import actions from '../../reducer/actions.js'

/* eslint-disable-next-line no-unused-vars */
const styles = theme => ({
    table: {
        marginTop: 30,
        overflowX: 'auto'
    },
    headerCell: {
        backgroundColor: theme.palette.grey[300],
    }
})

function mapStateToProps(state) {
    const table = state.database.table
    return {
        spaces: state.database.spaces,
        columns: table.columns,
        data: table.data,
        page: table.page,
        sortColumn: table.sortColumn,
        sortDirection: table.sortDirection,
        filter: table.filter
    }
}

function mapDispatchToProps(dispatch) {
    return {
        selectEntity: e => dispatch(actions.selectEntity(e)),
        changePage: page => dispatch(actions.changePage(page)),
        sortBy: column => dispatch(actions.sortBy(column)),
        setFilter: filter => dispatch(actions.setFilter(filter))
    }
}

class OpenBISTable extends React.Component {

    render() {
        const {data, columns, classes, page} = this.props
        return (
            <Paper className={classes.table}>
                <Table padding='checkbox'>
                    <TableHead>
                        <TableRow>
                            <TableCell className={classes.headerCell} variant='head'/>
                            {Object.entries(columns).map(([column, type]) =>
                                <TableCell key={column} className={classes.headerCell} numeric={type === 'int'}
                                           variant='head'>
                                    <TableSortLabel
                                        className={classes.headerCell}
                                        active={this.props.sortColumn === column}
                                        direction={this.props.sortDirection}
                                        onClick={() => this.props.sortBy(column)}>
                                        {column}
                                    </TableSortLabel>
                                </TableCell>
                            )}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {
                            data.slice(page * 10, page * 10 + 10).map(row =>
                                <OpenBISTableRow key={row.Id} row={row} columns={columns}/>
                            )
                        }
                    </TableBody>
                    <TableFooter className={classes.headerCell}>
                        <TableRow>
                            <TableCell>
                                <div
                                    style={{cursor: 'pointer'}}>
                                    <SettingsIcon/>
                                </div>
                            </TableCell>
                            <TableCell colSpan={2}>
                                <TextField
                                    onChange={e => this.props.setFilter(e.target.value)}
                                    placeholder="filter rows by value"
                                    InputProps={{
                                        startAdornment: (
                                            <InputAdornment position="start" variant="outlined">
                                                <FilterIcon/>
                                            </InputAdornment>
                                        ),
                                    }}/>
                            </TableCell>
                            <TablePagination
                                count={data.length}
                                rowsPerPage={10}
                                rowsPerPageOptions={[10]}
                                page={page}
                                onChangePage={(_, page) => this.props.changePage(page)}
                            />
                        </TableRow>
                    </TableFooter>
                </Table>
            </Paper>
        )
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(withStyles(styles)(OpenBISTable))
