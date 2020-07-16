import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'

import Content from '@src/js/components/common/content/Content.jsx'
import ContentTab from '@src/js/components/common/content/ContentTab.jsx'

import TypeBrowser from './browser/TypeBrowser.jsx'
import TypeSearch from './search/TypeSearch.jsx'
import TypeForm from './form/TypeForm.jsx'

const styles = () => ({
  container: {
    display: 'flex',
    width: '100%'
  }
})

class Types extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Types.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <TypeBrowser />
        <Content
          page={pages.TYPES}
          renderComponent={this.renderComponent}
          renderTab={this.renderTab}
        />
      </div>
    )
  }

  renderComponent(tab) {
    const { object } = tab
    if (object.type === objectType.SEARCH) {
      return <TypeSearch objectId={object.id} />
    } else {
      return <TypeForm object={object} />
    }
  }

  renderTab(tab) {
    const { object } = tab

    const prefixes = {
      [objectType.NEW_OBJECT_TYPE]: 'New Object Type ',
      [objectType.NEW_COLLECTION_TYPE]: 'New Collection Type ',
      [objectType.NEW_DATA_SET_TYPE]: 'New Data Set Type ',
      [objectType.NEW_MATERIAL_TYPE]: 'New Material Type ',
      [objectType.SEARCH]: 'search: '
    }

    return <ContentTab prefix={prefixes[object.type]} tab={tab} />
  }
}

export default withStyles(styles)(Types)
