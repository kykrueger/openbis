import React from 'react'
import PropTypes from 'prop-types'


class TabContent extends React.Component {
    render() {
        return <div>{this.props.children}</div>
    }
}

TabContent.propTypes = {
    name: PropTypes.string.isRequired,
    dirty: PropTypes.bool.isRequired,
    onSelect: PropTypes.func.isRequired,
    onClose: PropTypes.func.isRequired,
}

export default TabContent
